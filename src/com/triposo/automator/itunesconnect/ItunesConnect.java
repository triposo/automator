package com.triposo.automator.itunesconnect;

import com.google.common.base.Preconditions;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

public class ItunesConnect {

  private WebDriver driver;
  private String username;
  private String password;

  public static void main(String[] args) throws Exception {
    try {
      new ItunesConnect().run();
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  public void run() throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream("local.properties"));
    username = properties.getProperty("itunes.username");
    password = properties.getProperty("itunes.password");

    String version = "1.4.1";
    String whatsnew =
        "- new design \n" +
            "- dynamic suggestions \n" +
            "- checkins \n" +
            "- facebook integration";

//    driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
//    driver = new HtmlUnitDriver();
    driver = new FirefoxDriver();
    driver.manage().window().setSize(new Dimension(1200, 1000));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      if (!location.equals("Italy")) {
        continue;
      }
      Map guide = (Map) entry.getValue();
      Map ios = (Map) guide.get("ios");
      if (ios != null) {
        Integer appleId = (Integer) ios.get("apple_id");
        if (appleId != null && appleId.intValue() > 0) {
          System.out.println("Processing " + location);

          try {
            uploadScreenshots(location, ios, false);
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
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
    AppSummaryPage appSummaryPage = searchResultPage.clickFirstResult();
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    if (!appSummaryPage.containsText(version)) {
      NewVersionPage newVersionPage = appSummaryPage.clickAddVersion();
      newVersionPage.setVersionNumber(version);
      newVersionPage.setWhatsnew(whatsnew);
      newVersionPage.clickSave();
    }
    Preconditions.checkArgument(appSummaryPage.containsText(version), "New version was not added for some reason.");
    if (appSummaryPage.containsText("Prepare for Upload")) {
      VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
      LegalIssuesPage legalIssuesPage = versionDetailsPage.clickReadyToUploadBinary();
      legalIssuesPage.theUsualBlahBlah();
      AutoReleasePage autoReleasePage = legalIssuesPage.clickSave();
      autoReleasePage.setAutoReleaseYes();
      autoReleasePage.clickSave();
      autoReleasePage.clickContinue();
    }
  }

  private void uploadScreenshots(String location, Map ios, boolean newVersion) {
    Integer appleId = (Integer) ios.get("apple_id");
    if (appleId != null && appleId.intValue() > 0) {
      String bundleId = (String) ios.get("bundle_id");
      if (bundleId == null) {
        bundleId = "com.triposo." + location.toLowerCase() + "guide";
      }
      File directory = new File("/Users/tirsen/triposo/Dropbox/ios screenshots/" + bundleId);
      if (directory.exists()) {

        File doneFile = new File(directory, "DONE");
        if (doneFile.exists()) {
          System.out.println("Already done, skipping: " + appleId);
          System.out.println("(Delete " + doneFile.getAbsolutePath() + " if incorrect.)");
          return;
        }
        ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
        SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
        AppSummaryPage appSummaryPage = searchResultPage.clickFirstResult();
        if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
          System.out.println("Last version rejected, skipping: " + appleId);
          return;
        }
        VersionDetailsPage versionDetailsPage;
        if (newVersion) {
          if (!appSummaryPage.containsText("New Version")) {
            System.out.println("Doesn't have a new version, skipping: " + appleId);
            return;
          }
          versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
        } else {
          if (!appSummaryPage.containsText("Current Version")) {
            System.out.println("Doesn't have a current version, skipping: " + appleId);
            return;
          }
          versionDetailsPage = appSummaryPage.clickCurrentVersionViewDetails();
        }

        versionDetailsPage.clickEditUploads();

        if (new File(directory, "iphone4.png").exists()) {
          versionDetailsPage.deleteAllIphoneScreenshots();
          versionDetailsPage.uploadIphoneScreenshot(new File(directory, "iphone1.png"));
          versionDetailsPage.uploadIphoneScreenshot(new File(directory, "iphone2.png"));
          versionDetailsPage.uploadIphoneScreenshot(new File(directory, "iphone3.png"));
          versionDetailsPage.uploadIphoneScreenshot(new File(directory, "iphone4.png"));
        }

        if (new File(directory, "ipad4.png").exists()) {
          versionDetailsPage.deleteAllIpadScreenshots();
          versionDetailsPage.uploadIpadScreenshot(new File(directory, "ipad1.png"));
          versionDetailsPage.uploadIpadScreenshot(new File(directory, "ipad2.png"));
          versionDetailsPage.uploadIpadScreenshot(new File(directory, "ipad3.png"));
          versionDetailsPage.uploadIpadScreenshot(new File(directory, "ipad4.png"));
        }

        versionDetailsPage.clickSave();

        touch(doneFile);

      }
    }
  }

  private void touch(File doneFile) {
    try {
      FileOutputStream out = new FileOutputStream(doneFile);
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setVersion(Integer appleId, String version) {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect().gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
    AppSummaryPage appSummaryPage = searchResultPage.clickFirstResult();
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    if (!appSummaryPage.containsText("New Version")) {
      System.out.println("Doesn't have a new version, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    if (versionDetailsPage.containsText(version)) {
      System.out.println("Already set to new version, skipping: " + appleId);
      return;
    }
    versionDetailsPage.changeVersionNumber(version);
  }

  private MainPage gotoItunesConnect() {
    driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      return signinPage.signin(username, password);
    } else {
      return new MainPage(driver);
    }
  }
}
