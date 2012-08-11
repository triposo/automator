package com.triposo.automator.androidmarket;

import com.triposo.automator.Page;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Predicates.not;

public class AppEditorPage extends Page {
  @FindBy(id = "gwt-debug-multiple_apk-apk_list_tab")
  WebElement apkFilesTab;
  @FindBy(id = "gwt-debug-apk_list-upload_button")
  WebElement uploadApkButton;
  @FindBy(id = "gwt-debug-bundle_upload-save_button")
  WebElement uploadSaveButton;
  @FindBy(name = "Filedata")
  WebElement fileData;
  @FindBy(id = "gwt-debug-app_editor-apk-upload-upload_button")
  WebElement uploadButton;
  @FindBy(id = "gwt-debug-apk_list-apk_upload_dialog")
  WebElement uploadDialog;
  @FindBy(id = "gwt-debug-app_editor-locale-recent_changes-text_area")
  WebElement recentChanges;
  @FindBy(id = "gwt-debug-privacy_policy")
  WebElement privacyPolicy;
  @FindBy(id = "gwt-debug-multiple_apk-save_button")
  WebElement saveButton;
  @FindBy(id = "gwt-debug-multiple_apk-product_detail_tab")
  WebElement productDetailTabs;
  @FindBy(id = "gwt-debug-meets_guidelines-input")
  WebElement meetsGuidelinesCheckbox;
  @FindBy(id = "gwt-debug-export_laws-input")
  WebElement exportLawsCheckbox;
  @FindBy(id = "gwt-debug-apk_list-simple_mode_link")
  WebElement simpleModeLink;
  @FindBy(id = "gwt-debug-multiple_apk-unpublish_button")
  WebElement unpublishButton;
  @FindBy(id = "gwt-debug-multiple_apk-publish_button")
  WebElement publishButton;

  public AppEditorPage(WebDriver driver) {
    super(driver);
  }

  public void clickApkFilesTab() throws AppMissingException {
    if (!apkFilesTab.isDisplayed()) {
      throw new AppMissingException();
    }
    apkFilesTab.click();
  }

  public void clickProductDetailsTab() {
    productDetailTabs.click();
  }

  public void uploadApk(String versionCode, File file) {
    try {
      if (simpleModeLink.isDisplayed()) {
        simpleModeLink.click();
      }
    } catch (NoSuchElementException e) {
    }
    if (driver.findElement(By.tagName("body")).getText().contains(versionCode)) {
      System.out.println("This version is already uploaded skipping.");
      return;
    }
    uploadApkButton.click();
    wait(uploadDialog).until(isDisplayed());
    if (uploadSaveButton.isDisplayed()) {
      uploadSaveButton.click();
    } else {
      fileData.sendKeys(file.getAbsolutePath());
      uploadButton.click();
      wait(uploadSaveButton).withTimeout(15, TimeUnit.MINUTES).until(isDisplayed());
    }
    uploadSaveButton.click();
    try {
      wait(uploadDialog).until(not(isDisplayed()));
    } catch (NoSuchElementException e) {
    }
    if (!driver.findElement(By.tagName("body")).getText().contains(versionCode)) {
      throw new RuntimeException("Version not uploaded correctly");
    }
  }

  public void clickActivate(String versionCode) {
    try {
      WebElement activateLink = driver.findElement(By.id("gwt-debug-apk_list-activate_link-" + versionCode));
      if (!activateLink.isDisplayed()) {
        System.out.println("Already active. Skipping.");
        return;
      }
      activateLink.click();
      Alert alert = driver.switchTo().alert();
      alert.accept();
    } catch (NoSuchElementException e) {
      System.out.println("Already active. Skipping.");
    }
  }

  public void enterRecentChanges(String recentChanges) {
    this.recentChanges.clear();
    this.recentChanges.sendKeys(recentChanges);
  }

  public void enterPrivacyPolicyLink(String privacyPolicy) {
    this.privacyPolicy.clear();
    this.privacyPolicy.sendKeys(privacyPolicy);
  }

  public void clickSave() {
    saveButton.click();
    wait("Saved successfully.").until(textIsOnPage());
  }

  public void theLegalBlahBlah() {
    check(meetsGuidelinesCheckbox);
    check(exportLawsCheckbox);
  }

  private void check(WebElement checkbox) {
    if (!checkbox.isSelected()) {
      checkbox.click();
    }
  }

  public void clickUnpublish() {
    unpublishButton.click();
  }

  public void clickPublishIfNecessary() {
    try {
      if (publishButton.isDisplayed()) {
        publishButton.click();
      }
    } catch (Exception e) {
      // Let's ignore this if it happens.
      e.printStackTrace();
    }
  }

  public void waitForTabsLoaded() throws AppMissingException {
    try {
      wait("Product details").until(textIsOnPage());
      wait("APK files").until(textIsOnPage());
    } catch (UnhandledAlertException e) {
      // This application is out of date. Click OK to refresh application data.
      driver.switchTo().alert().dismiss();
      // An unexpected error occurred. Please try again later.
      driver.switchTo().alert().dismiss();
      driver.switchTo().alert().dismiss();
      driver.switchTo().alert().dismiss();
      throw new AppMissingException();
    }
  }
}
