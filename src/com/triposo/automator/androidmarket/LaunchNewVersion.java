package com.triposo.automator.androidmarket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
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

    String whatsnew = "1.7\n" +
        "- Smart travel suggestions on the main screen\n" +
        "- Location tools: weather, currency converter, phrasebook\n" +
        "- \"Read to me\" option for longer texts\n" +
        "- Map scale\n" +
        "- Tour prices in device's currency\n" +
        "- Add place directly on map by long-tapping\n" +
        "- Bug fixes and optimizations\n" +
        "Data:\n" +
        "- Now includes smaller pittoresque cities that invite discovery\n" +
        "- Health, Safety & Money POIs for your practical needs\n" +
        "- Travelpedia provides background articles";

    String versionName = "1.7.2";
    String versionCode = "131";
    String sheetName = "22";
    String folderName = sheetName + "-" + versionName;
    File apksFolder = new File("apks/" + folderName);

    gotoHome();

    Set<String> alreadyLaunchedGuides = getAlreadyLaunchedGuides(versionName);
    System.out.println("Already launched: " + alreadyLaunchedGuides);
    Map guides = getGuides();
    List<String> tooBig = Lists.newArrayList();
    List<String> notYetLaunched = Lists.newArrayList();
    List<String> failed = Lists.newArrayList();
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      if (alreadyLaunchedGuides.contains(location.toLowerCase(Locale.US))) {
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
          launchNewVersion(location, guide, apksFolder, versionCode, whatsnew);
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
    }

    System.out.println("All done.");
    System.out.println("Too big: " + tooBig);
    System.out.println("Not yet launched: " + notYetLaunched);
    System.out.println("Failed: " + failed);
  }

  private Set<String> getAlreadyLaunchedGuides(String versionName) {
    Set<String> locations = Sets.newHashSet();
    HomePage homePage = gotoHome();
    while (true) {
      locations.addAll(homePage.getAlreadyLaunched(versionName));
      if (!homePage.hasNext()) {
        break;
      }
      homePage = homePage.clickNext();
    }
    return locations;
  }

  private void launchNewVersion(String location, Map guide, File versionRoot, String versionCode, String recentChanges) throws Exception {
    String appName = (String) guide.get("app_name");
    if (appName == null) {
      appName = location;
    }
    File apkFile = new File(versionRoot, appName + ".apk");
    if (!apkFile.isFile()) {
      throw new FileNotFoundException(apkFile.toString());
    }
    if (apkFile.length() > MAX_APK_SIZE_MB * 1024 * 1024) {
      throw new ApkTooBigException(location + ", " + apkFile.length());
    }

    String packageName = "com.triposo.droidguide." + location.toLowerCase();
    AppEditorPage appEditorPage = gotoAppEditor(packageName);
    appEditorPage.clickApkFilesTab();
    appEditorPage.uploadApk(versionCode, apkFile);
    appEditorPage.clickActivate(versionCode);
    appEditorPage.clickProductDetailsTab();
    appEditorPage.enterRecentChanges(recentChanges);
    appEditorPage.theLegalBlahBlah();
    appEditorPage.enterPrivacyPolicyLink("http://triposo.com/tandc.html");
    appEditorPage.clickSave();
    appEditorPage.clickPublishIfNecessary();
  }

  private class ApkTooBigException extends Exception {
    public ApkTooBigException(String s) {
      super(s);
    }
  }
}
