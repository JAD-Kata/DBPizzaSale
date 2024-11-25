package com.jad.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CreateSaleTest {
    private static Connection connection;

    @BeforeAll
    public static void setUp() {
        DBConnection.INSTANCE.connect();
        CreateSaleTest.connection = DBConnection.INSTANCE.getConnection();
    }

    @Test
    public void doAllTests() {
        this.createSaleTest(this.getNextOrderNumberTest(), "Y");
        this.createSaleTest(this.getNextOrderNumberTest(), "N");

        final String orderNumber = this.getNextOrderNumberTest();
        this.createSaleTest(orderNumber, "Y");
        for (String type : this.getAllTypeTest()) {
            this.addSaleLineTest(orderNumber, type);
        }
    }

    private void createSaleTest(final String orderNumber, final String onSite) {
        final ResultSet[] actualResultSet = new ResultSet[1];

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "{CALL createSale(?, ?)}");
            callableStatement.setString(1, orderNumber);
            callableStatement.setString(2, onSite);
            callableStatement.execute();
        }, "Procedure 'createSale' should exist");

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT * FROM dv01_sale WHERE orderNumber = '" + orderNumber + "'");
            callableStatement.execute();
            callableStatement.getResultSet().next();
            actualResultSet[0] = callableStatement.getResultSet();
        }, "Table 'dv01_sale' should exist");

        assertDoesNotThrow(() -> assertEquals(onSite, actualResultSet[0].getString("onSite"),
                                              "Procedure 'createSale' should create a sale with the correct onSite value"),
                           "Procedure 'createSale' should create a sale with the correct onSite value");
    }

    private String getNextOrderNumberTest() {
        final String[] orderNumber = new String[1];

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "{CALL getNextOrderNumber()}");
            callableStatement.execute();
            callableStatement.getResultSet().next();
            orderNumber[0] = callableStatement.getResultSet().getString("orderNumber");
        }, "Procedure 'getNextOrderNumber' should exist");

        return orderNumber[0];
    }

    private List<String> getAllTypeTest() {
        final ArrayList<String> types = new ArrayList<>();
        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT denomination FROM typeproduit");
            callableStatement.execute();
            final ResultSet resultSet = callableStatement.getResultSet();
            while (resultSet.next()) {
                types.add(resultSet.getString("denomination"));
            }
        }, "Table 'product' should exist");
        return types;
    }

    private void addSaleLineTest(final String orderNumber, final String type) {
        final ResultSet[] actualResultSet = new ResultSet[1];
        final String product = this.getRandomProductByTypeTest(type);
        final int quantity = new Random().nextInt(4) + 1;
        final String[] size = {null};
        final String[][] options = {{null, null, null, null}};
        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "{CALL addSaleLine(?, ?, ?, ?, ?, ?, ?, ?)}");
            callableStatement.setString(1, orderNumber);
            callableStatement.setString(2, product);
            if (type.equals("Pizza")) {
                size[0] = this.getRandomSizeTest();
                options[0] = this.getRandomOptionsTest();
                callableStatement.setString(3, size[0]);
                callableStatement.setString(5, options[0][0]);
                callableStatement.setString(6, options[0][1]);
                callableStatement.setString(7, options[0][2]);
                callableStatement.setString(8, options[0][3]);
            } else {
                callableStatement.setNull(3, java.sql.Types.INTEGER);
                callableStatement.setNull(5, java.sql.Types.INTEGER);
                callableStatement.setNull(6, java.sql.Types.INTEGER);
                callableStatement.setNull(7, java.sql.Types.INTEGER);
                callableStatement.setNull(8, java.sql.Types.INTEGER);
            }
            callableStatement.setInt(4, quantity);
            callableStatement.execute();
        }, "Procedure 'addSaleLine' should exist");

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT * FROM dv01_saleline " +
                            " JOIN dv01_sale ON dv01_saleline.id_dv01_sale = dv01_sale.id " +
                            "WHERE orderNumber = '" + orderNumber + "' " +
                            "AND product = '" + product + "' " +
                            "AND quantity = " + quantity);
            callableStatement.execute();
            callableStatement.getResultSet().next();
            actualResultSet[0] = callableStatement.getResultSet();
            if (type.equals("Pizza")) {
                assertEquals(size[0], actualResultSet[0].getString("size"),
                             "Procedure 'addSaleLine' should add a sale line with the correct size");
                for (String option : options[0]) {
                    assertTrue(actualResultSet[0].getString("options").contains(String.valueOf(option)),
                               "Procedure 'addSaleLine' should add a sale line with the correct options");
                }

            } else {
                assertNull(actualResultSet[0].getObject("size"),
                           "Procedure 'addSaleLine' should add a sale line with the correct size");
                assertNull(actualResultSet[0].getObject("options"),
                           "Procedure 'addSaleLine' should add a sale line with the correct options");
            }
        }, "Procedure 'addSaleLine' should add a sale line");
    }

    private String getRandomProductByTypeTest(final String type) {
        final String[] product = {null};

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT produit.denomination FROM produit " +
                            "JOIN typeproduit ON produit.id_TypeProduit = typeproduit.id " +
                            "WHERE typeproduit.denomination = '" + type + "' ORDER BY RAND() LIMIT 1");
            callableStatement.execute();
            callableStatement.getResultSet().next();
            product[0] = callableStatement.getResultSet().getString("denomination");
        }, "Table 'product' should exist");

        return product[0];
    }

    private String getRandomSizeTest() {
        final String[] sizeId = {null};

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT denomination FROM typedetaille ORDER BY RAND() LIMIT 1");
            callableStatement.execute();
            callableStatement.getResultSet().next();
            sizeId[0] = callableStatement.getResultSet().getString("denomination");
        }, "Table 'typedetaille' should exist");

        return sizeId[0];
    }

    private String[] getRandomOptionsTest() {
        final String[] optionIds = {null, null, null, null};

        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = CreateSaleTest.connection.prepareCall(
                    "SELECT denomination FROM supplement ORDER BY RAND() LIMIT 4");
            callableStatement.execute();
            final ResultSet resultSet = callableStatement.getResultSet();
            for (int i = 0; i < 4; i++) {
                resultSet.next();
                optionIds[i] = resultSet.getString("denomination");
            }
        }, "Table 'supplement' should exist");

        return optionIds;
    }
}
