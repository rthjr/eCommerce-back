package com.ecommerce.order.repositories;

import com.ecommerce.order.models.SellerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerBankAccountRepository extends JpaRepository<SellerBankAccount, Long> {
    
    List<SellerBankAccount> findBySellerId(String sellerId);
    
    Optional<SellerBankAccount> findBySellerIdAndIsPrimaryTrue(String sellerId);
    
    boolean existsBySellerIdAndAccountNumber(String sellerId, String accountNumber);
}
