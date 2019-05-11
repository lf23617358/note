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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadeManyToManayBothRemoveTestback {

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
                Query query = entityManager.createNativeQuery("delete from book_author");
                query.executeUpdate();
                query = entityManager.createNativeQuery("delete from book");
                query.executeUpdate();
                query = entityManager.createNativeQuery("delete from author");
                query.executeUpdate();
            }
        });
    }

    @Test
    public void testRemove() {
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

    @Entity
    @Table(name = "author")
    public static class Author {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @ManyToMany(mappedBy = "authors",
                cascade = CascadeType.ALL)
        private List<Book> books = new ArrayList<>();

        private Author() {
        }

        public Author(String fullName) {
            this.fullName = fullName;
        }

        public Long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public List<Book> getBooks() {
            return books;
        }

        public void addBook(Book book) {
            books.add(book);
            book.getAuthors().add(this);
        }

        public void removeBook(Book book) {
            books.remove(book);
            book.getAuthors().remove(this);
        }

        public void removeAllBooks() {
            for (Book book : new ArrayList<>(books)) {
                removeBook(book);
            }
        }
    }

    @Entity
    @Table(name = "book")
    public static class Book {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column(name = "title", nullable = false)
        private String title;

        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "Book_Author",
                joinColumns = {
                        @JoinColumn(
                                name = "book_id",
                                referencedColumnName = "id"
                        )
                },
                inverseJoinColumns = {
                        @JoinColumn(
                                name = "author_id",
                                referencedColumnName = "id"
                        )
                }
        )
        private List<Author> authors = new ArrayList<>();

        private Book() {
        }

        public Book(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Author> getAuthors() {
            return authors;
        }
    }


}
