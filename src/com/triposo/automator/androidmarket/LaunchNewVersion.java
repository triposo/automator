package com.triposo.automator.androidmarket;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class LaunchNewVersion {

  private WebDriver driver;
  private final Properties properties = new Properties();

  public static void main(String[] args) throws Exception {
    try {
      new LaunchNewVersion().run();
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

    String whatsnew = "Check in to places to share your trip with your Facebook friends.";

    String versionName = "1.5";
    String versionCode = "117";
    String sheetName = "1_5";
    String folderName = sheetName + "_" + versionName;
    File apksFolder = new File("../../Dropbox/apks/" + folderName);

    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      Map guide = (Map) entry.getValue();
      if (Boolean.TRUE.equals(guide.get("apk"))) {
        System.out.println("Processing " + location);

        try {
          String packageName = "com.triposo.droidguide." + location.toLowerCase();
          launchNewVersion(location, packageName, apksFolder, versionCode, whatsnew);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private void launchNewVersion(String location, String packageName, File versionRoot, String versionCode, String recentChanges) {
    AppEditorPage appEditorPage = gotoAppEditor(packageName);
    appEditorPage.clickApkFilesTab();
    appEditorPage.uploadApk(versionCode, new File(versionRoot, location + ".apk"));
    appEditorPage.clickActivate(versionCode);
    appEditorPage.clickProductDetailsTab();
    appEditorPage.enterRecentChanges(recentChanges);
    appEditorPage.theLegalBlahBlah();
    appEditorPage.clickSave();
  }

  private AppEditorPage gotoAppEditor(String packageName) {
    driver.get("https://market.android.com/publish/Home#AppEditorPlace:p=" + packageName);
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
      driver.get("https://market.android.com/publish/Home#AppEditorPlace:p=" + packageName);
    }
    return new AppEditorPage(driver);
  }

  private void touch(File doneFile) {
    try {
      FileOutputStream out = new FileOutputStream(doneFile);
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
