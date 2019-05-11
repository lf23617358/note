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
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeOneToOneTest {

    @PersistenceContext
    private EntityManager entityManager;

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
                Query query = entityManager.createNativeQuery("delete from post_details");
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

            PostDetails details = new PostDetails();

            post.addDetails(details);

            entityManager.persist(post);
            return post;
        });
    }

    @Test
    public void testMerge() {
        Post post = createNewPost();
        post.setName("The New Post Name");
        post.getDetails().setVisible(true);

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

    @Entity
    @Table(name = "post_details")
    public static class PostDetails {

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

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Entity
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @OneToOne(mappedBy = "post",
                cascade = CascadeType.ALL, orphanRemoval = true)
        private PostDetails details;

        public Long getId() {
            return id;
        }

        public PostDetails getDetails() {
            return details;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void addDetails(PostDetails details) {
            this.details = details;
            details.setPost(this);
        }

        public void removeDetails() {
            if (details != null) {
                details.setPost(null);
            }
            this.details = null;
        }

    }
}

