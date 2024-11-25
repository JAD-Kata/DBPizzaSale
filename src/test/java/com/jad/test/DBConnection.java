package com.jad.test;

import lombok.Getter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public enum DBConnection {
    INSTANCE();

    private Connection connection;

    DBConnection() {
        this.connection = null;
    }

    public void connect() {
        if (this.connection != null) return;
        final DBProperties dbProperties = new DBProperties();
        try {
            dbProperties.load();
            Class.forName(dbProperties.getDriver());
            this.connection = DriverManager.getConnection(dbProperties.getUrl(),
                                                          dbProperties.getUser(),
                                                          dbProperties.getPassword());
        } catch (SQLException | IOException | ClassNotFoundException error) {
            throw new RuntimeException(error);
        }
    }

}
