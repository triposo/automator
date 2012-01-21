package com.triposo.automator.androidmarket;

import com.triposo.automator.Page;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
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

import static com.google.common.base.Predicates.not;
import static org.junit.Assert.assertTrue;

public class AndroidMarket {

  private WebDriver driver;
  private final Properties properties = new Properties();

  public static void main(String[] args) throws Exception {
    try {
      new AndroidMarket().run();
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

  private static class SigninPage extends Page {
    @FindBy(id = "Email")
    WebElement username;
    @FindBy(id = "Passwd")
    WebElement password;
    @FindBy(id = "signIn")
    WebElement signIn;

    public SigninPage(WebDriver driver) {
      super(driver);
    }

    public void signin(String username, String password) {
      this.username.clear();
      this.username.sendKeys(username);
      this.password.clear();
      this.password.sendKeys(password);
      signIn.click();
    }
  }

  private static class AppEditorPage extends Page {
    @FindBy(id = "gwt-debug-multiple_apk-apk_list_tab")
    WebElement apkFilesTab;
    @FindBy(id = "gwt-debug-apk_list-upload_button")
    WebElement uploadApkButton;
    @FindBy(id = "gwt-debug-bundle_upload-save_button")
    WebElement uploadSaveButton;
    @FindBy(name = "Filedata")
    WebElement fileData;
    @FindBy(id = "gwt-debug-app_editor-apk-upload-upload_button")
    WebElement uploadButton;
    @FindBy(id = "gwt-debug-apk_list-apk_upload_dialog")
    WebElement uploadDialog;
    @FindBy(id = "gwt-debug-app_editor-locale-recent_changes-text_area")
    WebElement recentChanges;
    @FindBy(id = "gwt-debug-multiple_apk-save_button")
    WebElement saveButton;
    @FindBy(id = "gwt-debug-multiple_apk-product_detail_tab")
    WebElement productDetailTabs;
    @FindBy(id = "gwt-debug-meets_guidelines-input")
    WebElement meetsGuidelinesCheckbox;
    @FindBy(id = "gwt-debug-export_laws-input")
    WebElement exportLawsCheckbox;
    @FindBy(id = "gwt-debug-apk_list-simple_mode_link")
    WebElement simpleModeLink;

    public AppEditorPage(WebDriver driver) {
      super(driver);
    }

    public void clickApkFilesTab() {
      apkFilesTab.click();
    }

    public void clickProductDetailsTab() {
      productDetailTabs.click();
    }

    public void uploadApk(String versionCode, File file) {
      try {
        if (simpleModeLink.isDisplayed()) {
          simpleModeLink.click();
        }
      } catch (NoSuchElementException e) {
      }
      if (driver.findElement(By.tagName("body")).getText().contains(versionCode)) {
        System.out.println("This version is already uploaded skipping.");
        return;
      }
      uploadApkButton.click();
      wait(uploadDialog).until(isDisplayed());
      if (uploadSaveButton.isDisplayed()) {
        uploadSaveButton.click();
      } else {
        fileData.sendKeys(file.getAbsolutePath());
        uploadButton.click();
        wait(uploadSaveButton).withTimeout(15, TimeUnit.MINUTES).until(isDisplayed());
      }
      uploadSaveButton.click();
      try {
        wait(uploadDialog).until(not(isDisplayed()));
      } catch (NoSuchElementException e) {
      }
      if (!driver.findElement(By.tagName("body")).getText().contains(versionCode)) {
        throw new RuntimeException("Version not uploaded correctly");
      }
    }

    public void clickActivate(String versionCode) {
      try {
        WebElement activateLink = driver.findElement(By.id("gwt-debug-apk_list-activate_link-" + versionCode));
        if (!activateLink.isDisplayed()) {
          System.out.println("Already active. Skipping.");
          return;
        }
        activateLink.click();
        Alert alert = driver.switchTo().alert();
        alert.accept();
      } catch (NoSuchElementException e) {
        System.out.println("Already active. Skipping.");
      }
    }

    public void enterRecentChanges(String recentChanges) {
      this.recentChanges.clear();
      this.recentChanges.sendKeys(recentChanges);
    }

    public void clickSave() {
      saveButton.click();
    }

    public void theLegalBlahBlah() {
      check(meetsGuidelinesCheckbox);
      check(exportLawsCheckbox);
    }

    private void check(WebElement checkbox) {
      if (!"on".equals(checkbox.isSelected())) {
        checkbox.click();
      }
    }
  }
}
