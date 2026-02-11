package com.ecommerce.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.models.PointTransaction;

@Repository
public interface PointTransactionRepository extends MongoRepository<PointTransaction, String> {
    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<PointTransaction> findByUserIdAndType(String userId, String type);
}
