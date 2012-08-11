package com.triposo.automator.androidmarket;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePage extends Page {
  @FindBy(linkText = "Next ›")
  private WebElement nextLink;
  @FindBy(id = "gwt-debug-applistingappList")
  private WebElement appsTable;

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public boolean hasNext() {
    try {
      return nextLink.isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }
  }


  public HomePage clickNext() {
    nextLink.click();
    return new HomePage(driver);
  }

  public void printStats() {
    List<WebElement> rows = appsTable.findElements(By.className("listingRow"));
    for (WebElement row : rows) {
      String text = row.getText();
      String name = null;
      String totalInstalls = null;
      String netInstalls = null;
      Iterable<String> lines = Splitter.on("\n").split(text);
      for (String line : lines) {
        if (name == null) {
          name = line;
        } else {
          Matcher matcher = Pattern.compile("^(.*) total user installs").matcher(line);
          if (matcher.find()) {
            totalInstalls = matcher.group(1).trim().replaceAll(",", "");
          }
          matcher = Pattern.compile("^(.*) active device installs").matcher(line);
          if (matcher.find()) {
            netInstalls = matcher.group(1).trim().replaceAll(",", "");
          }
        }
      }

      System.out.println(String.format("%s,%s,%s", name, totalInstalls, netInstalls));
    }
  }

  public Set<String> getAlreadyLaunched(String versionName) {
    Set<String> guides = Sets.newHashSet();
    Pattern pattern = Pattern.compile("\\s" + versionName.replaceAll("\\.", "\\.") + "\\s");
    List<WebElement> rows = appsTable.findElements(By.className("listingRow"));
    for (WebElement row : rows) {
      String text = row.getText();
      if (!pattern.matcher(text).find()) {
        continue;
      }
      WebElement link = row.findElement(By.id("gwt-debug-statsLink"));
      if (link == null) {
        continue;
      }
      String href = link.getAttribute("href");
      String[] tmp = href.split("\\.");
      String app = tmp[tmp.length - 1];
      guides.add(app);
    }
    return guides;
  }

  public void waitForAppListLoaded() {
    try {
      wait("All Google Play Android app listings").until(textIsOnPage());
      wait("Amsterdam").until(textIsOnPage());
    } catch (UnhandledAlertException e) {
      driver.switchTo().alert().dismiss();
    }
  }
}
