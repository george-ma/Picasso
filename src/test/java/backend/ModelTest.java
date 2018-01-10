package backend;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Don't change any of the test images in the test directory
 */
class ModelTest {

  private static final boolean ADD = true;
  private static final boolean REMOVE = false;

  /**
   * Strings/ArrayList for updateTag
   */
  private static final String mockTags = "tag1, tag2, tag3, tagToRemove";
  private static final String tagToRemove = "tagToRemove";

  private static final ArrayList<String> mockTagsList =
      new ArrayList<>(Arrays.asList("tag1", "tag2", "tag3"));

  private static final String mockTagsSpacedWeirdly = "tag1,    tag2, tag3   ,  tagToRemove  ";

  /**
   * File Path for getTags
   */
  private static final File getTagsPhoto = new File(
      "./testPictures/getTag @tag1 @tag2 @tag3.jpg");

  /**
   * File Paths for moveImage
   */
  private static final File imageLocation1 = new File(
      "./testPictures/testDirectory0/moveTag.jpg");
  private static final File imageMoveLocation1 = new File(
      "./testPictures/testDirectory1");

  private static final File imageLocation2 = new File(
      "./testPictures/testDirectory1/moveTag.jpg");
  private static final File imageMoveLocation2 = new File(
      "./testPictures/testDirectory0");

  /**
   * File Paths for updateRootName
   */
  private static final File newRootName = new File(
      "./testPictures/re named @tag1 @tag2 @tag3.jpg");

  /**
   * File Paths for saveImage
   */
  private static final File imageOldName = new File(
      "./testPictures/saveImage.jpg");
  private static final File imageNewName = new File(
      "./testPictures/saveImage @tag1 @tag2 @tag3 @tagToRemove.jpg");

  /**
   * File file paths for revertData
   */
  private static final String sampleTag1 =
      "rootName" + " \t "
          + "@tag1 @tag2 @tag3" + " \t ";
  private static final String sampleTag2 =
      "rootName" + " \t "
          + ""  + " \t "
      + " ext" + " \t ";
  private static final File imageBeforeRevert = new File(
      "./testPictures/revertImage.jpg");
  private static final File imageAfterRevert = new File(
      "./testPictures/revertImage @tag1 @tag2 @tag3.jpg");

  private static Connection connection = SqlCommands.TEST_CONNECTION;

  private Model model = new Model(connection);

  @BeforeAll
  static void startUp() {
    GenerateDatabase.generate();
    GenerateDatabase.generateTest();
  }

  @Test
  void updateTag() {

    // Add tags
    model.updateTag(mockTags, ADD);
    assertEquals(4, model.getUnsavedTagsArray().size());

    // Remove tags
    model.updateTag(tagToRemove, REMOVE);
    assertFalse(model.getUnsavedTagsArray().contains(tagToRemove));

    for (String tag : model.getUnsavedTagsArray()) {
      assertTrue(mockTagsList.contains(tag));
    }
  }

  @Test
  void updateTagSpacedWeirdly() {
    // Add tags
    model.updateTag(mockTagsSpacedWeirdly, ADD);
    assertEquals(4, model.getUnsavedTagsArray().size());

    // Remove tags
    model.updateTag(tagToRemove, REMOVE);
    assertFalse(model.getUnsavedTagsArray().contains(tagToRemove));

    for (String tag : model.getUnsavedTagsArray()) {
      assertTrue(mockTagsList.contains(tag));
    }
  }

  @Test
  void getTags() {
    for (String tag : model.getTags(getTagsPhoto)) {
      assertTrue(mockTags.contains(tag));
    }
  }

  @Test
  void moveImage() {
    model.moveImage(imageLocation1, imageMoveLocation1);
    assertTrue(imageLocation2.exists());
    model.moveImage(imageLocation2, imageMoveLocation2);
    assertTrue(imageLocation1.exists());
  }

  @Test
  void saveTags() {
    model.updateTag(mockTags, ADD);
    model.saveTags(imageOldName);
    assertTrue(imageNewName.exists());
    for (String tag : mockTags.split(", *")) {
      model.updateTag(tag, REMOVE);
    }
    model.saveTags(imageNewName);
    assertTrue(imageOldName.exists());
  }

  @Test
  void revertData() {
    model.revertData(imageBeforeRevert, sampleTag1);
    assertTrue(imageAfterRevert.exists());
    model.revertData(imageAfterRevert, sampleTag2);
    assertTrue(imageBeforeRevert.exists());
  }

  @Test
  void getFullTagList() {
    model.updateTag(mockTags, ADD);
    model.saveTags(imageOldName);
    assertTrue(imageNewName.exists());
    for (String tag : mockTags.split(", *")) {
      model.updateTag(tag, REMOVE);
    }
    model.saveTags(imageNewName);
    ArrayList<String> fullTagList = model.getFullTagList();
    fullTagList.remove(3);
    assertEquals(mockTagsList, fullTagList);
  }

  @Test
  void updateRootName() {
  model.addNewImages(new ArrayList<>(Collections.singleton(getTagsPhoto)));
  model.updateRootName(getTagsPhoto, "re named");
  assertTrue(newRootName.exists());
  model.updateRootName(newRootName, "getTag");
  assertTrue(getTagsPhoto.exists());
  }

  @Test
  void getFilesFromDir() {
    ArrayList<File> filesList = new ArrayList<>();
    filesList.add(Paths.get(".", "testPictures", "testDirectory1", "campfire.jpg").toFile());
    filesList.add(Paths.get(".", "testPictures", "testDirectory1", "lonelystreet.jpg").toFile());

    assertEquals(filesList, model.getImageFilesFromDir(new File("./testPictures/testDirectory1")));
  }
}