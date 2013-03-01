package com.triposo.automator.itunesconnect;

import java.util.Map;
import java.util.NoSuchElementException;

public class AddNewVersion extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new AddNewVersion().run();
  }

  public void doRun() throws Exception {
    int appleId = Integer.parseInt(getProperty("appleId"));
    String version = getProperty("version");
    String whatsnew = getProperty("whatsnew");
    String keywords = getProperty("keywords");
    addNewVersion(appleId, version, whatsnew, keywords);
    System.out.println("All done.");
  }

  private void addNewVersion(Integer appleId, String version, String whatsnew, String keywords) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage;
    if (!appSummaryPage.containsText(version)) {
      NewVersionPage newVersionPage = appSummaryPage.clickAddVersion();
      newVersionPage.setVersionNumber(version);
      newVersionPage.setWhatsnew(whatsnew);
      versionDetailsPage = newVersionPage.clickSave();
    } else {
      try {
        versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
      } catch (Exception e) {
        // When this is the first version of a guide we have to go to the current version.
        versionDetailsPage = appSummaryPage.clickCurrentVersionViewDetails();
      }
    }
    versionDetailsPage.changeMetadata(whatsnew, keywords);
    if (appSummaryPage.containsText("ERROR MESSAGE I CAN'T FIND") ||
        getProperty("appReviewForceUpdate", null) != null) {
      versionDetailsPage.changeAppReviewInformation(
          getProperty("appReviewFirstName"),
          getProperty("appReviewLastName"),
          getProperty("appReviewEmail"),
          getProperty("appReviewPhone"));
    }
    if (appSummaryPage.containsText("Prepare for Upload") || appSummaryPage.containsText("Developer Rejected")) {
      LegalIssuesPage legalIssuesPage = versionDetailsPage.clickReadyToUploadBinary();
      legalIssuesPage.theUsualBlahBlah();
      AutoReleasePage autoReleasePage = legalIssuesPage.clickSave();
      autoReleasePage.setAutoReleaseYes();
      autoReleasePage.clickSave();
    }
  }
}
