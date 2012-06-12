package com.triposo.automator.androidmarket;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaunchNewVersion {

  private WebDriver driver;
  private final Properties properties = new Properties();

  public static void main(String[] args) throws Exception {
    try {
      new LaunchNewVersion().run();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  public void run() throws Exception {
    properties.load(new FileInputStream("local.properties"));

    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);
    driver = new FirefoxDriver();
    driver.manage().window().setSize(new Dimension(1200, 1000));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

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

    driver.get(rootUrl());
    SigninPage signinPage = new SigninPage(driver);
    signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
    signinPage.waitForAppListLoaded();

    Set<String> alreadyLaunchedGuides = getAlreadyLaunchedGuides(versionName);
    System.out.println("Already launched: " + alreadyLaunchedGuides);
    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      if (alreadyLaunchedGuides.contains(location.toLowerCase(Locale.US))) {
        System.out.println("Skipping because it seems already updated: " + location);
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
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
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

  private String rootUrl() {
    return "https://play.google.com/apps/publish/Home?dev_acc=" + getDevAccountId();
  }

  private void launchNewVersion(String location, Map guide, File versionRoot, String versionCode, String recentChanges) {
    String appName = (String) guide.get("app_name");
    if (appName == null) {
      appName = location;
    }
    File apkFile = new File(versionRoot, appName + ".apk");
    if (!apkFile.isFile()) {
      System.out.println("Could not find apk for " + location + ". Skipping.");
      return;
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

  private AppEditorPage gotoAppEditor(String packageName) {
    String url = rootUrl() + "#AppEditorPlace:p=" + packageName;
    gotoPage(url);
    return new AppEditorPage(driver);
  }

  private HomePage gotoHome() {
    gotoPage(rootUrl());
    return new HomePage(driver);
  }

  private void gotoPage(String url) {
    driver.get(url);
    if (isSigninPage()) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
      driver.get(url);
      Preconditions.checkArgument(!isSigninPage(), "Cannot signin");
    }
  }

  private boolean isSigninPage() {
    return driver.findElement(By.cssSelector("body")).getText().contains("Password");
  }

  private String getDevAccountId() {
    return "06870337150021354184";
  }
}
