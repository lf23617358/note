package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Post;
import com.ayuayu.jpa.entity.PostDetails;
import com.ayuayu.jpa.repository.PostDetailsRepository;
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
public class CascadeOneToOneTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostDetailsRepository postDetailsRepository;

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
                postDetailsRepository.deleteAllInBatch();
                postRepository.deleteAllInBatch();
            }
        });
    }


    @Test
    public void testPersist() {
        transactionTemplate.execute(status -> {
            Post post = new Post();
            post.setName("Post Name");

            PostDetails details = new PostDetails();

            post.addDetails(details);

            entityManager.persist(post);
            return post;
        });
    }

    @Test
    public void testMerge() {
        Post post = createNewPost();
        transactionTemplate.execute(status -> {
            post.setName("The New Post Name");
            post.getDetails().setVisible(true);

            entityManager.merge(post);

            return post;
        });
    }

    @Test
    public void testRemove() {
        Long id = createNewPost().getId();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Post post = entityManager.find(Post.class, id);
                entityManager.remove(post);
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
                System.out.println();
                post.removeDetails();
            }
        });

    }

    protected Post createNewPost() {
        return transactionTemplate.execute(status -> {
            Post post = new Post();
            post.setName("Post Name");

            PostDetails details = new PostDetails();

            post.addDetails(details);
            entityManager.persist(post);
            return post;
        });
    }


}
