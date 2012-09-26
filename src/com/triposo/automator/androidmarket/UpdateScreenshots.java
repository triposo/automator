package com.triposo.automator.androidmarket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateScreenshots extends LaunchNewVersion {

  private final ArrayList<String> locations;

  public static void main(String[] args) throws Exception {
    new UpdateScreenshots(args).run();
  }

  public UpdateScreenshots(String[] locations) {
    this.locations = Lists.newArrayList(locations);
  }

  @Override
  public void doRun() throws Exception {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);

    gotoHome();
    Set<String> upToDateGuides = getUpToDateGuides();
    System.out.println("Up to date apps: " + upToDateGuides);
    Map<String, File> guides = getGuidesWithScreenshots();
    if (locations.isEmpty()) {
      locations.addAll(guides.keySet());
    }
    Collections.sort(locations);
    for (String location : locations) {
      if (!locations.isEmpty() && !locations.contains(location)) {
        System.out.println("Skipping because not specified: " + location);
        continue;
      }
      File dir = guides.get(location);
      if (dir == null) {
        System.out.println("Skipping because app not yet launched: " + location);
        continue;
      }
      if (!upToDateGuides.contains(location)) {
        System.out.println("Skipping because app not up to date: " + location);
        continue;
      }
      List<File> images = getGuideScreenshots(dir);
      if (images.isEmpty()) {
        System.out.println("Skipping because no images found: " + location);
        continue;
      }
      System.out.println("Processing " + location);
      uploadScreenshots(location, images);
      System.out.println("Done " + location);
    }
  }

  private List<File> getGuideScreenshots(File dir) {
    List<File> images = Lists.newArrayList(dir.listFiles());
    Collections.sort(images);
    return images;
  }

  private Map<String, File> getGuidesWithScreenshots() {
    Map<String, File> screenshots = Maps.newHashMap();
    for (File guideImagesDir : new File("droidguide-screenshots").listFiles()) {
      if (!guideImagesDir.isDirectory()) {
        continue;
      }
      screenshots.put(guideImagesDir.getName().toLowerCase(Locale.US), guideImagesDir);
    }
    return screenshots;
  }

  private void uploadScreenshots(String location, List<File> images) throws AppMissingException {
    AppEditorPage appEditorPage = gotoAppEditorForLocation(location);
    appEditorPage.deleteScreenshots();
    appEditorPage.uploadScreenshots(images);
    appEditorPage.clickSave();
  }
}
