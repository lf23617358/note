package com.ayuayu.jpa.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PostDetails {

    @Id
    private Long id;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    private boolean visible;

    @OneToOne
    @MapsId
    private Post post;

    public Long getId() {
        return id;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
