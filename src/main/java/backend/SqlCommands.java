package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General structure for SQL commands used to connect and grab information from the database.
 */
abstract class SqlCommands {

  private static final String URL = "jdbc:sqlite:./TaggingSystemDB";
  private static final String TEST_URL = "jdbc:sqlite:./TestTaggingSystemDB";
  private static Connection connection;
  private static Connection testConnection;
  static final Connection CONNECTION = getConnection();
  static final Connection TEST_CONNECTION = getTestConnection();

  // Logger to handle SQLExceptions.
  private static Logger LOGGER = Logger.getLogger(SqlCommands.class.getName());

  // Connection to the database we are currently communicating with.
  Connection currentConnection;

  /**
   * The constructor of SqlCommands that also establishes a connection to the database.
   *
   * @param connection connection to the database
   */
  SqlCommands(Connection connection) {
    this.currentConnection = connection;
  }

  /**
   * Connects the program to the main SQL database. This was adapted from a tutorial on SQLite Java:
   * Updating Data on 20171104 here: http://www.sqlitetutorial.net/sqlite-java/update/
   *
   * @return a connection to the database
   */
  private static Connection getConnection() {
    if (SqlCommands.connection == null) {
      try {
        SqlCommands.connection = DriverManager.getConnection(SqlCommands.URL);
      } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return SqlCommands.connection;
  }

  /**
   * Connects the program to the Test SQL database. This was adapted from a tutorial on SQLite Java:
   * Updating Data on 20171104 here: http://www.sqlitetutorial.net/sqlite-java/update/
   *
   * @return a connection to the Test database
   */
  private static Connection getTestConnection() {
    if (SqlCommands.testConnection == null) {
      try {
        SqlCommands.testConnection = DriverManager.getConnection(SqlCommands.TEST_URL);
      } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return SqlCommands.testConnection;
  }

  /**
   * This is a generic update statement for updating the line that has the specified primary key.
   * This was adapted from a tutorial on SQLite Java: Updating Data on 20171104 here:
   * http://www.sqlitetutorial.net/sqlite-java/update/
   *
   * @param updateType is the prepared statement that is custom for which column to update
   * @param primaryKey is the key to the row we need to update
   * @throws SQLException if there is an issue with the database or the ImageTags Table
   */
  void updateStatement(String updateType, Object primaryKey)
      throws SQLException {
    PreparedStatement prepStatement = getConnection().prepareStatement(updateType);
    prepStatement.setObject(1, false);
    prepStatement.setObject(2, primaryKey);
    prepStatement.executeUpdate();
  }
}
