package com.triposo.automator.itunesconnect;

import java.io.File;
import java.util.Map;

public class UploadIcon extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new UploadIcon().run();
  }

  public void doRun() throws Exception {
    String splashDir = getProperty("ios.splash.dir");
    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Integer appleId = getAppleIdOfGuide(guide);
      if (appleId != null && appleId > 0) {
        System.out.println("Processing " + location);

        try {
          uploadIcon(location, appleId, splashDir);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private void uploadIcon(String location, Integer appleId, String splashDir) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    versionDetailsPage.clickEditVersionDetails();

    File iconFile = new File(splashDir + "/" + location.toLowerCase() + "/icon1024x1024.png");
    versionDetailsPage.uploadLargeIcon(iconFile);

    versionDetailsPage.clickSaveVersionDetails();
  }

}
