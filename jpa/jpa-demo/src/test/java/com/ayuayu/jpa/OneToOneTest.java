package com.ayuayu.jpa;

//import com.ayuayu.jpa.repository.PostDetailsRepository;
//import com.ayuayu.jpa.repository.PostRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.*;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OneToOneTest {

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
    public void testChildQuery() {
        createNewPost();
        System.out.println("start");
//            details.getPost();
        PostDetails postDetails = transactionTemplate.execute(status -> {
            PostDetails tmpPostDetails = entityManager.find(PostDetails.class, 1l);
//            System.out.println("get detail");
            return tmpPostDetails;
        });
        //        System.out.println(post.getName());
    }

    @Test
    public void testParentQuery() {
        createNewPost();
        System.out.println("start");
//            details.getPost();
        Post post = transactionTemplate.execute(status -> {
            Post tmpPost = entityManager.find(Post.class, 1l);
//            System.out.println("get detail");
            return tmpPost;
        });
        //        System.out.println(post.getName());
    }

    protected Post createNewPost() {
        return transactionTemplate.execute(status -> {
            Post post = new Post();
            post.setTitle("New Title");

            PostDetails details = new PostDetails();
//            details.setPost(post);

            post.addDetails(details);
            entityManager.persist(post);
//            entityManager.flush();
            return post;
        });
    }

    @Entity
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL,
                fetch = FetchType.LAZY, optional = false)
        private PostDetails details;

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public PostDetails getDetails() {
            return details;
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

    @Entity
    @Table(name = "post_details")
    public static class PostDetails {

        @Id
        @GeneratedValue
        private Long id;

        @Column(name = "created_on")
        private Date createdOn;

        @Column(name = "created_by")
        private String createdBy;

        private boolean visible;

        @OneToOne(fetch = FetchType.LAZY)
        @MapsId
        private Post post;

        public PostDetails() {
        }

        public PostDetails(String createdBy) {
            createdOn = new Date();
            this.createdBy = createdBy;
        }

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


}

