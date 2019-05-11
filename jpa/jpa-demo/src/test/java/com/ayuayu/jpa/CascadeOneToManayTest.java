package com.ayuayu.jpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeOneToManayTest {

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    private PostRepository postRepository;

//    @Autowired
//    private CommentRepository commentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Query query = entityManager.createNativeQuery("delete from comment");
                query.executeUpdate();
                query = entityManager.createNativeQuery("delete from post");
                query.executeUpdate();
            }
        });
    }


    @Test
    public void testPersist() {
        transactionTemplate.execute(status -> {
            Post post = new Post();
            post.setName("Post Name");

            Comment comment1 = new Comment();
            comment1.setReview("Good post!");
            Comment comment2 = new Comment();
            comment2.setReview("Nice post!");

            post.addComment(comment1);
            post.addComment(comment2);

            entityManager.persist(post);
            return post;
        });
    }

    @Test
    public void testMerge() {
        Post post = createNewPost();
        post.setName("The New Post Name");

        post.getComments()
                .stream()
                .filter(comment -> comment.getReview().toLowerCase()
                        .contains("nice"))
                .findAny()
                .ifPresent(comment ->
                        comment.setReview("Keep up the good work!")
                );
        transactionTemplate.execute(status -> {
            entityManager.merge(post);
            return post;
        });
    }

    @Test
    public void testRemove() {
        Post post = createNewPost();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                entityManager.remove(entityManager.merge(post));
            }
        });
    }

    @Test
    public void testOrphanRemoval() {
        Long id = createNewPost().getId();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Post post = entityManager.find(Post.class, id);
                post.removeComment(post.getComments().get(0));
            }
        });

    }

    protected Post createNewPost() {
        return transactionTemplate.execute(status -> {
            Post post = new Post();
            post.setName("Post Name");

            Comment comment1 = new Comment();
            comment1.setReview("Good post!");
            Comment comment2 = new Comment();
            comment2.setReview("Nice post!");

            post.addComment(comment1);
            post.addComment(comment2);

            entityManager.persist(post);
            return post;
        });
    }


    @Entity
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @OneToMany(cascade = CascadeType.ALL,
                mappedBy = "post", orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }

        public void removeComment(Comment comment) {
            comment.setPost(null);
            this.comments.remove(comment);
        }
    }

    @Entity
    @Table(name = "comment")
    public static class Comment {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Post post;

        private String review;

        public void setPost(Post post) {
            this.post = post;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }

}
