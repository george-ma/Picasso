package frontend;

import backend.GenerateDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Starting point for the front-end. Communicates with the MenuGUI to begin the application.
 */
public class Main extends Application {


  /**
   * Launches the application.
   *
   * @param args optional arguments for launching the application
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * The start method is called after the init method has returned,and after the system is ready for
   * the application to begin running.
   *
   * @param primaryStage the primary stage for this application
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    GenerateDatabase.generate();
    Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
    primaryStage.setTitle("Picasso Photo Manager");
    primaryStage.setScene(new Scene(root, 980, 600));
    primaryStage.setResizable(true);
    primaryStage.show();

  }
}
