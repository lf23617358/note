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
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UnidirectionalManyToOneTest {

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
    public void testQuery() {
        List<PostComment> comments = entityManager.createQuery(
                "select pc " +
                        "from PostComment1 pc " +
                        "where pc.post.id = :postId", PostComment.class)
                .setParameter("postId", 1L)
                .getResultList();

    }

    @Test
    public void testPersist() {
        transactionTemplate.execute(status -> {
            Post post = new Post("First post");
            entityManager.persist(post);

            entityManager.persist(
                    new PostComment("My first review")
            );
            entityManager.persist(
                    new PostComment("My second review")
            );
            entityManager.persist(
                    new PostComment("My third review")
            );
            return post;
        });
    }


    protected Post createNewPost() {
        return transactionTemplate.execute(status -> {
            Post post = new Post("First post");
            entityManager.persist(post);

            entityManager.persist(
                    new PostComment("My first review")
            );
            entityManager.persist(
                    new PostComment("My second review")
            );
            entityManager.persist(
                    new PostComment("My third review")
            );
            return post;
        });
    }

    @Entity(name = "Post1")
    @Table(name = "post1")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

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

    }

    @Entity(name = "PostComment1")
    @Table(name = "post_comment1")
    public static class PostComment {

        @Id
        @GeneratedValue
        private Long id;

        private String review;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id")
        private Post post;

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

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

    }


}

