package com.ecommerce.user.services;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.models.LoyaltyAccount;
import com.ecommerce.user.models.PointTransaction;
import com.ecommerce.user.models.ReferralCode;
import com.ecommerce.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ReferralCodeRepository referralCodeRepository;

    public LoyaltyAccountDTO getOrCreateLoyaltyAccount(String userId) {
        Optional<LoyaltyAccount> existingAccount = loyaltyAccountRepository.findByUserId(userId);

        if (existingAccount.isPresent()) {
            return mapToDTO(existingAccount.get());
        }

        LoyaltyAccount newAccount = new LoyaltyAccount();
        newAccount.setUserId(userId);
        newAccount.setTotalPoints(0);
        newAccount.setCurrentPoints(0);
        newAccount.setTier("BRONZE");
        newAccount.setTierUpdatedAt(LocalDateTime.now());
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        LoyaltyAccount saved = loyaltyAccountRepository.save(newAccount);
        return mapToDTO(saved);
    }

    public Page<PointTransactionDTO> getTransactions(String userId, int page, int size) {
        Page<PointTransaction> transactions = pointTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return transactions.map(this::mapToTransactionDTO);
    }

    public LoyaltyAccountDTO earnPoints(String userId, Integer points, String orderId, String description) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> createNewAccount(userId));

        account.setTotalPoints(account.getTotalPoints() + points);
        account.setCurrentPoints(account.getCurrentPoints() + points);
        account.setUpdatedAt(LocalDateTime.now());

        updateTier(account);

        loyaltyAccountRepository.save(account);

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setPoints(points);
        transaction.setType("EARN");
        transaction.setOrderId(orderId);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setExpiresAt(LocalDateTime.now().plusYears(1));
        pointTransactionRepository.save(transaction);

        return mapToDTO(account);
    }

    public LoyaltyAccountDTO redeemPoints(String userId, Integer points) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty account not found"));

        if (account.getCurrentPoints() < points) {
            throw new RuntimeException("Insufficient points");
        }

        account.setCurrentPoints(account.getCurrentPoints() - points);
        account.setUpdatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);

        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setPoints(-points);
        transaction.setType("REDEEM");
        transaction.setDescription("Points redeemed");
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(transaction);

        return mapToDTO(account);
    }

    public ReferralCodeDTO getReferralCode(String userId) {
        Optional<ReferralCode> existing = referralCodeRepository.findByUserId(userId);

        if (existing.isPresent()) {
            return mapToReferralDTO(existing.get());
        }

        // Generate unique code
        String code = generateUniqueCode(userId);

        ReferralCode newCode = new ReferralCode();
        newCode.setUserId(userId);
        newCode.setCode(code);
        newCode.setUsageCount(0);
        newCode.setMaxUsage(100);
        newCode.setCreatedAt(LocalDateTime.now());

        ReferralCode saved = referralCodeRepository.save(newCode);
        return mapToReferralDTO(saved);
    }

    public LoyaltyAccountDTO applyReferralCode(String userId, String code) {
        ReferralCode referralCode = referralCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid referral code"));

        if (referralCode.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot use your own referral code");
        }

        if (referralCode.getUsageCount() >= referralCode.getMaxUsage()) {
            throw new RuntimeException("Referral code usage limit reached");
        }

        // Update referral code usage
        referralCode.setUsageCount(referralCode.getUsageCount() + 1);
        referralCodeRepository.save(referralCode);

        // Award points to both users
        earnPoints(referralCode.getUserId(), 100, null, "Referral reward");
        return earnPoints(userId, 50, null, "New user referral bonus");
    }

    private LoyaltyAccount createNewAccount(String userId) {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setUserId(userId);
        account.setTotalPoints(0);
        account.setCurrentPoints(0);
        account.setTier("BRONZE");
        account.setTierUpdatedAt(LocalDateTime.now());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private void updateTier(LoyaltyAccount account) {
        int points = account.getTotalPoints();
        String currentTier = account.getTier();
        String newTier;

        if (points >= 10000) {
            newTier = "PLATINUM";
        } else if (points >= 5000) {
            newTier = "GOLD";
        } else if (points >= 1000) {
            newTier = "SILVER";
        } else {
            newTier = "BRONZE";
        }

        if (!newTier.equals(currentTier)) {
            account.setTier(newTier);
            account.setTierUpdatedAt(LocalDateTime.now());
        }
    }

    private String generateUniqueCode(String userId) {
        String code;
        do {
            code = "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (referralCodeRepository.existsByCode(code));
        return code;
    }

    private LoyaltyAccountDTO mapToDTO(LoyaltyAccount account) {
        LoyaltyAccountDTO dto = new LoyaltyAccountDTO();
        dto.setId(account.getId());
        dto.setUserId(account.getUserId());
        dto.setTotalPoints(account.getTotalPoints());
        dto.setCurrentPoints(account.getCurrentPoints());
        dto.setTier(account.getTier());
        dto.setTierUpdatedAt(account.getTierUpdatedAt() != null ? account.getTierUpdatedAt().toString() : null);
        dto.setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : null);
        dto.setUpdatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().toString() : null);
        return dto;
    }

    private PointTransactionDTO mapToTransactionDTO(PointTransaction transaction) {
        PointTransactionDTO dto = new PointTransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setPoints(transaction.getPoints());
        dto.setType(transaction.getType());
        dto.setOrderId(transaction.getOrderId());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null);
        dto.setExpiresAt(transaction.getExpiresAt() != null ? transaction.getExpiresAt().toString() : null);
        return dto;
    }

    private ReferralCodeDTO mapToReferralDTO(ReferralCode code) {
        ReferralCodeDTO dto = new ReferralCodeDTO();
        dto.setId(code.getId());
        dto.setUserId(code.getUserId());
        dto.setCode(code.getCode());
        dto.setUsageCount(code.getUsageCount());
        dto.setMaxUsage(code.getMaxUsage());
        dto.setCreatedAt(code.getCreatedAt() != null ? code.getCreatedAt().toString() : null);
        return dto;
    }
}
