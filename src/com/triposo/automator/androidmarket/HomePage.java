package com.triposo.automator.androidmarket;

import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
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
      Matcher matcher = Pattern.compile("^(.*) Travel Guide", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).matcher(text);
      if (matcher.find()) {
        System.out.print(matcher.group(1).trim());
        System.out.print(",");
      }
      matcher = Pattern.compile("^(.*) total installs", Pattern.MULTILINE).matcher(text);
      if (matcher.find()) {
        System.out.print(matcher.group(1).trim().replaceAll(",", ""));
        System.out.print(",");
      }
      matcher = Pattern.compile("^(.*) net installs", Pattern.MULTILINE).matcher(text);
      if (matcher.find()) {
        System.out.print(matcher.group(1).trim().replaceAll(",", ""));
        System.out.print("\n");
      }
    }
  }
}
