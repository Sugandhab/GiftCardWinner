package com.ecom.giftcardwinner.controller;

import com.ecom.giftcardwinner.model.Winner;
import com.ecom.giftcardwinner.repository.WinnerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/winner")
public class WinnerController {

    private final WinnerRepository winnerRepository;

    public WinnerController(WinnerRepository winnerRepository) {
        this.winnerRepository = winnerRepository;
    }

    @GetMapping("/latest")
    public Winner getLatestWinner() {
        return winnerRepository.findTopByOrderByCreatedAtDesc();
    }

    @GetMapping("/all")
    public List<Winner> getAllWinners() {
        return winnerRepository.findAllByOrderByCreatedAtDesc();
    }
}
