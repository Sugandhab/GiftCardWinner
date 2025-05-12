// Persists User entities to the database using JPA.
package com.ecom.giftcardwinner.writer;

import com.ecom.giftcardwinner.model.User;
import org.springframework.batch.item.database.JpaItemWriter;
import jakarta.persistence.EntityManagerFactory;

public class UserWriter extends JpaItemWriter<User> {
    public UserWriter(EntityManagerFactory entityManagerFactory) {
        setEntityManagerFactory(entityManagerFactory);
    }
}