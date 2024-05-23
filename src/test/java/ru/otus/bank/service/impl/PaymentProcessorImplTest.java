package ru.otus.bank.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.AccountService;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorImplTest {
    Agreement sourceAgreement;
    Agreement destinationAgreement;
    Account sourceAccount;
    Account destinationAccount;

    @Mock
    AccountService accountService;

    @InjectMocks
    PaymentProcessorImpl paymentProcessor;

    @BeforeEach
    public void setUp() {
        sourceAgreement = new Agreement();
        destinationAgreement = new Agreement();
        sourceAccount = new Account();
        destinationAccount = new Account();

        sourceAgreement.setId(1L);
        destinationAgreement.setId(2L);
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(0);
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(0);
    }

    @Test
    public void testTransfer() {
        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId() == 1L))).thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId() == 2L))).thenReturn(List.of(destinationAccount));

        paymentProcessor.makeTransfer(sourceAgreement, destinationAgreement,
                0, 0, BigDecimal.ONE);

    }

    @Test
    public void testTransferWithCommission() {
        when(accountService.getAccounts(
                argThat(argument -> argument != null && argument.getId() == 1L)))
                .thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(
                argThat(argument -> argument != null && argument.getId() == 2L))).
                thenReturn(List.of(destinationAccount));

        paymentProcessor.makeTransferWithComission(sourceAgreement, destinationAgreement,
                0, 0, BigDecimal.TEN, BigDecimal.ONE);

    }

    @Test
    public void testTransferNoDestinationAccount() {
        when(accountService.getAccounts(
                argThat(argument -> argument != null && argument.getId() == 1L))).thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(
                argThat(argument -> argument != null && argument.getId() == 2L))).thenReturn(List.of());

        assertThrows(AccountException.class, () -> paymentProcessor.makeTransfer(
                sourceAgreement, destinationAgreement, 0, 0, BigDecimal.ONE));
    }

    @Test
    public void testTransferNoSourceAccount() {
        when(accountService.getAccounts(
                argThat(argument -> argument != null && argument.getId() == 1L))).thenReturn(List.of());

        assertThrows(AccountException.class, () -> paymentProcessor.makeTransfer(
                sourceAgreement, destinationAgreement, 0, 0, BigDecimal.ONE));
    }

    @Test
    public void testTransferWithCommissionAndNoSourceAccount() {
        when(accountService.getAccounts(argThat(
                argument -> argument != null && argument.getId() == 1L))).thenReturn(List.of());

        assertThrows(AccountException.class, () -> paymentProcessor.makeTransferWithComission(
                sourceAgreement, destinationAgreement, 0, 0, BigDecimal.ONE, BigDecimal.TEN));
    }

    @Test
    public void testTransferWithCommissionAndNoDestinationAccount() {
        when(accountService.getAccounts(argThat(
                argument -> argument != null && argument.getId() == 1L))).thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(argThat(
                argument -> argument != null && argument.getId() == 2L))).thenReturn(List.of());

        assertThrows(AccountException.class,
                () -> paymentProcessor.makeTransferWithComission(
                        sourceAgreement, destinationAgreement,
                        0, 0, BigDecimal.ONE, BigDecimal.TEN));
    }
}
