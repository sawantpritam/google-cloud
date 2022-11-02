# Copyright © 2022 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
Feature:Run-Time SLT Scenario

  @SLT @Run-Time @Sanity
  Scenario: Validate that MTID can be created and updated using CDF_R_SLT_SETTINGS program
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program

  @SLT @Run-Time @Sanity
  Scenario Outline: Validate that SLT job can be configured for table containing all data types
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Given Open CDF replication and initiate pipeline creation
    When Fetche the record count of table : "<table>" from SAP
    Then Delete GCS folder for mass transfer id table "<table>"
    Then Drop target BigQuery table "<table>"
    Then Verify that user is able to update table "<table>" to "<mode>" on LTRC
    When Select SAP SLT as source and fill connection parameters
    Then Click on next
    Then Validate Table is available and  can be selected "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Validate Replication Pipeline can be deployed
    Then Run the slt Pipeline in Runtime
    Then Open Logs of SLT Pipeline
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Close logs and stop the pipeline
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate record count in BQ matches with count in SAP
    Examples:
      | table      | mode        |
      | ZDATA_TYPE | replication |

  @SLT @Run-Time @Sanity
  Scenario Outline: Validate that SLT job can be configured for in LTRC and executed for Initial Load Mode
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Given Open CDF replication and initiate pipeline creation
    When Fetche the record count of table : "<table>" from SAP
    Then Delete GCS folder for mass transfer id table "<table>"
    Then Drop target BigQuery table "<table>"
    Then Verify that user is able to update table "<table>" to "<mode>" on LTRC
    When Select SAP SLT as source and fill connection parameters
    Then Click on next
    Then Validate Table is available and  can be selected "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Validate Replication Pipeline can be deployed
    Then Run the slt Pipeline in Runtime
    Then Open Logs of SLT Pipeline
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Close logs and stop the pipeline
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate record count in BQ matches with count in SAP
    Examples:
      | table | mode         |
      | adr6  | initial_load |
      | t001  | initial_load |
      | t006a | initial_load |
      | t023t | initial_load |
      | t023  | initial_load |


  @SLT @Run-Time
  Scenario Outline: Validate that SLT job can be configured for job in LTRC and executed in Replication mode
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Given Open CDF replication and initiate pipeline creation
    When Fetche the record count of table : "<table>" from SAP
    Then Delete GCS folder for mass transfer id table "<table>"
    Then Drop target BigQuery table "<table>"
    Then Verify that user is able to update table "<table>" to "<mode>" on LTRC
    When Select SAP SLT as source and fill connection parameters
    Then Click on next
    Then Validate Table is available and  can be selected "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Validate Replication Pipeline can be deployed
    Then Run the slt Pipeline in Runtime
    Then Open Logs of SLT Pipeline
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate record count in BQ matches with count in SAP
    Then "<cud_type>" the "<count>" records with "<rfc>" in the sap table
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Close logs and stop the pipeline
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate record count in BQ matches with count in SAP
    Then "<cud_type>" the "<count>" records with "<rfc>" in the sap table
    Then Run the slt Pipeline in Runtime
    Then Open Logs of SLT Pipeline
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Close logs and stop the pipeline
    When Fetche the record count of table : "<table>" from SAP
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate record count in BQ matches with count in SAP
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Examples:
      | table | mode        | count | cud_type | rfc       |
      | mara  | replication | 1     | create   | rfc_matnr |

  @SLT @Run-Time
  Scenario Outline: Verify only replication data transfers when Replicate Existing Data is false
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Given Open CDF replication and initiate pipeline creation
    When Fetche the record count of table : "<table>" from SAP
    Then Delete GCS folder for mass transfer id table "<table>"
    Then Drop target BigQuery table "<table>"
    Then Verify that user is able to update table "<table>" to "<mode>" on LTRC
    When Select SAP SLT as source and fill connection parameters
    Then Validate Replicate Existing Data can be set to false
    Then Click on next
    Then Validate Table is available and  can be selected "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Validate Replication Pipeline can be deployed
    Then "<cud_type>" the "<count>" records with "<rfc>" in the sap table
    Then Run the slt Pipeline in Runtime
    Then Open Logs of SLT Pipeline
    Then Wait till SLT pipeline is in running state and no error occurs
    Then Close logs and stop the pipeline
    When Fetche the record count of table : "<table>" from SAP
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Then Delete the mtid
    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
    Then Validate only replication data is loaded ie "<count>"
    Examples:
      | table | mode        | count | cud_type | rfc       |
      | mara  | replication | 1     | create   | rfc_matnr |
