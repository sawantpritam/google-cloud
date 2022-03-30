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
Feature: Design Time SLT Scenario

  @SLT @Design-Time @Sanity
  Scenario Outline: Validate mandatory SLT parameters is present and editable
    Given Open CDF replication and initiate pipeline creation
    When Select SAP SLT as source
    Then Validate SLT parameter "<option>" can be set as "<input>"
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
  Scenario: Validate Replicate Existing Data can be set as false
    Given Open CDF replication and initiate pipeline creation
    When Select SAP SLT as source
    Then Validate Replicate Existing Data can be set to false

  @SLT @Design-Time @Sanity
  Scenario: Validate suspend Slt Job can be set as true
    Given Open CDF replication and initiate pipeline creation
    When Select SAP SLT as source
    Then Validate Suspend Slt Job can be set to true

  @SLT @Design-Time @Sanity
  Scenario Outline: Validate CDF replication job can be configured job for "<table>"
    Given Create new MTID on "Automation" SAP
    When Validate that MTID can be Updated using CDF_R_SLT_SETTINGS program
    Then Verify that user is able to update table "<table>" to "replication" on LTRC
    Given Open CDF replication and initiate pipeline creation
    When Select SAP SLT as source and fill connection parameters
    Then Click on next
    Then Validate Table is available and  can be selected "<table>"
    Then Click on next
    Then Enter the BigQuery Properties for slt datasource
    Then Click on next
    Then Click on next
    Then Click on next
    Then Validate Replication Pipeline can be deployed
    Then Verify that user is able to update table "<table>" to "stop_load" on LTRC
    Examples:
      | table |
      | vbap |
      | ekko  |
