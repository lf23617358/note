package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Author;
import com.ayuayu.jpa.entity.Book;
import com.ayuayu.jpa.repository.AuthorRepository;
import com.ayuayu.jpa.repository.BookRepository;
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
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeManyToManayTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

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
//                authorRepository.deleteAllInBatch();
//                bookRepository.deleteAllInBatch();
//            }
//        });
    }


    @Test
    public void testPersist() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author _John_Smith = new Author("John Smith");
                Author _Michelle_Diangello =
                        new Author("Michelle Diangello");
                Author _Mark_Armstrong =
                        new Author("Mark Armstrong");

                Book _Day_Dreaming = new Book("Day Dreaming");
                Book _Day_Dreaming_2nd =
                        new Book("Day Dreaming, Second Edition");

                _John_Smith.addBook(_Day_Dreaming);
                _Michelle_Diangello.addBook(_Day_Dreaming);

                _John_Smith.addBook(_Day_Dreaming_2nd);
                _Michelle_Diangello.addBook(_Day_Dreaming_2nd);
                _Mark_Armstrong.addBook(_Day_Dreaming_2nd);

                entityManager.persist(_John_Smith);
                entityManager.persist(_Michelle_Diangello);
                entityManager.persist(_Mark_Armstrong);
            }
        });
    }

    @Test
    public void testMerge() {
        List<Author> authors = createNewAuthors();
        authors.get(1).setFullName("New Mark Armstrong");
        authors.get(0).getBooks().get(1).setTitle("Day Dreaming, Third Edition");

        transactionTemplate.execute(status -> {
            entityManager.merge(authors.get(0));
            entityManager.merge(authors.get(1));
            return authors;
        });
    }

    @Test
    public void testParentRemove() {
        createNewAuthors();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author _Mark_Armstrong = entityManager.find(Author.class, 5l);
                entityManager.remove(_Mark_Armstrong);
                entityManager.flush();
                Author _John_Smith = entityManager.find(Author.class, 1l);
                assertEquals(1, _John_Smith.getBooks().size());
            }
        });
    }

    @Test
    public void testBothRemove() {
        createNewAuthors();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author _Mark_Armstrong = entityManager.find(Author.class, 5l);
                entityManager.remove(_Mark_Armstrong);
                entityManager.flush();
                Author _John_Smith = entityManager.find(Author.class, 1l);
                assertNull(_John_Smith);
            }
        });
    }

    @Test
    public void testDissociatingRemove() {
        createNewAuthors();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author _Mark_Armstrong = entityManager.find(Author.class, 5l);
                System.out.println("begin removeAllBooks");
                _Mark_Armstrong.removeAllBooks();
                System.out.println("after removeAllBooks");
                entityManager.remove(_Mark_Armstrong);
            }
        });
    }
//
//    @Test
//    public void testOrphanRemoval() {
//        Long id = createNewPost().getId();
//        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//            @Override
//            protected void doInTransactionWithoutResult(TransactionStatus status) {
//                Post post = entityManager.find(Post.class, id);
//                post.removeComment(post.getComments().get(0));
//            }
//        });
//
//    }

    protected List<Author> createNewAuthors() {
        return transactionTemplate.execute(status -> {
            Author _John_Smith = new Author("John Smith");
            Author _Michelle_Diangello =
                    new Author("Michelle Diangello");
            Author _Mark_Armstrong =
                    new Author("Mark Armstrong");

            Book _Day_Dreaming = new Book("Day Dreaming");
            Book _Day_Dreaming_2nd =
                    new Book("Day Dreaming, Second Edition");

            _John_Smith.addBook(_Day_Dreaming);
            _Michelle_Diangello.addBook(_Day_Dreaming);

            _John_Smith.addBook(_Day_Dreaming_2nd);
            _Michelle_Diangello.addBook(_Day_Dreaming_2nd);
            _Mark_Armstrong.addBook(_Day_Dreaming_2nd);

            entityManager.persist(_John_Smith);
            entityManager.persist(_Michelle_Diangello);
            entityManager.persist(_Mark_Armstrong);

            List<Author> authors = new ArrayList<>();
            authors.add(_John_Smith);
            authors.add(_Michelle_Diangello);
            authors.add(_Mark_Armstrong);
            return authors;
        });
    }


}
