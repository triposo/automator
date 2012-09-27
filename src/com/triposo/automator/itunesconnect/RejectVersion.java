package com.triposo.automator.itunesconnect;

import java.util.Map;

public class RejectVersion extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new RejectVersion().run();
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
            rejectVersion(appleId);
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

  private void rejectVersion(Integer appleId) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    if (!appSummaryPage.containsText("Waiting For Review")) {
      System.out.println("There is no version for review. Skipping.");
      return;
    }
    BinaryDetailsPage binaryDetailsPage = versionDetailsPage.clickBinaryDetails();
    binaryDetailsPage.clickRejectThisBinary();
  }
}
