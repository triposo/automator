package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class AppSummaryPage extends Page {
  @FindBy(xpath = "//div[contains(@class, 'version-container') and contains(h3/text(), 'Current Version')]/div[contains(@class, 'app-icon')]/a")
  WebElement currentVersionDetails;
  @FindBy(xpath = "//div[contains(@class, 'version-container') and contains(h3/text(), 'New Version')]/div[contains(@class, 'app-icon')]/a")
  WebElement newVersionDetails;
  @FindBy(xpath = "//a[contains(text(), 'Add Version')]")
  WebElement addVersion;

  public AppSummaryPage(WebDriver driver) {
    super(driver);
  }

  public VersionDetailsPage clickCurrentVersionViewDetails() {
    currentVersionDetails.click();
    return new VersionDetailsPage(driver);
  }

  public NewVersionPage clickAddVersion() {
    addVersion.click();
    return new NewVersionPage(driver);
  }

  public VersionDetailsPage clickNewVersionViewDetails() {
    newVersionDetails.click();
    return new VersionDetailsPage(driver);
  }
}
