package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A utility class that provides update and query methods for the Tags table.
 */
class SqlCommandsTags extends SqlCommands {

  /**
   * CONSTANT prepared statements.
   */
  private static final String INSERT_STATEMENT = "INSERT INTO Tags("
      + "name )"
      + " VALUES(?)";
  private static final String SELECT_ALL = "SELECT * FROM Tags "
      + "ORDER BY name";
  private static final String SELECT_BY_NAME = "SELECT * FROM Tags WHERE name=? ";

  /**
   * The constructor of SqlCommandsTags that uses default constructor from super class to establish
   * database connection.
   *
   * @param connection connection to the database
   */
  SqlCommandsTags(Connection connection) {
    super(connection);
  }

  /**
   * Inserts a new entry into the ImageTags table using the parameters as its values.
   *
   * @param name name of the new tag
   * @throws SQLException if there is an issue with the database or the Tags Table
   */
  void insert(String name) throws SQLException {
    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(INSERT_STATEMENT);
    preparedStatement.setString(1, name);
    preparedStatement.executeUpdate();
  }

  /**
   * Returns all the rows in the tags table from the database as a ResultSet. This was adapted from
   * a tutorial on SQLite Java: Select Data on 20171104 here: http://www.sqlitetutorial.net/sqlite-java/select/
   *
   * @return ResultSet of all the Tags in the database
   * @throws SQLException if there is an issue with the database or the Tags Table
   */
  ResultSet selectAll() throws SQLException {

    Statement statement = this.currentConnection.createStatement();

    return statement.executeQuery(SELECT_ALL);
  }

  /**
   * Gets the tag's id that is specified by the given name from the Tags Table.else return -1 to
   * represent that the Tag specified by the given name doesn't exist in the Tags Table.
   *
   * @param name name of the tag you want
   * @return an int which represents the id of the Tag we searched by if it exists in the Tags Table
   * @throws SQLException if there is an issue with the database or the Tags Table
   */
  int selectByName(String name) throws SQLException {

    PreparedStatement preparedStatement = this.currentConnection.prepareStatement(SELECT_BY_NAME);
    preparedStatement.setString(1, name);
    ResultSet resultSet = preparedStatement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getInt("id");
    }
    return -1;
  }
}