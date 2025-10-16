package com.app.ecom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.ecom.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
}