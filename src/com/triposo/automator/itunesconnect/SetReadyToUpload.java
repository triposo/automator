package com.triposo.automator.itunesconnect;

import java.util.Map;

public class SetReadyToUpload extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new SetReadyToUpload().run();
  }

  public void doRun() throws Exception {
    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Map ios = (Map) guide.get("ios");
      if (ios != null) {
        Integer appleId = (Integer) ios.get("apple_id");
        if (appleId != null && appleId > 0) {
          System.out.println("Processing " + location);

          try {
            setReadyToUpload(appleId);
          } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error processing, skipping: " + location);
          }

          System.out.println("Done " + location);
        }
      }
    }

    System.out.println("All done.");
  }

  private void setReadyToUpload(Integer appleId) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    LegalIssuesPage legalIssuesPage = versionDetailsPage.clickReadyToUploadBinary();
    legalIssuesPage.theUsualBlahBlah();
    AutoReleasePage autoReleasePage = legalIssuesPage.clickSave();
    autoReleasePage.setAutoReleaseYes();
    autoReleasePage.clickSave();
  }

}
