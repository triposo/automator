package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class AppSummaryPage extends Page {
  @FindBy(css = ".left .app-icon")
  WebElement leftHandSideVersionDetails;
  @FindBy(css = ".right img[title='View Details']") WebElement rightHandSideVersionDetails;

  public AppSummaryPage(WebDriver driver) {
    super(driver);
  }

  public VersionDetailsPage clickNewVersionViewDetails() {
    rightHandSideVersionDetails.click();
    return new VersionDetailsPage(driver);
  }

  public VersionDetailsPage clickCurrentVersionViewDetails() {
    rightHandSideVersionDetails.click();
    return new VersionDetailsPage(driver);
  }

  public NewVersionPage clickAddVersion() {
    rightHandSideVersionDetails.click();
    return new NewVersionPage(driver);
  }
}
