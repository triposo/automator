package com.triposo.automator.androidmarket;

import com.google.common.base.Preconditions;
import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

  protected AppEditorPage gotoAppEditor(String packageName) {
    gotoPage(rootUrl() + "#AppEditorPlace:p=" + packageName);
    return new AppEditorPage(driver);
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

  protected Map getGuides() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    return (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
  }
}
