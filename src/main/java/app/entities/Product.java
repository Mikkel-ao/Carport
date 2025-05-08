package app.entities;

public class Product {
    int productId;
    String name;
    String unit;
    int price;
    int widthInCm;

    public Product(int productId, String name, String unit, int price, int widthInCm) {
        this.productId = productId;
        this.name = name;
        this.unit = unit;
        this.price = price;
        this.widthInCm = widthInCm;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                '}';
    }
}