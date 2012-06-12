package com.triposo.automator;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class Task {
  protected WebDriver driver;
  private final Properties properties = new Properties();

  public final void run() {
    try {
      properties.load(new FileInputStream("local.properties"));

      driver = new FirefoxDriver();
      driver.manage().window().setSize(new Dimension(1200, 1000));
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

      doRun();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected String getProperty(String name) {
    return properties.getProperty(name);
  }

  public abstract void doRun() throws Exception;
}
