package com.ecommerce.order.services;

import com.ecommerce.order.dtos.*;
import com.ecommerce.order.models.*;
import com.ecommerce.order.models.SellerTransaction.TransactionStatus;
import com.ecommerce.order.models.SellerTransaction.TransactionType;
import com.ecommerce.order.models.SellerPayout.PayoutStatus;
import com.ecommerce.order.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerFinancialService {

    private final SellerTransactionRepository transactionRepository;
    private final SellerPayoutRepository payoutRepository;
    private final SellerBankAccountRepository bankAccountRepository;
    private final OrderRepository orderRepository;

    // Configuration
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal PAYMENT_GATEWAY_FEE_RATE = new BigDecimal("0.025"); // 2.5%
    private static final int HOLD_PERIOD_DAYS = 14;

    // Financial Overview
    public SellerFinancialOverview getFinancialOverview(String sellerId) {
        BigDecimal availableBalance = transactionRepository.getAvailableBalance(sellerId);
        BigDecimal pendingBalance = transactionRepository.getPendingBalance(sellerId);
        BigDecimal totalPaid = payoutRepository.getTotalPaidToSeller(sellerId);
        BigDecimal pendingPayout = payoutRepository.getPendingPayoutAmount(sellerId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyRevenue = transactionRepository.getTotalSalesByDateRange(sellerId, startOfMonth, now);

        return new SellerFinancialOverview(
                availableBalance != null ? availableBalance : BigDecimal.ZERO,
                pendingBalance != null ? pendingBalance : BigDecimal.ZERO,
                totalPaid != null ? totalPaid : BigDecimal.ZERO,
                pendingPayout != null ? pendingPayout : BigDecimal.ZERO,
                monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO
        );
    }

    // Transactions
    @Transactional
    public SellerTransactionResponse createSaleTransaction(String sellerId, Order order) {
        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal platformFee = orderTotal.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gatewayFee = orderTotal.multiply(PAYMENT_GATEWAY_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = orderTotal.subtract(platformFee).subtract(gatewayFee);

        SellerTransaction transaction = new SellerTransaction();
        transaction.setSellerId(sellerId);
        transaction.setOrder(order);
        transaction.setType(TransactionType.SALE);
        transaction.setAmount(orderTotal);
        transaction.setPlatformFee(platformFee);
        transaction.setPaymentGatewayFee(gatewayFee);
        transaction.setNetAmount(netAmount);
        transaction.setDescription("Sale from Order #" + order.getId());
        transaction.setStatus(TransactionStatus.HOLD); // Initial status is HOLD

        SellerTransaction saved = transactionRepository.save(transaction);
        return mapToTransactionResponse(saved);
    }

    public Page<SellerTransactionResponse> getSellerTransactions(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return transactionRepository.findBySellerId(sellerId, pageable).map(this::mapToTransactionResponse);
    }

    public List<SellerTransactionResponse> getTransactionsByType(String sellerId, TransactionType type) {
        return transactionRepository.findBySellerIdAndType(sellerId, type)
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    // Release transactions from hold after hold period
    @Transactional
    public void releaseHeldTransactions() {
        LocalDateTime holdEndDate = LocalDateTime.now().minusDays(HOLD_PERIOD_DAYS);
        List<SellerTransaction> transactionsToRelease = transactionRepository.findTransactionsReadyForRelease(holdEndDate);

        for (SellerTransaction transaction : transactionsToRelease) {
            transaction.setStatus(TransactionStatus.AVAILABLE);
            transactionRepository.save(transaction);
        }
    }

    // Payouts
    public Page<SellerPayoutResponse> getSellerPayouts(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return payoutRepository.findBySellerId(sellerId, pageable).map(this::mapToPayoutResponse);
    }

    public Optional<SellerPayoutResponse> getPayoutById(Long id) {
        return payoutRepository.findById(id).map(this::mapToPayoutResponse);
    }

    @Transactional
    public Optional<SellerPayoutResponse> requestPayout(String sellerId) {
        BigDecimal availableBalance = transactionRepository.getAvailableBalance(sellerId);
        if (availableBalance == null || availableBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        // Get primary bank account
        SellerBankAccount bankAccount = bankAccountRepository.findBySellerIdAndIsPrimaryTrue(sellerId)
                .orElseThrow(() -> new IllegalStateException("No primary bank account configured"));

        // Get available transactions
        List<SellerTransaction> availableTransactions = transactionRepository.findAvailableForPayout(sellerId);

        BigDecimal platformFees = availableTransactions.stream()
                .map(SellerTransaction::getPlatformFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gatewayFees = availableTransactions.stream()
                .map(SellerTransaction::getPaymentGatewayFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create payout
        SellerPayout payout = new SellerPayout();
        payout.setSellerId(sellerId);
        payout.setAmount(availableBalance.add(platformFees).add(gatewayFees));
        payout.setPlatformFee(platformFees);
        payout.setPaymentGatewayFee(gatewayFees);
        payout.setNetAmount(availableBalance);
        payout.setOrdersCount(availableTransactions.size());
        payout.setBankAccountName(bankAccount.getAccountHolderName());
        payout.setBankAccountNumber(bankAccount.getAccountNumber());
        payout.setBankName(bankAccount.getBankName());
        payout.setStatus(PayoutStatus.PENDING);

        SellerPayout saved = payoutRepository.save(payout);

        // Mark transactions as settled
        for (SellerTransaction transaction : availableTransactions) {
            transaction.setIsSettled(true);
            transaction.setSettledAt(LocalDateTime.now());
            transaction.setPayout(saved);
            transaction.setStatus(TransactionStatus.SETTLED);
            transactionRepository.save(transaction);
        }

        return Optional.of(mapToPayoutResponse(saved));
    }

    // Bank Account Management
    public List<SellerBankAccountResponse> getBankAccounts(String sellerId) {
        return bankAccountRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToBankAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SellerBankAccountResponse addBankAccount(String sellerId, BankAccountRequest request) {
        // If this is the first account or marked as primary, update existing primary
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            bankAccountRepository.findBySellerIdAndIsPrimaryTrue(sellerId)
                    .ifPresent(existing -> {
                        existing.setIsPrimary(false);
                        bankAccountRepository.save(existing);
                    });
        }

        SellerBankAccount account = new SellerBankAccount();
        account.setSellerId(sellerId);
        account.setBankName(request.getBankName());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setAccountNumber(request.getAccountNumber());
        account.setRoutingNumber(request.getRoutingNumber());
        account.setSwiftCode(request.getSwiftCode());
        account.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);

        // First account is automatically primary
        if (bankAccountRepository.findBySellerId(sellerId).isEmpty()) {
            account.setIsPrimary(true);
        }

        SellerBankAccount saved = bankAccountRepository.save(account);
        return mapToBankAccountResponse(saved);
    }

    @Transactional
    public Optional<SellerBankAccountResponse> setPrimaryBankAccount(String sellerId, Long accountId) {
        return bankAccountRepository.findById(accountId)
                .filter(account -> account.getSellerId().equals(sellerId))
                .map(account -> {
                    // Unset current primary
                    bankAccountRepository.findBySellerIdAndIsPrimaryTrue(sellerId)
                            .ifPresent(existing -> {
                                existing.setIsPrimary(false);
                                bankAccountRepository.save(existing);
                            });
                    
                    account.setIsPrimary(true);
                    return mapToBankAccountResponse(bankAccountRepository.save(account));
                });
    }

    @Transactional
    public boolean deleteBankAccount(String sellerId, Long accountId) {
        return bankAccountRepository.findById(accountId)
                .filter(account -> account.getSellerId().equals(sellerId))
                .filter(account -> !account.getIsPrimary()) // Can't delete primary account
                .map(account -> {
                    bankAccountRepository.delete(account);
                    return true;
                })
                .orElse(false);
    }

    // Revenue Reports
    public RevenueReportResponse getRevenueReport(String sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalSales = transactionRepository.getTotalSalesByDateRange(sellerId, startDate, endDate);
        
        List<SellerTransaction> transactions = transactionRepository.findBySellerId(sellerId)
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        BigDecimal totalPlatformFees = transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .map(SellerTransaction::getPlatformFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGatewayFees = transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .map(SellerTransaction::getPaymentGatewayFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunds = transactions.stream()
                .filter(t -> t.getType() == TransactionType.REFUND)
                .map(SellerTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = (totalSales != null ? totalSales : BigDecimal.ZERO)
                .subtract(totalPlatformFees)
                .subtract(totalGatewayFees)
                .subtract(totalRefunds);

        int ordersCount = (int) transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .count();

        return new RevenueReportResponse(
                totalSales != null ? totalSales : BigDecimal.ZERO,
                totalPlatformFees,
                totalGatewayFees,
                totalRefunds,
                netRevenue,
                ordersCount,
                startDate,
                endDate
        );
    }

    // Mapping methods
    private SellerTransactionResponse mapToTransactionResponse(SellerTransaction transaction) {
        SellerTransactionResponse response = new SellerTransactionResponse();
        response.setId(transaction.getId());
        response.setSellerId(transaction.getSellerId());
        response.setOrderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null);
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setPlatformFee(transaction.getPlatformFee());
        response.setPaymentGatewayFee(transaction.getPaymentGatewayFee());
        response.setNetAmount(transaction.getNetAmount());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus());
        response.setPayoutId(transaction.getPayout() != null ? transaction.getPayout().getId() : null);
        response.setIsSettled(transaction.getIsSettled());
        response.setSettledAt(transaction.getSettledAt());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    private SellerPayoutResponse mapToPayoutResponse(SellerPayout payout) {
        SellerPayoutResponse response = new SellerPayoutResponse();
        response.setId(payout.getId());
        response.setSellerId(payout.getSellerId());
        response.setAmount(payout.getAmount());
        response.setPlatformFee(payout.getPlatformFee());
        response.setPaymentGatewayFee(payout.getPaymentGatewayFee());
        response.setNetAmount(payout.getNetAmount());
        response.setPeriodStartDate(payout.getPeriodStartDate());
        response.setPeriodEndDate(payout.getPeriodEndDate());
        response.setOrdersCount(payout.getOrdersCount());
        response.setStatus(payout.getStatus());
        response.setBankAccountName(payout.getBankAccountName());
        response.setBankAccountNumber(payout.getBankAccountNumber());
        response.setBankName(payout.getBankName());
        response.setTransactionReference(payout.getTransactionReference());
        response.setProcessedAt(payout.getProcessedAt());
        response.setCreatedAt(payout.getCreatedAt());
        return response;
    }

    private SellerBankAccountResponse mapToBankAccountResponse(SellerBankAccount account) {
        SellerBankAccountResponse response = new SellerBankAccountResponse();
        response.setId(account.getId());
        response.setSellerId(account.getSellerId());
        response.setBankName(account.getBankName());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setAccountNumber(maskAccountNumber(account.getAccountNumber()));
        response.setIsPrimary(account.getIsPrimary());
        response.setIsVerified(account.getIsVerified());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
