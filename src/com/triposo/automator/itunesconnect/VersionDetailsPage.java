package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Predicates.not;

class VersionDetailsPage extends Page {
  @FindBy(css = ".metadataFieldReadonly input")
  WebElement version;
  @FindBy(id = "lightboxSaveButtonEnabled") WebElement save;

  @FindBy(id = "fileInput_iPhoneandiPodtouchScreenshots") WebElement iphoneScreenshotUpload;
  @FindBy(css = "#iPhoneandiPodtouchScreenshots .lcUploadSpinner") WebElement iphoneUploadSpinner;

  @FindBy(id = "fileInput_iPadScreenshots") WebElement ipadScreenshotUpload;
  @FindBy(css = "#iPadScreenshots .lcUploadSpinner") WebElement ipadUploadSpinner;
  @FindBy(css = ".wrapper-topright-button input") private WebElement readyToUploadBinary;

  public VersionDetailsPage(WebDriver driver) {
    super(driver);
  }

  public void clickEditVersionDetails() {
    final List<WebElement> elements = driver.findElements(By.cssSelector("img[alt=Edit]"));
    elements.get(0).click();
  }

  public void clickEditUploads() {
    // Make sure it's loaded.
    sleep(1000);
    final List<WebElement> elements = driver.findElements(By.cssSelector("img[alt=Edit]"));
    elements.get(2).click();
    // Make sure lightbox is loaded.
    sleep(1000);
  }

  public void deleteAllIphoneScreenshots() {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "906");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "905");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "904");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "903");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "902");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "901");
    deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "900");
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  public void deleteAllIpadScreenshots() {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    deleteIpadScreenshot("iPadScreenshots", "906");
    deleteIpadScreenshot("iPadScreenshots", "905");
    deleteIpadScreenshot("iPadScreenshots", "904");
    deleteIpadScreenshot("iPadScreenshots", "903");
    deleteIpadScreenshot("iPadScreenshots", "902");
    deleteIpadScreenshot("iPadScreenshots", "901");
    deleteIpadScreenshot("iPadScreenshots", "900");
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  private void deleteIpadScreenshot(String containerId, String imageId) {
    try {
      WebElement delete = driver.findElement(
          By.cssSelector("#lcUploaderImageContainer_" + containerId + "_" + imageId + " .lcUploaderImageDelete"));
      delete.click();
      sleep(1000);
    } catch (NoSuchElementException e) {
      // No such image I guess...
    }
  }

  public void uploadIpadScreenshot(File file) {
    ipadScreenshotUpload.sendKeys(file.getAbsolutePath());
    wait(ipadUploadSpinner).until(isDisplayed());
    wait(ipadUploadSpinner).until(not(isDisplayed()));
  }

  public void uploadIphoneScreenshot(File file) {
    iphoneScreenshotUpload.sendKeys(file.getAbsolutePath());
    wait(iphoneUploadSpinner).until(isDisplayed());
    wait(iphoneUploadSpinner).until(not(isDisplayed()));
  }

  public void changeVersionNumber(String version) {
    clickEditVersionDetails();
    wait(this.version).until(isDisplayed());
    this.version.clear();
    this.version.sendKeys(version);
    clickSave();
  }

  public void clickSave() {
    save.click();
  }

  public LegalIssuesPage clickReadyToUploadBinary() {
    readyToUploadBinary.click();
    return new LegalIssuesPage(driver);
  }
}
