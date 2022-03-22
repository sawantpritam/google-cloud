# Copyright © 2021 Cask Data, Inc.
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
Feature: Design Time SLT Scenario

  @SLT @Design-Time @Sanity
  Scenario Outline: User is able to provides input to SLT parameters
    Given Open CDF replication and initiate pipeline creation
    When Source is SAP SLT
    Then User is able to set SLT parameter "<option>" as "<input>"
    Examples:
      | option            | input         |
      | gcsDataPath       | invalid       |
      | guid              | invalidguid   |
      | massTransferId    | ZZZ66         |
      | sapJcoLibGcsPath  |gs://ga_slt_cdf|
      | jco.client.ashost | invalid       |
      | jco.client.sysnr  | abc           |
      | jco.client.client | abc           |
      | jco.client.lang   | invalid       |
      | jco.client.user   | invalid       |
      | jco.client.passwd | invalid       |

  @SLT @Design-Time @Sanity
  Scenario: User is able to set Replicate Existing Data as false
    Given Open CDF replication and initiate pipeline creation
    When Source is SAP SLT
    Then Replicate Existing Data is set to false

  @SLT @Design-Time @Sanity
  Scenario: User is able to set suspend Slt Job as true
    Given Open CDF replication and initiate pipeline creation
    When Source is SAP SLT
    Then Suspend Slt Job is set to true

  @SLT @Design-Time @Sanity
  Scenario Outline: User is able to configured job for "<table>"
    When User crates new MTID on "Automation" SAP
    When User updates mtid config in CDF_R_SLT_SETTINGS program
    Then Update mass transfer id table: "<table>" job mode to "replication"
    Given Open CDF replication and initiate pipeline creation
    When Source is SAP SLT fill connection parameters
    Then Click on next
    Then Select Table "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Click on Deploy Replication Pipeline
    Then Update mass transfer id table: "<table>" job mode to "stop_load"
    Examples:
      | table |
      | vbap |
      | ekko  |

#  @SLT @Design-Time @Sanity
#  Scenario Outline: User configured SLT job in LTRC for "<table>" and executes pipeline in "<mode>"
#    When User crates new MTID on "Automation" SAP
#    When User updates mtid config in CDF_R_SLT_SETTINGS program
#    Then Update mass transfer id table: "<table>" job mode to "stop_load"
#    Given Open CDF replication and initiate pipeline creation
#    When User fetches the record count of table : "<table>" from SAP
#    Then Delete GCS folder for mass transfer id table "<table>"
#    Then Drop target BigQuery table "<table>"
#    Then Update mass transfer id table: "<table>" job mode to "<mode>"
#    When Source is SAP SLT fill connection parameters
#    Then Click on next
#    Then Select Table "<table>"
#    Then Click on next
#    Then Enter the BigQuery Properties for slt datasource
#    Then Click on next
#    Then Click on next
#    Then Click on next
#    Then Click on Deploy Replication Pipeline
#    Then Run the slt Pipeline in Runtime
#    Then Open Logs of SLT Pipeline
#    Then Wait till SLT pipeline is in running state and no error occurs
#    Then Close logs and stop the pipeline
#    Then Update mass transfer id table: "<table>" job mode to "stop_load"
#    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
#    Then User is able to validate record count in BQ matches with count in SAP
#    Examples:
#      | table   | mode            |
#      | adr6    | initial_load     |
#      | t023    | replication     |
#      | t001    | replication     |
#
#  @SLT @Design-Time @Sanity
#  Scenario Outline: User configured SLT job for table containing all data types
#    When User crates new MTID on "Automation" SAP
#    When User updates mtid config in CDF_R_SLT_SETTINGS program
#    Then Update mass transfer id table: "<table>" job mode to "stop_load"
#    Given Open CDF replication and initiate pipeline creation
#    When User fetches the record count of table : "<table>" from SAP
#    Then Delete GCS folder for mass transfer id table "<table>"
#    Then Drop target BigQuery table "<table>"
#    Then Update mass transfer id table: "<table>" job mode to "<mode>"
#    When Source is SAP SLT fill connection parameters
#    Then Click on next
#    Then Select Table "<table>"
#    Then Click on next
#    Then Enter the BigQuery Properties for slt datasource
#    Then Click on next
#    Then Click on next
#    Then Click on next
#    Then Click on Deploy Replication Pipeline
#    Then Run the slt Pipeline in Runtime
#    Then Open Logs of SLT Pipeline
#    Then Wait till SLT pipeline is in running state and no error occurs
#    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
#    Then User is able to validate record count in BQ matches with count in SAP
#  Examples:
#    | table       | mode            |
#    | ZDATA_TYPE  | replication     |
##    | adr6        | replication     |
#
#
#
#
#  @SLT @Design-Time @Sanity
#  Scenario Outline: User configured SLT job in LTRC and executes pipeline in Replication
#    When User crates new MTID on "Automation" SAP
#    When User updates mtid config in CDF_R_SLT_SETTINGS program
#    Then Update mass transfer id table: "<table>" job mode to "stop_load"
#    Given Open CDF replication and initiate pipeline creation
#    When User fetches the record count of table : "<table>" from SAP
#    Then Delete GCS folder for mass transfer id table "<table>"
#    Then Drop target BigQuery table "<table>"
#    Then Update mass transfer id table: "<table>" job mode to "<mode>"
#    When Source is SAP SLT fill connection parameters
#    Then Click on next
#    Then Select Table "<table>"
#    Then Click on next
#    Then Enter the BigQuery Properties for slt datasource
#    Then Click on next
#    Then Click on next
#    Then Click on next
#    Then Click on Deploy Replication Pipeline
#    Then Run the slt Pipeline in Runtime
#    Then Open Logs of SLT Pipeline
#    Then Wait till SLT pipeline is in running state and no error occurs
#    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
#    Then User is able to validate record count in BQ matches with count in SAP
#    Then "<cud_type>" the "<count>" records with "<rfc>" in the sap table
#    Then Wait till SLT pipeline is in running state and no error occurs
#    Then Close logs and stop the pipeline
##    Then Update mass transfer id table: "<table>" job mode to "stop_load"
#    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
#    Then User is able to validate record count in BQ matches with count in SAP
#    Then "<cud_type>" the "<count>" records with "<rfc>" in the sap table
#    Then Run the slt Pipeline in Runtime
#    Then Open Logs of SLT Pipeline
#    Then Wait till SLT pipeline is in running state and no error occurs
#    Then Close logs and stop the pipeline
#    When User fetches the record count of table : "<table>" from SAP
#    Then Get Count of no of records transferred from SLT to BigQuery in "<table>"
#    Then User is able to validate record count in BQ matches with count in SAP
#    Then Update mass transfer id table: "<table>" job mode to "stop_load"
##    Then User deletes the mtid
#    Examples:
#      | table   | mode            | count | cud_type| rfc             |
#      | mara    | replication     | 1     | create  | rfc_matnr       |
#      | EKKO    | replication     | 5     | create  | rfc_2LIS_02_HDR |
##      | EKKO    | replication     | 10    | update  | rfc_2LIS_02_HDR |
##      | mara    | replication     | 10    | create  | rfc_matnr       |
##      | mara    | replication     | 10    | delete  | rfc_matnr       |
##      | mara    | replication     | 10    | update  | rfc_matnr       |
#
#  @SLT @Design-Time @Sanity
#  Scenario: User creates record in SAP
#    Then "create" the "1" records with "rfc_matnr" in the sap table
