package com.magento.grpctest.server.model.storage.data;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="prods")
public class Product {
    @Id
    private UUID id;

    private String sku;

    private Float price;

    private String title;

    private String description;

    private String imgUrl;

    private Boolean available;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "product", fetch = FetchType.EAGER)
    private Set<Option> options;

    public Product() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Set<Option> getOptions() {
        return options;
    }

    public void setOptions(Set<Option> options) {
        this.options = options;
    }
}
