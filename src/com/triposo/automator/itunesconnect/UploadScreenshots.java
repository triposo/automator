package com.triposo.automator.itunesconnect;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UploadScreenshots extends ItunesConnectTask {

  public static final String LAST_IPHONE_SCREENSHOT = "15-city-tours.png";
  private static final String LAST_IPAD_SCREENSHOT = "TODO";

  private WebDriver driver;

  public static void main(String[] args) throws Exception {
    new UploadScreenshots().run();
  }

  @Override
  public void doRun() throws Exception {
    driver = new FirefoxDriver();
    driver.manage().window().setSize(new Dimension(1200, 1000));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      if (!location.equals("Italy")) {
        continue;
      }
      Map ios = (Map) guide.get("ios");
      if (ios != null) {
        Integer appleId = (Integer) ios.get("apple_id");
        if (appleId != null && appleId > 0) {
          System.out.println("Processing " + location);
          boolean newVersion = "new".equals(getProperty("ios.screenshots.app.version.to.update"));
          try {
            uploadScreenshots(location, ios, newVersion);
          } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Error processing, skipping: " + location);
          }

          System.out.println("Done " + location);
        }
      }
    }

    System.out.println("All done.");
  }

  private void uploadScreenshots(String location, Map ios, boolean newVersion)
      throws VersionMissingException, MostRecentVersionRejectedException {
    Integer appleId = (Integer) ios.get("apple_id");
    if (appleId != null && appleId > 0) {
      File directoryIPhone = new File(getProperty("ios.screenshots.iphone.dir") + "/" + location);
      File directoryIPad = new File(getProperty("ios.screenshots.ipad.dir") + "/" + location);
      List<File> screenshotsIPhone = getGuideScreenshots(directoryIPhone);
      List<File> screenshotsIPad = getGuideScreenshots(directoryIPad);
      if (screenshotsIPhone.isEmpty() && screenshotsIPad.isEmpty()) {
        // Nothing to upload.
        return;
      }
      VersionDetailsPage versionDetailsPage = getVersionDetailsPage(appleId, newVersion);
      versionDetailsPage.clickEditMetadataAndUploads();

      if (screenshotsContain(screenshotsIPhone, LAST_IPHONE_SCREENSHOT)) {
        versionDetailsPage.deleteAllIphoneScreenshots();
        for (File screenshot : screenshotsIPhone) {
          versionDetailsPage.uploadIphoneScreenshot(screenshot);
        }
        markGuideScreenshotsUploaded(directoryIPhone);
      }
      if (screenshotsContain(screenshotsIPad, LAST_IPAD_SCREENSHOT)) {
        versionDetailsPage.deleteAllIpadScreenshots();
        for (File screenshot : screenshotsIPad) {
          versionDetailsPage.uploadIpadScreenshot(screenshot);
        }
        markGuideScreenshotsUploaded(directoryIPad);
      }

      versionDetailsPage.clickSaveVersionDetails();
    }
  }

  private VersionDetailsPage getVersionDetailsPage(Integer appleId, boolean newVersion)
      throws VersionMissingException, MostRecentVersionRejectedException {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      throw new MostRecentVersionRejectedException();
    }
    if (newVersion) {
      if (!appSummaryPage.containsText("New Version")) {
        throw new VersionMissingException("Doesn't have a new version: " + appleId);
      }
      return appSummaryPage.clickNewVersionViewDetails();
    } else {
      if (!appSummaryPage.containsText("Current Version")) {
        throw new VersionMissingException("Doesn't have a current version: " + appleId);
      }
      return appSummaryPage.clickCurrentVersionViewDetails();
    }
  }
}
