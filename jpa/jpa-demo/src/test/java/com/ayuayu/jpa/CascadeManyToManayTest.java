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
public class CascadeManyToManayTest {

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
    public void testPersist() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Author _John_Smith = new Author(1l,"John Smith");
                Author _Michelle_Diangello =
                        new Author(3l,"Michelle Diangello");
                Author _Mark_Armstrong =
                        new Author(5l,"Mark Armstrong");

                Book _Day_Dreaming = new Book(2l,"Day Dreaming");
                Book _Day_Dreaming_2nd =
                        new Book(4l,"Day Dreaming, Second Edition");

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
            Author _John_Smith = new Author(1l,"John Smith");
            Author _Michelle_Diangello =
                    new Author(3l,"Michelle Diangello");
            Author _Mark_Armstrong =
                    new Author(5l,"Mark Armstrong");

            Book _Day_Dreaming = new Book(2l,"Day Dreaming");
            Book _Day_Dreaming_2nd =
                    new Book(4l,"Day Dreaming, Second Edition");

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
        private Long id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @ManyToMany(mappedBy = "authors",
                cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        private List<Book> books = new ArrayList<>();

        private Author() {
        }

        public Author(Long id, String fullName) {
            this.id = id;
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
        private Long id;

        @Column(name = "title", nullable = false)
        private String title;

        @ManyToMany(cascade =
                {CascadeType.PERSIST, CascadeType.MERGE})
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

        public Book(Long id, String title) {
            this.id = id;
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
