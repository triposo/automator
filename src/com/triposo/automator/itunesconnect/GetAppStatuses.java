package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class GetAppStatuses extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new GetAppStatuses().run();
  }

  public void doRun() throws Exception {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.clickSeeAll();
    searchResultPage.printStats();
    System.exit(0);
  }
}
