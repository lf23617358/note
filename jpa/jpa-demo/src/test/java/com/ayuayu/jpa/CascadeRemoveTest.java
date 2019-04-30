package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Item;
import com.ayuayu.jpa.entity.Order;
import com.ayuayu.jpa.repository.ItemRepository;
import com.ayuayu.jpa.repository.OrderRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

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
//        entityManager.flush();
//        entityManager.clear();
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
//        entityManager.remove(items.get(0));
//        itemRepository.deleteAll(items);
        Assert.assertEquals(0, orderRepository.count());
        Assert.assertEquals(0, itemRepository.count());
    }
}
