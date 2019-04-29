package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface ItemRepository extends JpaRepository<Item, Integer> {
}