package com.jad.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetSaleByIdTest {
    static final int NUMBER_OF_ID_TEST = 10;
    private static Connection connection;

    @BeforeAll
    public static void setUp() {
        DBConnection.INSTANCE.connect();
        GetSaleByIdTest.connection = DBConnection.INSTANCE.getConnection();
    }

    @Test
    public void doAllTests() {
        for (int i = 0; i < GetSaleByIdTest.NUMBER_OF_ID_TEST; i++) {
            this.getSaleByIdTest();
        }
    }

    public void getSaleByIdTest() {
        final ResultSet[] expectedSale = new ResultSet[1];
        final ResultSet[] procedureSale = new ResultSet[1];
        assertDoesNotThrow(() -> {
            final CallableStatement countStatement = GetSaleByIdTest.connection.prepareCall(
                    "SELECT vente.id, " +
                            "vente.date, " +
                            "vente.numVente AS orderNumber, " +
                            "consommation.denomination AS onSite " +
                            "FROM vente " +
                            "INNER JOIN consommation ON vente.id_consommation = consommation.id " +
                            "ORDER BY RAND() LIMIT 1");
            countStatement.execute();
            countStatement.getResultSet().next();
            expectedSale[0] = countStatement.getResultSet();
        }, "Table 'vente' should exist");
        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = GetSaleByIdTest.connection.prepareCall(
                    "{CALL getSaleById(?)}");
            callableStatement.setInt(1, expectedSale[0].getInt("id"));
            callableStatement.execute();
            callableStatement.getResultSet().next();
            final ResultSet resultSet = callableStatement.getResultSet();
            procedureSale[0] = resultSet;
        }, "Procedure 'getSaleById' should exist");
        assertDoesNotThrow(() -> {
            assertEquals(expectedSale[0].getInt("id"), procedureSale[0].getInt("id"),
                         "Procedure 'getSaleById' should return the sale with the same id");
            assertEquals(expectedSale[0].getString("date"), procedureSale[0].getString("date"),
                         "Procedure 'getSaleById' should return the sale with the same date");
            assertEquals(expectedSale[0].getInt("orderNumber"), procedureSale[0].getInt("orderNumber"),
                         "Procedure 'getSaleById' should return the sale with the same orderNumber");
            if (expectedSale[0].getString("onSite").equals("Sur place")) {
                assertEquals("Y", procedureSale[0].getString("onSite"),
                             "Procedure 'getSaleById' should return the sale with the same on site");
            } else {
                assertEquals("N", procedureSale[0].getString("onSite"),
                             "Procedure 'getSaleById' should return the sale with the same on site");
            }
        }, "Procedure 'getSaleById' should return the sale with the same id");
    }
}
