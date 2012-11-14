package com.triposo.automator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class Task {
  protected WebDriver driver;
  private final Properties properties = new Properties();

  public final void run() {
    try {
      properties.load(new FileInputStream("local.properties"));

      if (getProperty("browser", "firefox").equals("chrome")) {
        driver = new ChromeDriver();
      } else {
        driver = new FirefoxDriver();
      }
      driver.manage().window().setSize(new Dimension(1200, 1000));
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

      doRun();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected String getProperty(String name) {
    if (System.getProperties().containsKey(name)) {
      return System.getProperty(name);
    } else if (properties.containsKey(name)) {
      return properties.getProperty(name);
    } else {
      throw new IllegalStateException(
          String.format("Need to specify property %s on command line or as a system property.", name));
    }
  }

  protected String getProperty(String name, String defaultValue) {
    try {
      return getProperty(name);
    } catch (IllegalStateException e) {
      return defaultValue;
    }
  }

  protected Map getGuides() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    return (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
  }

  protected List<File> getGuideScreenshots(File dir) {
    if (!dir.isDirectory()) {
      // No biggie.
      System.out.println("Screenshots directory missing: " + dir);
      return Lists.newArrayList();
    }
    File doneFile = getDoneFileForScreenshotsDir(dir);
    if (doneFile.exists()) {
      System.out.println("Already uploaded: " + dir);
      System.out.println("(Delete " + dir.getAbsolutePath() + " if incorrect.)");
      return Lists.newArrayList();
    }
    List<File> images = Lists.newArrayList(dir.listFiles());
    Collections.sort(images);
    return Lists.newArrayList(
        Iterators.filter(images.iterator(),
        new Predicate<File>() {
          @Override
          public boolean apply(File file) {
            return file != null && file.getName().endsWith(".png");
          }
        }));
  }

  private File getDoneFileForScreenshotsDir(File dir) {
    return new File(dir, "DONE");
  }

  protected void markGuideScreenshotsUploaded(File dir) {
    touch(getDoneFileForScreenshotsDir(dir));
  }

  private void touch(File doneFile) {
    try {
      FileOutputStream out = new FileOutputStream(doneFile);
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean screenshotsContain(List<File> files, String fileName) {
    for (File file : files) {
      if (file.getName().equals(fileName)) {
        return true;
      }
    }
    return false;
  }

  public abstract void doRun() throws Exception;
}
