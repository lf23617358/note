package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface BookRepository extends JpaRepository<Book, Integer> {
}