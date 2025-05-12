// Provides database access methods for querying winner records.
package com.ecom.giftcardwinner.repository;

import com.ecom.giftcardwinner.model.Winner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WinnerRepository extends JpaRepository<Winner, Long> {
    Winner findTopByOrderByCreatedAtDesc();
    List<Winner> findAllByOrderByCreatedAtDesc();
}