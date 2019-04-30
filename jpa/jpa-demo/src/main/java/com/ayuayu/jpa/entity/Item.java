package com.ayuayu.jpa.entity;

import javax.persistence.*;

@Entity
@Table(name = "t_item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column
    private String name;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Order.class, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "order_id")
    private Order order;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}