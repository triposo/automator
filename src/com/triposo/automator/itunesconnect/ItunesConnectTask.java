package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public abstract class ItunesConnectTask extends Task {
  protected MainPage gotoItunesConnect() {
    driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(getProperty("itunes.username"), getProperty("itunes.password"));
    }
    try {
      WebElement continueButton = driver.findElement(By.cssSelector("img.customActionButton"));
      continueButton.click();
    } catch (NoSuchElementException e) {
    }
    return new MainPage(driver);
  }

  protected AppSummaryPage gotoAppSummary(Integer appleId) {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
    return searchResultPage.clickFirstResult();
  }

  protected class VersionMissingException extends Throwable {
    public VersionMissingException(String s) {
      super(s);
    }
  }

  protected class MostRecentVersionRejectedException extends Throwable {
  }
}
