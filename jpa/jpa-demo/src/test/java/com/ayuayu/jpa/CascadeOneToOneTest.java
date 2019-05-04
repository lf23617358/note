package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Post;
import com.ayuayu.jpa.entity.PostDetails;
import com.ayuayu.jpa.repository.PostDetailsRepository;
import com.ayuayu.jpa.repository.PostRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class CascadeOneToOneTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostDetailsRepository postDetailsRepository;

    @After
    public void tearDown() {
//        postDetailsRepository.deleteAllInBatch();
//        postRepository.deleteAllInBatch();
    }


    @Test
    @Commit
    public void testPersist() {
        Post post = new Post();
        post.setName("Post Name");

        PostDetails details = new PostDetails();

        post.addDetails(details);

        entityManager.persist(post);

    }

}
