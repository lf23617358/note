# JPA Cascade
## CascadeType.PERSIST
* 在新增DB紀錄時被關聯的物件也會一併新增
### 執行下列測試，展示不同情境下不同結果
``` java
@RunWith(SpringRunner.class)
@SpringBootTest
//@DataJpaTest
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



