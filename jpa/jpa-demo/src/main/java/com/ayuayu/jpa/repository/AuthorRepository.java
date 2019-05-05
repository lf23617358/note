package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Author;
import com.ayuayu.jpa.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface AuthorRepository extends JpaRepository<Author, Integer> {
}