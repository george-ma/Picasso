package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.sqlite.util.StringUtils;

/**
 * A utility class that provides update and query methods for the ImageTags table.
 */
class SqlCommandsImageTags extends SqlCommands {

  /**
   * CONSTANT prepared statements.
   */
  private static final String INSERT_STATEMENT = "INSERT INTO ImageTags("
      + "directory,"
      + " rootName,"
      + "	tags,"
      + " ext,"
      + " current,"
      + "	createdOn)"
      + " VALUES(?,?,?,?,?,?)";

  private static final String SELECT_ALL = "SELECT * FROM ImageTags "
      + "ORDER BY rootName, ext, createdOn";
  private static final String SELECT_BY_PATH = "SELECT * FROM ImageTags "
      + "WHERE rootName=? AND directory=? AND ext=? ORDER BY createdOn DESC";
  private static final String SELECT_CURRENT = "SELECT * FROM ImageTags "
      + "WHERE current=1 AND rootName=? AND directory=? AND ext=? ";
  private static final String SELECT_BY_TAG = "SELECT * FROM ImageTags WHERE current=1 AND (";
  private static final String UPDATE_CURRENT = "UPDATE ImageTags SET current=? WHERE id=? ";
  private static final String UPDATE_DIR = "UPDATE ImageTags SET directory=? "
      + "WHERE rootName=? AND directory=? AND ext=? ";
  private static final String UPDATE_ROOT_NAME = "UPDATE ImageTags SET rootName=? "
      + "WHERE rootName=? AND directory=? AND ext=? ";

  /**
   * The constructor of SqlCommandsImageTags that uses default constructor from super class to
   * establish database connection.
   *
   * @param connection connection to the database
   */
  SqlCommandsImageTags(Connection connection) {
    super(connection);
  }

  /**
   * Inserts a new entry into the ImageTags table using the parameters as its values. This was
   * adapted from a tutorial on SQLite Java: Inserting Data on 20171104 here:
   * http://www.sqlitetutorial.net/sqlite-java/insert/
   *
   * @param directory directory in which the image file to be added is saved in
   * @param rootName the name of the image file to be added excluding the tags.
   * @param tags the tags fo the the image file to be added
   * @param ext the extension of the image file to be added
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void insert(String directory, String rootName, String tags, String ext)
      throws SQLException {
    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(INSERT_STATEMENT);
    preparedStatement.setString(1, directory);
    preparedStatement.setString(2, rootName);
    preparedStatement.setString(3, tags);
    preparedStatement.setString(4, ext);
    preparedStatement.setBoolean(5, true);
    preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
    preparedStatement.executeUpdate();
  }

  /**
   * Returns all the rows in our ImageTag table. This was adapted from a tutorial on SQLite Java:
   * Select Data on 20171104 here: http://www.sqlitetutorial.net/sqlite-java/select/
   *
   * @return ResultSet of all the entries in the ImageTags Table
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ResultSet selectAll() throws SQLException {
    Statement statement = this.currentConnection.createStatement();
    return statement.executeQuery(SELECT_ALL);
  }

  /**
   * Returns all the rows in our ImageTag table, where directory field is equal to the given
   * directory and the name of the is equal to the given Image file name and the extension is equal
   * to the given extension.
   *
   * @param directory directory of the desired Image.
   * @param rootName name of the desired Image excluding tags.
   * @param ext the extension of the image
   * @return A ResultSet of all the entries in the ImageTags Table that have the same directory,
   * rootName and extension as the given parameters.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ResultSet selectByPath(String directory, String rootName, String ext) throws SQLException {
    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(SELECT_BY_PATH);
    preparedStatement.setString(1, rootName);
    preparedStatement.setString(2, directory);
    preparedStatement.setString(3, ext);
    return preparedStatement.executeQuery();
  }

  /**
   * Searches for all the imageTags that have been logged in the database and return then ones that
   * have at least one of the tags we are searching by.
   *
   * @param tags the list of tags we want to search by.
   * @return A ResultSet of all the ImageTags that have at least one of the tags we searched by.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  ResultSet selectByTags(ArrayList<String> tags) throws SQLException {
    ArrayList<String> sqlCommand = new ArrayList<>();
    sqlCommand.add(SELECT_BY_TAG);
    for (String tag : tags) {
      sqlCommand.add("tags LIKE '%@" + tag + "%'");
      sqlCommand.add("OR");
    }
    sqlCommand.remove(sqlCommand.size() - 1);
    sqlCommand.add(")");
    Statement statement = this.currentConnection.createStatement();
    return statement.executeQuery(StringUtils.join(sqlCommand, " "));
  }

  /**
   * Gets the current tag data of the image specified by the given directory and rootName from the
   * ImageTags Table. If the image specified by the given directory and rootName doesn't exist in
   * the ImageTags Table, then the function will return -1, else the id of the current tag data of
   * the specified image will be returned.
   *
   * @param directory directory in which your image in saved
   * @param rootName the original name of your image file before tags where added
   * @param ext the extension of the image file
   * @return A int representing the id of the image specified by the given directory and rootName,
   * else return -1 to represent that the image specified doesn't exist in the ImageTags Table.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  int selectCurrent(String directory, String rootName, String ext) throws SQLException {

    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(SELECT_CURRENT);
    preparedStatement.setString(1, rootName);
    preparedStatement.setString(2, directory);
    preparedStatement.setString(3, ext);
    ResultSet resultSet = preparedStatement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getInt("id");
    }
    return -1;
  }

  /**
   * Updates a given tag data instance’s (id) current field to what the user specifies(current).
   *
   * @param id of the tag instance to be updated
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateCurrentToFalse(int id) throws SQLException {
    this.updateStatement(UPDATE_CURRENT, id);
  }

  /**
   * Updates a given tag data instance’s directory field to what the user specifies(newDir). This
   * method is called when an image is moved to a different directory.
   *
   * @param newDir is the new directory of the image
   * @param rootName ordinal name of the image
   * @param currentDir is the current directory
   * @param currentExt the type of the image
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateDir(String newDir, String rootName, String currentDir, String currentExt)
      throws SQLException {
    updateFilePath(newDir, rootName, currentDir, currentExt, UPDATE_DIR);
  }

  /**
   * Updates a given tag data instance’s rootName field to what the user specifies(newRootName).
   * This method is called when an image is moved to a different directory.
   *
   * @param newRootName new root name
   * @param rootName original root name of the image
   * @param currentDir is the full path name for the parent directory.
   * @param currentExt the type of the image
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateRootName(String newRootName, String rootName, String currentDir, String currentExt)
      throws SQLException {
    updateFilePath(newRootName, rootName, currentDir, currentExt, UPDATE_ROOT_NAME);
  }

  /**
   * Updates a given tag data instance’s directory field to what the user specifies(updateParam) if
   * sqlCommand is equal to UPDATE_DIR or it updates given tag data instance’s rootName field to
   * what the user specifies(updateParam) if sqlCommand is equal to UPDATE_ROOT_NAME.
   *
   * @param newRootNameOrDir The new value to be updated
   * @param rootName rootName
   * @param currentDir current directory
   * @param currentExt is the extension for the photos
   * @param sqlCommand used for updating.
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  private void updateFilePath(String newRootNameOrDir, String rootName,
      String currentDir, String currentExt, String sqlCommand) throws SQLException {
    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(sqlCommand);
    preparedStatement.setString(1, newRootNameOrDir);
    preparedStatement.setString(2, rootName);
    preparedStatement.setString(3, currentDir);
    preparedStatement.setString(4, currentExt);
    preparedStatement.executeUpdate();
  }
}
