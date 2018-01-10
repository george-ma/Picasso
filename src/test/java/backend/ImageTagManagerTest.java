package backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ImageTagManagerTest {

  private static final File photo1WithTags = new File(
      "./testPictures/p1 @tag1 @tag2 @tag3.jpg");

  private static final File photo1WithoutTags =
      Paths.get(".", "testPictures", "p1.jpg").toFile();

  private static final String photo1RootName = "p1";

  private static final String photo1Tags = "@tag1 @tag2 @tag3";

  /**
   * generateArchive test files
   */
  private static File archive =
      Paths.get(".", "testPictures", "archive.jpg").toFile();
  private static File archiveWithTags =
      Paths.get(".", "testPictures", "archive @tag1 @tag2 @tag3.jpg").toFile();

  /**
   * generateAllArchives test files
   */
  private static File file1 =
      Paths.get(".", "testPictures", "testPicture1.jpg").toFile();
  private static File file1Changed =
      Paths.get(".", "testPictures", "testPicture1 @tag1 @tag2 @tag3.jpg").toFile();
  private static File file2 =
      Paths.get(".", "testPictures", "testPicture2.jpg").toFile();
  private static File file3 =
      Paths.get(".", "testPictures", "testPicture3.jpg").toFile();
  private static String archivesTestDirectory =
      Paths.get(".", "testPictures").toString();

  /**
   * updateImageTag test files
   */

  // Change Directory
  private static File updateImageTagName =
      Paths.get(
          ".",
          "testPictures",
          "testDirectory",
          "updateImageTagName.jpg").toFile();

  private static File updateImageTagNameNewTag =
      Paths.get(
          ".",
          "testPictures",
          "testDirectory",
          "updateImageTagName @tag1 @tag2 @tag3.jpg").toFile();

  /**
   * sliceFile test files
   */
  private static File sliceFileName =
      Paths.get(
          ".",
          "testPictures",
          "testDirectory",
          "sliceFileName @tag1 @tag2 @tag3.jpg").toFile();
  private static String sliceFileNameDirectory =
      Paths.get(".", "testPictures", "testDirectory").toString();

  private static File sliceFileNameNoTags =
      Paths.get(".", "testPictures", "testDirectory", "sliceFileName.jpg").toFile();

  /**
   * Change File Paths for moveImage
   */
  private static File imageLocation1 = new File(
      "./testPictures/testDirectory0/moveTag @tag1 @tag2 @tag3.jpg");

  private static File imageLocation2 = new File(
      "./testPictures/testDirectory1/moveTag @tag1 @tag2 @tag3.jpg");

  /**
   * Change Root Names for moveImage
   */
  private static File imageNameOne =
      Paths.get(
          ".",
          "testPictures",
          "testDirectory",
          "nameOne @tag1 @tag2 @tag3.jpg").toFile();

  private static File imageNameTwo =
      Paths.get(
          ".",
          "testPictures",
          "testDirectory",
          "nameTwo @tag1 @tag2 @tag3.jpg").toFile();

  /**
   * isValidRootName test files
   */
  private static File validRootName =
      Paths.get(".", "testPictures", "testDirectory", "validRootName.jpg").toFile();

  /**
   * searchByTagsWhereFilesExist testFiles
   */
  private static final File photo1 = new File(
      "./testPictures/photo1 @test1 @test2 @test3.jpg");
  private static final File photo2 = new File(
      "./testPictures/photo2 @test1 @test2 @test3.jpg");
  private static final File photo3 = new File(
      "./testPictures/photo3 @test1 @test2 @test3.jpg");

  private static final File photo3WithTags = new File(
      "./testPictures/p3 @tag7 @tag2 @tag6 @tag1.jpg");

  private static final ArrayList<String> mockTagsList2 =
      new ArrayList<>(Arrays.asList("tag102", "tag100"));

  private static final File photo2WithTags = new File(
      "./testPictures/p2 @tag4 @tag3.jpg");

  private static String tagsAsString = "@tag1 @tag2 @tag3";

  private ImageTagManager imageTagController = new ImageTagManager(
      SqlCommands.TEST_CONNECTION);

  private static Logger LOGGER = Logger.getLogger(ImageTagManagerTest.class.getName());

  private static String TAB = " \t ";




  @BeforeAll
  static void initializeDB() {
    GenerateDatabase.generate();
    GenerateDatabase.generateTest();
  }

  @Test
  void generateArchive() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(archive)));
      imageTagController.updateImageTag(archiveWithTags);
      ArrayList<String> data = imageTagController.generateArchive(archiveWithTags);
      String[] strArrayData1 = data.get(0).split(TAB);
      assertEquals("archive", strArrayData1[0]);
      assertEquals(tagsAsString, strArrayData1[1]);
      String[] strArrayData2 = data.get(1).split(TAB);
      assertEquals("archive", strArrayData2[0]);
      assertEquals("", strArrayData2[1]);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void aGenerateAllArchive() {
    ArrayList<File> images = new ArrayList<>(Arrays.asList(file1, file2, file3));

    try {
      SqlCommands.TEST_CONNECTION.createStatement().execute("delete from ImageTags");
      imageTagController.addNewImages(images);
      imageTagController.updateImageTag(file1Changed);
      ArrayList<ArrayList<String>> archive = imageTagController.generateAllArchive();
      // row1
      ArrayList<String> strArrayData1 = archive.get(0);
      assertEquals(archivesTestDirectory, strArrayData1.get(0));
      assertEquals("", strArrayData1.get(1));
      assertEquals("testPicture1 ", strArrayData1.get(2));
      // row 2
      ArrayList<String> strArrayData2 = archive.get(1);
      assertEquals(archivesTestDirectory, strArrayData2.get(0));
      assertEquals("testPicture1 ", strArrayData2.get(1));
      assertEquals("testPicture1" + " " + tagsAsString, strArrayData2.get(2));
      //row 3
      ArrayList<String> strArrayData3 = archive.get(2);
      assertEquals(archivesTestDirectory, strArrayData3.get(0));
      assertEquals("", strArrayData3.get(1));
      assertEquals("testPicture2 ", strArrayData3.get(2));
      //row 3
      ArrayList<String> strArrayData4 = archive.get(3);
      assertEquals(archivesTestDirectory, strArrayData4.get(0));
      assertEquals("", strArrayData4.get(1));
      assertEquals("testPicture3 ", strArrayData4.get(2));

    }catch (SQLException e){
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }



  @Test
  void sliceFile() {
    ArrayList<String> slicedFile = imageTagController.sliceFile(sliceFileName);
    assertEquals(sliceFileNameDirectory, slicedFile.get(0));
    String sliceFileNameRoot = "sliceFileName";
    assertEquals(sliceFileNameRoot, slicedFile.get(1));
    assertEquals(tagsAsString, slicedFile.get(2));
    String extAsString = ".jpg";
    assertEquals(extAsString, slicedFile.get(3));
  }

  @Test
  void sliceFileNoTags() {
    ArrayList<String> slicedFile = imageTagController.sliceFile(sliceFileNameNoTags);
    assertEquals(sliceFileNameDirectory, slicedFile.get(0));
    String sliceFileNameRoot = "sliceFileName";
    assertEquals(sliceFileNameRoot, slicedFile.get(1));
    assertEquals("", slicedFile.get(2));
    String extAsString = ".jpg";
    assertEquals(extAsString, slicedFile.get(3));
  }

  @Test
  void updateImageTag() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(updateImageTagName)));
      imageTagController.updateImageTag(updateImageTagNameNewTag);
      ArrayList<String> data = imageTagController.generateArchive(updateImageTagNameNewTag);
      String[] strArrayData1 = data.get(0).split(TAB);
      String updateImageTagNameRoot = "updateImageTagName";
      assertEquals(updateImageTagNameRoot, strArrayData1[0]);
      assertEquals(tagsAsString, strArrayData1[1]);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void updateImagePath() {
    try {
      // Change Directory
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(imageLocation1)));
      imageTagController.updateImagePath(imageLocation1, imageLocation2, false);
      String[] strArrayData1 = imageTagController.generateArchive(imageLocation2).get(0).split(TAB);
      assertEquals("moveTag", strArrayData1[0]);
      assertEquals(tagsAsString, strArrayData1[1]);
      imageTagController.updateImagePath(imageLocation2, imageLocation1, false);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void updateRootName() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(imageNameOne)));
      imageTagController.updateImagePath(imageNameOne, imageNameTwo, true);
      String[] strArrayData1 = imageTagController.generateArchive(imageNameTwo).get(0).split(TAB);
      assertEquals("nameTwo", strArrayData1[0]);
      assertEquals(tagsAsString, strArrayData1[1]);
      imageTagController.updateImagePath(imageNameTwo, imageNameOne, true);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void addNewImageWithTags() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(photo1WithTags)));
      ArrayList<String> data = imageTagController.generateArchive(photo1WithTags);
      String[] strArrayData1 = data.get(0).split(TAB);
      assertEquals(photo1RootName, strArrayData1[0]);
      assertEquals(photo1Tags, strArrayData1[1]);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void addNewImageWithImageAlreadyInDB() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(photo1WithoutTags)));
      imageTagController.addNewImages(new ArrayList<>(Collections.singleton(photo1WithTags)));
      ArrayList<String> data = imageTagController.generateArchive(photo1WithTags);
      String[] strArrayData1 = data.get(0).split(TAB);
      assertEquals(photo1RootName, strArrayData1[0]);
      assertNotEquals("", strArrayData1[1]);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }


  @Test
  void isValidRootName() throws SQLException {
    try {
      assertTrue(imageTagController.isValidRootName(validRootName));
      LOGGER.log(Level.FINE, "the given image is not in the DB:{0}");
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void searchByTagsWhereFilesExist() {
    try {
      imageTagController.addNewImages(new ArrayList<>(Arrays.asList(photo1, photo2, photo3)));
      ArrayList<File> Files = imageTagController.searchByTags(
          new ArrayList<>(Arrays.asList("test1", "test2")));

      assertEquals(photo1, Files.get(0));
      assertEquals(photo2, Files.get(1));
      assertEquals(photo3, Files.get(2));

    }catch (SQLException e){
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

  @Test
  void searchByTagsWhereFilesDoNotExist() {
    ArrayList<File> images = new ArrayList<>();
    images.add(photo1WithoutTags);
    images.add(photo2WithTags);
    images.add(photo3WithTags);

    try {
      imageTagController.addNewImages(images);
      imageTagController.updateImageTag(photo1WithTags);
      ArrayList<File> Files = imageTagController.searchByTags(mockTagsList2);

      assertEquals(0, Files.size());

    }catch (SQLException e){
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }

}