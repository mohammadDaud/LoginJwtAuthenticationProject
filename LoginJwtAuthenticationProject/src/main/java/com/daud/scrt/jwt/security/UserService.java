package com.daud.scrt.jwt.security;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.daud.scrt.jwt.model.Users;
import com.daud.scrt.jwt.repositories.UserRepository;

@Service
public class UserService {
	private static final long EXPIRE_TOKEN_AFTER_MINUTES = 10;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	public String forgotPassword(String email) {
		Optional<Users> optional = userRepository.findByEmail(email);
		Users user=optional.get();
		Optional<Users> userOptional = Optional.ofNullable(user);
		if (!userOptional.isPresent()) {
			return "Invalid email id.";
		}
		long millis = System.currentTimeMillis();
		Date date = new Date(millis);
		user.setRestpasswordtoken(generateToken());
		user.setTokenCreationDate(date);
		user = userRepository.save(user);
		return user.getRestpasswordtoken();
	}

	public String resetPassword(String token, String password) {
		Optional<Users> optional = userRepository.findByRestpasswordtoken(token);
		Users user=optional.get();
		Optional<Users> userOptional = Optional.ofNullable(user);
		if (!userOptional.isPresent()) {
			return "Invalid token.";
		}
		Date tokenCreationDate = user.getTokenCreationDate();
		if (isTokenExpired(tokenCreationDate)) {
			return "Token expired.";
		}
		user.setPassword(passwordEncoder.encode(password));
		user.setRestpasswordtoken(null);
		user.setTokenCreationDate(null);
		userRepository.save(user);
		return "Your password successfully updated.";
	}

	/**
	 * Generate unique token. You may add multiple parameters to create a strong
	 * token.
	 * 
	 * @return unique token
	 */
	private String generateToken() {
		StringBuilder token = new StringBuilder();

		return token.append(UUID.randomUUID().toString()).append(UUID.randomUUID().toString()).toString();
	}

	/**
	 * Check whether the created token expired or not.
	 * 
	 * @param tokenCreationDate
	 * @return true or false
	 */
	private boolean isTokenExpired(final Date tokenCreationDate) {

		long millis = System.currentTimeMillis();
		Date now = new Date(millis);
		long diffInMillies = Math.abs(now.getTime() - tokenCreationDate.getTime());
		long diff = diffInMillies / 60000;
		return diff >= EXPIRE_TOKEN_AFTER_MINUTES;
	}
}
