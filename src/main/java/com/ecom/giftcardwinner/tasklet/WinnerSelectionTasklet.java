package com.ecom.giftcardwinner.tasklet;

import com.ecom.giftcardwinner.dto.EligibleUser;
import com.ecom.giftcardwinner.model.User;
import com.ecom.giftcardwinner.model.Winner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class WinnerSelectionTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(WinnerSelectionTasklet.class);

   // @PersistenceContext
    private final EntityManager entityManager;

    @Value("${batch.winner.eligibility.amount}")
    private double minimumSpend;

    @Autowired
    public WinnerSelectionTasklet(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        //Find all user IDs where total spend is above the configured threshold
        TypedQuery<EligibleUser> query = entityManager.createQuery("""
                    SELECT o.userId, SUM(o.amount)
                    FROM Order o
                    GROUP BY o.userId
                    HAVING SUM(o.amount) > :min
                """, EligibleUser.class);
        query.setParameter("min", minimumSpend);

        List<EligibleUser> results = query.getResultList();

        if (results.isEmpty()) {
            logger.info("No eligible users found with total spend above {}", minimumSpend);
            return RepeatStatus.FINISHED;
        }

        // Pick one at random
        EligibleUser picked = results.get(new Random().nextInt(results.size()));
        Long userId = picked.userId();
        Double total = picked.totalAmount();

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            logger.warn("User with ID {} not found. Skipping winner selection.", userId);
            return RepeatStatus.FINISHED;
        }

        // Optional: Skip if this user has already won
//        long existingWinners = entityManager.createQuery("""
//            SELECT COUNT(w) FROM Winner w WHERE w.userId = :userId
//        """, Long.class)
//                .setParameter("userId", userId)
//                .getSingleResult();
//
//        if (existingWinners > 0) {
//            logger.info("User {} has already been selected as a winner. Skipping.", userId);
//            return RepeatStatus.FINISHED;
//        }

        //Save winner
        Winner winner = new Winner();
        winner.setUserId(userId);
        winner.setName(user.getName());
        winner.setAmount(total);
        winner.setCreatedAt(LocalDateTime.now());

        entityManager.persist(winner);
        logger.info("Winner selected: {} (userId={}, amount={})", user.getName(), userId, total);

        return RepeatStatus.FINISHED;
    }
}
