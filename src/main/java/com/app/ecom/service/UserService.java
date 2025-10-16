package com.app.ecom.service;

import java.util.List;
import java.util.Optional;

import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;
import com.app.ecom.entity.User;

public interface UserService {
	List<UserResponse> fetchAllUser();

	void addUser(UserRequest userRequest);

	Optional<UserResponse> fetchUser(Long id);

	boolean updateUser(Long id, UserRequest updatedUserRequest);

	void updateUserFromRequest(User user, UserRequest userRequest);

	UserResponse mapToUserResponse(User user);

}
