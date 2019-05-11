//package com.ayuayu.jpa.entity;
//
//import javax.persistence.*;
//
//@Entity
//public class Commit {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    private String comment;
//
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinTable(
//        name = "Branch_Merge_Commit",
//        joinColumns = @JoinColumn(
//            name = "commit_id",
//            referencedColumnName = "id"),
//        inverseJoinColumns = @JoinColumn(
//            name = "branch_merge_id",
//            referencedColumnName = "id")
//    )
//    private BranchMerge branchMerge;
//
//    public Commit() {
//    }
//
//    public Commit(String comment) {
//        this.comment = comment;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getComment() {
//        return comment;
//    }
//
//    public void setComment(String comment) {
//        this.comment = comment;
//    }
//
//    public BranchMerge getBranchMerge() {
//        return branchMerge;
//    }
//
//    public void setBranchMerge(BranchMerge branchMerge) {
//        this.branchMerge = branchMerge;
//    }
//
//    public void addBranchMerge(
//        String fromBranch, String toBranch) {
//        this.branchMerge = new BranchMerge(
//             fromBranch, toBranch);
//    }
//
//    public void removeBranchMerge() {
//        this.branchMerge = null;
//    }
//}
