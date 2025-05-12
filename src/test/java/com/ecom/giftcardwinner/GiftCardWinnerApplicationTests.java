package com.ecom.giftcardwinner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GiftCardWinnerApplicationTest {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context, "Application context should not be null");
    }

    @Test
    void schedulingIsEnabled() {
        assertTrue(GiftCardWinnerApplication.class.isAnnotationPresent(EnableScheduling.class),
                "GiftCardWinnerApplication should be annotated with @EnableScheduling");
    }

    @Test
    void mainMethodRunsWithoutException() {
        GiftCardWinnerApplication.main(new String[]{});
    }
}