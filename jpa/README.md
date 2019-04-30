# JPA Cascade
## CascadeType.PERSIST
* 在新增DB紀錄時被關聯的物件也會一併新增
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
* 在刪除DB紀錄時被關聯的物件也會一併刪除
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
* 一對一關係不在此限
