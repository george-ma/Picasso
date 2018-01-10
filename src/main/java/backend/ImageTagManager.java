package backend;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages all information associated with an individual image. eg. Adding tags, removing tags,
 * moving the image to a new directory and generating archives used for the complete logs.
 */
class ImageTagManager {

  /**
   * Index for file names spliced by method spliceFile().
   */
  static final int FILE_SPLICE_PARENT_DIR = 0;
  static final int FILE_SPLICE_ROOT_NAME = 1;
  static final int FILE_SPLICE_TAGS = 2;
  static final int FILE_SPLICE_EXT = 3;
  /**
   * Column names for ImageTagTable.
   */
  private static final String DIRECTORY = "directory";
  private static final String ROOT_NAME = "rootName";
  private static final String TAGS = "tags";
  private static final String EXT = "ext";
  private static final String CREATED_ON = "createdOn";
  private static final String AT = " @";
  private static final char PERIOD = '.';
  private static final String TAB = " \t ";

  /**
   * SglCommands object to allow for grabbing data from the ImageTags Table in the Database.
   */
  private SqlCommandsImageTags sqlCommandsImageTags;

  /**
   * The constructor of ImageTagManager.
   *
   * @param connection connection to the database
   */
  ImageTagManager(Connection connection) {
    this.sqlCommandsImageTags = new SqlCommandsImageTags(connection);
  }

  /**
   * Gets a list of archives for the given image.
   *
   * @param file The file file for the given image.
   * @return an ArrayList of all the previous version of the given Image
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ArrayList<String> generateArchive(File file) throws SQLException {
    ArrayList<String> archive = new ArrayList<>();

    ArrayList<String> slicedFileData = sliceFile(file);
    ResultSet resultSet = sqlCommandsImageTags.selectByPath(
        slicedFileData.get(FILE_SPLICE_PARENT_DIR),
        slicedFileData.get(FILE_SPLICE_ROOT_NAME),
        slicedFileData.get(FILE_SPLICE_EXT));

    while (resultSet.next()) {
      archive.add(resultSet.getString(ROOT_NAME) + TAB
          + resultSet.getString(TAGS) + TAB
          + resultSet.getTimestamp(CREATED_ON));
    }
    return archive;
  }

  /**
   * Get an ArrayList of all the previous and current imageTag states.
   *
   * @return an ArrayList of all the previous and current imageTag states.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ArrayList<ArrayList<String>> generateAllArchive() throws SQLException {
    ArrayList<ArrayList<String>> archive = new ArrayList<>();
    ResultSet resultSet = sqlCommandsImageTags.selectAll();
    String previousDir = "";
    String previousRootName = "";
    String previousTags = "";
    while (resultSet.next()) {
      ArrayList<String> log = new ArrayList<>();
      String dir = resultSet.getString(DIRECTORY);
      String rootName = resultSet.getString(ROOT_NAME);
      String tags = resultSet.getString(TAGS);
      String timeStamp = resultSet.getTimestamp(CREATED_ON).toString();
      if (!previousDir.equals(dir) || !previousRootName.equals(rootName)) {
        log.add(dir);
        log.add("");
        log.add(rootName + " " + tags);
        log.add(timeStamp);
        previousDir = dir;
        previousRootName = rootName;
        previousTags = tags;
      } else {
        log.add(dir);
        log.add(rootName + " " + previousTags);
        log.add(rootName + " " + tags);
        log.add(timeStamp);
        previousDir = dir;
        previousRootName = rootName;
        previousTags = tags;
      }
      archive.add(log);
    }
    return archive;
  }

  /**
   * Takes the file file for a given image and splits it up into 3 part the parent directory of the
   * image, the rootName of the image and the tags.
   *
   * @param file file file for the given image
   * @return parent directory (stored at index 0 of the returned array), the rootName of the image
   * (stored at index 1 of the returned array) and the tags (stored at index 2 of the returned
   * array).
   */
  ArrayList<String> sliceFile(File file) {
    ArrayList<String> slicedFileData = new ArrayList<>();

    // Parent directory
    slicedFileData.add(file.getParent());

    String name = file.getName();
    int indexOfAtSign = name.indexOf(AT);
    int indexOfExt = name.lastIndexOf(PERIOD);

    if (indexOfAtSign == -1) {

      // RootName
      slicedFileData.add(name.substring(0, indexOfExt));

      // Tags
      slicedFileData.add("");

      // Extension
      slicedFileData.add(name.substring(indexOfExt));
    } else {
      // RootName
      slicedFileData.add(name.substring(0, indexOfAtSign));

      // Tags
      slicedFileData.add(name.substring(indexOfAtSign + 1, indexOfExt));

      // Extension
      slicedFileData.add(name.substring(indexOfExt));
    }
    return slicedFileData;
  }

  /**
   * Updates the imageTag data for a given file.
   *
   * @param file The file path for the given file to update.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateImageTag(File file) throws SQLException {

    // Splice file file path
    ArrayList<String> slicedFileData = this.sliceFile(file);

    // Get id for current file tag
    int id = sqlCommandsImageTags.selectCurrent(
        slicedFileData.get(FILE_SPLICE_PARENT_DIR),
        slicedFileData.get(FILE_SPLICE_ROOT_NAME),
        slicedFileData.get(FILE_SPLICE_EXT));

    // Set current file tag to be false
    sqlCommandsImageTags.updateCurrentToFalse(id);

    // Add updated file tag to database
    sqlCommandsImageTags.insert(
        slicedFileData.get(FILE_SPLICE_PARENT_DIR),
        slicedFileData.get(FILE_SPLICE_ROOT_NAME),
        slicedFileData.get(FILE_SPLICE_TAGS),
        slicedFileData.get(FILE_SPLICE_EXT)
    );
  }

  /**
   * Updates an old images tags for a given image, based on if the user moves an image to another
   * directory or if a user changes the images root name excluding the tags.
   *
   * @param oldFileName is the original file name
   * @param newFile is the new file name
   * @param rootNameOrDir tells if the file changed locations or if the root name changed
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateImagePath(File oldFileName, File newFile, boolean rootNameOrDir) throws SQLException {
    ArrayList<String> slicedOldFileData = sliceFile(oldFileName);
    ArrayList<String> slicedFileData = sliceFile(newFile);
    if (rootNameOrDir) {
      sqlCommandsImageTags.updateRootName(
          slicedFileData.get(FILE_SPLICE_ROOT_NAME),
          slicedOldFileData.get(FILE_SPLICE_ROOT_NAME),
          slicedOldFileData.get(FILE_SPLICE_PARENT_DIR),
          slicedFileData.get(FILE_SPLICE_EXT));
    } else {
      sqlCommandsImageTags.updateDir(
          slicedFileData.get(FILE_SPLICE_PARENT_DIR),
          slicedOldFileData.get(FILE_SPLICE_ROOT_NAME),
          slicedOldFileData.get(FILE_SPLICE_PARENT_DIR),
          slicedFileData.get(FILE_SPLICE_EXT));
    }
  }

  /**
   * Adds image files and their image tags to the database, for those image files that don’t have
   * tags already in the database.
   *
   * @param files the files names of all the images that might need to be added in the database
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void addNewImages(ArrayList<File> files) throws SQLException {
    for (File image : files) {

      // Splice image file path
      ArrayList<String> slicedFileData = this.sliceFile(image);

      if (isValidRootName(image)) {
        // Add new image tag to database
        sqlCommandsImageTags.insert(
            slicedFileData.get(FILE_SPLICE_PARENT_DIR),
            slicedFileData.get(FILE_SPLICE_ROOT_NAME),
            slicedFileData.get(FILE_SPLICE_TAGS),
            slicedFileData.get(FILE_SPLICE_EXT)
        );
      }
    }
  }

  /**
   * Check if a files rootName already exists a certain directory by checking the database history.
   *
   * @param file the file name of the image we want to check against the database
   * @return true if the rootName of this given file is acceptable
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  boolean isValidRootName(File file) throws SQLException {

    // Splice file file path
    ArrayList<String> slicedFileData = this.sliceFile(file);

    // Get id for current file tag
    int id = sqlCommandsImageTags.selectCurrent(
        slicedFileData.get(FILE_SPLICE_PARENT_DIR),
        slicedFileData.get(FILE_SPLICE_ROOT_NAME),
        slicedFileData.get(FILE_SPLICE_EXT));

    // if -1 then valid
    return id == -1;

  }

  /**
   * Searches for all the images that have been logged in the database and return then ones that
   * have at least one of the tags we are searching by.
   *
   * @param tags the list of tags we want to search for the images by.
   * @return a list of all the files that have the at least one of the tags we searched by.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ArrayList<File> searchByTags(ArrayList<String> tags) throws SQLException {
    ResultSet imageTags = sqlCommandsImageTags.selectByTags(tags);
    ArrayList<File> imageFiles = new ArrayList<>();
    while (imageTags.next()) {
      imageFiles.add(new File(new File(imageTags.getString(DIRECTORY)),
          imageTags.getString(ROOT_NAME)
              + " "
              + imageTags.getString(TAGS)
              + imageTags.getString(EXT)));
    }
    return imageFiles;
  }
}