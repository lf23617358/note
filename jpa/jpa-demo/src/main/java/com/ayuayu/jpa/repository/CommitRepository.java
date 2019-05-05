package com.ayuayu.jpa.repository;

import com.ayuayu.jpa.entity.BranchMerge;
import com.ayuayu.jpa.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Item Repository
 */
public interface CommitRepository extends JpaRepository<Commit, Integer> {
}