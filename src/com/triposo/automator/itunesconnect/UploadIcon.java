package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;

public class UploadIcon extends Task {

  public static void main(String[] args) throws Exception {
    new UploadIcon().run();
  }

  public void doRun() throws Exception {
    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      Map guide = (Map) entry.getValue();
      Map ios = (Map) guide.get("ios");
      if (ios != null) {
        Integer appleId = (Integer) ios.get("apple_id");
        if (appleId != null && appleId.intValue() > 0) {
          System.out.println("Processing " + location);

          try {
            uploadIcon(location, appleId);
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

  private void uploadIcon(String location, Integer appleId) {
    ManageApplicationsPage manageApplicationsPage = gotoItunesConnect(getProperty("itunes.username"), getProperty("itunes.password")).gotoManageApplications();
    SearchResultPage searchResultPage = manageApplicationsPage.searchByAppleId(appleId);
    AppSummaryPage appSummaryPage = searchResultPage.clickFirstResult();
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      System.out.println("Last version rejected, skipping: " + appleId);
      return;
    }
    VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();
    versionDetailsPage.clickEditVersionDetails();

    versionDetailsPage.uploadLargeIcon(new File("../../Dropbox/splash/" + location.toLowerCase() + "/icon1024x1024.png"));

    versionDetailsPage.clickSaveVersionDetails();
  }

  private MainPage gotoItunesConnect(String username, String password) {
    driver.get("https://itunesconnect.apple.com");
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(username, password);
    }
    try {
      WebElement continueButton = driver.findElement(By.cssSelector("img.customActionButton"));
      continueButton.click();
    } catch (NoSuchElementException e) {
    }
    return new MainPage(driver);
  }
}
