# JPA

## JPA Life Cycle

![alt JPA生命週期](life-cycle.png "JPA生命週期")

## JPA Relationship

* 方向性
* 雙向關係應該總是更新他們之間的關聯， __Parent__ 應該有 `addChild(child)` and `removeChild(child)`方法，避免資料錯亂引發其他的問題

### One-To-One Mapping

* 雙向的`@OneToOne`使用`FetchType.LAZY`父的那一邊是沒有效的，查詢資料時會下兩個查詢

* 對`@OneToOne`使用`@MapsId`(直接使用parmay key 當 foreign key)是比較好的

* 使用單向關聯就好

``` java
Post post = entityManager.find(Post.class, 1l);
```

``` sql
select
    onetoonete0_.id as id1_4_0_,
    onetoonete0_.title as title3_4_0_
from
    post onetoonete0_
where
    onetoonete0_.id=1

select
    onetoonete0_.post_id as post_id4_5_0_,
    onetoonete0_.created_by as created_3_5_0_,
    onetoonete0_.created_on as created_1_5_0_,
    onetoonete0_.visible as visible2_5_0_
from
    post_details onetoonete0_
where
    onetoonete0_.post_id=1
```

使用單向關聯時可以用 __Child__ (有寫`@OneToOne`的那邊)的那端儲存

``` java
Post post = entityManager.find(Post.class, 1L);
PostDetails details = new PostDetails("John Doe");
details.setPost(post);
entityManager.persist(details);
```

查詢的話用 __Child__ 那端查詢

``` java
PostDetails details = entityManager.find(
    PostDetails.class,
    post.getId()
);
```

### One-To-Many Mapping

#### 單向`@OneToMany`

可以看到使用單向`@OneToMany`會有太多不必要的查詢

``` java
Post post = new Post("First post");
post.getComments().add(
    new PostComment("My first review")
);
post.getComments().add(
    new PostComment("My second review")
);
post.getComments().add(
    new PostComment("My third review")
);
entityManager.persist(post);
```

``` sql
insert
into
    post
    (title, id)
values
    ('First post', 1)

insert
into
    post_comment
    (review, id)
values
    ('My first review', 2)

insert
into
    post_comment
    (review, id)
values
    ('My second review', 3)

insert
into
    post_comment
    (review, id)
values
    ('My third review', 4)

insert
into
    post_comments
    (post_id, comments_id)
values
    (1, 2)

insert
into
    post_comments
    (post_id, comments_id)
values
    (1, 3)

insert
into
    post_comments
    (post_id, comments_id)
values
    (1, 4)
```

加上`@JoinColumn`可以修正這個問題，但還是會有不必要的update

``` sql
insert
into
    post
    (title, id)
values
    ('First post', 1)

insert
into
    post_comment
    (review, id)
values
    ('My first review', 2)

insert
into
    post_comment
    (review, id)
values
    ('My second review', 3)

insert
into
    post_comment
    (review, id)
values
    ('My third review', 4)

update
    post_comment
set
    post_id=1
where
    id=2

update
    post_comment
set
    post_id=1
where
    id=3

update
    post_comment
set
    post_id=1
where
    id=4
```

移除也有類似的現象

``` java
post.getComments().remove(0);
```

``` sql
update
    post_comment
set
    post_id=null
where
    post_id=1
    and id=2

delete
from
    post_comment
where
    id=2
```

#### 雙向`@OneToMany`

* `@ManyToOne`使用`FetchType.LAZY`對效能比較好
* 雙向關聯的 __Parent__ 方使用utility方法(e.g. `addComment` and `removeComment`)去維護關連
* __Child__ 方最好總是實作`equals` and `hashCode`方法

```sql
insert
into
    post
    (title, id)
values
    ('First post', 1)

insert
into
    post_comment
    (post_id, review, id)
values
    (1, 'My first review', 2)

insert
into
    post_comment
    (post_id, review, id)
values
    (1, 'My second review', 3)

insert
into
    post_comment
    (post_id, review, id)
values
    (1, 'My third review', 4)
```

移除

``` sql
delete
from
    post_comment
where
    id=?
```

#### 單向`@ManyToOne`

``` java
List<PostComment> comments = entityManager.createQuery(
    "select pc " +
    "from PostComment pc " +
    "where pc.post.id = :postId", PostComment.class)
.setParameter( "postId", 1L )
.getResultList();
```

``` sql
select
    unidirecti0_.id as id1_7_,
    unidirecti0_.post_id as post_id3_7_,
    unidirecti0_.review as review2_7_
from
    post_comment unidirecti0_
where
    unidirecti0_.post_id=1
```

### Many-To-Many Mapping

---

## JPA Cascade

| JPA EntityManager action | JPA CascadeType |
|:---:|:---:|
| `detach(entity)` | DETACH |
| `merge(entity)` | MERGE |
| `persist(entity)` | PERSIST |
| `refresh(entity)` | REFRESH |
| `remove(entity)` | REMOVE |

* 最好只在 __Parent__ 方加上CascadeType，在 __Child__ 加同常不太有用，而且一些行為可能會跟你想的不同
* 有時候CascadeType即使不加但還是會有cascade的效果，是因為Hibernate有一些檢查的機制造成

### One-To-One

#### One-To-One PERSIST

``` sql
insert
into
    post
    (name, id)
values
    ('Post Name', 1)

insert
into
    post_details
    (created_on, visible, post_id)
values
    ('2019-05-01 10:17:19.000', false, 1)
```

#### One-To-One MERGE

``` sql
select
    post0_.id as id1_0_1_,
    post0_.name as name2_0_1_,
    postdetail1_.post_id as post_id3_1_0_,
    postdetail1_.created_on as created_1_1_0_,
    postdetail1_.visible as visible2_1_0_
from
    post post0_
left outer join
    post_details postdetail1_
        on post0_.id=postdetail1_.post_id
where
    post0_.id=1

update
    post_details
set
    created_on='2019-05-01 10:17:19.000',
    visible=true
where
    post_id=1

update
    post
set
    name='The New Post Name'
where
    id=1
```

#### One-To-One REMOVE

``` sql
delete
from
    post_details
where
    post_id=1

delete
from
    post
where
    id=1
```

#### One-To-One Orphan Removal

``` sql
delete
from
    post_details
where
    post_id=1
```

### One-To-Many

* 只在一對多的這邊加，多對一不加Casecade

#### One-To-Many PERSIST

``` sql
insert
into
    post
    (name, id)
values
    ('Post Name', 1)

insert
into
    comment
    (post_id, review, id)
values
    (1, 'Good post!', 1)

insert
into
    comment
    (post_id, review, id)
values
    (1, 'Nice post!', 2)
```

#### One-To-Many MERGE

``` sql
select
    post0_.id as id1_4_2_,
    post0_.name as name2_4_2_,
    comments1_.post_id as post_id3_2_4_,
    comments1_.id as id1_2_4_,
    comments1_.id as id1_2_0_,
    comments1_.post_id as post_id3_2_0_,
    comments1_.review as review2_2_0_
from
    post post0_
left outer join
    comment comments1_
        on post0_.id=comments1_.post_id
where
    post0_.id=1

update
    post
set
    name='Post Name'
where
    id=1

update
    comment
set
    post_id=1,
    review='Keep up the good work!'
where
    id=2
```

#### One-To-Many REMOVE

``` sql
delete
from
    comment
where
    id=1

delete
from
    comment
where
    id=2

delete
from
    post
where
    id=1
```

#### One-To-Many Orphan Removal

``` sql
delete
from
    comment
where
    id=1
```

如果沒有加上`orphanRemoval = true`，那執行同樣測試會把Child的 _FOREIGN KEY_ 設成 _null_ ，如下SQL所示

``` sql
update
    comment
set
    post_id=null,
    review='Good post!'
where
    id=2
```

### Many-To-Many

* 不要用`CascadeType.ALL`
* `CascadeType.REMOVE`可能會刪掉過多的資料

#### Many-To-Many PERSIST

``` sql
insert
into
    author
    (full_name, id)
values
    ('John Smith', 1)

insert
into
    book
    (title, id)
values
    ('Day Dreaming', 2)

insert
into
    author
    (full_name, id)
values
    ('Michelle Diangello', 3)

insert
into
    book
    (title, id)
values
    ('Day Dreaming, Second Edition', 4)

insert
into
    author
    (full_name, id)
values
    ('Mark Armstrong', 5)

insert
into
    book_author
    (book_id, author_id)
values
    (2, 1)

insert
into
    book_author
    (book_id, author_id)
values
    (2, 3)

insert
into
    book_author
    (book_id, author_id)
values
    (4, 1)

insert
into
    book_author
    (book_id, author_id)
values
    (4, 3)

insert
into
    book_author
    (book_id, author_id)
values
    (4, 5)
```

#### Many-To-Many MERGE

``` sql
select
    author0_.id as id1_0_1_,
    author0_.full_name as full_nam2_0_1_,
    books1_.author_id as author_i2_2_3_,
    book2_.id as book_id1_2_3_,
    book2_.id as id1_1_0_,
    book2_.title as title2_1_0_
from
    author author0_
left outer join
    book_author books1_
        on author0_.id=books1_.author_id
left outer join
    book book2_
        on books1_.book_id=book2_.id
where
    author0_.id=1

select
    author0_.id as id1_0_1_,
    author0_.full_name as full_nam2_0_1_,
    books1_.author_id as author_i2_2_3_,
    book2_.id as book_id1_2_3_,
    book2_.id as id1_1_0_,
    book2_.title as title2_1_0_
from
    author author0_
left outer join
    book_author books1_
        on author0_.id=books1_.author_id
left outer join
    book book2_
        on books1_.book_id=book2_.id
where
    author0_.id=3

select
    author0_.id as id1_0_1_,
    author0_.full_name as full_nam2_0_1_,
    books1_.author_id as author_i2_2_3_,
    book2_.id as book_id1_2_3_,
    book2_.id as id1_1_0_,
    book2_.title as title2_1_0_
from
    author author0_
left outer join
    book_author books1_
        on author0_.id=books1_.author_id
left outer join
    book book2_
        on books1_.book_id=book2_.id
where
    author0_.id=5

select
    authors0_.book_id as book_id1_2_0_,
    authors0_.author_id as author_i2_2_0_,
    author1_.id as id1_0_1_,
    author1_.full_name as full_nam2_0_1_
from
    book_author authors0_
inner join
    author author1_
        on authors0_.author_id=author1_.id
where
    authors0_.book_id=4

select
    authors0_.book_id as book_id1_2_0_,
    authors0_.author_id as author_i2_2_0_,
    author1_.id as id1_0_1_,
    author1_.full_name as full_nam2_0_1_
from
    book_author authors0_
inner join
    author author1_
        on authors0_.author_id=author1_.id
where
    authors0_.book_id=2

update
    book
set
    title='Day Dreaming, Third Edition'
where
    id=4

update
    author
set
    full_name='New Mark Armstrong'
where
    id=3
```

#### Many-To-Many REMOVE

從下面sql可以看到他可能刪掉你預期沒有要刪掉的資料

``` sql
delete
from
    book_author
where
    book_id=4

delete
from
    book
where
    id=4

delete
from
    author
where
    id=5
```

現在來看雙向`CascadeType.REMOVE`

``` sql
select
    authors0_.book_id as book_id1_2_0_,
    authors0_.author_id as author_i2_2_0_,
    author1_.id as id1_0_1_,
    author1_.full_name as full_nam2_0_1_
from
    book_author authors0_
inner join
    author author1_
        on authors0_.author_id=author1_.id
where
    authors0_.book_id=4

select
    books0_.author_id as author_i2_2_0_,
    books0_.book_id as book_id1_2_0_,
    book1_.id as id1_1_1_,
    book1_.title as title2_1_1_
from
    book_author books0_
inner join
    book book1_
        on books0_.book_id=book1_.id
where
    books0_.author_id=1

select
    authors0_.book_id as book_id1_2_0_,
    authors0_.author_id as author_i2_2_0_,
    author1_.id as id1_0_1_,
    author1_.full_name as full_nam2_0_1_
from
    book_author authors0_
inner join
    author author1_
        on authors0_.author_id=author1_.id
where
    authors0_.book_id=2

select
    books0_.author_id as author_i2_2_0_,
    books0_.book_id as book_id1_2_0_,
    book1_.id as id1_1_1_,
    book1_.title as title2_1_1_
from
    book_author books0_
inner join
    book book1_
        on books0_.book_id=book1_.id
where
    books0_.author_id=3

delete
from
    book_author
where
    book_id=4

delete
from
    book_author
where
    book_id=2

delete
from
    author
where
    id=3

delete
from
    book
where
    id=2

delete
from
    author
where
    id=1

delete
from
    book
where
    id=4

delete
from
    author
where
    id=5
```

正確的做法，但也不建議

``` sql
select
    books0_.author_id as author_i2_2_0_,
    books0_.book_id as book_id1_2_0_,
    book1_.id as id1_1_1_,
    book1_.title as title2_1_1_
from
    book_author books0_
inner join
    book book1_
        on books0_.book_id=book1_.id
where
    books0_.author_id=5

select
    authors0_.book_id as book_id1_2_0_,
    authors0_.author_id as author_i2_2_0_,
    author1_.id as id1_0_1_,
    author1_.full_name as full_nam2_0_1_
from
    book_author authors0_
inner join
    author author1_
        on authors0_.author_id=author1_.id
where
    authors0_.book_id=4

delete
from
    book_author
where
    book_id=4

insert
into
    book_author
    (book_id, author_id)
values
    (4, 1)

insert
into
    book_author
    (book_id, author_id)
values
    (4, 3)

delete
from
    author
where
    id=5
```