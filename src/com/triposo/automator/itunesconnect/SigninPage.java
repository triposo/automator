package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class SigninPage extends Page {
  @FindBy(name = "theAccountName")
  WebElement username;
  @FindBy(name = "theAccountPW") WebElement password;
  @FindBy(css = "input[alt|=\"Sign In\"]") WebElement signIn;

  public SigninPage(WebDriver driver) {
    super(driver);
  }

  public MainPage signin(String username, String password) {
    this.username.clear();
    this.username.sendKeys(username);
    this.password.clear();
    this.password.sendKeys(password);
    signIn.click();
    return new MainPage(driver);
  }

}
