package com.ecom.giftcardwinner.controller;

import com.ecom.giftcardwinner.model.Winner;
import com.ecom.giftcardwinner.repository.WinnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WinnerController.class)
class WinnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WinnerRepository winnerRepository;

    @Test
    void testGetLatestWinner() throws Exception {
        // Arrange
        Winner winner = new Winner();
        winner.setUserId(1L);
        winner.setName("Leanne Graham");
        winner.setAmount(55.73);
        winner.setCreatedAt(LocalDateTime.now());
        when(winnerRepository.findTopByOrderByCreatedAtDesc()).thenReturn(winner);

        // Act & Assert
        mockMvc.perform(get("/api/winner/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Leanne Graham"))
                .andExpect(jsonPath("$.amount").value(55.73));
    }

    @Test
    void testGetAllWinners() throws Exception {
        // Arrange
        Winner winner1 = new Winner();
        winner1.setUserId(1L);
        winner1.setName("Leanne Graham");
        winner1.setAmount(55.73);
        winner1.setCreatedAt(LocalDateTime.now());
        Winner winner2 = new Winner();
        winner2.setUserId(2L);
        winner2.setName("Ervin Howell");
        winner2.setAmount(31.13);
        winner2.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(winnerRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(winner1, winner2));

        // Act & Assert
        mockMvc.perform(get("/api/winner/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[1].userId").value(2));
    }
}