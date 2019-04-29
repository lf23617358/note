package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Order Repository
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {
}