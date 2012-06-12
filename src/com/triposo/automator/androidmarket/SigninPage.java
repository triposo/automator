package com.triposo.automator.androidmarket;

import com.triposo.automator.Page;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SigninPage extends Page {
  @FindBy(id = "Email")
  WebElement username;
  @FindBy(id = "Passwd")
  WebElement password;
  @FindBy(id = "signIn")
  WebElement signIn;

  public SigninPage(WebDriver driver) {
    super(driver);
  }

  public void signin(String username, String password) {
    this.username.clear();
    this.username.sendKeys(username);
    this.password.clear();
    this.password.sendKeys(password);
    signIn.click();
  }
}
