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

package io.cdap.plugin.gcsmove.locators;

import io.cdap.e2e.utils.SeleniumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * GCSMove related locators.
 */
public class GCSMoveLocators {

  @FindBy(how = How.XPATH, using = "//input[@data-cy='project']")
  public static WebElement gcsMoveProjectID;

  @FindBy(how = How.XPATH, using = "//input[@data-cy='sourcePath']")
  public static WebElement gcsMoveSourcePath;

  @FindBy(how = How.XPATH, using = "//input[@data-cy='destPath']")
  public static WebElement gcsMoveDestinationPath;

  public static WebElement moveAllSubdirectories(String value) {
    return SeleniumDriver.getDriver().findElement(By.xpath("//input[@name='recursive' and @value='" + value + "']"));
  }

  public static WebElement overwriteExistingFiles(String value) {
    return SeleniumDriver.getDriver().findElement(By.xpath("//input[@name='overwrite' and @value='" + value + "']"));
  }

  @FindBy(how = How.XPATH, using = "//input[@data-cy='location']")
  public static WebElement gcsMoveLocation;

  @FindBy(how = How.XPATH, using = "//input[@data-cy='cmekKey']")
  public static WebElement gcsMoveEncryptionKey;
}
