package com.triposo.automator.itunesconnect;

import java.util.Map;

public class ChangeVersion extends ItunesConnectTask {
  // To be changed!
  private final String version = "2.0.1";

  public static void main(String[] args) throws Exception {
    new ChangeVersion().run();
  }

  public void doRun() throws Exception {
    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Integer appleId = getAppleIdOfGuide(guide);
      if (appleId != null && appleId > 0) {
        System.out.println("Processing " + location);

        try {
          changeVersion(appleId, version);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private void changeVersion(Integer appleId, String version) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    versionDetailsPage.changeVersionNumber(version);
  }
}
