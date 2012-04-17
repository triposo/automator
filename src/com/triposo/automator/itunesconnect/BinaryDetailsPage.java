package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BinaryDetailsPage extends Page {
  @FindBy(id="rejectBinaryButtonId")
  WebElement rejectBinaryButton;
  @FindBy(css = "#rejectBinaryMessage a")
  WebElement acceptRejectBinaryButton;

  public BinaryDetailsPage(WebDriver driver) {
    super(driver);
  }

  public void clickRejectThisBinary() {
    rejectBinaryButton.click();
    acceptRejectBinaryButton.click();
  }
}
