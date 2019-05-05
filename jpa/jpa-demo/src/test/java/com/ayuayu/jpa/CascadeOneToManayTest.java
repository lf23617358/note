package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Comment;
import com.ayuayu.jpa.entity.Post;
import com.ayuayu.jpa.repository.CommentRepository;
import com.ayuayu.jpa.repository.PostRepository;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeOneToManayTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

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
                commentRepository.deleteAllInBatch();
                postRepository.deleteAllInBatch();
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


}
