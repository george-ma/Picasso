package frontend;

import backend.Model;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.sqlite.util.StringUtils;

/**
 * Controller for the MenuGUI.fxml (View). Initializes the assets of the Menu and allows the user to
 * interact with the GUI.
 */
public class Controller {

  /**
   * Instance variables that allow the controller to interact with the FXML elements. Implementation
   * of observableArrayList is adapted from this tutorial: Updating Data on 20171112 here:
   * https://docs.oracle.com/javafx/2/fxml_get_started/fxml_tutorial_intermediate.htm
   */

  @FXML // Selected image
  private ImageView imgView;
  @FXML
  private Label lblRootName;
  @FXML
  private Label lblTags;
  @FXML
  private Label lblPath;
  @FXML // Drop down of tags that can be removed from the selected image
  private ChoiceBox<String> choiceRemovableTags;
  @FXML
  private TextField txtCurDirectory;
  @FXML // Text field of tags that can be used to search images (tags can be separated by commas)
  private TextField txtSearchTag;
  @FXML
  private TextField txtNewTagToImage;
  @FXML // Text field of root name to be changed
  private TextField txtRootName;
  @FXML
  private TextField txtNewTagToRepo;
  @FXML // This is displayed in the "Image Log" tab
  private ListView<String> listImageArchives;
  @FXML // The list view displayed on the right side of the GUI indicated by "Tag Repo"
  private ListView<String> listTagsRepo;
  @FXML // The list view displayed on the left side of the GUI indicated by "Images/Filepath"
  private ListView<TilePane> listPopulatedEntries;
  @FXML // This table is displayed in the "Complete Logs" tab.
  private TableView<Map> tableCompleteArchives;
  @FXML
  private Button btnRemove;
  @FXML
  private Button btnAdd;
  @FXML
  private Button btnMove;
  @FXML
  private Button btnRevert;
  @FXML
  private Button btnChangeRoot;
  @FXML
  private Button btnAddFromRepo;
  @FXML
  private Button btnOpenInOperatingSystem;

  /** Keeps track of the list of tags of the selected image.
   * Implementation of observableArrayList is adapted from this tutorial:
   * Updating Data on 20171112 here:
   * https://docs.oracle.com/javafx/2/fxml_get_started/fxml_tutorial_intermediate.htm */
  private ObservableList<String> observableTagsList = FXCollections.observableArrayList();

  // Model that gives information back and forth between frontend and backend classes.
  private Model model = new Model();

  // Boolean used for determining whether the user has searched by directory or tag.
  private boolean searchingByTag = false;
  // Image files that are in either the selected directory/tags searched.
  private ArrayList<File> currentImageFiles = new ArrayList<>();
  // HBox inside of the tile pane from the listPopulatedEntries.
  private HBox selectedBox;
  private File selectedImageFile;
  private String searchedTags;
  private String currentDirectory;

  /* Boolean that remains false until the user attempts to modify an image. (either creating a new
   tag */
  private boolean saveEnabled = false;


  /**
   * Initializer for the GUI. First clears some assets of the MenuGUI.fxml and then initializes some
   * of the initial assets of MenuGUI.fxml. (txtCurDirectory, choiceRemovableTags)
   */
  @FXML
  public void initialize() {
    txtCurDirectory.setText("Directory");
    // Links the observable list to the drop down menu
    choiceRemovableTags.setItems(observableTagsList);
    // The list view on the right now allows multiple selection
    listTagsRepo.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    updateCompleteArchives();
    updateTagsRepo();
    updateButtonsState();
  }

  /**
   * Allows the user to select a directory from their computer, using their OS file explorer. This
   * refers to Oracle documentation. Updating Data on 20171112
   * here: https://docs.oracle.com/javase/8/javafx/api/javafx/stage/DirectoryChooser.html
   */
  public void changeDirectory() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    File chosenFolder = directoryChooser.showDialog(new Stage());
    if (chosenFolder != null) {
      searchingByTag = false;
      txtCurDirectory.setText(chosenFolder.getAbsolutePath());
      currentDirectory = chosenFolder.getAbsolutePath();
      setUpFiles(chosenFolder);
      resetUI();
      model.addNewImages(currentImageFiles);
    }
  }

  /**
   * Allows the user to search all images initialized through the application by tag(s). Updates the
   * list view of images to the searched tag(s).
   */
  public void searchByTag() {
    clearImageInfo();
    searchingByTag = true;
    currentImageFiles.clear();
    searchedTags = txtSearchTag.getText();
    currentImageFiles.addAll(model.searchByTags(searchedTags));

    resetUI();
  }

  /**
   * Set up currentImageFiles depending on whether the user searched by tags or through the
   * directory.
   *
   * @param chosenFolder folder that is chosen
   */
  private void setUpFiles(File chosenFolder) {
    currentImageFiles.clear();
    if (!searchingByTag) {
      ArrayList<File> filesInChosenFolder = model.getAllImageFiles(chosenFolder);
      currentImageFiles.addAll(filesInChosenFolder);
      model.initialPopulationOfDatabase(filesInChosenFolder);
    } else {
      currentImageFiles.addAll(model.searchByTags(searchedTags));
    }
  }

  /**
   * Reset the UI after repopulating the <code>currentImageFiles</code>.
   */
  private void resetUI() {
    clearImageInfo();
    updateCompleteArchives();
    updateTagsRepo();
    listPopulatedEntries.getItems().clear();
    saveEnabled = false;
    updateListPopulatedEntries(currentImageFiles);
    updateButtonsState();
  }

  /**
   * Clear elements in the UI that contain info about the selected image.
   */
  private void clearImageInfo() {
    imgView.setVisible(false);
    lblRootName.setText("");
    lblTags.setText("");
    lblPath.setText("");
    txtNewTagToImage.setText("");
    choiceRemovableTags.getItems().clear();
    listImageArchives.getItems().clear();
  }

  /**
   * Update the list view of images that are stored on the left. (thumbnail + file name)
   *
   * @param files Files from the selected directory or searched tag.
   */
  private void updateListPopulatedEntries(ArrayList<File> files) {

    // for images in the directory
    for (File file : files) {
      Label lbl = new Label(file.getName());
      ImageView img = new ImageView(
          new Image(new File("file:" + file.getAbsolutePath()).toString(),
              45, 45, true, true));
      TilePane tilePane = new TilePane();
      tilePane.setId(file.getAbsolutePath());

      /* Creates the image element using an HBox, consisting of a label and image-view and places it
      into the list view of image elements. (listPopulatedEntries) */
      HBox hbox = new HBox();
      hbox.setAlignment(Pos.CENTER_LEFT);
      hbox.getChildren().add(img);
      hbox.getChildren().add(lbl);
      hbox.getChildren().add(new Label(file.getParentFile().toString()));
      hbox.setSpacing(5);
      tilePane.getChildren().add(hbox);
      listPopulatedEntries.getItems().add(tilePane);
    }
  }

  /**
   * Checks for when listPopulatedEntries is scrolled through with the up and down arrow keys and
   * navigates the list view accordingly.
   *
   * @param e the user entered event key-press
   */
  public void imageNavigated(KeyEvent e) {
    if (e.getCode().toString().equals("UP") || e.getCode().toString().equals("DOWN")) {
      imageElementSelected();
    }
  }

  /**
   * Check for when an image element is selected (either clicked or scrolled through with the up and
   * down arrow keys). Tags, root name, and image log information become updated in the view.
   */
  public void imageElementSelected() {
    /* Check whether:
     * 1) There are items in the listPopulatedEntries
     * 2) User clicked on a valid item
     * 3) The item that the user selects is not the one that is currently selected.
     * (This is so we do not load again when we re-click the same item. */
    if (listPopulatedEntries.getItems() != null
        && listPopulatedEntries.getSelectionModel().getSelectedItem() != null
        && selectedBox != listPopulatedEntries.getSelectionModel().getSelectedItem()
        .getChildren().get(0)) {

      // Retrieves information from each selected tile.
      selectedBox = (HBox) listPopulatedEntries.getSelectionModel().getSelectedItem().getChildren()
          .get(0);

      Label lbl = (Label) selectedBox.getChildren().get(1);
      String clickedImgPath = ((Label) selectedBox.getChildren().get(2)).getText();
      txtCurDirectory.setText(clickedImgPath);

      selectedImageFile = new File(txtCurDirectory.getText() + "/" + lbl.getText());

      /* Updates the temporary local tags of the image, all previous tags used, and the
       image log. */
      updateRemovableTags(model.getTags(selectedImageFile));
      updateImageArchives(model.getArchives(selectedImageFile));
      updateImageInfo();
      saveEnabled = false;
      updateButtonsState();
    }

  }

  /**
   * Update the image info within the "Image" tab that deals with the selected image. Ex.
   * lblRootName, lblTags, and imgView. This method does this by obtaining a primitive array of info
   * from the selected image through the Model class.
   */
  private void updateImageInfo() {
    String[] imageInfo = model.getImageInfo(selectedImageFile);
    lblTags.setText(imageInfo[2]);
    lblTags.setAlignment(Pos.CENTER);
    lblPath.setText(selectedImageFile.getAbsolutePath());
    lblPath.setAlignment(Pos.CENTER);
    imgView.setImage(new Image("File:" + selectedImageFile.getAbsolutePath(),
        imgView.getFitWidth(), imgView.getFitHeight(), true, true));
    imgView.setVisible(true);
    lblRootName.setText(imageInfo[1]);
    lblRootName.setAlignment(Pos.CENTER);
  }

  /**
   * Move the selected image to another directory, and updates the database with the image in the
   * new directory.
   */
  public void moveImage() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setInitialDirectory(new File(txtCurDirectory.getText()));
    File targetFolder = directoryChooser.showDialog(new Stage());
    if (model.moveImage(selectedImageFile, targetFolder)) {
      setUpFiles(new File(currentDirectory));
      resetUI();
    } else {
      popUp("The image has not been moved. This can be a result of a duplicate root name and/or an "
          + "invalid directory.");
    }
  }

  /**
   * Remove a tag associated with an image. Called when the user hits the "Remove" button in the
   * GUI.
   */
  public void removeTagFromImage() {
    saveEnabled = model.updateTag(choiceRemovableTags.getValue(), false);
    updateButtonsState();
    saveTagsOfImage();
  }

  /**
   * Adds either a single new tag to the image, or multiple new tags when separated by a comma.
   * Called when the user hits the "Add New Tag to Image" button in the GUI.
   */
  public void addTagToImage() {
    boolean tagUpdated = model.updateTag(txtNewTagToImage.getText(), true);
    if (!saveEnabled) {
      saveEnabled = tagUpdated;
    }
    txtNewTagToImage.setText("");
    updateButtonsState();
    saveTagsOfImage();
  }

  /**
   * Adds the selected tag(s) from the tags repo.
   * Adds the tag when the user clicks the "<< Add Tag(s) to Image button underneath the tag repo.
   */
  public void addTagFromRepo() {
    if (listPopulatedEntries.getSelectionModel().getSelectedItem() != null) {
      boolean tagUpdated = model.updateTag(StringUtils
          .join(listTagsRepo.getSelectionModel().getSelectedItems(), ", "), true);
      if (!saveEnabled) {
        saveEnabled = tagUpdated;
      }
      updateButtonsState();
      saveTagsOfImage();
    }
  }

  /**
   * Add a new tag to the repo.
   */
  public void addTagToRepo() {
    if (!txtNewTagToRepo.getText().equals("")
        && model.addTags(txtNewTagToRepo.getText())) {

      model.addTags(txtNewTagToRepo.getText());
      txtNewTagToRepo.clear();
      updateTagsRepo();
    } else {
      popUp("The tag was not added. This can be due to the tag being empty or not being valid.");
    }
  }

  /**
   * Saves tags of the current image.
   */
  private void saveTagsOfImage() {
    if (model.saveTags(selectedImageFile)) {
      Integer i = listPopulatedEntries.getSelectionModel().getSelectedIndex();
      setUpFiles(new File(txtCurDirectory.getText()));
      resetUI();
      listPopulatedEntries.getSelectionModel().select(i);
      imageElementSelected();
    } else {
      popUp("The data is not saved. This can be a result of the list of tags not having been "
          + "changed.");
    }
  }

  /**
   * Changes the root name of the image. Called when the user enters a new root name and hits the
   * "Rename" button.
   */
  public void changeRootName() {
    if (!lblRootName.getText().equals(txtRootName.getText())
        && model.updateRootName(selectedImageFile, txtRootName.getText())) {

      Integer i = listPopulatedEntries.getSelectionModel().getSelectedIndex();
      lblRootName.setText(txtRootName.getText());
      saveEnabled = true;
      txtRootName.setText("");
      setUpFiles(new File(txtCurDirectory.getText()));
      resetUI();
      listPopulatedEntries.getSelectionModel().select(i);
      imageElementSelected();
      popUp("Root name changed.");
    } else {
      popUp("Invalid Root Name, Root Name did not change.");
    }

  }

  /**
   * Revert the tag states of the current image.
   */
  public void revertImageState() {
    if (model.revertData(selectedImageFile,
        listImageArchives.getSelectionModel().getSelectedItem())) {

      Integer i = listPopulatedEntries.getSelectionModel().getSelectedIndex();
      setUpFiles(new File(txtCurDirectory.getText()));
      resetUI();
      listPopulatedEntries.getSelectionModel().select(i);
      imageElementSelected();
      popUp("The data is reverted.");
    } else {
      popUp("The data is not reverted. This is due to the selected log not being valid for "
          + "reverting.");
    }
  }

  /**
   * Update the repo of the tags in the right side of the GUI. Clears first and then populates the
   * list view with the tags using the getFullTagList method from the model.
   */
  private void updateTagsRepo() {
    listTagsRepo.getItems().clear();
    listTagsRepo.getItems().addAll(model.getFullTagList());
  }

  /**
   * Update observableTagsList. This updates the tags that can be removed within the drop down menu
   * of the selected image.
   *
   * @param tags Tags that are to be added into observableTagsList.
   */
  private void updateRemovableTags(ArrayList<String> tags) {
    observableTagsList.clear();
    observableTagsList.addAll(tags);
  }

  /**
   * Update the archive logs of the selected image.
   *
   * @param archives array list of tags that are added into listImageArchives.
   */
  private void updateImageArchives(ArrayList<String> archives) {
    listImageArchives.getItems().clear();
    listImageArchives.getItems().addAll(archives);
  }

  /**
   * Update the full log of tags for all images that are initialized through the application. Clears
   * first and then adds using the getFullArchives method from the model. <p> Implementation of the
   * tableview value generation t is adapted from this tutorial: Updating Data on 20171129 here:
   * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/table-view.htm
   */
  private void updateCompleteArchives() {
    tableCompleteArchives.getItems().clear();
    ObservableList<Map> allData = FXCollections.observableArrayList();
    for (ArrayList<String> log : model.getFullArchives()) {
      Map<String, String> dataRow = new HashMap<>();
      dataRow.put("valuesCompleteDir", log.get(0));
      dataRow.put("valuesCompleteOld", log.get(1));
      dataRow.put("valuesCompleteNew", log.get(2));
      dataRow.put("valuesCompleteTime", log.get(3));
      allData.add(dataRow);
    }
    tableCompleteArchives.setItems(allData);
  }

  /**
   * Helper method that displays a pop up dialog.
   *
   * @param message to be displayed.
   */
  private void popUp(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Confirmation");
    alert.setContentText(message);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  /**
   * Text which displays the full root name in a tool tip when the rootName label is hovered over.
   */
  public void rootNameHover() {
    Tooltip tt = new Tooltip(lblRootName.getText());
    tt.setPrefWidth(400);
    tt.setWrapText(true);
    lblRootName.setTooltip(tt);
  }

  /**
   * Text which displays all the tag names of an image in a tool tip when the cursor hovers over the
   * tag label.
   */
  public void tagNameHover() {
    Tooltip tt = new Tooltip(lblTags.getText());
    tt.setPrefWidth(300);
    tt.setWrapText(true);
    lblTags.setTooltip(tt);
  }

  /**
   * Text which displays the absolute path in a tool tip when the cursor hovers over the path label.
   */
  public void imagePathHover() {
    if (selectedImageFile != null) {
      Tooltip tt = new Tooltip(selectedImageFile.getAbsolutePath());
      tt.setPrefWidth(300);
      tt.setWrapText(true);
      lblPath.setTooltip(tt);
    }

  }

  /**
   * Text which displays a tool tip for explaining how to enter multiple tags.
   */
  public void separatedTagHover() {
    Tooltip tt = new Tooltip("To add multiple tags, separate by a comma.");
    tt.setPrefWidth(200);
    tt.setWrapText(true);
    txtSearchTag.setTooltip(tt);
    txtNewTagToImage.setTooltip(tt);
    txtNewTagToRepo.setTooltip(tt);

  }

  /**
   * Text which displays a help tool tip for the repo when hovering over the tag repo list view.
   */
  public void tagRepoHover() {
    Tooltip tt = new Tooltip("Hold Control or Shift To Select Multiple");
    tt.setPrefWidth(300);
    tt.setWrapText(true);
    listTagsRepo.setTooltip(tt);
  }

  /**
   * Open the file directory in the OS.
   */
  public void openInOperatingSystem() {
    String operatingSystem = System.getProperty("os.name").toLowerCase();
    if (operatingSystem.contains("nix")
        || operatingSystem.contains("nux")
        || operatingSystem.indexOf("aix") > 0) {
      try {
        Runtime.getRuntime().exec(new String[]{"nautilus", txtCurDirectory.getText()});
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        Desktop.getDesktop().open(new File(txtCurDirectory.getText()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Update button enable state when archive log is selected.
   */
  public void archiveSelected() {
    updateButtonsState();
  }

  /**
   * Disable or enable all buttons depending on whether the buttons can create changes
   * with the given user input.
   */
  private void updateButtonsState() {
    if (listPopulatedEntries.getItems() != null
        && listPopulatedEntries.getSelectionModel().getSelectedItem() != null) {

      btnMove.setDisable(false);
      btnOpenInOperatingSystem.setDisable(false);
      btnAdd.setDisable(false);
      btnChangeRoot.setDisable(false);
      btnAddFromRepo.setDisable(false);
      // btnRevert is enabled/disabled depending on whether listImageArchives is populated/selected.
      btnRevert.setDisable(listImageArchives.getItems() == null
          || listImageArchives.getSelectionModel().getSelectedItem() == null);
      // btnRemove is enabled/disabled depending on whether there are tags to be removed.
      btnRemove.setDisable(choiceRemovableTags.getItems().size() == 0);
    } else {
      btnMove.setDisable(true);
      btnOpenInOperatingSystem.setDisable(true);
      btnAdd.setDisable(true);
      btnRemove.setDisable(true);
      btnRevert.setDisable(true);
      btnChangeRoot.setDisable(true);
      btnAddFromRepo.setDisable(true);
    }
  }
}

