@GCSMove
Feature:GCSMove - Verification of successful objects move from one bucket to another

  @CMEK @GCS_CSV_TEST @GCS_SINK_TEST
  Scenario:Validate successful move object from one bucket to another new bucket
    Given Open Datafusion Project to configure pipeline
    When Expand Plugin group in the LHS plugins list: "Conditions and Actions"
    When Select plugin: "GCS Move" from the plugins list as: "Action"
    When Navigate to the properties page of plugin: "GCS Move"
    Then Enter GCSMove property projectId "projectId"
    Then Enter GCSMove property source path "gcsCsvFile"
    Then Enter GCSMove property destination path
    Then Enter GCSMove property encryption key name "cmekGCS" if cmek is enabled
    Then Validate "GCSMove" plugin properties
    Then Close GCSMove properties
    Then Save and Deploy Pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open and capture logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate GCSMove successfully moved object "gcsCsvFile" to destination bucket
    Then Validate the cmek key "cmekGCS" of target GCS bucket if cmek is enabled

  @GCS_READ_RECURSIVE_TEST @GCS_SINK_TEST
  Scenario:Validate successful move objects from one bucket to another with Move All Subdirectories set to true
    Given Open Datafusion Project to configure pipeline
    When Expand Plugin group in the LHS plugins list: "Conditions and Actions"
    When Select plugin: "GCS Move" from the plugins list as: "Action"
    When Navigate to the properties page of plugin: "GCS Move"
    Then Enter GCSMove property projectId "projectId"
    Then Enter GCSMove property source path "gcsMoveReadRecursivePath"
    Then Enter GCSMove property destination path
    Then Select GCSMove property move all subdirectories as "true"
    Then Validate "GCSMove" plugin properties
    Then Close GCSMove properties
    Then Save and Deploy Pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open and capture logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate GCSMove successfully moved object "gcsMoveReadRecursivePath" to destination bucket

  @GCS_CSV_TEST @GCS_SINK_EXISTING_BUCKET_TEST
  Scenario:Validate successful move objects from one bucket to another existing bucket with Overwrite Existing Files set to true
    Given Open Datafusion Project to configure pipeline
    When Expand Plugin group in the LHS plugins list: "Conditions and Actions"
    When Select plugin: "GCS Move" from the plugins list as: "Action"
    When Navigate to the properties page of plugin: "GCS Move"
    Then Enter GCSMove property projectId "projectId"
    Then Enter GCSMove property source path "gcsCsvFile"
    Then Enter GCSMove property destination path "gcsMoveReadRecursivePath"
    Then Select GCSMove property overwrite existing files as "true"
    Then Validate "GCSMove" plugin properties
    Then Close GCSMove properties
    Then Save and Deploy Pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open and capture logs
    Then Verify the pipeline status is "Succeeded"
    Then Validate GCSMove successfully moved object "gcsCsvFile" to destination bucket

  @GCS_CSV_TEST @GCS_SINK_EXISTING_BUCKET_TEST @PLUGIN-1134
  Scenario:Validate successful move objects from one bucket to another existing bucket with Overwrite Existing Files set to false
    Given Open Datafusion Project to configure pipeline
    When Expand Plugin group in the LHS plugins list: "Conditions and Actions"
    When Select plugin: "GCS Move" from the plugins list as: "Action"
    When Navigate to the properties page of plugin: "GCS Move"
    Then Enter GCSMove property projectId "projectId"
    Then Enter GCSMove property source path "gcsCsvFile"
    Then Enter GCSMove property destination path "gcsMoveReadRecursivePath"
    Then Select GCSMove property overwrite existing files as "false"
    Then Validate "GCSMove" plugin properties
    Then Close GCSMove properties
    Then Save and Deploy Pipeline
    Then Run the Pipeline in Runtime
    Then Wait till pipeline is in running state
    Then Open and capture logs
    Then Verify the pipeline status is "failed"
    Then Validate GCSMove failed to move object "gcsCsvFile" to destination bucket
