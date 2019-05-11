//package com.ayuayu.jpa.entity;
//
//import javax.persistence.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//public class Post {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    private String name;
//
//    @OneToOne(mappedBy = "post",
//            cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
//    private PostDetails details;
//
//    @OneToMany(cascade = CascadeType.ALL,
//            mappedBy = "post",orphanRemoval = true)
//    private List<Comment> comments = new ArrayList<>();
//
//    public Long getId() {
//        return id;
//    }
//
//    public PostDetails getDetails() {
//        return details;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void addDetails(PostDetails details) {
//        this.details = details;
//        details.setPost(this);
//    }
//
//    public void removeDetails() {
//        if (details != null) {
//            details.setPost(null);
//        }
//        this.details = null;
//    }
//
//    public List<Comment> getComments() {
//        return comments;
//    }
//
//    public void addComment(Comment comment) {
//        comments.add(comment);
//        comment.setPost(this);
//    }
//
//    public void removeComment(Comment comment) {
//        comment.setPost(null);
//        this.comments.remove(comment);
//    }
//}