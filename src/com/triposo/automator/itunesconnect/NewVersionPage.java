package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class NewVersionPage extends Page {
  @FindBy(css = ".metadataFieldReadonly input")
  WebElement version;
  @FindBy(css = "#whatsNewinthisVersionUpdateContainerId textarea") WebElement whatsnew;
  @FindBy(css = ".wrapper-right-button input") WebElement save;

  public NewVersionPage(WebDriver driver) {
    super(driver);
  }

  public void setVersionNumber(String version) {
    this.version.clear();
    this.version.sendKeys(version);
  }

  public void setWhatsnew(String whatsnew) {
    this.whatsnew.clear();
    this.whatsnew.sendKeys(whatsnew);
  }

  public void clickSave() {
    save.click();
  }
}
