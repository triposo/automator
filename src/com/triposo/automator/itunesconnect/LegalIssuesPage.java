package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class LegalIssuesPage extends Page {
  @FindBy(css = "input[name=encryptionHasChanged][value=false]")
  WebElement encryptionHasChangedNo;
  @FindBy(css = "input[name=hasLegalIssues][value=false]") WebElement hasLegalIssuesNo;
  @FindBy(css = ".wrapper-right-button input") WebElement save;

  public LegalIssuesPage(WebDriver driver) {
    super(driver);
  }

  public void theUsualBlahBlah() {
    encryptionHasChangedNo.click();
    hasLegalIssuesNo.click();
  }

  public AutoReleasePage clickSave() {
    save.click();
    return new AutoReleasePage(driver);
  }
}
