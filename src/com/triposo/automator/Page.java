package com.triposo.automator;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.concurrent.TimeUnit;

public abstract class Page {
  protected final WebDriver driver;

  public Page(WebDriver driver) {
    this.driver = driver;
    PageFactory.initElements(driver, this);
  }

  public boolean containsText(String text) {
    return driver.findElement(By.cssSelector("body")).getText().contains(text);
  }

  protected <T> FluentWait<T> wait(T input) {
    return new FluentWait<T>(input).withTimeout(10, TimeUnit.SECONDS);
  }

  protected Predicate<WebElement> isDisplayed() {
    return new Predicate<WebElement>() {
      @Override
      public boolean apply(WebElement webElement) {
        return webElement.isDisplayed();
      }
    };
  }

  protected void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
  }
}
