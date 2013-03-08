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

  private static final int MAX_APK_SIZE_MB = 50;

  public static void main(String[] args) throws Exception {
    new LaunchNewVersion().run();
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    String version = getProperty("version");
    String versionCode = getProperty("versionCode");
    String whatsnew = getProperty("whatsnew");
    String sheet = getProperty("sheet");

    Map guides = getGuides();
    Set<String> upToDate = getUpToDateGuides(version);
    System.out.println("Up to date apps: " + upToDate);
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
        // Has to be updated manually.
        continue;
      }
      Map guide = (Map) entry.getValue();
      System.out.println("Processing " + location);
      try {
        launchNewVersion(location, guide, sheet, version, versionCode, whatsnew);
        System.out.println("Done " + location);
      } catch (ApkTooBigException e) {
        tooBig.add(location);
        System.out.println("Skipping because apk too big: " + e.getMessage());
      } catch (AppMissingException e) {
        notYetLaunched.add(location);
        System.out.println("Skipping because app not yet launched: " + location);
      } catch (Exception e) {
        failed.add(location);
        e.printStackTrace();
        System.out.println("Skipping because operation failed: " + location);
      }
    }

    System.out.println("All done.");
    System.out.println("Too big: " + tooBig);
    System.out.println("Not yet launched in Google Play: " + notYetLaunched);
    System.out.println("Failed: " + failed);
    System.out.println("Please manually update the world guide!");
  }

  protected Set<String> getUpToDateGuides(String version) {
    Set<String> locations = Sets.newHashSet();
    HomePage homePage = gotoHome();
    while (true) {
      locations.addAll(homePage.getAlreadyLaunched(version));
      if (!homePage.hasNext()) {
        break;
      }
      homePage = homePage.clickNext();
    }
    return locations;
  }

  private void launchNewVersion(
      String location, Map guide, String sheet, String version,
      String versionCode, String whatsnew) throws Exception {
    String appName = (String) guide.get("app_name");
    if (appName == null) {
      appName = location;
    }
    File apksDir = new File(getProperty("android.apks.dir"), sheet + "-" + version);
    File apkFile = new File(apksDir, appName + ".apk");
    if (!apkFile.isFile()) {
      throw new FileNotFoundException(apkFile.toString());
    }
    if (apkFile.length() > MAX_APK_SIZE_MB * 1024 * 1024) {
      throw new ApkTooBigException(location + ", " + apkFile.length());
    }

    AppEditorPage appEditorPage = gotoAppEditorForLocation(location);
    appEditorPage.clickApkFilesTab();
    appEditorPage.uploadApk(versionCode, apkFile);
    appEditorPage.clickActivate(versionCode);
    appEditorPage.clickProductDetailsTab();
    appEditorPage.enterRecentChanges(whatsnew);
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
