package com.triposo.automator.androidmarket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaunchNewVersion extends MarketTask {

  // To be changed when launching:
  // This is the what's new message for standalone guides!
  protected static final String RECENT_CHANGES = "1.9\n" +
      "- New action bar button for quickly adding photos, notes and checkins to your travel log;\n" +
      "- New travel log display;\n" +
      "- Allow editing travel log entries;\n" +
      "- Travel log syncing between devices;\n" +
      "- Bug fixes and other improvements;\n" +
      "Data:\n" +
      "- More info on national parks.\n" +
      "\n" +
      "1.8.6\n" +
      "- Allow bookmarking tours and Travelpedia pages;\n" +
      "- Added an app menu for accessing quickly the map or the travel log;\n" +
      "Data:\n" +
      "- More images, better location coverage.\n";
  protected static final String VERSION_NAME = "1.9";
  protected static final String VERSION_CODE = "150";
  protected static final String SHEET_NAME = "24";

  private static final String APKS_FOLDER = SHEET_NAME + "-" + VERSION_NAME;

  private static final int MAX_APK_SIZE_MB = 50;

  public static void main(String[] args) throws Exception {
    new LaunchNewVersion().run();
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    gotoHome();
    Set<String> upToDate = getUpToDateGuides();
    System.out.println("Up to date apps: " + upToDate);
    Map guides = getGuides();
    List<String> tooBig = Lists.newArrayList();
    List<String> notYetLaunched = Lists.newArrayList();
    List<String> failed = Lists.newArrayList();
    for (Object o : guides.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      String location = (String) entry.getKey();
      if (upToDate.contains(location.toLowerCase(Locale.US))) {
        System.out.println("Skipping because app already updated: " + location);
        continue;
      }
      if (location.equals("world")) {
        continue;
      }
      Map guide = (Map) entry.getValue();
      if (Boolean.TRUE.equals(guide.get("apk"))) {
        System.out.println("Processing " + location);
        try {
          launchNewVersion(location, guide);
          System.out.println("Done " + location);
        } catch (ApkTooBigException e) {
          tooBig.add(location);
          System.out.println("Skipping because apk too big: " + e.getMessage());
        } catch (AppMissingException e) {
          notYetLaunched.add(location);
          System.out.println("Skipping because app not up to date: " + location);
        } catch (Exception e) {
          failed.add(location);
          e.printStackTrace();
          System.out.println("Skipping because operation failed: " + location);
        }
      }
    }

    System.out.println("All done.");
    System.out.println("Too big: " + tooBig);
    System.out.println("Not yet launched in Google Play: " + notYetLaunched);
    System.out.println("Failed: " + failed);
    System.out.println("Please manually update the world guide!");
  }

  protected Set<String> getUpToDateGuides() {
    Set<String> locations = Sets.newHashSet();
    HomePage homePage = gotoHome();
    while (true) {
      locations.addAll(homePage.getAlreadyLaunched(VERSION_NAME));
      if (!homePage.hasNext()) {
        break;
      }
      homePage = homePage.clickNext();
    }
    return locations;
  }

  private void launchNewVersion(String location, Map guide) throws Exception {
    String appName = (String) guide.get("app_name");
    if (appName == null) {
      appName = location;
    }
    File apksDir = new File(getProperty("android.apks.dir"), APKS_FOLDER);
    File apkFile = new File(apksDir, appName + ".apk");
    if (!apkFile.isFile()) {
      throw new FileNotFoundException(apkFile.toString());
    }
    if (apkFile.length() > MAX_APK_SIZE_MB * 1024 * 1024) {
      throw new ApkTooBigException(location + ", " + apkFile.length());
    }

    AppEditorPage appEditorPage = gotoAppEditorForLocation(location);
    appEditorPage.clickApkFilesTab();
    appEditorPage.uploadApk(VERSION_CODE, apkFile);
    appEditorPage.clickActivate(VERSION_CODE);
    appEditorPage.clickProductDetailsTab();
    appEditorPage.enterRecentChanges(RECENT_CHANGES);
    appEditorPage.theLegalBlahBlah();
    appEditorPage.enterPrivacyPolicyLink("http://www.triposo.com/tandc.html");
    appEditorPage.clickSave();
    appEditorPage.clickPublishIfNecessary();
  }

  private class ApkTooBigException extends Exception {
    public ApkTooBigException(String s) {
      super(s);
    }
  }
}
