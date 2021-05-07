/*
 * Copyright © 2021 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.gcp.bigquery.sink;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.hadoop.io.bigquery.BigQueryFileFormat;
import com.google.cloud.hadoop.io.bigquery.output.BigQueryOutputConfiguration;
import com.google.cloud.hadoop.io.bigquery.output.BigQueryTableFieldSchema;
import com.google.cloud.hadoop.io.bigquery.output.BigQueryTableSchema;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.gcp.bigquery.common.BigQueryBaseConfig;
import io.cdap.plugin.gcp.bigquery.util.BigQueryConstants;
import io.cdap.plugin.gcp.bigquery.util.BigQueryUtil;
import io.cdap.plugin.gcp.common.GCPUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Utility class for the BigQuery DelegatingMultiSink.
 *
 * The logic in this class has been extracted from the {@link AbstractBigQuerySink} in order to make this functionality
 * available to other classes in this package.
 */
final class BigQuerySinkUtils {

  private static final String gcsPathFormat = "gs://%s/%s";
  private static final String temporaryBucketFormat = gcsPathFormat + "/input/%s-%s";
  private static final String DATETIME = "DATETIME";

  /**
   * Creates Hadoop configuration for the given table and its fields.
   *
   * @param bucket bucket name
   * @param tableName table name
   * @param fields list of Big Query fields
   * @return Hadoop configuration
   */
  public static Configuration getOutputConfiguration(Configuration baseConfiguration,
                                                     BigQueryBaseConfig config,
                                                     String bucket,
                                                     String pathPrefix,
                                                     String tableName,
                                                     List<BigQueryTableFieldSchema> fields) throws IOException {
    Configuration configuration = new Configuration(baseConfiguration);
    String temporaryGcsPath = getTemporaryGcsPath(bucket, pathPrefix, tableName);

    configureOutput(configuration,
                    config.getDatasetProject(),
                    config.getDataset(),
                    tableName,
                    temporaryGcsPath,
                    fields);

    return configuration;
  }

  /**
   * Creates the given dataset and bucket if they do not already exist. If the dataset already exists but the
   * bucket does not, the bucket will be created in the same location as the dataset. If the bucket already exists
   * but the dataset does not, the dataset will attempt to be created in the same location. This may fail if the bucket
   * is in a location that BigQuery does not yet support.
   *
   * @param bigQuery the bigquery client for the project
   * @param storage the storage client for the project
   * @param datasetName the name of the dataset
   * @param bucketName the name of the bucket
   * @param location the location of the resources, this is only applied if both the bucket and dataset do not exist
   * @param cmekKey the name of the cmek key
   * @throws IOException if there was an error creating or fetching any GCP resource
   */
  public static void createResources(BigQuery bigQuery, Storage storage,
                                     String datasetName, String bucketName, @Nullable String location,
                                     @Nullable String cmekKey) throws IOException {
    Dataset dataset = bigQuery.getDataset(datasetName);
    Bucket bucket = storage.get(bucketName);

    if (dataset == null && bucket == null) {
      createBucket(storage, bucketName, location, cmekKey,
                   () -> String.format("Unable to create Cloud Storage bucket '%s'", bucketName));
      createDataset(bigQuery, datasetName, location,
                    () -> String.format("Unable to create BigQuery dataset '%s'", datasetName));
    } else if (bucket == null) {
      createBucket(
        storage, bucketName, dataset.getLocation(), cmekKey,
        () -> String.format(
          "Unable to create Cloud Storage bucket '%s' in the same location ('%s') as BigQuery dataset '%s'. "
            + "Please use a bucket that is in the same location as the dataset.",
          bucketName, dataset.getLocation(), datasetName));
    } else if (dataset == null) {
      createDataset(
        bigQuery, datasetName, bucket.getLocation(),
        () -> String.format(
          "Unable to create BigQuery dataset '%s' in the same location ('%s') as Cloud Storage bucket '%s'. "
            + "Please use a bucket that is in a supported location.",
          datasetName, bucket.getLocation(), bucketName));
    }
  }

  private static void createDataset(BigQuery bigQuery, String dataset, @Nullable String location,
                                    Supplier<String> errorMessage) throws IOException {
    DatasetInfo.Builder builder = DatasetInfo.newBuilder(dataset);
    if (location != null) {
      builder.setLocation(location);
    }
    try {
      bigQuery.create(builder.build());
    } catch (BigQueryException e) {
      if (e.getCode() != 409) {
        // A conflict means the dataset already exists (https://cloud.google.com/bigquery/troubleshooting-errors)
        // This most likely means multiple stages in the same pipeline are trying to create the same dataset.
        // Ignore this and move on, since all that matters is that the dataset exists.
        throw new IOException(errorMessage.get(), e);
      }
    }
  }

  private static void createBucket(Storage storage, String bucket, @Nullable String location,
                                   @Nullable String cmekKey, Supplier<String> errorMessage) throws IOException {
    try {
      GCPUtils.createBucket(storage, bucket, location, cmekKey);
    } catch (StorageException e) {
      if (e.getCode() != 409) {
        // A conflict means the bucket already exists
        // This most likely means multiple stages in the same pipeline are trying to create the same dataset.
        // Ignore this and move on, since all that matters is that the dataset exists.
        throw new IOException(errorMessage.get(), e);
      }
    }
  }

  private static void configureOutput(Configuration configuration,
                                      String projectName,
                                      String datasetName,
                                      String tableName,
                                      String temporaryGCSPath,
                                      List<BigQueryTableFieldSchema> fields) throws IOException {
    BigQueryTableSchema outputTableSchema = new BigQueryTableSchema();
    if (!fields.isEmpty()) {
      outputTableSchema.setFields(fields);
    }

    BigQueryFileFormat fileFormat = getFileFormat(fields);
    BigQueryOutputConfiguration.configure(
      configuration,
      String.format("%s:%s.%s", projectName, datasetName, tableName),
      outputTableSchema,
      temporaryGCSPath,
      fileFormat,
      getOutputFormat(fileFormat));
  }

  public static void configureMultiSinkOutput(JobContext context,
                                              String projectName,
                                              String datasetName,
                                              String bucketName,
                                              String bucketPathPrefix,
                                              String tableName,
                                              Schema schema) throws IOException {
    Configuration configuration = context.getConfiguration();
    String temporaryGcsPath = BigQuerySinkUtils.getTemporaryGcsPath(bucketName, bucketPathPrefix, tableName);
    List<BigQueryTableFieldSchema> fields = BigQuerySinkUtils.getBigQueryTableFieldsFromSchema(schema);

    configureOutput(configuration,
                    projectName,
                    datasetName,
                    tableName,
                    temporaryGcsPath,
                    fields);

    // Set operation as Insertion. Currently the BQ MultiSink can only support the insertion operation.
    configuration.set(BigQueryConstants.CONFIG_OPERATION, Operation.INSERT.name());
  }

  public static String getTemporaryGcsPath(String bucket, String pathPrefix, String tableName) {
    return String.format(temporaryBucketFormat, bucket, pathPrefix, tableName, pathPrefix);
  }

  public static List<BigQueryTableFieldSchema> getBigQueryTableFieldsFromSchema(Schema tableSchema) {
    List<Schema.Field> inputFields = Objects.requireNonNull(tableSchema.getFields(), "Schema must have fields");
    return inputFields.stream()
      .map(BigQuerySinkUtils::generateTableFieldSchema)
      .collect(Collectors.toList());
  }

  private static BigQueryTableFieldSchema generateTableFieldSchema(Schema.Field field) {
    BigQueryTableFieldSchema fieldSchema = new BigQueryTableFieldSchema();
    fieldSchema.setName(field.getName());
    fieldSchema.setMode(getMode(field.getSchema()).name());
    LegacySQLTypeName type = getTableDataType(field.getSchema());
    fieldSchema.setType(type.name());
    if (type == LegacySQLTypeName.RECORD) {
      List<Schema.Field> schemaFields;
      if (Schema.Type.ARRAY == field.getSchema().getType()) {
        schemaFields = Objects.requireNonNull(field.getSchema().getComponentSchema()).getFields();
      } else {
        schemaFields = field.getSchema().isNullable()
          ? field.getSchema().getNonNullable().getFields()
          : field.getSchema().getFields();
      }
      fieldSchema.setFields(Objects.requireNonNull(schemaFields).stream()
                              .map(BigQuerySinkUtils::generateTableFieldSchema)
                              .collect(Collectors.toList()));

    }
    return fieldSchema;
  }

  private static Field.Mode getMode(Schema schema) {
    boolean isNullable = schema.isNullable();
    Schema.Type nonNullableType = isNullable ? schema.getNonNullable().getType() : schema.getType();
    if (isNullable && nonNullableType != Schema.Type.ARRAY) {
      return Field.Mode.NULLABLE;
    } else if (nonNullableType == Schema.Type.ARRAY) {
      return Field.Mode.REPEATED;
    }
    return Field.Mode.REQUIRED;
  }

  private static LegacySQLTypeName getTableDataType(Schema schema) {
    schema = BigQueryUtil.getNonNullableSchema(schema);
    Schema.LogicalType logicalType = schema.getLogicalType();

    if (logicalType != null) {
      switch (logicalType) {
        case DATE:
          return LegacySQLTypeName.DATE;
        case TIME_MILLIS:
        case TIME_MICROS:
          return LegacySQLTypeName.TIME;
        case TIMESTAMP_MILLIS:
        case TIMESTAMP_MICROS:
          return LegacySQLTypeName.TIMESTAMP;
        case DECIMAL:
          return LegacySQLTypeName.NUMERIC;
        case DATETIME:
          return LegacySQLTypeName.DATETIME;
        default:
          throw new IllegalStateException("Unsupported type " + logicalType.getToken());
      }
    }

    Schema.Type type = schema.getType();
    switch (type) {
      case INT:
      case LONG:
        return LegacySQLTypeName.INTEGER;
      case STRING:
        return LegacySQLTypeName.STRING;
      case FLOAT:
      case DOUBLE:
        return LegacySQLTypeName.FLOAT;
      case BOOLEAN:
        return LegacySQLTypeName.BOOLEAN;
      case BYTES:
        return LegacySQLTypeName.BYTES;
      case ARRAY:
        return getTableDataType(schema.getComponentSchema());
      case RECORD:
        return LegacySQLTypeName.RECORD;
      default:
        throw new IllegalStateException("Unsupported type " + type);
    }
  }

  private static BigQueryFileFormat getFileFormat(List<BigQueryTableFieldSchema> fields) {
    for (BigQueryTableFieldSchema field : fields) {
      if (DATETIME.equals(field.getType())) {
        return BigQueryFileFormat.NEWLINE_DELIMITED_JSON;
      }
    }
    return BigQueryFileFormat.AVRO;
  }

  private static Class<? extends FileOutputFormat> getOutputFormat(BigQueryFileFormat fileFormat) {
    if (fileFormat == BigQueryFileFormat.NEWLINE_DELIMITED_JSON) {
      return TextOutputFormat.class;
    }
    return AvroOutputFormat.class;
  }
}
