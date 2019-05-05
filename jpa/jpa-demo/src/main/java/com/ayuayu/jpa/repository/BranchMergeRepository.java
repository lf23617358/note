package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.BranchMerge;
import com.ayuayu.jpa.entity.PostDetails;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface BranchMergeRepository extends JpaRepository<BranchMerge, Integer> {
}