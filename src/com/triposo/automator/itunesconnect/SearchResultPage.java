package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

class SearchResultPage extends Page {
  public SearchResultPage(WebDriver driver) {
    super(driver);
  }

  public AppSummaryPage clickFirstResult() {
    final List<WebElement> elements = driver.findElements(By.cssSelector("div.software-column-type-col-0 a"));
    elements.get(0).click();
    return new AppSummaryPage(driver);
  }
}
