package org.jom.Model;

public class DistributionModel {
    private int id;
    private String first_name;
    private String last_name;
    private int remaining;
    private String type;
    private String category;
    private int product;
    private String price;
    private int outlet;
    private int distributor;

    public DistributionModel(int id, String first_name, String last_name, int remaining) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.remaining = remaining;
    }

    public DistributionModel(int remaining, int product, String price, int outlet, int distributor) {
        this.remaining = remaining;
        this.product = product;
        this.price = price;
        this.outlet = outlet;
        this.distributor = distributor;
    }

    public DistributionModel(int id, int remaining, String type, String category, int product) {
        this.id = id;
        this.remaining = remaining;
        this.type = type;
        this.category = category;
        this.product = product;
    }

    public DistributionModel(int remaining, String type, String category, int product, String price) {
        this.remaining = remaining;
        this.type = type;
        this.category = category;
        this.product = product;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getOutlet() {
        return outlet;
    }

    public void setOutlet(int outlet) {
        this.outlet = outlet;
    }

    public int getDistributor() {
        return distributor;
    }

    public void setDistributor(int distributor) {
        this.distributor = distributor;
    }
}