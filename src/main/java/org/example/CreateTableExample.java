package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;

public class CreateTableExample {
    private final String url = "jdbc:postgresql://localhost/demo";
    private final String user = "postgres";
    private final String password = "postgres";

//    private static final String createTableSQL = "CREATE TABLE users " +
//            "(ID INT PRIMARY KEY ," +
//            " NAME TEXT, " +
//            " EMAIL VARCHAR(50), " +
//            " COUNTRY VARCHAR(50), " +
//            " PASSWORD VARCHAR(50))";

    private static final String createTableSQL = "CREATE TABLE review " +
            "(ID SERIAL," +
            " COURSE_NAME VARCHAR(128), " +
            " STUDENT_NAME VARCHAR(50), " +
            " TIMESTAMP timestamp, " +
            " RATING float, " +
            " COMMENT VARCHAR(1024))";

    public static void main(String[] args) throws SQLException {
        CreateTableExample createTableExample = new CreateTableExample();
 //       createTableExample.createTable();
//        createTableExample.parameterizedBatchUpdate();
        createTableExample.performBatchUpdate();
    }

    private void performBatchUpdate() {

        String csvFilePath = "src/main/java/org/example/Review_simple.csv";
        int batchSize = 20;

        Connection connection = null;

        String INSERT_USERS_SQL = "INSERT INTO review" + "  ( course_name, student_name, timestamp, rating,comment) VALUES " +
                " (?, ?, ?, ?, ?);";

        try {
            connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement(INSERT_USERS_SQL);
            connection.setAutoCommit(false);

            BufferedReader lineReader = new BufferedReader(new FileReader(csvFilePath));
            String lineText = null;

            int count = 0;

            lineReader.readLine(); // skip header line

            while ((lineText = lineReader.readLine()) != null) {
                String[] data = lineText.split(",");
                String courseName = data[0];
                String studentName = data[1];
                String timestamp = data[2];
                String rating = data[3];
                String comment = data.length == 5 ? data[4] : "";

                statement.setString(1, courseName);
                statement.setString(2, studentName);

                Timestamp sqlTimestamp = Timestamp.valueOf(timestamp);
                statement.setTimestamp(3, sqlTimestamp);

                Float fRating = Float.parseFloat(rating);
                statement.setFloat(4, fRating);

                statement.setString(5, comment);

                statement.addBatch();

                if (count % batchSize == 0) {
                    statement.executeBatch();
                }
            }

            lineReader.close();

            // execute the remaining queries
            statement.executeBatch();

            connection.commit();
            connection.close();

        }catch (IOException ex) {
            System.err.println(ex);
        } catch (SQLException ex) {
            ex.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTable() throws SQLException  {

        System.out.println(createTableSQL);
        // Step 1: Establishing a Connection
        try {
           Connection connection = DriverManager.getConnection(url, user, password);

                /* Step 2:Create a statement using connection object */
             Statement statement = connection.createStatement();

            // Step 3: Execute the query or update query
            statement.execute(createTableSQL);
        } catch (SQLException e) {

            // print SQL exception information
            printSQLException(e);
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

    private  void parameterizedBatchUpdate() {

        String INSERT_USERS_SQL = "INSERT INTO users" + "  (id, name, email, country, password) VALUES " +
                " (?, ?, ?, ?, ?);";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL);
            connection.setAutoCommit(false);

            preparedStatement.setInt(1, 20);
            preparedStatement.setString(2, "a");
            preparedStatement.setString(3, "a@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 21);
            preparedStatement.setString(2, "b");
            preparedStatement.setString(3, "b@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 22);
            preparedStatement.setString(2, "c");
            preparedStatement.setString(3, "c@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 23);
            preparedStatement.setString(2, "d");
            preparedStatement.setString(3, "d@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println(Arrays.toString(updateCounts));
            connection.commit();
            connection.setAutoCommit(true);
        } catch (BatchUpdateException batchUpdateException) {
            printBatchUpdateException(batchUpdateException);
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void printBatchUpdateException(BatchUpdateException b) {

        System.err.println("----BatchUpdateException----");
        System.err.println("SQLState:  " + b.getSQLState());
        System.err.println("Message:  " + b.getMessage());
        System.err.println("Vendor:  " + b.getErrorCode());
        System.err.print("Update counts:  ");
        int[] updateCounts = b.getUpdateCounts();

        for (int i = 0; i < updateCounts.length; i++) {
            System.err.print(updateCounts[i] + "   ");
        }
    }
}
