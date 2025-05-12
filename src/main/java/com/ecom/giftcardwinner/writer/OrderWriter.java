package com.ecom.giftcardwinner.writer;

import com.ecom.giftcardwinner.model.Order;
import org.springframework.batch.item.database.JpaItemWriter;
import jakarta.persistence.EntityManagerFactory;

public class OrderWriter extends JpaItemWriter<Order> {
    public OrderWriter(EntityManagerFactory entityManagerFactory) {
        setEntityManagerFactory(entityManagerFactory);
    }
}