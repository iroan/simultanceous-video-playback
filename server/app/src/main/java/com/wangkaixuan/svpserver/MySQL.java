package com.wangkaixuan.svpserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private Connection conn;

    public Connection getConn() {
        if (conn != null) {
            return conn;
        }

        try {
            var uri = (String) Config.ins().get("MySQLUri");
            conn = DriverManager.getConnection(uri);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return conn;
    }
}
