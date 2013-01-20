package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class AppSummaryPage extends Page {
  @FindBy(xpath = "//div[contains(@class, 'version-container') and contains(h3/text(), 'Current Version')]/div[contains(@class, 'app-icon')]/a")
  WebElement currentVersionDetails;
  // This has to work with both "Add Version" and "View Details".
  @FindBy(xpath = "//div[contains(@class, 'version-container') and contains(h3/text(), 'New Version')]/div[contains(@class, 'app-icon')]/a")
  WebElement newVersionDetails;

  public AppSummaryPage(WebDriver driver) {
    super(driver);
  }

  public VersionDetailsPage clickCurrentVersionViewDetails() {
    currentVersionDetails.click();
    return new VersionDetailsPage(driver);
  }

  public NewVersionPage clickAddVersion() {
    newVersionDetails.click();
    return new NewVersionPage(driver);
  }

  public VersionDetailsPage clickNewVersionViewDetails() {
    newVersionDetails.click();
    return new VersionDetailsPage(driver);
  }
}
