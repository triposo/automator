package com.triposo.automator.itunesconnect;

public class GetAppStatuses extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new GetAppStatuses().run();
  }

  public void doRun() throws Exception {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.clickSeeAll();
    searchResultPage.printStats();
  }
}
