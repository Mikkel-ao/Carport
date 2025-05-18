package app.entities;

public class Product {
    int productId;
    String name;
    String unit;
    double width;
    int pricePrUnit;

    //Overloading the constructor to fit our various needs!
    public Product(int productId, String name, String unit, int pricePrUnit) {
        this.productId = productId;
        this.name = name;
        this.unit = unit;
        this.pricePrUnit = pricePrUnit;
    }

    public Product(int productId, String name, String unit, double width, int pricePrUnit) {
        this.productId = productId;
        this.name = name;
        this.unit = unit;
        this.width = width;
        this.pricePrUnit = pricePrUnit;
    }

    public double getWidth() {
        return width;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductId() {
        return productId;
    }

    public String getUnit() {
        return unit;
    }

    public int getPricePrUnit() {
        return pricePrUnit;
    }


    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", width=" + width +
                ", pricePrUnit=" + pricePrUnit +
                '}';
    }
}