package com.triposo.automator.androidmarket;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractStats extends MarketTask {

  public static void main(String[] args) throws Exception {
    new ExtractStats().run();
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    System.out.println("Guide,Total installs,Net installs");
    HomePage homePage = gotoHome();
    while (homePage.hasNext()) {
      homePage.printStats();
      homePage = homePage.clickNext();
    }
    homePage.printStats();
  }
}
