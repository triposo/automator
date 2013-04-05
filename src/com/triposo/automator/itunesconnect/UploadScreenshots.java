package com.triposo.automator.itunesconnect;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UploadScreenshots extends ItunesConnectTask {

  private static final int SCREENSHOT_COUNT = 5;

  public static void main(String[] args) throws Exception {
    new UploadScreenshots().run();
  }

  @Override
  public void doRun() throws Exception {
    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Integer appleId = getAppleIdOfGuide(guide);
      if (appleId != null && appleId > 0) {
        System.out.println("Processing " + location);
        try {
          uploadScreenshots(location, appleId);
        } catch (Throwable e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }


  private abstract class Device {
    private final File directory;
    private final List<File> screenshots;
    private final boolean uploaded;

    public Device(String location, String dirProperty) {
      directory = new File(getProperty(dirProperty, "/nonexisting") + "/" + location);
      screenshots = getGuideScreenshots(directory);
      uploaded = areGuideScreenshotsUploaded(directory);
    }

    public boolean canBeUploaded() {
      return !uploaded && screenshots.size() == SCREENSHOT_COUNT;
    }

    public void uploadAllIfCanBeUploaded(VersionDetailsPage versionDetailsPage) {
      if (!canBeUploaded()) {
        return;
      }
      deleteAll(versionDetailsPage);
      for (File screenshot : screenshots) {
        upload(versionDetailsPage, screenshot);
      }
      markGuideScreenshotsUploaded(directory);
    }

    public abstract void deleteAll(VersionDetailsPage versionDetailsPage);
    public abstract void upload(VersionDetailsPage versionDetailsPage, File screenshot);
  }


  private class IPhoneDevice extends Device {
    public IPhoneDevice(String location) {
      super(location, "ios.screenshots.iphone.dir");
    }

    @Override
    public void deleteAll(VersionDetailsPage versionDetailsPage) {
      versionDetailsPage.deleteAllIphoneScreenshots();
    }

    @Override
    public void upload(VersionDetailsPage versionDetailsPage, File screenshot) {
      versionDetailsPage.uploadIphoneScreenshot(screenshot);
    }
  }


  private class IPhone4InchDevice extends Device {
    public IPhone4InchDevice(String location) {
      super(location, "ios.screenshots.iphone-4inch.dir");
    }

    @Override
    public void deleteAll(VersionDetailsPage versionDetailsPage) {
      versionDetailsPage.deleteAllIphone4InchScreenshots();
    }

    @Override
    public void upload(VersionDetailsPage versionDetailsPage, File screenshot) {
      versionDetailsPage.uploadIphone4InchScreenshot(screenshot);
    }
  }


  private class IPadDevice extends Device {
    public IPadDevice(String location) {
      super(location, "ios.screenshots.ipad.dir");
    }

    @Override
    public void deleteAll(VersionDetailsPage versionDetailsPage) {
      versionDetailsPage.deleteAllIpadScreenshots();
    }

    @Override
    public void upload(VersionDetailsPage versionDetailsPage, File screenshot) {
      versionDetailsPage.uploadIpadScreenshot(screenshot);
    }
  }


  private void uploadScreenshots(String location, Integer appleId)
      throws VersionMissingException, MostRecentVersionRejectedException {
    if (appleId == null || appleId <= 0) {
      return;
    }
    Device iPhone = new IPhoneDevice(location);
    Device iPhone4Inch = new IPhone4InchDevice(location);
    Device iPad = new IPadDevice(location);
    if (!iPhone.canBeUploaded() &&
        !iPhone4Inch.canBeUploaded() &&
        !iPad.canBeUploaded()) {
      // Nothing to upload.
      return;
    }
    VersionDetailsPage versionDetailsPage = getVersionDetailsPage(appleId);
    versionDetailsPage.clickEditMetadataAndUploads();
    iPhone.uploadAllIfCanBeUploaded(versionDetailsPage);
    iPhone4Inch.uploadAllIfCanBeUploaded(versionDetailsPage);
    iPad.uploadAllIfCanBeUploaded(versionDetailsPage);
    versionDetailsPage.clickSaveVersionDetails();
  }

  private VersionDetailsPage getVersionDetailsPage(Integer appleId)
      throws VersionMissingException, MostRecentVersionRejectedException {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      throw new MostRecentVersionRejectedException();
    }
    // This is a bit awkward. In theory it's possible to upload the screenshots
    // to the Current Version or to the New Version, but:
    // - Not yet launched apps do not have a New Version.
    // - Already launched apps cannot have the screenshots of the Current Version updated.
    // That's why if there is a New Version, we always upload there.
    if (appSummaryPage.containsText("New Version")) {
      return appSummaryPage.clickNewVersionViewDetails();
    } else if (appSummaryPage.containsText("Current Version")) {
      return appSummaryPage.clickCurrentVersionViewDetails();
    } else {
      throw new VersionMissingException("Doesn't have a current or new version: " + appleId);
    }
  }
}
