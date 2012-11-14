package com.triposo.automator.itunesconnect;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class GetAppStatuses extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new GetAppStatuses().run();
  }

  public void doRun() throws Exception {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.clickSeeAll();
    Writer out = new OutputStreamWriter(new FileOutputStream(getProperty("appStatusesOut")));
    searchResultPage.printStats(out);
    out.close();
  }
}
