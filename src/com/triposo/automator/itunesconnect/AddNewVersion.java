package com.triposo.automator.itunesconnect;

import java.io.FileNotFoundException;
import java.util.Map;

public class AddNewVersion extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new AddNewVersion().run();
  }

  public void doRun() throws Exception {
    String version = getProperty("version", "2.3.0");
    String whatsnew = getProperty("whatsnew");
    if (whatsnew == null) {
      throw new RuntimeException("Have to specify -Dwhatsnew");
    }

    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Map ios = (Map) guide.get("ios");
      Integer appleId = (Integer) ios.get("apple_id");
      String keywords = (String) ios.get("keywords");
      if (keywords == null) {
        keywords = "app, apps, travel, guide, free, offline, " + getProductName(location);
      }
      if (appleId != null && appleId > 0) {
        System.out.println("Processing " + location);

        try {
          addNewVersion(appleId, version, whatsnew, keywords);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private String getProductName(String location) throws FileNotFoundException {
    Map ios = (Map) getGuides().get(location);
    String productName = (String) ios.get("product_name");
    if (productName == null) {
      productName = location.replace('_', ' ');
    }
    return productName;
  }

  private void addNewVersion(Integer appleId, String version, String whatsnew, String keywords) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage;
    if (!appSummaryPage.containsText(version)) {
      if (appSummaryPage.containsText("New Version")) {
        // There is an old New Version. Update it.
        versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
      } else {
        if (!appSummaryPage.containsText("Ready for Sale")) {
          // There is an old, never launched, Current Version. Update it.
          versionDetailsPage = appSummaryPage.clickCurrentVersionViewDetails();
          // This field is missing for not-yet-launched apps.
          whatsnew = null;
          versionDetailsPage.clickEditVersionDetails();
          versionDetailsPage.changeVersionNumber(version);
          versionDetailsPage.clickSaveVersionDetails();
        } else {
          // Add a new version.
          NewVersionPage newVersionPage = appSummaryPage.clickAddVersion();
          newVersionPage.setVersionNumber(version);
          newVersionPage.setWhatsnew(whatsnew);
          versionDetailsPage = newVersionPage.clickSave();
        }
      }
    } else {
      try {
        versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
      } catch (Exception e) {
        // When this is the first version of a guide we have to go to the current version.
        versionDetailsPage = appSummaryPage.clickCurrentVersionViewDetails();
      }
    }
    versionDetailsPage.changeMetadata(whatsnew, keywords);
    if (versionDetailsPage.containsText("ERROR MESSAGE I CAN'T FIND") ||
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
