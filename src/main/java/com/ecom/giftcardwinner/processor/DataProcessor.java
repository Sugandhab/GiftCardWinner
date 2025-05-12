// Validates and filters incoming User or Order data before writing.
package com.ecom.giftcardwinner.processor;

import com.ecom.giftcardwinner.model.Order;
import com.ecom.giftcardwinner.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class DataProcessor<T> implements ItemProcessor<T, T> {

    private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

    @Override
    public T process(T item) {
        if (item == null) {
            log.warn("Skipping null item.");
            return null;
        }
        if (item instanceof User user) {
            return validateUser(user);
        } else if (item instanceof Order order) {
            return validateOrder(order);
        } else {
            log.warn("Unknown item type: {}. Skipping.", item.getClass().getName());
            return null;
        }
    }

    private T validateUser(User user) {
        if (user.getId() == null) {
            log.warn("Skipping user: missing ID");
            return null;
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Skipping user ID {}: missing or blank name", user.getId());
            return null;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Skipping user ID {}: missing or blank email", user.getId());
            return null;
        }

        return (T) user;
    }

    private T validateOrder(Order order) {
        if (order.getUserId() == null) {
            log.warn("Skipping order: missing user ID");
            return null;
        }
        if (order.getAmount() == null || order.getAmount() <= 0) {
            log.warn("Skipping order for user {}: invalid amount {}", order.getUserId(), order.getAmount());
            return null;
        }
        return (T) order;
    }
}
