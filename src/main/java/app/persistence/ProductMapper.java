package app.persistence;

import app.entities.Product;
import app.entities.ProductVariant;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper {

    public static ProductVariant getVariantsByProductAndLength(int productLength, int productId, String keyword, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "SELECT * FROM product_variant\n" +
                "                INNER JOIN product p USING (product_id)\n" +
                "INNER JOIN product_description USING (product_id)\n" +
                "                WHERE product_id = ? AND length = ? AND key_word = ?";

        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, productId);
            ps.setInt(2, productLength);
            ps.setString(3, keyword);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int variantId = rs.getInt("product_variant_id");
                int product_id = rs.getInt("product_id");
                int length = rs.getInt("length");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");
                String description = rs.getString("description");

                Product product = new Product(product_id, name, unit, price);
                ProductVariant productVariant = new ProductVariant(variantId, length, description, product);
                return productVariant;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl, kunne ikke få fat på users fra database", e.getMessage());
        }
        return null;
    }

    public static Product getProductByProductId(int productId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM product WHERE product_id = ?";

        try (Connection connection = connectionPool.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, productId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");


                Product product = new Product(productId, name, unit, price);
                return product;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl, kunne ikke få fat på users fra database", e.getMessage());
        }
        return null;
    }

}
