package com.triposo.automator.androidmarket;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractStats {

  private WebDriver driver;
  private final Properties properties = new Properties();

  public static void main(String[] args) throws Exception {
    try {
      new ExtractStats().run();
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

    System.out.println("Guide,Total installs,Net installs");
    HomePage homePage = gotoHome();
    while (homePage.hasNext()) {
      homePage.printStats();
      homePage = homePage.clickNext();
    }
    homePage.printStats();
  }

  private HomePage gotoHome() {
    String url = "https://market.android.com/publish/Home";
    driver.get(url);
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
      driver.get(url);
    }
    return new HomePage(driver);
  }
}
