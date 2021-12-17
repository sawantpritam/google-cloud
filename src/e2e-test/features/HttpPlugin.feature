Feature: HTTP Plugin Positive scenarios

  @HTTP-TC
  Scenario: TC-HTTP-BQ-1:User is able to Login and confirm data is getting transferred from HTTP to BigQuery
    Given Open Datafusion Project to configure pipeline
    When Source is HTTP
    When Target is BigQuery
    Then Link Source HTTP and Sink Bigquery to establish connection
    Then Open HTTP Properties
    Then Enter the HTTP Properties with Url "httpSrcUrl" , method "httpSrcMethod" and format "httpSrcFormat"
    Then Enter outputSchema "httpSrcValidOutputSchema" , jsonResultPath "httpSrcResultPath" and jsonFieldsMapping "httpSrcValidJsonFieldsMapping"
    Then Validate HTTP properties
    Then Capture output schema
    Then Close the HTTP Properties
    Then Enter the BigQuery Sink Properties for table "httpBqTableName"
    Then Close the BigQuery Properties
    Then Save the pipeline
    Then Preview and run the pipeline
    Then Verify the preview of pipeline is "success"
    Then Click on PreviewData for HTTP
    Then Verify Preview output schema matches the outputSchema captured in properties
    Then Close the Preview
    Then Deploy the pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open Logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate successMessage is displayed
    Then Validate OUT record count is equal to IN record count
    Then Get Count of no of records transferred to BigQuery in "httpBqTableName"

  @HTTP-TC1
  Scenario Outline: TC-HTTP-BQ-2:User is able to Login and confirm data is getting transferred from HTTP to BigQuery
    Given Open Datafusion Project to configure pipeline
    When Source is HTTP
    When Target is BigQuery
    Then Link Source HTTP and Sink Bigquery to establish connection
    Then Open HTTP Properties
    Then Enter the HTTP Properties with Url "<Url>" , method "<Method>" and format "<Format>"
    Then Enter request body "<RequestBody>"
    Then Enter outputSchema "<OutputSchema>" , jsonResultPath "<ResultPath>" and jsonFieldsMapping "<FieldsMapping>"
    Then Validate HTTP properties
    Then Capture output schema
    Then Close the HTTP Properties
    Then Enter the BigQuery Sink Properties for table "httpBqTableName"
    Then Close the BigQuery Properties
    Then Save the pipeline
    Then Preview and run the pipeline
    Then Verify the preview of pipeline is "success"
    Then Click on PreviewData for HTTP
    Then Verify Preview output schema matches the outputSchema captured in properties
    Then Close the Preview
    Then Deploy the pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open Logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate successMessage is displayed
    Then Validate OUT record count is equal to IN record count
    Then Get Count of no of records transferred to BigQuery in "httpBqTableName"
    Then Validate BigQuery records count is equal to HTTP records count with Url "<Url>" "<Method>" "<RequestBody>" "<ResultPath>"
    Examples:
    | Url           |  Method           | Format           | RequestBody          | OutputSchema               |  ResultPath           | FieldsMapping                   |
    | httpSrcUrl_1  |  httpSrcMethod_1  | httpSrcFormat_1  |httpSrcRequestBody_1  | httpSrcValidOutputSchema_1 |  httpSrcResultPath_1  | httpSrcValidJsonFieldsMapping_1 |
    | httpSrcUrl_2  |  httpSrcMethod_2  | httpSrcFormat_2  |httpSrcRequestBody_2  | httpSrcValidOutputSchema_2 |  httpSrcResultPath_2  | httpSrcValidJsonFieldsMapping_2 |

  @HTTP-TC
  Scenario: TC-HTTP-BQ-3:User is able to Login and confirm data is getting transferred from HTTP with basic auth to BigQuery
    Given Open Datafusion Project to configure pipeline
    When Source is HTTP
    When Target is BigQuery
    Then Link Source HTTP and Sink Bigquery to establish connection
    Then Open HTTP Properties
    Then Enter the HTTP Properties with Url "httpSrcUrl" , method "httpSrcMethod" and format "httpSrcFormat"
    Then Enter outputSchema "httpSrcValidOutputSchema" , jsonResultPath "httpSrcResultPath" and jsonFieldsMapping "httpSrcValidJsonFieldsMapping"
    Then Enter basic authentication username and password
    Then Validate HTTP properties
    Then Capture output schema
    Then Close the HTTP Properties
    Then Enter the BigQuery Sink Properties for table "httpBqTableName"
    Then Close the BigQuery Properties
    Then Save the pipeline
    Then Preview and run the pipeline
    Then Verify the preview of pipeline is "success"
    Then Click on PreviewData for HTTP
    Then Verify Preview output schema matches the outputSchema captured in properties
    Then Close the Preview
    Then Deploy the pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open Logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate successMessage is displayed
    Then Validate OUT record count is equal to IN record count
    Then Get Count of no of records transferred to BigQuery in "httpBqTableName"

  @HTTP-TC
  Scenario: TC-HTTP-04:User is able to Login and confirm data is getting transferred from GCS to HTTP
    Given Open Datafusion Project to configure pipeline
    When Source is GCS bucket
    When Target is HTTP
    Then Link Source GCS and Sink HTTP to establish connection
    Then Enter the GCS Properties with "httpGSCBucket" GCS bucket
    Then Capture output schema
    Then Close the GCS Properties
    Then Open HTTP Properties
    Then Enter the HTTP Sink Properties
    Then Close the HTTP Properties
    Then Save the pipeline
    Then Preview and run the pipeline
    Then Verify the preview of pipeline is "success"
    Then Click on PreviewData for GCS
    Then Verify Preview output schema matches the outputSchema captured in properties
    Then Close the Preview
    Then Deploy the pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open Logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate successMessage is displayed
