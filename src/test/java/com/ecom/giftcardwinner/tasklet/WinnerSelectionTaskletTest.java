package com.ecom.giftcardwinner.tasklet;

import com.ecom.giftcardwinner.dto.EligibleUser;
import com.ecom.giftcardwinner.model.User;
import com.ecom.giftcardwinner.model.Winner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WinnerSelectionTaskletTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<EligibleUser> query;

    @Mock
    private StepContribution contribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    @InjectMocks
    private WinnerSelectionTasklet tasklet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tasklet, "minimumSpend", 100.0);
    }

    @Test
    void testUserFoundSuccessfullySelectsWinner() {
        EligibleUser eligibleUser = new EligibleUser(1L, 150.0);
        List<EligibleUser> eligibleUsers = List.of(eligibleUser);
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        when(entityManager.createQuery(any(String.class), eq(EligibleUser.class))).thenReturn(query);
        when(query.setParameter(eq("min"), eq(100.0))).thenReturn(query);
        when(query.getResultList()).thenReturn(eligibleUsers);
        when(entityManager.find(eq(User.class), eq(1L))).thenReturn(user);

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, result);
        verify(entityManager, times(1)).persist(any(Winner.class));
        verify(entityManager).createQuery(any(String.class), eq(EligibleUser.class));
        verify(query).setParameter(eq("min"), eq(100.0));
        verify(query).getResultList();
        verify(entityManager).find(eq(User.class), eq(1L));
    }

    @Test
    void testNoEligibleUserReturnsFinished() {

        when(entityManager.createQuery(any(String.class), eq(EligibleUser.class))).thenReturn(query);
        when(query.setParameter(eq("min"), eq(100.0))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, result);
        verify(entityManager, never()).persist(any());
        verify(entityManager, never()).find(any(), any());
        verify(query).getResultList();
    }

    @Test
    void testEligibleUserFoundButUserNotFound() {

        EligibleUser eligibleUser = new EligibleUser(1L, 150.0);
        List<EligibleUser> eligibleUsers = List.of(eligibleUser);

        when(entityManager.createQuery(any(String.class), eq(EligibleUser.class))).thenReturn(query);
        when(query.setParameter(eq("min"), eq(100.0))).thenReturn(query);
        when(query.getResultList()).thenReturn(eligibleUsers);
        when(entityManager.find(eq(User.class), eq(1L))).thenReturn(null);

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, result);
        verify(entityManager, never()).persist(any());
        verify(entityManager).find(eq(User.class), eq(1L));
        verify(query).getResultList();
    }

    @Test
    void testWinnerPersistedWithCorrectDetails() {

        EligibleUser eligibleUser = new EligibleUser(1L, 150.0);
        List<EligibleUser> eligibleUsers = List.of(eligibleUser);
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        when(entityManager.createQuery(any(String.class), eq(EligibleUser.class))).thenReturn(query);
        when(query.setParameter(eq("min"), eq(100.0))).thenReturn(query);
        when(query.getResultList()).thenReturn(eligibleUsers);
        when(entityManager.find(eq(User.class), eq(1L))).thenReturn(user);

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, result);
        verify(entityManager).persist(argThat(winner -> {
            Winner w = (Winner) winner;
            return w.getUserId().equals(1L) && w.getName().equals("John Doe") && w.getAmount().equals(150.0) && w.getCreatedAt() != null && w.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1));
        }));
        verify(query).getResultList();
        verify(entityManager).find(eq(User.class), eq(1L));
    }
}