/*
 * Copyright © 2022 Cask Data, Inc.
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
package io.cdap.plugin.slt.stepsdesign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adapter.connector.SAPAdapterImpl;
import com.google.adapter.connector.SAPProperties;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.util.ErrorCapture;
import com.google.adapter.util.ExceptionUtils;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sap.conn.jco.JCoException;
import io.cdap.e2e.utils.*;
import io.cdap.plugin.slt.actions.SLTActions;
import io.cdap.plugin.slt.locators.SLTLocators;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stepsdesign.BeforeActions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.cdap.e2e.utils.BigQueryClient.getSoleQueryResult;

/**
 * Contains SLT test scenarios step definitions.
 */
public class StepDefinitionSLT implements CdfHelper {

  private static final Logger logger = LoggerFactory.getLogger(PluginPropertyUtils.class);
  private SAPProperties sapProps;
  private ErrorCapture errorCapture;
  private SAPAdapterImpl sapAdapterImpl;
  static String action;
  private ExceptionUtils exceptionUtils;
  public static SLTActions sltActions = new SLTActions();
  public static SLTLocators sltLocators = new SLTLocators();
  public static String rawLog = null;
  BigQueryClient bqClient = new BigQueryClient();
  static int sapRecordsCount;
  static int bqRecordCount = 0;
  private static ArrayList<String> tabs = null;
  private static String parent = null;
  private static Properties connection = new SAPProperties();
  private List<String> fields = new ArrayList<>();
  private static boolean isErrorPresent = false;
  private static String mtId = null;
  private static String guid = null;
  static boolean errorExist = false;
  static String color;
  private static AssertionHelper assertionHelper = new AssertionHelper();
  private static ElementHelper elementHelper = new ElementHelper();
  private static WaitHelper waitHelper = new WaitHelper();
  private static PluginPropertyUtils pluginPropertyUtils = new PluginPropertyUtils();


  static {
    sltLocators = SeleniumHelper.getPropertiesLocators(SLTLocators.class);
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream("src/e2e-test/resources/Google_SAP_Connection.properties"));
    } catch (IOException e) {
      logger.error("Failed while reading SAP properties file" + e);
    }
    for (String property : prop.stringPropertyNames()) {
      connection.put(property, prop.getProperty(property));
    }
  }

  @Then("^Set Dummy MTID")
  public void setDummyMTID() {
    mtId = "02K";
  }


  @Given("^Open CDF replication and initiate pipeline creation")
  public void open_cdf_replication() throws IOException, InterruptedException {
    openCdf();
    SeleniumDriver.getDriver().get("http://localhost:11011/cdap/ns/default/replication/create");
    sltActions.enterName(UUID.randomUUID().toString().replaceAll("-", ""));
    sltActions.clickNextButton();
    isErrorPresent = false;
    bqRecordCount = 0;
    sapRecordsCount = 0;
  }

  @When("Select SAP SLT as source and fill connection parameters")
  public void source_is_sap_slt() {
    sltActions.clickSltPlugin();
    sltActions.enterMandatoryParameters(
      "gcpProjectId",
      "gcsDataPath",
      mtId,
      "sapJcoLibGcsPath",
      "jcoclienthost",
      "jcoclient",
      "jcoclientsysnr",
      "jcoclientuser",
      "jcoclientpasswd");
  }

  @When("Select SAP SLT as source")
  public void source_is_sapslt() {
    sltActions.clickSltPlugin();
  }

  @Then("Verify that after setting SLT parameter {string} as {string} plugin is throws error {string}")
  public void userIsAbleToSetSLTParameterAsAndGettingRowForWrongInput(
    String inputParameter, String inputValue, String errorMessage) {
    errorExist = false;
    elementHelper.replaceElementValue(sltLocators.inputParameter(inputParameter), inputValue);
    sltActions.clickNextButton();
    errorExist = pluginPropertyUtils.errorProp(errorMessage).
      toLowerCase().contains(SLTLocators.rowError.getText().toLowerCase());
    color = sltActions.getRowErrorBorderColor();
    Assert.assertTrue(errorExist);
    BeforeActions.scenario.write("Color of the text box" + color);
    Assert.assertTrue(color.toLowerCase().contains("rgb(209, 86, 104)"));
  }

  @Then("Validate SLT parameter {string} can be set as {string}")
  public void userIsAbleToSetSLTParameter(
    String inputParameter, String inputValue) {
    elementHelper.replaceElementValue(sltLocators.inputParameter(inputParameter), inputValue);
  }

  @Then("Validate Replicate Existing Data can be set to false")
  public void replicate_existing_data() {
    sltActions.clickReplicateExistingData();
    assertionHelper.verifyElementContainsText(sltLocators.replicateExistingData,
                                              "No");
  }

  @Then("Validate Suspend Slt Job can be set to true")
  public void suspend_existing_job() {
    sltActions.clickSuspendSltJob();
    assertionHelper.verifyElementContainsText(sltLocators.suspendSltJob,
                                              "Yes");
  }

  @Then("^Click on next$")
  public void click_next() {
    sltActions.clickNextButton();
  }

  @Then("Validate Table is available and  can be selected {string}")
  public void select_table_something(String table) {
    sltActions.selectTable(table.toLowerCase());
  }

  @Then("^Enter the BigQuery Properties for slt datasource$")
  public void enter_the_bigquery_properties_for_slt_datasource_something() throws IOException {
    elementHelper.replaceElementValue(sltLocators.project,
                                      pluginPropertyUtils.pluginProp("bqProjectId"));
    sltActions.enterDatasetName(pluginPropertyUtils.pluginProp("dataset"));
  }

  @Then("Validate Replication Pipeline can be deployed")
  public void click_deployPipeline() {
    sltActions.deployPipelineClick();
  }

  @Then("Run the slt Pipeline in Runtime")
  public void run_the_slt_pipeline_in_runtime() throws Throwable {
    sltActions.startPipeline();
    waitHelper.waitForElementToBeDisplayed(sltLocators.running, 60);
  }

  @Then("Open Logs of SLT Pipeline")
  public void open_logs_of_slt_pipeline() {
    sltLocators.logs.click();
    parent = SeleniumDriver.getDriver().getWindowHandle();
    tabs = new ArrayList(SeleniumDriver.getDriver().getWindowHandles());
    SeleniumDriver.getDriver().switchTo().window((String) tabs.get(tabs.indexOf(parent) + 1));
    sltLocators.advancedLogs.click();
  }

  @Then("Wait till SLT pipeline is in running state and no error occurs")
  public void wait_till_slt_pipeline_is_in_running_state() {
    WebDriverWait wait = new WebDriverWait(SeleniumDriver.getDriver(), 1000000);
    wait.until(ExpectedConditions.or(
      ExpectedConditions.numberOfElementsToBeMoreThan(
        sltLocators.replicationStatus, 2),
      ExpectedConditions.visibilityOf(sltLocators.error3)));
  }


  @Then("Get Count of no of records transferred from SLT to BigQuery in {string}")
  public void get_count_of_no_of_records_transferred_from_SLT_to_bigquery_in_something(String table) throws
    IOException, InterruptedException {
    String selectQuery = "SELECT count(*)  FROM `" +
      pluginPropertyUtils.pluginProp("bqProjectId") + "." +
      pluginPropertyUtils.pluginProp("dataset") + "." +
      table.toLowerCase() + "`";

    bqRecordCount = (Integer) getSoleQueryResult(selectQuery).
      map(Integer::parseInt).orElse(0);
  }

  @Then("Drop target BigQuery table {string}")
  public void drop_target_bigquery_table_something(String table) {
    String selectQuery = null;
    selectQuery = "DROP TABLE `" + pluginPropertyUtils.pluginProp("bqProjectId") +
      "." + pluginPropertyUtils.pluginProp("dataset") + "." +
      table.toLowerCase() + "`";
    try {
      bqRecordCount = (Integer) getSoleQueryResult(selectQuery).map(Integer::parseInt).orElse(0);
    } catch (InterruptedException | IOException | BigQueryException e) {
      System.out.println("BQ Table is not present, It will be created by BQ plugin");
    }
  }

  @Then("Delete GCS folder for mass transfer id table {string}")
  public void delete_gcs_folder_for_mass_transfer_id_something_table_something(String table) throws IOException {
    Storage storage = StorageOptions.newBuilder().
      setProjectId(pluginPropertyUtils.pluginProp("gcpProjectId")).
      build().getService();
    Page<Blob> blobs = storage.list(
      pluginPropertyUtils.pluginProp("gcsDataPath").replaceAll("gs://", ""),
      Storage.BlobListOption.prefix(mtId + "/" + table));
    for (Blob blob : blobs.iterateAll()) {
      System.out.println(blob.getName());
      storage.delete(pluginPropertyUtils.pluginProp("gcsDataPath").replaceAll("gs://", ""),
                     blob.getName());
    }
  }

  @Then("Close logs and stop the pipeline")
  public void close_logs_and_stop_the_pipeline() {
    WebDriverWait wait = new WebDriverWait(SeleniumDriver.getDriver(), 1000000);
    SeleniumDriver.getDriver().close();
    SeleniumDriver.getDriver().switchTo().window(parent);
    sltActions.stopPipeline();
    wait.until(ExpectedConditions.visibilityOf(sltLocators.stopped));
  }

  @Then("Validate only replication data is loaded ie {string}")
  public void user_is_able_to_validate_record_count_in_bq_matches_with_repl_count(String count) {
    Assert.assertEquals(Integer.parseInt(count), bqRecordCount);
    Assert.assertTrue(sapRecordsCount > bqRecordCount);
  }

  @Then("Validate record count in BQ matches with count in SAP")
  public void user_is_able_to_validate_record_count_in_bq_matches_with_count_in_sap() {
    Assert.assertEquals(sapRecordsCount, bqRecordCount);
  }

  @Then("Verify that user is able to update table {string} to {string} on LTRC")
  public void slt_job_for_mtid_something_table_something_in_something_mode
    (String table, String mode)
    throws JCoException, InterruptedException {
    if (mode.equalsIgnoreCase("initial_load")) {
      action = "IM_START_LOAD";
    } else if (mode.equalsIgnoreCase("replication")) {
      action = "IM_START_REPL";
    } else if (mode.equalsIgnoreCase("only_replication")) {
      action = "IM_START_REP_ONLY";
    } else if (mode.equalsIgnoreCase("stop_load")) {
      action = "IM_STOP_REPLICATION";
    } else if (mode.equalsIgnoreCase("start_recording")) {
      action = "IM_START_RECORD";
    }
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);

    Map opProps = new HashMap<>();
    opProps.put("RFC", "ZRFM_LTRC_OPERATIONS");
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("IM_MTID", mtId);
      objectNode.put("IM_TABLES", table.toUpperCase());
      objectNode.put(action, "X");
      JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
      BeforeActions.scenario.write("LTRC_OPERATIONS RFM response :-" + response);
      Assert.assertTrue(response.toString().contains("\"MESSAGE_TYPE\":\"S\""));
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
    Thread.sleep(10000); //sleep required to wait for async operation to be executed on SAP
  }

  @When("Fetche the record count of table : {string} from SAP")
  public void user_fetches_the_record_count_of_table_something_from_sap(String table)
    throws JCoException {
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);
    Map opProps = new HashMap<>();
    opProps.put("RFC", "ZFM_TABLE_COUNT");
    opProps.put("autoCommit", "true");
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("IM_TABLE", table.toUpperCase());
      JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
      sapRecordsCount = Integer.parseInt(response.get("EX_COUNT").toString().replaceAll("\"", ""));
      BeforeActions.scenario.write("Total record count : " + sapRecordsCount);
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Given("Create new MTID on {string} SAP")
  public void user_creates_mtid_on_sap(String mtIdDes)
    throws JCoException, InterruptedException {
    if (mtId == null) {
      sapProps = SAPProperties.getDefault(connection);
      errorCapture = new ErrorCapture(exceptionUtils);
      sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);
      Map opProps = new HashMap<>();
      opProps.put("RFC", "ZRFM_IUUC_CREATE_CONFIG");
      opProps.put("autoCommit", "true");
      try {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("IV_SCHEMA_NAME", mtIdDes);
        objectNode.put("IV_SCHEMA_DESCR", mtIdDes);
        objectNode.put("IV_APPLICATION", "ZGOOGLE_CDF");
        objectNode.put("IV_REPLAY_JOBS", "009");
        objectNode.put("IV_INIT_LOAD_JOBS", "009");
        objectNode.put("IV_REPL_MODE", "1");
        objectNode.put("IV_SOURCE_SYSTEM_TYPE", "SAP");
        objectNode.put("IV_SOURCE_RFCDEST", "CQ1");
        objectNode.put("IV_TARGET_RFCDEST", "NONE");
        objectNode.put("IV_HANA_INSTANCE", "0");
        objectNode.put("IV_HANA_PROXYPORT", "000");
        objectNode.put("IV_RFC_CLIENT_ONLY", "X");
        objectNode.put("IV_REUSE_SENDER", "X");
        objectNode.put("IV_IL_SCENARIOIV_IL_SCENARIO", "1");
        objectNode.put("IV_NUM_JOBS_ACPCALC", "000");
        JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
        mtId = response.get("EV_MT_ID").asText();
        guid = response.get("EV_CONFIG_GUID").asText();
        System.out.println("MT id : " + mtId + " guid : " + guid);
      } catch (Exception e) {
        throw SystemException.throwException(e.getMessage(), e);
      }
      Thread.sleep(6000); //sleep required to wait for async operation to be executed on SAP
    }
  }

  @When("Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program")
  public void user_updates_mtid_in_program()
    throws JCoException, InterruptedException {
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);
    Map opProps = new HashMap<>();
    opProps.put("RFC", "ZRFM_LTRC_ADVANCE_SETTINGS_1");
    opProps.put("autoCommit", "true");
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("IM_TR_KEY", mtId);
      objectNode.put("IM_MASS_TR_ID", mtId);
      objectNode.put("IM_EXTRA_FLDS", "X");
      objectNode.put("IM_GCP_KEY_NAME",
                     pluginPropertyUtils.pluginProp("IM_GCP_KEY_NAME"));
      objectNode.put("IM_GCS_BUCKET",
                     pluginPropertyUtils.pluginProp("gcsDataPath").
                       replaceAll("gs://", ""));
      objectNode.put("IM_IS_SET_ACT", "X");
      JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
      BeforeActions.scenario.write("LTRC_ADVANCE_SETTINGS RFM Response : " + response);

    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
    Thread.sleep(10000); //sleep required to wait for async operation to be executed on SAP
  }

  @When("Delete the mtid$")
  public void user_deletes_mtid()
    throws JCoException, InterruptedException {

    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);

    Map opProps = new HashMap<>();

    opProps.put("RFC", "ZRFM_IUUC_REPL_DELETE_CONFIG");
    opProps.put("autoCommit", "true");
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("IM_MT_ID", mtId);
      objectNode.put("IV_CONFIG_GUID", guid);
      JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
      BeforeActions.scenario.write("MTID DELETE CONFIG RFM Response : " + response);
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
  }

  @Then("{string} the {string} records with {string} in the sap table")
  public void createTheRecordsInTheSLTDatasourceFromJCO(String process, String recordcount, String rfcName)
    throws IOException, JCoException, InterruptedException {
    action = process;
    if (action.equalsIgnoreCase("create")) {
      action = "I_NUM_C";
    } else if (action.equalsIgnoreCase("delete")) {
      action = "I_NUM_D";
    } else if (action.equalsIgnoreCase("update")) {
      action = "I_NUM_U";
    }
    sapProps = SAPProperties.getDefault(connection);
    errorCapture = new ErrorCapture(exceptionUtils);
    sapAdapterImpl = new SAPAdapterImpl(errorCapture, connection);

    Map opProps = new HashMap<>();
    opProps.put("RFC", pluginPropertyUtils.pluginProp(rfcName));
    opProps.put("autoCommit", "true");
    try {

      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put(action, recordcount);
      JsonNode response = sapAdapterImpl.executeRFC(objectNode.toString(), opProps, "", "");
      System.out.println(response.asText());
      int noOfRecords = Integer.parseInt(response.get("EX_COUNT").asText());
      Iterator<JsonNode> iteratedData = response.get("EX_DATA").iterator();
      while (iteratedData.hasNext()) {
        JsonNode object = iteratedData.next();
        Iterator<String> fieldName = object.fieldNames();
        if (fieldName.hasNext()) {
          fields.add(object.get(fieldName.next()).asText());
        }
      }
      BeforeActions.scenario.write("No of records :-" + noOfRecords + Arrays.toString(fields.toArray()));
    } catch (Exception e) {
      throw SystemException.throwException(e.getMessage(), e);
    }
    Thread.sleep(6000); //sleep required to wait for SAP record creation
  }
}
