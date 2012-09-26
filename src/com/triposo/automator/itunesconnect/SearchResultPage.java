package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Iterator;
import java.util.List;

class SearchResultPage extends Page {
  @FindBy(css = ".resultList TABLE")
  private WebElement appsTable;
  @FindBy(css = ".next A")
  private WebElement next;

  public SearchResultPage(WebDriver driver) {
    super(driver);
  }

  public AppSummaryPage clickFirstResult() {
    final List<WebElement> elements = driver.findElements(By.cssSelector("div.software-column-type-col-0 a"));
    elements.get(0).click();
    return new AppSummaryPage(driver);
  }

  public void printStats() {
    printStats(true);
  }

  public void printStats(boolean printHeader) {
    List<WebElement> rows = appsTable.findElements(By.tagName("TR"));
    Iterator<WebElement> rowIterator = rows.iterator();
    WebElement header = rowIterator.next();
    if (printHeader) {
      Iterator<WebElement> cellIterator = header.findElements(By.tagName("TH")).iterator();
      while (cellIterator.hasNext()) {
        WebElement th = cellIterator.next();
        System.out.print(th.getText());
        if (cellIterator.hasNext()) {
          System.out.print(",");
        }
      }
      System.out.println();
    }
    while (rowIterator.hasNext()) {
      WebElement row = rowIterator.next();
      Iterator<WebElement> cellIterator = row.findElements(By.tagName("TD")).iterator();
      while (cellIterator.hasNext()) {
        WebElement td = cellIterator.next();
        String text = td.getText();
        // What's on the first line is related to the new version,
        // we're not (in this case) interested in the previous version.
        String newVersionInfo = text.split("\n")[0];
        System.out.print(newVersionInfo);
        if (cellIterator.hasNext()) {
          System.out.print(",");
        }
      }
      System.out.println();
    }
    try {
      SearchResultPage page = clickNext();
      page.printStats(false);
    } catch (NoSuchElementException e) {
    }
  }

  private SearchResultPage clickNext() {
    next.click();
    return new SearchResultPage(driver);
  }
}
