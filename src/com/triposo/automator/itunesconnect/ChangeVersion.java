package com.triposo.automator.itunesconnect;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class ChangeVersion extends ItunesConnectTask {
  // To be changed!
  private final String version = "2.0.1";

  public static void main(String[] args) throws Exception {
    new ChangeVersion().run();
  }

  public void doRun() throws Exception {

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
            changeVersion(appleId, version);
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

  private void changeVersion(Integer appleId, String version) {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    versionDetailsPage.changeVersionNumber(version);
  }
}
