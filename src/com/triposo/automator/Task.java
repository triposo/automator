package com.triposo.automator;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Predicates.not;
import static org.junit.Assert.assertTrue;

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
