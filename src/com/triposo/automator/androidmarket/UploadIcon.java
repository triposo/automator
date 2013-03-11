package com.triposo.automator.androidmarket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadIcon extends MarketTask {
  public static void main(String[] args) throws Exception {
    new UploadIcon().run();
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    String version = getProperty("version");
    String iconPath = getProperty("iconPath");

    Map guides = getGuides();
    Set<String> upToDateGuides = getUpToDateGuides(version);
    System.out.println("Up to date apps: " + upToDateGuides);
    List<String> notYetLaunched = Lists.newArrayList();
    List<String> failed = Lists.newArrayList();
    for (Object o : guides.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      String location = (String) entry.getKey();
      if (!upToDateGuides.contains(location.toLowerCase(Locale.US))) {
        System.out.println("Skipping because app is not version " + version + ": " + location);
        continue;
      }
      System.out.println("Processing " + location);
      try {
        uploadIcon(location, iconPath);
        System.out.println("Done " + location);
      } catch (AppMissingException e) {
        notYetLaunched.add(location);
        System.out.println("Skipping because app not yet launched: " + location);
      } catch (Exception e) {
        failed.add(location);
        e.printStackTrace();
        System.out.println("Skipping because operation failed: " + location);
      }
    }

    System.out.println("Not yet launched in Google Play: " + notYetLaunched);
    System.out.println("Failed: " + failed);
  }

  protected Set<String> getUpToDateGuides(String version) {
    Set<String> locations = Sets.newHashSet();
    HomePage homePage = gotoHome();
    while (true) {
      locations.addAll(homePage.getAlreadyLaunched(version));
      if (!homePage.hasNext()) {
        break;
      }
      homePage = homePage.clickNext();
    }
    return locations;
  }

  private void uploadIcon(
      String location, String iconPath) throws Exception {
    AppEditorPage appEditorPage = gotoAppEditorForLocation(location);
    appEditorPage.uploadIcon(iconPath);
    appEditorPage.clickSave();
  }
}
