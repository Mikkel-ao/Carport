package app.entities;

public class ProductDescription {
    int descriptionId;
    String keyWord;
    String description;
    int productId;

    public ProductDescription(int descriptionId, String keyWord, String description, int productId) {
        this.descriptionId = descriptionId;
        this.keyWord = keyWord;
        this.description = description;
        this.productId = productId;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(int descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "ProductDescription{" +
                "descriptionId=" + descriptionId +
                ", keyWord='" + keyWord + '\'' +
                ", description='" + description + '\'' +
                ", productId=" + productId +
                '}';
    }
}