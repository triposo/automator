package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class GetAppStatuses extends Task {

  public static void main(String[] args) throws Exception {
    new GetAppStatuses().run();
  }

  public void doRun() throws Exception {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect(getProperty("itunes.username"), getProperty("itunes.password")).gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.clickSeeAll();
    searchResultPage.printStats();
    System.exit(0);
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
