package com.ayuayu.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BranchMerge {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
 
    private String fromBranch;
 
    private String toBranch;
 
    public BranchMerge() {
    }
 
    public BranchMerge(
        String fromBranch, String toBranch) {
        this.fromBranch = fromBranch;
        this.toBranch = toBranch;
    }
 
    public Long getId() {
        return id;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public void setFromBranch(String fromBranch) {
        this.fromBranch = fromBranch;
    }

    public String getToBranch() {
        return toBranch;
    }

    public void setToBranch(String toBranch) {
        this.toBranch = toBranch;
    }
}