package com.jad.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetSaleLineByIdTest {
    static final int NUMBER_OF_ID_TEST = 10;
    private static Connection connection;

    @BeforeAll
    public static void setUp() {
        DBConnection.INSTANCE.connect();
        GetSaleLineByIdTest.connection = DBConnection.INSTANCE.getConnection();
    }

    @Test
    public void doAllTests() {
        for (int i = 0; i < GetSaleLineByIdTest.NUMBER_OF_ID_TEST; i++) {
            this.getSaleLineByIdTest();
        }
    }

    public void getSaleLineByIdTest() {
        final ResultSet[] expectedSale = new ResultSet[1];
        final ResultSet[] procedureSale = new ResultSet[1];
        assertDoesNotThrow(() -> {
            final CallableStatement countStatement = GetSaleLineByIdTest.connection.prepareCall(
                    """
                    SELECT lignedevente.id AS id,
                           produit.denomination AS product, \
                           typedetaille.denomination AS size, \
                           lignedevente.quantite AS quantity, \
                           lignedevente.prix AS unitPrice, \
                           GROUP_CONCAT(supplement.denomination SEPARATOR ',') AS options, \
                           lignedevente.id_Vente AS id_dv01_sale \
                    FROM lignedevente \
                    INNER JOIN produit ON lignedevente.id_Produit = produit.id \
                    LEFT OUTER JOIN typedetaille ON lignedevente.id_TypeDeTaille = typedetaille.id \
                    LEFT OUTER JOIN lignedoption ON lignedevente.id = lignedoption.id_LigneDeVente \
                    LEFT OUTER JOIN supplement ON lignedoption.id_Supplement = supplement.id \
                    GROUP BY lignedevente.id, produit.denomination,
                             typedetaille.denomination, lignedevente.quantite,
                             lignedevente.prix, lignedevente.id_Vente \
                    ORDER BY RAND() LIMIT 1""");
            countStatement.execute();
            countStatement.getResultSet().next();
            expectedSale[0] = countStatement.getResultSet();
        }, "Table 'vente' should exist");
        assertDoesNotThrow(() -> {
            final CallableStatement callableStatement = GetSaleLineByIdTest.connection.prepareCall(
                    "{CALL getSaleLineById(?)}");
            callableStatement.setInt(1, expectedSale[0].getInt("id"));
            callableStatement.execute();
            callableStatement.getResultSet().next();
            final ResultSet resultSet = callableStatement.getResultSet();
            procedureSale[0] = resultSet;
        }, "Procedure 'getSaleById' should exist");
        assertDoesNotThrow(() -> {
            assertEquals(expectedSale[0].getInt("id"), procedureSale[0].getInt("id"),
                         "Procedure 'getSaleById' should return the sale with the same id");
            assertEquals(expectedSale[0].getString("product"), procedureSale[0].getString("product"),
                         "Procedure 'getSaleById' should return the sale with the same product");
            assertEquals(expectedSale[0].getString("size"), procedureSale[0].getString("size"),
                         "Procedure 'getSaleById' should return the sale with the same size");
            assertEquals(expectedSale[0].getInt("quantity"), procedureSale[0].getInt("quantity"),
                         "Procedure 'getSaleById' should return the sale with the same quantity");
            assertEquals(expectedSale[0].getDouble("unitPrice"), procedureSale[0].getDouble("unitPrice"),
                         "Procedure 'getSaleById' should return the sale with the same unitPrice");
            assertEquals(expectedSale[0].getString("options"), procedureSale[0].getString("options"),
                         "Procedure 'getSaleById' should return the sale with the same options");
            assertEquals(expectedSale[0].getInt("id_dv01_sale"), procedureSale[0].getInt("id_dv01_sale"),
                         "Procedure 'getSaleById' should return the sale with the same id_dv01_sale");
        }, "Procedure 'getSaleById' should return the sale with the same id");
    }
}
