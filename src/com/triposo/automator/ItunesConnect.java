package com.triposo.automator;

import com.google.common.base.Predicate;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.FluentWait;
import org.yaml.snakeyaml.Yaml;

import static com.google.common.base.Predicates.not;
import static org.junit.Assert.assertTrue;

public class ItunesConnect {

  private WebDriver driver;
  private String username;
  private String password;

  public static void main(String[] args) throws Exception {
    try {
      new ItunesConnect().run();
    } finally {
      System.exit(0);
    }
  }

  public void run() throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream("local.properties"));
    username = properties.getProperty("username");
    password = properties.getProperty("password");

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
    Map guides = (Map) yaml.load(new FileInputStream(new File("pipeline/config/guides.yaml")));
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
            uploadScreenshots(location, ios);
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
    assertTrue("New version was not added for some reason.", appSummaryPage.containsText(version));
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

  private void uploadScreenshots(String location, Map ios) {
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
        if (!appSummaryPage.containsText("New Version")) {
          System.out.println("Doesn't have a new version, skipping: " + appleId);
          return;
        }
        VersionDetailsPage versionDetailsPage = appSummaryPage.clickNewVersionViewDetails();

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

  private static class SigninPage extends Page {
    @FindBy(name = "theAccountName") WebElement username;
    @FindBy(name = "theAccountPW") WebElement password;
    @FindBy(css = "input[alt|=\"Sign In\"]") WebElement signIn;

    public SigninPage(WebDriver driver) {
      super(driver);
    }

    public MainPage signin(String username, String password) {
      this.username.clear();
      this.username.sendKeys(username);
      this.password.clear();
      this.password.sendKeys(password);
      signIn.click();
      return new MainPage(driver);
    }
    
  }
  
  private static class MainPage extends Page {
    @FindBy(partialLinkText = "Manage Your Applications") WebElement manageApplications;

    public MainPage(WebDriver driver) {
      super(driver);
    }

    public ManageApplicationsPage gotoManageApplications() {
      manageApplications.click();
      return new ManageApplicationsPage(driver);
    }
  }

  private static class ManageApplicationsPage extends Page {
    @FindBy(css = ".search-param-value-appleId input") WebElement appleId;
    @FindBy(css = "input[value|=\"Search\"]") WebElement search;

    public ManageApplicationsPage(WebDriver driver) {
      super(driver);
    }

    public SearchResultPage searchByAppleId(Integer appleId) {
      this.appleId.clear();
      this.appleId.sendKeys(appleId.toString());
      search.click();
      return new SearchResultPage(driver);
    }
  }

  private static class SearchResultPage extends Page {
    public SearchResultPage(WebDriver driver) {
      super(driver);
    }

    public AppSummaryPage clickFirstResult() {
      final List<WebElement> elements = driver.findElements(By.cssSelector("div.software-column-type-col-0 a"));
      elements.get(0).click();
      return new AppSummaryPage(driver);
    }
  }

  private static class AppSummaryPage extends Page {
    @FindBy(css = ".right a") WebElement rightHandSideVersionDetails;
    
    public AppSummaryPage(WebDriver driver) {
      super(driver);
    }

    public VersionDetailsPage clickNewVersionViewDetails() {
      rightHandSideVersionDetails.click();
      return new VersionDetailsPage(driver);
    }

    public NewVersionPage clickAddVersion() {
      rightHandSideVersionDetails.click();
      return new NewVersionPage(driver);
    }
  }

  private static class VersionDetailsPage extends Page {
    @FindBy(css = ".metadataFieldReadonly input") WebElement version;
    @FindBy(id = "lightboxSaveButtonEnabled") WebElement save;

    @FindBy(id = "fileInput_iPhoneandiPodtouchScreenshots") WebElement iphoneScreenshotUpload;
    @FindBy(css = "#iPhoneandiPodtouchScreenshots .lcUploadSpinner") WebElement iphoneUploadSpinner;

    @FindBy(id = "fileInput_iPadScreenshots") WebElement ipadScreenshotUpload;
    @FindBy(css = "#iPadScreenshots .lcUploadSpinner") WebElement ipadUploadSpinner;
    @FindBy(css = ".wrapper-topright-button input") private WebElement readyToUploadBinary;

    public VersionDetailsPage(WebDriver driver) {
      super(driver);
    }

    public void clickEditVersionDetails() {
      final List<WebElement> elements = driver.findElements(By.cssSelector("img[alt=Edit]"));
      elements.get(0).click();
    }

    public void clickEditUploads() {
      // Make sure it's loaded.
      sleep(1000);
      final List<WebElement> elements = driver.findElements(By.cssSelector("img[alt=Edit]"));
      elements.get(2).click();
      // Make sure lightbox is loaded.
      sleep(1000);
    }

    public void deleteAllIphoneScreenshots() {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "906");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "905");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "904");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "903");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "902");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "901");
      deleteIpadScreenshot("iPhoneandiPodtouchScreenshots", "900");
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public void deleteAllIpadScreenshots() {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
      deleteIpadScreenshot("iPadScreenshots", "906");
      deleteIpadScreenshot("iPadScreenshots", "905");
      deleteIpadScreenshot("iPadScreenshots", "904");
      deleteIpadScreenshot("iPadScreenshots", "903");
      deleteIpadScreenshot("iPadScreenshots", "902");
      deleteIpadScreenshot("iPadScreenshots", "901");
      deleteIpadScreenshot("iPadScreenshots", "900");
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    private void deleteIpadScreenshot(String containerId, String imageId) {
      try {
        WebElement delete = driver.findElement(
            By.cssSelector("#lcUploaderImageContainer_" + containerId + "_" + imageId + " .lcUploaderImageDelete"));
        delete.click();
        sleep(1000);
      } catch (NoSuchElementException e) {
        // No such image I guess...
      }
    }

    public void uploadIpadScreenshot(File file) {
      ipadScreenshotUpload.sendKeys(file.getAbsolutePath());
      wait(ipadUploadSpinner).until(isDisplayed());
      wait(ipadUploadSpinner).until(not(isDisplayed()));
    }

    public void uploadIphoneScreenshot(File file) {
      iphoneScreenshotUpload.sendKeys(file.getAbsolutePath());
      wait(iphoneUploadSpinner).until(isDisplayed());
      wait(iphoneUploadSpinner).until(not(isDisplayed()));
    }

    public void changeVersionNumber(String version) {
      clickEditVersionDetails();
      wait(this.version).until(isDisplayed());
      this.version.clear();
      this.version.sendKeys(version);
      clickSave();
    }

    public void clickSave() {
      save.click();
    }

    public LegalIssuesPage clickReadyToUploadBinary() {
      readyToUploadBinary.click();
      return new LegalIssuesPage(driver);
    }
  }

  private static class LegalIssuesPage extends Page {
    @FindBy(css = "input[name=encryptionHasChanged][value=false]") WebElement encryptionHasChangedNo;
    @FindBy(css = "input[name=hasLegalIssues][value=false]") WebElement hasLegalIssuesNo;
    @FindBy(css = ".wrapper-right-button input") WebElement save;

    public LegalIssuesPage(WebDriver driver) {
      super(driver);
    }

    public void theUsualBlahBlah() {
      encryptionHasChangedNo.click();
      hasLegalIssuesNo.click();
    }

    public AutoReleasePage clickSave() {
      save.click();
      return new AutoReleasePage(driver);
    }
  }

  private static class AutoReleasePage extends Page {
    @FindBy(css = "input[name=goLive][value=false]") WebElement autoReleaseYes;
    @FindBy(css = ".wrapper-right-button input") WebElement save;
    @FindBy(css = ".wrapper-right-button input") WebElement continueButton;

    public AutoReleasePage(WebDriver driver) {
      super(driver);
    }

    public void setAutoReleaseYes() {
      autoReleaseYes.click();
    }

    public void clickSave() {
      save.click();
    }

    public void clickContinue() {
      continueButton.click();
    }
  }

  private static class NewVersionPage extends Page {
    @FindBy(css = ".metadataFieldReadonly input") WebElement version;
    @FindBy(css = "#whatsNewinthisVersionUpdateContainerId textarea") WebElement whatsnew;
    @FindBy(css = ".wrapper-right-button input") WebElement save;

    public NewVersionPage(WebDriver driver) {
      super(driver);
    }

    public void setVersionNumber(String version) {
      this.version.clear();
      this.version.sendKeys(version);
    }

    public void setWhatsnew(String whatsnew) {
      this.whatsnew.clear();
      this.whatsnew.sendKeys(whatsnew);
    }

    public void clickSave() {
      save.click();
    }
  }
}
