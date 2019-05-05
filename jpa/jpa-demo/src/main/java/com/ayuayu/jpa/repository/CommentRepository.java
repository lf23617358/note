package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.Comment;
import com.ayuayu.jpa.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {
}