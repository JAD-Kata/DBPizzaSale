package com.jad.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetAllSaleLinesTest {
    private static Connection connection;

    @BeforeAll
    public static void setUp() {
        DBConnection.INSTANCE.connect();
        GetAllSaleLinesTest.connection = DBConnection.INSTANCE.getConnection();
    }

    @Test
    public void doAllTests() {
        this.getAllSalesTest();
    }

    public void getAllSalesTest() {
        final AtomicInteger procedureCount = new AtomicInteger();
        final AtomicInteger expectedCount = new AtomicInteger();
        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = GetAllSaleLinesTest.connection.prepareCall(
                    "{CALL getAllSaleLines()}");
            callableStatement.execute();
            final ResultSet resultSet = callableStatement.getResultSet();
            int size = 0;
            while (resultSet.next()) {
                size++;
            }
            procedureCount.set(size);
        }, "Procedure 'getAllSales' should exist");
        assertDoesNotThrow(() -> {
            final CallableStatement countStatement = GetAllSaleLinesTest.connection.prepareCall(
                    "SELECT count(*) FROM lignedevente");
            countStatement.execute();
            countStatement.getResultSet().next();
            expectedCount.set(countStatement.getResultSet().getInt(1));
        }, "Table 'vente' should exist");
        assertEquals(expectedCount.get(), procedureCount.get(), "Procedure 'getAllSales' should return all sales");
    }
}
