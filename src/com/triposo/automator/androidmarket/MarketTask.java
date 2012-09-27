package com.triposo.automator.androidmarket;

import com.google.common.base.Preconditions;
import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Map;

/**
 * A Google Play related task.
 */
public abstract class MarketTask extends Task {

  protected HomePage gotoHome() {
    gotoPage(rootUrl());
    HomePage homePage = new HomePage(driver);
    homePage.waitForAppListLoaded();
    return homePage;
  }

  protected String rootUrl() {
    return "https://play.google.com/apps/publish/Home?dev_acc=" + getDevAccountId();
  }

  protected AppEditorPage gotoAppEditor(String packageName) throws AppMissingException {
    gotoPage(rootUrl() + "#AppEditorPlace:p=" + packageName);
    AppEditorPage appEditorPage = new AppEditorPage(driver);
    appEditorPage.waitForTabsLoaded();
    return appEditorPage;
  }

  protected AppEditorPage gotoAppEditorForLocation(String location) throws AppMissingException {
    String packageName = "com.triposo.droidguide." + location.toLowerCase(Locale.US);
    return gotoAppEditor(packageName);
  }

  protected void gotoPage(String url) {
    driver.get(url);
    if (isSigninPage()) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(
          getProperty("android.username"), getProperty("android.password"));
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
