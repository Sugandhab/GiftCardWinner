package com.ecom.giftcardwinner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GiftCardWinnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiftCardWinnerApplication.class, args);
    }

}
