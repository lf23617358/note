package com.ayuayu.jpa;

import com.ayuayu.jpa.entity.Item;
import com.ayuayu.jpa.entity.Order;
import com.ayuayu.jpa.repository.ItemRepository;
import com.ayuayu.jpa.repository.OrderRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

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
