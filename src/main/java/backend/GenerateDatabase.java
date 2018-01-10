package backend;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the Database for storing tags within the Program and for Unit Testing. Notes: This
 * class was created by following http://www.sqlitetutorial.net/sqlite-java/ to understand how Java
 * Sql commands work.
 */
public class GenerateDatabase {

  private static final String GENERATE_IMAGE_TAGS_TABLE_SCRIPT =
      "CREATE TABLE IF NOT EXISTS ImageTags (\n"
          + "	id integer PRIMARY KEY,\n"
          + "	directory VARCHAR NOT NULL,\n"
          + "	rootName VARCHAR NOT NULL,\n"
          + "	tags VARCHAR NOT NULL,\n"
          + "	ext VARCHAR NOT NULL,\n"
          + " current BIT NOT NULL,\n"
          + "	createdOn TIMESTAMP\n"
          + ");";

  private static final String GENERATE_TAGS_TABLE_SCRIPT =
      "CREATE TABLE IF NOT EXISTS Tags (\n"
          + "	id integer PRIMARY KEY,\n"
          + "	name VARCHAR NOT NULL\n"
          + ");";

  private static final String CLEAR_IMAGE_TAGS_TABLE_SCRIPT =
      "DROP TABLE IF EXISTS ImageTags";

  private static final String CLEAR_TAGS_TABLE_SCRIPT =
      "DROP TABLE IF EXISTS Tags";

  /**
   * logger
   */
  private static Logger LOGGER = Logger.getLogger(GenerateDatabase.class.getName());

  /**
   * Creates a table in our database.
   *
   * @param generateScript script of the table you want to generate
   * @param connection connection to the database
   */
  private static void generateClearTable(String generateScript, Connection connection) {
    try (Statement statement = connection.createStatement()) {
      statement.execute(generateScript);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Generates all the tables in your database, if they are not already created.
   */
  public static void generate() {
    GenerateDatabase.generateClearTable(GENERATE_IMAGE_TAGS_TABLE_SCRIPT, SqlCommands.CONNECTION);
    GenerateDatabase.generateClearTable(GENERATE_TAGS_TABLE_SCRIPT, SqlCommands.CONNECTION);
  }

  /**
   * Drops all the tables in your test database if they already exist and then recreates them,
   * otherwise generates all the tables in your test database.
   */
  static void generateTest() {
    // Drop Tables
    GenerateDatabase.generateClearTable(CLEAR_TAGS_TABLE_SCRIPT, SqlCommands.TEST_CONNECTION);
    GenerateDatabase.generateClearTable(CLEAR_IMAGE_TAGS_TABLE_SCRIPT, SqlCommands.TEST_CONNECTION);

    // Generate Tables
    GenerateDatabase
        .generateClearTable(GENERATE_IMAGE_TAGS_TABLE_SCRIPT, SqlCommands.TEST_CONNECTION);
    GenerateDatabase.generateClearTable(GENERATE_TAGS_TABLE_SCRIPT, SqlCommands.TEST_CONNECTION);
  }
}
