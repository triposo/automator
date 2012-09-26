package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class AddNewVersion extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new AddNewVersion().run();
  }

  public void doRun() throws Exception {
    String version = getProperty("version");
    String whatsnew = getProperty("whatsnew");

    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Object o : guides.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      String location = (String) entry.getKey();
      Map guide = (Map) entry.getValue();
      Map ios = (Map) guide.get("ios");
      if (ios != null) {
        Integer appleId = (Integer) ios.get("apple_id");
        if (appleId != null && appleId > 0) {
          System.out.println("Processing " + location);

          try {
            addNewVersion(appleId, version, whatsnew);
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

  private void addNewVersion(Integer appleId, String version, String whatsnew) {
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
      versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
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
