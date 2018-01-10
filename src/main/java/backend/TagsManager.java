package backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages all tags and logs that are created in the database and communicates with the Model.
 */
class TagsManager {

  /**
   * SglCommands object to allow for grabbing data from the Tags Table in the Database.
   */
  private SqlCommandsTags sqlCommandsTags;

  /**
   * The constructor of TagManager.
   *
   * @param connection connection to the database
   */
  TagsManager(Connection connection) {
    this.sqlCommandsTags = new SqlCommandsTags(connection);
  }

  /**
   * Gets all the the tags are in the Tags table in the database.
   *
   * @return an ArrayList of all the tags that are in the Tags table
   * @throws SQLException if there is an issue with the database or the Tags Table
   */
  ArrayList<String> getAllTags() throws SQLException {
    ArrayList<String> tags = new ArrayList<>();
    ResultSet resultSet = sqlCommandsTags.selectAll();

    while (resultSet.next()) {
      tags.add(resultSet.getString("name"));
    }
    return tags;
  }

  /**
   * Adds in a new tag to the Tags table in the database.
   *
   * @param tags list of tags you want to add.
   * @throws SQLException if there is an issue with the database or the Tags Table
   */
  void addTags(ArrayList<String> tags) throws SQLException {
    for (String tag : tags) {
      int id = sqlCommandsTags.selectByName(tag);
      if (id == -1) {
        sqlCommandsTags.insert(tag);
      }
    }
  }
}
