package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class AutoReleasePage extends Page {
  @FindBy(css = "input[name=goLive][value=false]")
  WebElement autoReleaseYes;
  @FindBy(css = ".wrapper-right-button input") WebElement save;
  @FindBy(css = ".wrapper-right-button input") WebElement continueButton;

  public AutoReleasePage(WebDriver driver) {
    super(driver);
  }

  public void setAutoReleaseYes() {
    autoReleaseYes.click();
  }

  public void clickSave() {
    save.click();
  }

  public void clickContinue() {
    continueButton.click();
  }
}
