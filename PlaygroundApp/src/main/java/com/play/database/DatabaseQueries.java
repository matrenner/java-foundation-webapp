package com.play.database;

public final class DatabaseQueries {
    public static final String SELECT_USERNAME_COUNT = "SELECT COUNT(*) FROM user WHERE username = ?;";
    public static final String SELECT_EMAIL_COUNT = "SELECT COUNT(*) FROM user WHERE email = ?;";
    public static final String INSERT_NEW_USER =
            "INSERT INTO user(username, email, password, create_time)" +
                    " values(?,?,?,CURRENT_TIME());";
    public static final String SELECT_USER_PASSWORD = "SELECT password FROM user WHERE username = ? or email = ?";
}
