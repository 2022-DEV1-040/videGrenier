package be.icc.entity;

import be.icc.dto.ProductDto;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Scohier Dorian on 04-11-18.
 */
@Entity
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private User seller;
    @ManyToOne
    private Category category;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private double price;
    @Column
    private String picture;
    @Column
    private boolean isAuction;
    @Column
    private boolean isSell;
    @Column
    private Date endDate;
    @ManyToMany(mappedBy = "auctionedProduct")
    private Set<User> bidders;

    public Product() {
    }
    public ProductDto toDto(){
        ProductDto product = new ProductDto();
        product.setCategory(category.toDto());
        for(User user : getBidders()) {
            product.getBidders().add(user.toDto());
        }
        product.setSeller(this.getSeller().toDto());
        product.setPicture(this.getPicture());
        product.setName(this.getName());
        product.setDescription(this.getDescription());
        product.setPrice(this.getPrice());
        product.setAuction(this.isAuction());
        product.setSell(this.isSell());
        product.setEndDate(this.getEndDate());
        return product;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAuction() {
        return isAuction;
    }

    public void setAuction(boolean auction) {
        isAuction = auction;
    }

    public boolean isSell() {
        return isSell;
    }

    public void setSell(boolean sell) {
        isSell = sell;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<User> getBidders() {
        if (bidders == null) {
            bidders = new HashSet<>();
        }
        return bidders;
    }

    public void setBidders(Set<User> bidders) {
        this.bidders = bidders;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }
}
