package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class ManageApplicationsPage extends Page {
  @FindBy(css = ".search-param-value-appleId input")
  WebElement appleId;
  @FindBy(css = "input[value|=\"Search\"]") WebElement search;

  public ManageApplicationsPage(WebDriver driver) {
    super(driver);
  }

  public SearchResultPage searchByAppleId(Integer appleId) {
    this.appleId.clear();
    this.appleId.sendKeys(appleId.toString());
    search.click();
    return new SearchResultPage(driver);
  }
}
