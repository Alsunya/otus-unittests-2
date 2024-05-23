package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Test
    public void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    public void testSourceNotFound() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, () -> accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10)));
        assertEquals("No source account", result.getLocalizedMessage());
    }


    @Test
    public void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
    }

    @Test
    public void testAddAccount() {
        Long expectedAgreementId = 1L;
        String expectedAccountNumber = "2";
        int expectedType = 0;
        BigDecimal expectedAmount = BigDecimal.ONE;

        Agreement agreement = new Agreement();
        agreement.setId(expectedAgreementId);

        Account expectedAccount = new Account();
        expectedAccount.setAgreementId(expectedAgreementId);
        expectedAccount.setNumber(expectedAccountNumber);
        expectedAccount.setType(expectedType);
        expectedAccount.setAmount(expectedAmount);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        when(accountDao.save(captor.capture())).thenReturn(expectedAccount);

        Account result = accountServiceImpl.addAccount(agreement, expectedAccountNumber, expectedType, expectedAmount);

        Account savedAccount = captor.getValue();
        assertEquals(expectedAgreementId, savedAccount.getAgreementId());
        assertEquals(expectedAccountNumber, savedAccount.getNumber());
        assertEquals(expectedType, savedAccount.getType());
        assertEquals(expectedAmount, savedAccount.getAmount());

        assertEquals(expectedAgreementId, result.getAgreementId());
        assertEquals(expectedAccountNumber, result.getNumber());
        assertEquals(expectedType, result.getType());
        assertEquals(expectedAmount, result.getAmount());
        assertEquals(expectedAgreementId, result.getAgreementId());
    }

    @Test
    public void testCharge() {
        Long testAccountId = 1L;
        BigDecimal testAmount = BigDecimal.ONE;

        Account testAccount = new Account();
        testAccount.setId(testAccountId);
        testAccount.setAmount(testAmount);

        when(accountDao.findById(any())).thenReturn(Optional.of(testAccount));

        assertTrue(accountServiceImpl.charge(testAccountId, testAmount));
    }
}
