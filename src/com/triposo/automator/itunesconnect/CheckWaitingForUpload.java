package com.triposo.automator.itunesconnect;

import com.triposo.automator.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class CheckWaitingForUpload extends ItunesConnectTask {

  public static void main(String[] args) throws Exception {
    new CheckWaitingForUpload().run();
  }

  public void doRun() throws Exception {
    Integer appleId = Integer.parseInt(getProperty("apple_id"));

    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("Waiting For Upload")) {
      System.out.println("YES");
    } else {
      System.out.println("NO");
    }
  }
}
