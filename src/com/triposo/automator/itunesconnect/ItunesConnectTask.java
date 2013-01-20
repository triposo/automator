package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

public abstract class ItunesConnectTask extends Task {

  @Override
  protected boolean isGuideValid(Map guide) {
    // If the ios subsection exists, the ios app exists.
    return guide.get("ios") != null;
  }

  protected Integer getAppleIdOfGuide(Map guide) {
    Map ios = (Map) guide.get("ios");
    return (Integer) ios.get("apple_id");
  }

  protected MainPage gotoItunesConnect() {
    driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Sign In")) {
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
