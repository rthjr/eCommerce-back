package com.app.ecom.service.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecom.dto.AddressDTO;
import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;
import com.app.ecom.entity.Address;
import com.app.ecom.entity.User;
import com.app.ecom.repository.UserRepository;
import com.app.ecom.service.UserService;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository repo;

	@Override
	public List<UserResponse> fetchAllUser() {
		return repo.findAll().stream().map(this::mapToUserResponse).toList();
	}

	@Override
	public void addUser(UserRequest userRequest) {
		User user = new User();
		updateUserFromRequest(user, userRequest);
		repo.save(user);
	}

	@Override
	public Optional<UserResponse> fetchUser(Long id) {
		return repo.findById(id).map(this::mapToUserResponse);
	}

	@Override
	public boolean updateUser(Long id, UserRequest updatedUserRequest) {
		return repo.findById(id).map(existingUser -> {
			updateUserFromRequest(existingUser, updatedUserRequest);
			repo.save(existingUser);
			return true;
		}).orElse(false);
	}

	@Override
	public void updateUserFromRequest(User user, UserRequest userRequest) {
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setEmail(userRequest.getEmail());
		user.setPhone(userRequest.getPhone());

		if (userRequest.getAddress() != null) {
			Address address = new Address();
			address.setStreet(userRequest.getAddress().getStreet());
			address.setCity(userRequest.getAddress().getCity());
			address.setState(userRequest.getAddress().getState());
			address.setZipCode(userRequest.getAddress().getZipCode());
			address.setCountry(userRequest.getAddress().getCountry());
			user.setAddress(address);
		}
	}

	@Override
	public UserResponse mapToUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setEmail(user.getEmail());
		response.setPhone(user.getPhone());
		response.setRole(user.getRole().name());
		if (user.getAddress() != null) {
			AddressDTO addressDTO = new AddressDTO();
			addressDTO.setStreet(user.getAddress().getStreet());
			addressDTO.setCity(user.getAddress().getCity());
			addressDTO.setState(user.getAddress().getState());
			addressDTO.setZipCode(user.getAddress().getZipCode());
			addressDTO.setCountry(user.getAddress().getCountry());
			response.setAddress(addressDTO);
		}
		return response;

	}

}
