package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Item;
import com.ayuayu.jpa.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface PostRepository extends JpaRepository<Post, Integer> {
}