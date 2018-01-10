package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sqlite.util.StringUtils;

/**
 * Model that sends info from backend to frontend classes and vice versa.
 */
public class Model {

  /**
   * Regular expressions used for splitting strings.
   */
  private static final String SPLIT_BY_AT_SIGN = " +@";
  private static final String SPLIT_BY_COMMA = ", *";
  private static final String EMPTY_STRING = "";
  private static final String TAB = " \t ";
  private static final String SPACE = " ";
  private static final String SLASH = "/";
  private static final String SPACE_AT = " @";

  /**
   * Backend Manager for ImageTags
   */
  private ImageTagManager imageTagManger;

  /**
   * Backend Manger for Tags
   */
  private TagsManager tagsManger;

  /**
   * List of tags of the selected image
   */
  private ArrayList<String> tags = new ArrayList<>();

  /**
   * Constructor for testing, allows connection to test or working database.
   *
   * @param connection - Can be connection for test and working database
   */
  Model(Connection connection) {
    this.imageTagManger = new ImageTagManager(connection);
    this.tagsManger = new TagsManager(connection);
  }

  /**
   * The constructor of Model
   */
  public Model() {
    this.imageTagManger = new ImageTagManager(SqlCommands.CONNECTION);
    this.tagsManger = new TagsManager(SqlCommands.CONNECTION);
  }

  /**
   * User can update the list of tags by adding/removing one. Duplicate tag or tags containing only
   * spaces will not be added. User can add multiple tags by separating tags by comma.
   *
   * @param inputTag The name of the tag to be updated.
   * @param toAdd Boolean that checks whether to add a tag or remove it.
   * @return Return whether the tag was updated or not.
   */
  public boolean updateTag(String inputTag, Boolean toAdd) {
    if (inputTag == null) {
      return false;
    }
    if (toAdd) {
      int i = tags.size();
      for (String tag : inputTag.split(SPLIT_BY_COMMA)) {
        if (!tag.equals(EMPTY_STRING) && !tags.contains(tag.trim()) && !tag.trim().equals("")) {
          tags.add(tag.trim());
        }
      }
      return i != tags.size();
    } else if (tags.size() > 0) {
      tags.remove(inputTag);
      return true;
    }
    return false;
  }

  /**
   * Get information about the image as a primitive type array.
   *
   * @param file File of the selected image.
   * @return String[] of first element being directory name, second being root name, third being a
   * string of tags, and fourth being the file extension.
   */
  public String[] getImageInfo(File file) {
    return imageTagManger.sliceFile(file).toArray(new String[4]);
  }

  /**
   * Get all the tags of an image as an array list.
   *
   * @param file File of the selected image.
   * @return An ArrayList of tags of the selected image.
   */
  public ArrayList<String> getTags(File file) {
    String allTags = imageTagManger.sliceFile(file).get(ImageTagManager.FILE_SPLICE_TAGS);
    ArrayList<String> listOfTags = new ArrayList<>();
    tags.clear();
    if (!allTags.equals(EMPTY_STRING)) {
      listOfTags = new ArrayList<>(Arrays.asList(allTags.substring(1).split(SPLIT_BY_AT_SIGN)));
      tags.addAll(listOfTags);
    }
    return listOfTags;
  }

  /**
   * Return an archive logs of the selected image as an array list.
   *
   * @param file File of the selected image.
   * @return Array list of archive logs.
   */
  public ArrayList<String> getArchives(File file) {
    try {
      return imageTagManger.generateArchive(file);
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Change the directory of the selected image and store this new info in the database.
   *
   * @param file File of the selected image.
   * @param targetFolder Folder that the image will move to.
   * @return boolean Return whether the file was renamed or not.
   */
  public boolean moveImage(File file, File targetFolder) {
    ArrayList<String> imageInfo = imageTagManger.sliceFile(file);
    if (targetFolder != null) {
      String destinationPath = targetFolder.getAbsolutePath()
          + SLASH
          + imageInfo.get(ImageTagManager.FILE_SPLICE_ROOT_NAME)
          + (imageInfo.get(ImageTagManager.FILE_SPLICE_TAGS).length() > 0 ? SPACE : EMPTY_STRING)
          + imageInfo.get(ImageTagManager.FILE_SPLICE_TAGS)
          + imageInfo.get(ImageTagManager.FILE_SPLICE_EXT);
      File destinationFile = new File(destinationPath);
      try {
        imageTagManger.addNewImages(getImageFilesFromDir(targetFolder));
        if (imageTagManger.isValidRootName(destinationFile)) {
          imageTagManger.updateImagePath(file,
              Files.move(file.toPath(), destinationFile.toPath()).toFile(), false);
          return !file.getAbsolutePath().equals(destinationPath);
        }
      } catch (SQLException | IOException e) {
        return false;
      }
    }
    return false;
  }

  /**
   * Save tag data of the current image.
   *
   * @param file File of the selected image.
   * @return boolean Return whether the change was saved or not.
   */
  public boolean saveTags(File file) {

    ArrayList<String> imageInfo = imageTagManger.sliceFile(file);
    File renamedFile = new File(
        imageInfo.get(ImageTagManager.FILE_SPLICE_PARENT_DIR)
            + SLASH
            + imageInfo.get(ImageTagManager.FILE_SPLICE_ROOT_NAME)
            + (tags.size() > 0 ? SPACE_AT : EMPTY_STRING)
            + StringUtils.join(tags, SPACE_AT)
            + imageInfo.get(ImageTagManager.FILE_SPLICE_EXT));

    try {
      if (file.renameTo(renamedFile)) {
        imageTagManger.updateImageTag(renamedFile);
        tagsManger.addTags(tags);
        return true;
      }
    } catch (SQLException e) {
      return false;
    }
    return false;
  }

  /**
   * Revert the data of the current image back to the log: archivePath.
   *
   * @param file File of the selected image.
   * @param selectedLog Selected archive log.
   * @return Return whether the data was reverted.
   */
  public boolean revertData(File file, String selectedLog) {
    ArrayList<String> imageInfo = imageTagManger.sliceFile(file);
    String[] strArray = selectedLog.split(TAB);
    File revertedFile = new File(
        imageInfo.get(ImageTagManager.FILE_SPLICE_PARENT_DIR)
            + SLASH
            + imageInfo.get(ImageTagManager.FILE_SPLICE_ROOT_NAME)
            + (strArray[1].length() > 0 ? SPACE : EMPTY_STRING)
            + strArray[1]
            + imageInfo.get(ImageTagManager.FILE_SPLICE_EXT));

    try {
      if (file.renameTo(revertedFile)) {
        imageTagManger.updateImageTag(revertedFile);
        return true;
      }
    } catch (SQLException e) {
      return false;
    }
    return false;
  }

  /**
   * Get unsaved tags as an array list.
   *
   * @return An ArrayList of unsaved tags.
   */
  ArrayList<String> getUnsavedTagsArray() {
    return tags;
  }


  /**
   * Add images and their image tags to the database, for those images that don’t have tags already
   * in the database.
   *
   * @param paths Paths of the files.
   */
  public void addNewImages(ArrayList<File> paths) {
    try {
      imageTagManger.addNewImages(paths);
    } catch (SQLException ignored) {
    }

  }

  /**
   * Return full list of tags as an array list.
   *
   * @return ArrayList the full array list of tags.
   */
  public ArrayList<String> getFullTagList() {
    try {
      return tagsManger.getAllTags();
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Update the root name of the image.
   *
   * @param file File that is selected.
   * @param newRootName new Root name of the selected file.
   * @return Return whether the root name was updated or not.
   */
  public boolean updateRootName(File file, String newRootName) {
    ArrayList<String> imageInfo = imageTagManger.sliceFile(file);
    File renamedFile = new File(
        imageInfo.get(ImageTagManager.FILE_SPLICE_PARENT_DIR)
            + SLASH
            + newRootName
            + (imageInfo.get(
            ImageTagManager.FILE_SPLICE_TAGS).length() > 0 ? SPACE : EMPTY_STRING)
            + imageInfo.get(ImageTagManager.FILE_SPLICE_TAGS)
            + imageInfo.get(ImageTagManager.FILE_SPLICE_EXT));

    try {
      Pattern p = Pattern.compile(" *@");
      Matcher m = p.matcher(newRootName);
      if (imageTagManger.isValidRootName(renamedFile) && !m.find()) {
        if (file.renameTo(renamedFile)) {
          imageTagManger.updateImagePath(file, renamedFile, true);
          return true;
        }
      }
    } catch (SQLException e) {
      return false;
    }
    return false;
  }

  /**
   * Return an array list of image files of the passed in directory.
   *
   * @param folder Directory that is selected.
   * @return ArrayList Array list of the files under the directory.
   */
  ArrayList<File> getImageFilesFromDir(File folder) {
    ArrayList<File> listImages = new ArrayList<>();
    File[] filesUnderFolder = folder.listFiles();
    if (filesUnderFolder != null) {
      for (File file : filesUnderFolder) {
        if (file.getName().contains(".jpg") || file.getName().contains(".png")
            || file.getName().contains(".gif")) {
          listImages.add(file);
        }
      }
    }
    return listImages;
  }

  /**
   * Return all the image files of the given directory and under all the sub directories.
   *
   * @param folder Directory that is selected.
   * @return ArrayList Array list of the files under the directory and all sub directories.
   */
  public ArrayList<File> getAllImageFiles(File folder) {
    if (folder != null && folder.isDirectory()) {
      ArrayList<File> selectedImages = getImageFilesFromDir(folder);
      ArrayList<File> listImages = new ArrayList<>();
      ArrayList<File> subFolders = getFoldersFromDir(folder);
      if (subFolders != null) {
        for (File file : subFolders) {
          if (file != null) {
            listImages.addAll(getAllImageFiles(file));
          }
        }
        listImages.addAll(selectedImages);
        return listImages;
      }
    }
    return null;

  }

  /**
   * Helper function that return folders of the passed in directory.
   *
   * @param folder Directory that is selected
   * @return ArrayList Array list of the folders under the directory
   */
  private ArrayList<File> getFoldersFromDir(File folder) {
    ArrayList<File> listFolders = new ArrayList<>();
    File[] filesUnderFolder = folder.listFiles();
    if (filesUnderFolder != null) {
      for (File file : filesUnderFolder) {
        if (file.isDirectory()) {
          listFolders.add(file);
        }
      }
    }
    return listFolders;
  }

  /**
   * Return an array list consisting of all changes ever done to images within the database.
   *
   * @return array list of all archives.
   */
  public ArrayList<ArrayList<String>> getFullArchives() {
    try {
      return imageTagManger.generateAllArchive();
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Return an array list of all images in the database that contain any of the user entered tags.
   *
   * @param tags String of the tags that are searched.
   * @return ArrayList Array list of all selected files.
   */
  public ArrayList<File> searchByTags(String tags) {
    try {
      return imageTagManger.searchByTags(new ArrayList<>(Arrays.asList(tags.split(" *, *"))));
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Add tag(s) to the full list of tags.
   *
   * @param tags Tags that are to be added to the full list of tags.
   * @return boolean Return whether the change was saved or not.
   */
  public boolean addTags(String tags) {
    try {
      tagsManger.addTags(new ArrayList<>(Arrays.asList(tags.split(" *, *"))));
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * Takes a list of files and adds their tags to the list of tags.
   *
   * @param files files that we want to take tags from and add to our data base.
   */
  private void addTagsFromFiles(ArrayList<File> files) {
    for (File file : files) {
      String tags = imageTagManger.sliceFile(file).get(2);
      tags = tags.replaceAll(" @", ", ");
      tags = tags.replaceAll("@", "");
      if (!tags.equals("")) {
        addTags(tags);
      }
    }
  }

  /**
   * When the UI is repopulated this method is called to index all the images in our the current
   * directory we are in on the UI and also index all the Tags that any fo the images might have.
   *
   * @param files list of files that are in the current directory we are in on the UI
   */
  public void initialPopulationOfDatabase(ArrayList<File> files) {
    addNewImages(files);
    addTagsFromFiles(files);
  }

}

