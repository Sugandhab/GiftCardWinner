package com.ecom.giftcardwinner.reader;

import com.ecom.giftcardwinner.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApiReaderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserApiReader reader;

    private final String apiUrl = "https://jsonplaceholder.typicode.com/users";

    @BeforeEach
    void setUp() {
        setField(reader, "apiUrl", apiUrl);
        setField(reader, "maxRetries", 3);
        setField(reader, "initialBackoffMs", 1000L);
        setField(reader, "restTemplate", restTemplate);
        setField(reader, "userIterator", null);
        reset(restTemplate);
    }

    @Test
    void testReadSuccessfulFetch() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Leanne Graham");
        user1.setEmail("Sincere@april.biz");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Ervin Howell");
        user2.setEmail("Shanna@melissa.tv");

        User[] users = {user1, user2};
        ResponseEntity<User[]> response = new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(apiUrl), eq(User[].class))).thenReturn(response);

        User result1 = reader.read();
        assertNotNull(result1);
        assertEquals(1L, result1.getId());
        assertEquals("Leanne Graham", result1.getName());
        assertEquals("Sincere@april.biz", result1.getEmail());

        User result2 = reader.read();
        assertNotNull(result2);
        assertEquals(2L, result2.getId());
        assertEquals("Ervin Howell", result2.getName());
        assertEquals("Shanna@melissa.tv", result2.getEmail());

        assertNull(reader.read(), "No more users should be returned");
        verify(restTemplate, times(1)).getForEntity(eq(apiUrl), eq(User[].class));
    }

    @Test
    void testReadWithRetrySuccessOnSecondAttempt() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Leanne Graham");
        user.setEmail("Sincere@april.biz");

        User[] users = {user};
        ResponseEntity<User[]> response = new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(apiUrl), eq(User[].class)))
                .thenThrow(new RestClientException("Connection failed"))
                .thenReturn(response);

        User result = reader.read();
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Leanne Graham", result.getName());
        assertEquals("Sincere@april.biz", result.getEmail());

        assertNull(reader.read());
        verify(restTemplate, times(2)).getForEntity(eq(apiUrl), eq(User[].class));
    }

    @Test
    void testReadWithAllRetriesFailed() throws Exception {
        when(restTemplate.getForEntity(eq(apiUrl), eq(User[].class)))
                .thenThrow(new RestClientException("Connection failed"));

        User user = reader.read();
        assertNull(user, "No users should be returned after all retries fail");
        verify(restTemplate, times(3)).getForEntity(eq(apiUrl), eq(User[].class));
    }

    @Test
    void testReadWithNullResponseBody() throws Exception {
        ResponseEntity<User[]> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(apiUrl), eq(User[].class))).thenReturn(response);

        User user = reader.read();
        assertNull(user, "No users should be returned for null response body");
        verify(restTemplate, times(3)).getForEntity(eq(apiUrl), eq(User[].class));
    }

    @Test
    void testReadWithInterruptedRetry() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Leanne Graham");
        user.setEmail("Sincere@april.biz");

        User[] users = {user};
        ResponseEntity<User[]> response = new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(apiUrl), eq(User[].class)))
                .thenThrow(new RestClientException("Connection failed"))
                .thenReturn(response);
        Thread.currentThread().interrupt();

        User result = reader.read();
        assertNull(result, "No users should be returned after interruption");

        verify(restTemplate, times(1)).getForEntity(eq(apiUrl), eq(User[].class));
        assertTrue(Thread.currentThread().isInterrupted(), "Interrupt status should be preserved");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}