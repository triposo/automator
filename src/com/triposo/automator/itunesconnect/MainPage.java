package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class MainPage extends Page {
  @FindBy(partialLinkText = "Manage Your Applications")
  WebElement manageApplications;

  public MainPage(WebDriver driver) {
    super(driver);
  }

  public ManageApplicationsPage gotoManageApplications() {
    manageApplications.click();
    return new ManageApplicationsPage(driver);
  }
}
