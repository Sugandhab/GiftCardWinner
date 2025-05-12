// Reads user data from a remote API with retry and fault tolerance.
package com.ecom.giftcardwinner.reader;

import com.ecom.giftcardwinner.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
public class UserApiReader implements ItemReader<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserApiReader.class);
    private final RestTemplate restTemplate  = new RestTemplate();;
    private Iterator<User> userIterator = null;;

    @Value("${user.api.url}")
    private String apiUrl;

    @Value("${user.api.max-retries:3}")
    private int maxRetries;

    @Value("${user.api.initial-backoff-ms:1000}")
    private long initialBackoffMs;

    @Override
    public User read() {
        if (userIterator == null) {
            List<User> users = fetchUsersWithRetry();
            this.userIterator = users.iterator();
        }
        return userIterator.hasNext() ? userIterator.next() : null;
    }

    private List<User> fetchUsersWithRetry() {
        int attempt = 0;
        long backoff = initialBackoffMs;

        while (attempt < maxRetries) {
            try {
                logger.info("Attempting to fetch users (Attempt {}/{})", attempt + 1, maxRetries);
                ResponseEntity<User[]> response = restTemplate.getForEntity(apiUrl, User[].class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<User> users = Arrays.asList(response.getBody());
                    logger.info("Fetched {} users successfully.", users.size());
                    return users;
                } else {
                    logger.warn("API call failed. Status: {}", response.getStatusCode());
                }

            } catch (RestClientException e) {
                logger.warn("Error on attempt {}: {}", attempt + 1, e.getMessage());
            }

            attempt++;
            if (attempt < maxRetries) {
                try {
                    logger.info("Retrying in {} ms...", backoff);
                    Thread.sleep(backoff);
                    backoff *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Retry interrupted. Stopping further retries.");
                    break;
                }
            }
        }
        logger.error("All {} attempts to fetch users failed.", maxRetries);
        return Collections.emptyList();
    }
}
