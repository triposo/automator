package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class CheckWaitingForUpload extends Task {

  public static void main(String[] args) throws Exception {
    new CheckWaitingForUpload().run();
  }

  public void doRun() throws Exception {
    Integer appleId = Integer.parseInt(getProperty("apple_id"));

    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect(getProperty("itunes.username"), getProperty("itunes.password")).gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
    AppSummaryPage appSummaryPage = searchResultPage.clickFirstResult();
    if (appSummaryPage.containsText("Waiting For Upload")) {
      System.out.println("YES");
    } else {
      System.out.println("NO");
    }
  }

  private MainPage gotoItunesConnect(String username, String password) {
    driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(username, password);
    }
    try {
      WebElement continueButton = driver.findElement(By.cssSelector("img.customActionButton"));
      continueButton.click();
    } catch (NoSuchElementException e) {
    }
    return new MainPage(driver);
  }
}
