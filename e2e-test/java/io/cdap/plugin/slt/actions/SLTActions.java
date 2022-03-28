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
package io.cdap.plugin.slt.actions;

import io.cdap.e2e.utils.ElementHelper;
import io.cdap.e2e.utils.PluginPropertyUtils;
import io.cdap.e2e.utils.SeleniumDriver;
import io.cdap.plugin.slt.locators.SLTLocators;
import org.openqa.selenium.WebDriver;

/**
 * SLT Actions.
 */
public class SLTActions {
    private static WebDriver cdfDriver;
    private static PluginPropertyUtils pluginPropertyUtils = new PluginPropertyUtils();

    static {
        cdfDriver = SeleniumDriver.getDriver();
    }
    public static void enterName(String name) {
        ElementHelper.sendKeys(SLTLocators.name, name);
    }
    public void enterMandatoryParameters(String gcpProjectId,
                                         String gcsDataPath,
                                         String massTransferId,
                                         String sapJcoLibGcsPath,
                                         String jcoclienthost,
                                         String jcoclient,
                                         String jcoclientsysnr,
                                         String jcoclientuser,
                                         String jcoclientpasswd) {
        ElementHelper.replaceElementValue(SLTLocators.gcpProjectId,
                pluginPropertyUtils.pluginProp(gcpProjectId));
        ElementHelper.sendKeys(SLTLocators.gcsDataPath, pluginPropertyUtils.pluginProp(gcsDataPath));
        ElementHelper.sendKeys(SLTLocators.massTransferId, massTransferId);
        ElementHelper.sendKeys(SLTLocators.sapJcoLibGcsPath, pluginPropertyUtils.pluginProp(sapJcoLibGcsPath));
        ElementHelper.sendKeys(SLTLocators.jcoClientHost, pluginPropertyUtils.pluginProp(jcoclienthost));
        ElementHelper.sendKeys(SLTLocators.jcoClient, pluginPropertyUtils.pluginProp(jcoclient));
        ElementHelper.sendKeys(SLTLocators.jcoClientSysnr,pluginPropertyUtils.pluginProp(jcoclientsysnr));
        ElementHelper.sendKeys(SLTLocators.jcoClientUser, pluginPropertyUtils.pluginProp(jcoclientuser));
        ElementHelper.sendKeys(SLTLocators.jcoClientPasswd, pluginPropertyUtils.pluginProp(jcoclientpasswd));
    }
    public void clickSltPlugin() {
        ElementHelper.clickOnElement(SLTLocators.sltplugin);
    }

    public String getRowErrorBorderColor() {
        return ElementHelper.getElementCssProperty(SLTLocators.rowError, "border-color");
    }

    public void clickNextButton() {
        ElementHelper.clickOnElement(SLTLocators.next);
    }

    public void selectTable(String tableName) {
        ElementHelper.clickOnElement(SLTLocators.table(tableName));
    }

    public void enterDatasetName (String stagingBucket) {
        ElementHelper.sendKeys(SLTLocators.datasetName, stagingBucket);
    }

    public void deployPipelineClick() {
        ElementHelper.clickOnElement(SLTLocators.deployPipeline);
    }

    public void startPipeline() {
        ElementHelper.clickIfDisplayed(SLTLocators.start, 60l);
    }

    public void stopPipeline() {
        ElementHelper.clickOnElement(SLTLocators.stop);
    }

    public void clickReplicateExistingData() {
        ElementHelper.clickOnElement(SLTLocators.replicateExistingData);
    }

    public String getReplicateExistingDataText() {
        return ElementHelper.getElementText(SLTLocators.replicateExistingData);
    }

    public void clickSuspendSltJob() {
        ElementHelper.clickOnElement(SLTLocators.suspendSltJob);
    }

    public String getSuspendSltJobText() {
        return ElementHelper.getElementText(SLTLocators.suspendSltJob);
    }
}
