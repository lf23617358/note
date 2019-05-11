package com.ayuayu.jpa;

//import com.ayuayu.jpa.repository.PostDetailsRepository;
//import com.ayuayu.jpa.repository.PostRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UnidirectionalOneToManyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
//        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//            @Override
//            protected void doInTransactionWithoutResult(TransactionStatus status) {
//                postDetailsRepository.deleteAllInBatch();
//                postRepository.deleteAllInBatch();
//            }
//        });

    }

    @Test
    public void testPersist() {
        transactionTemplate.execute(status -> {
            Post post = new Post("First post");

            post.getComments().add(
                    new PostComment("My first review")
            );
            post.getComments().add(
                    new PostComment("My second review")
            );
            post.getComments().add(
                    new PostComment("My third review")
            );
            entityManager.persist(post);
            return post;
        });
    }


    @Test
    public void testRemove() {
        createNewPost();
        transactionTemplate.execute(status -> {
            Post post = entityManager.find(Post.class, 1l);
            post.getComments().remove(0);
            return post;
        });
    }

    protected Post createNewPost() {
        return transactionTemplate.execute(status -> {
            Post post = new Post("First post");

            post.getComments().add(
                    new PostComment("My first review")
            );
            post.getComments().add(
                    new PostComment("My second review")
            );
            post.getComments().add(
                    new PostComment("My third review")
            );
            entityManager.persist(post);
            return post;
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @OneToMany(
                cascade = CascadeType.ALL,
                orphanRemoval = true
        )
        @JoinColumn(name = "post_id")
        private List<PostComment> comments = new ArrayList<>();

        public Post() {
        }

        public Post(String title) {
            this.title = title;
        }


        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<PostComment> getComments() {
            return comments;
        }

        public void addComment(PostComment comment) {
            this.comments.add(comment);
        }
    }

    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment {

        @Id
        @GeneratedValue
        private Long id;

        private String review;

        public PostComment() {
        }

        public PostComment(String review) {
            this.review = review;
        }

        public Long getId() {
            return id;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }

    }


}

