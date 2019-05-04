# JPA

## JPA Life Cycle

![alt JPA生命週期](life-cycle.png "JPA生命週期")

## JPA Relationship

* 方向性
* 雙向關係應該總是更新他們之間的關聯， __Parent__ 應該有 `addChild(child)` and `removeChild(child)`方法，避免資料錯亂引發其他的問題

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

## Cascading best practices

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
    comments1_.review as review2_2_0_,
    postdetail2_.post_id as post_id3_5_1_,
    postdetail2_.created_on as created_1_5_1_,
    postdetail2_.visible as visible2_5_1_
from
    post post0_
left outer join
    comment comments1_
        on post0_.id=comments1_.post_id
left outer join
    post_details postdetail2_
        on post0_.id=postdetail2_.post_id
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
    post_id=1,
    review='Good post!'
where
    id=1
```

----
* CascadeType.PERSIST在Parent物件呼叫JPA的`persist()`方法時，Child物件也會一併呼叫`persist()`

### 執行下列測試，展示不同情境下不同結果

``` java
@RunWith(SpringRunner.class)
@SpringBootTest
public class CascadePersistTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;

    @After
    public void tearDown() {
        itemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    public void saveOrderTest() {
        Order order = new Order();
        order.setName("order1");

        Item item1 = new Item();
        item1.setName("item1_order1");
        item1.setOrder(order);

        Item item2 = new Item();
        item2.setName("item2_order1");
        item2.setOrder(order);

        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        order.setItems(items);

        orderRepository.save(order);
        Assert.assertEquals(1, orderRepository.count());
        Assert.assertEquals(2, itemRepository.count());
    }

    @Test
    public void saveItemTest() {
        Order order = new Order();
        order.setName("order1");

        Item item1 = new Item();
        item1.setName("item1_order1");
        item1.setOrder(order);

        Item item2 = new Item();
        item2.setName("item2_order1");
        item2.setOrder(order);

        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        order.setItems(items);

        itemRepository.saveAll(items);
        Assert.assertEquals(1, orderRepository.count());
        Assert.assertEquals(2, itemRepository.count());
    }
}
```

* __Case1__ _Order_, _Item_ 都不加`CascadeType.PERSIST`

1. Save Order  
只有 _Order_ 會被儲存
``` sql
insert into t_order (name, id) values (?, ?)
```
2. Save Item  
會拋出以下Exception，理由是因為 _Order_ 沒有被儲存相關的紀錄，_Item_ foreign key會對應不到
> org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : com.ayuayu.jpa.entity.Item.order -> com.ayuayu.jpa.entity.Order; nested exception is java.lang.IllegalStateException: org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : com.ayuayu.jpa.entity.Item.order -> com.ayuayu.jpa.entity.Order

* __Case2__ _Order_ 加`CascadeType.PERSIST`, _Item_ 不加
1. Save Order  
_Order_ 和 _Item_ 都會被儲存
``` sql
insert into t_order (name, id) values (?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
```
2. Save Item  
同樣會拋出以下Exception，理由同 Case1
> org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : com.ayuayu.jpa.entity.Item.order -> com.ayuayu.jpa.entity.Order; nested exception is java.lang.IllegalStateException: org.hibernate.TransientPropertyValueException: object references an unsaved transient instance - save the transient instance before flushing : com.ayuayu.jpa.entity.Item.order -> com.ayuayu.jpa.entity.Order

* __Case3__ _Order_ 不加, _Item_ 加`CascadeType.PERSIST`
1. Save Order  
只有 _Order_ 會被儲存
``` sql
insert into t_order (name, id) values (?, ?)
```
2. Save Item  
先儲存 _Order_ 再儲存 _Item_  
``` sql
insert into t_order (name, id) values (?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
```

* __Case4__ _Order_, _Item_ 都加`CascadeType.PERSIST`
1. Save Order  
先儲存 _Order_ 再儲存 _Item_
``` sql
insert into t_order (name, id) values (?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
insert into t_item (name, order_id, id) values (?, ?, ?)
```
2. Save Item  
同上

## CascadeType.REMOVE
* 在Parent物件呼叫JPA的`remove()`方法時，Child物件也會一併呼叫`remove()`
### 執行下列測試，展示不同情境下不同結果
``` java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CascadeRemoveTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private Order order;

    private List<Item> items;

    @Before
    public void setUp() {
        order = new Order();
        order.setName("order1");

        Item item1 = new Item();
        item1.setName("item1_order1");
        item1.setOrder(order);

        Item item2 = new Item();
        item2.setName("item2_order1");
        item2.setOrder(order);

        items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        order.setItems(items);

        entityManager.persist(order);
        for (Item item : items) {
            entityManager.persist(item);
        }
    }

    @After
    public void tearDown() {
        Query query = entityManager.createNativeQuery("delete from t_item");
        query.executeUpdate();
    }

    @Test
    public void removeOrderTest() {
        entityManager.remove(order);
        Assert.assertEquals(0, orderRepository.count());
        Assert.assertEquals(0, itemRepository.count());
    }

    @Test
    public void removeItemTest() {
        for (Item item : items) {
            entityManager.remove(item);
        }
        Assert.assertEquals(0, orderRepository.count());
        Assert.assertEquals(0, itemRepository.count());
    }
}
```
* __Case1__ _Order_, _Item_ 都不加`CascadeType.REMOVE`
1. Remove Order
``` sql
delete from t_order where id=?
```
會拋出以下Exception，原因是 _Item_ FOREIGN KEY限制，_Order_ 紀錄無法被刪除
> could not execute statement; SQL [n/a]; constraint ["FKTESK72NTB0EUBN30CXIDBYMP4: PUBLIC.T_ITEM FOREIGN KEY(ORDER_ID) REFERENCES PUBLIC.T_ORDER(ID) (4)"; SQL statement:
delete from t_order where id=? [23503-199]];
2. Remove Item  
只有 _Item_ 會被刪除
``` sql
delete from t_item where id=?
delete from t_item where id=?
```

* __Case2__ _Order_ 加`CascadeType.REMOVE`, _Item_ 不加
1. Remove Order 
先刪除 _Item_ 再刪除 _Order_  
``` sql
delete from t_item where id=?
delete from t_item where id=?
delete from t_order where id=?
```
2. Remove Item  
只有 _Item_ 會被刪除
``` sql
delete from t_item where id=?
delete from t_item where id=?
```

* __Case3__ _Order_ 不加, _Item_ 加`CascadeType.REMOVE`
1. Remove Order  
同樣會拋出以下Exception，理由同 Case1
> could not execute statement; SQL [n/a]; constraint ["FKTESK72NTB0EUBN30CXIDBYMP4: PUBLIC.T_ITEM FOREIGN KEY(ORDER_ID) REFERENCES PUBLIC.T_ORDER(ID) (4)"; SQL statement:
delete from t_order where id=? [23503-199]];
2. Remove Item  
先把 _Item_ 的FOREIGN KEY設成null，然後刪除 _Order_ 和 _Item_  
``` sql
update t_item set name=?, order_id=? where id=?
delete from t_item where id=?
delete from t_order where id=?
delete from t_item where id=?
```

* __Case4__ _Order_, _Item_ 都加`CascadeType.REMOVE`
1. Remove Order  
先刪除 _Item_ 再刪除 _Order_
``` sql
delete from t_item where id=?
delete from t_item where id=?
delete from t_order where id=?
```
2. Remove Item  
同上

### 結論
* 最好只用在一那邊，不要用在關係維護端(有FOREIGN KEY的Table)
* 不要兩邊都加，有可能會刪到你本來不想刪的紀錄
``` java
entityManager.remove(items.get(0));
```
則會產生下列SQL
``` sql
delete from t_item where id=?
delete from t_item where id=?
delete from t_order where id=?
```
另外一種情形是使用spring data的`deleteAll(T entity)`方法會產生下面sql
``` sql
insert into t_item (name, order_id, id) values (?, ?, ?)
delete from t_item where id=?
delete from t_item where id=?
delete from t_order where id=?
delete from t_item where id=?
```
不確定是不是bug?總之不要兩邊都加`CascadeType.REMOVE`是最安全的
* 一對一關係不在此限

## CascadeType.MERGE
* 在Parent物件呼叫JPA的`merge()`方法時，Child物件也會一併呼叫`merge()`
### 執行下列測試，展示不同情境下不同結果
``` java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CascadeMergeTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private Order order;

    private List<Item> items;

    @Before
    public void setUp() {
        order = new Order();
        order.setName("order1");

        Item item1 = new Item();
        item1.setName("item1_order1");
        item1.setOrder(order);

        Item item2 = new Item();
        item2.setName("item2_order1");
        item2.setOrder(order);

        items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        order.setItems(items);

        entityManager.persist(order);
        for (Item item : items) {
            entityManager.persist(item);
        }
    }

    @After
    public void tearDown() {
        Query query = entityManager.createNativeQuery("delete from t_item");
        query.executeUpdate();
    }

    @Test
    public void mergeOrderTest() {
        order.setName("new order1");
        entityManager.merge(order);
        Assert.assertEquals(0, orderRepository.count());
        Assert.assertEquals(0, itemRepository.count());
    }

    @Test
    public void mergeItemTest() {
        for (Item item : items) {
            item.setName("new " + item.getName());
            entityManager.merge(item);
        }
        Assert.assertEquals(0, orderRepository.count());
        Assert.assertEquals(0, itemRepository.count());
    }
}
```
* __Case1__ _Order_, _Item_ 都不加`CascadeType.MERGE`
1. Merger Order  
只有 _Order_ 會被更新
``` sql
update t_order set name=? where id=?
```
2. Merger Item
只有 _Item_ 會被更新
``` sql
update t_item set name=?, order_id=? where id=?
update t_item set name=?, order_id=? where id=?
```

* __Case2__ _Order_ 加`CascadeType.MERGE`, _Item_ 不加
1. Merger Order  
_Order_, _Item_ 都會被更新
``` sql
update t_item set name=?, order_id=? where id=?
update t_order set name=? where id=?
update t_item set name=?, order_id=? where id=?
```
2. Merger Item
只有 _Item_ 會被更新
``` sql
update t_item set name=?, order_id=? where id=?
update t_item set name=?, order_id=? where id=?
```

* __Case3__ _Order_ 不加, _Item_ 加`CascadeType.MERGE`
1. Merger Order  
_Order_, _Item_ 都會被更新
``` sql
update t_order set name=? where id=?
```
2. Merger Item
只有 _Item_ 會被更新
``` sql
update t_order set name=? where id=?
update t_item set name=?, order_id=? where id=?
update t_item set name=?, order_id=? where id=?
```

* __Case4__ _Order_, _Item_ 都加`CascadeType.MERGE`
1. Merger Order  
_Order_, _Item_ 都會被更新
``` sql
update t_item set name=?, order_id=? where id=?
update t_order set name=? where id=?
update t_item set name=?, order_id=? where id=?
```
2. Merger Item
只有 _Item_ 會被更新
``` sql
update t_order set name=? where id=?
update t_item set name=?, order_id=? where id=?
update t_item set name=?, order_id=? where id=?
```