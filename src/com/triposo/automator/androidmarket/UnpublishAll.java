package com.triposo.automator.androidmarket;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnpublishAll extends MarketTask {

  public static void main(String[] args) throws Exception {
    new UnpublishAll().run();
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    gotoHome();

    Map guides = getGuides();
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      if (location.equals("world")) {
        continue;
      }
      System.out.println("Processing " + location);
      try {
        unpublishAll(location);
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error processing, skipping: " + location);
      }
      System.out.println("Done " + location);
    }

    System.out.println("All done.");
  }

  private void unpublishAll(String location) throws AppMissingException {
    AppEditorPage appEditorPage = gotoAppEditorForLocation(location);
    appEditorPage.clickUnpublish();
  }
}
