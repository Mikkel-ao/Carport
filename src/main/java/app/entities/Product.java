package app.entities;

public class Product {
    int productId;
    String name;
    String unit;
    int pricePrUnit;


    public Product(int productId, String name, String unit, int pricePrUnit) {
        this.productId = productId;
        this.name = name;
        this.unit = unit;
        this.pricePrUnit = pricePrUnit;
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

    public int getPricePrUnit() {
        return pricePrUnit;
    }

    public void setPricePrUnit(int pricePrUnit) {
        this.pricePrUnit = pricePrUnit;
    }



    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + pricePrUnit +
                '}';
    }
}