package com.daud.scrt.jwt.controller;

import java.net.URI;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.daud.scrt.jwt.exception.AppException;
import com.daud.scrt.jwt.model.Role;
import com.daud.scrt.jwt.model.RoleName;
import com.daud.scrt.jwt.model.Users;
import com.daud.scrt.jwt.payload.ApiResponse;
import com.daud.scrt.jwt.payload.JwtResponse;
import com.daud.scrt.jwt.payload.LoginRequest;
import com.daud.scrt.jwt.payload.SignUpRequest;
import com.daud.scrt.jwt.repositories.RoleRepository;
import com.daud.scrt.jwt.repositories.UserRepository;
import com.daud.scrt.jwt.security.JwtTokenGenerator;
import com.daud.scrt.jwt.security.UserService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtTokenGenerator tokenProvider;

	@Autowired
	private UserService userService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		response.setHeader("Authorization", tokenProvider.generateToken(authentication));
		return ResponseEntity.ok(new JwtResponse(tokenProvider.generateToken(authentication)));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
		if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
			return new ResponseEntity(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
		}
		if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
			return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"), HttpStatus.BAD_REQUEST);
		}
		// Creating user's account
		Users user = new Users(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				signUpRequest.getPassword());
		long millis = System.currentTimeMillis();
		Date date = new Date(millis);
		user.setCreatedBy(date);
		user.setUpdatedBy(date);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
				.orElseThrow(() -> new AppException("User Role not set."));

		user.setRoles(Collections.singleton(userRole));
		Users result = userRepository.save(user);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}")
				.buildAndExpand(result.getUsername()).toUri();
		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody SignUpRequest signUpRequest) {
		String res = "";
		String response = userService.forgotPassword(signUpRequest.getEmail());
		if (!response.startsWith("Invalid")) {
			res += response;
		}
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}")
				.buildAndExpand(res).toUri();
		return ResponseEntity.created(location).body(new ApiResponse(true, res));
	}

	@PutMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody SignUpRequest signUpRequest, @RequestParam String token) {
		String res = userService.resetPassword(token, signUpRequest.getPassword());
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}")
				.buildAndExpand(res).toUri();
		return ResponseEntity.created(location).body(new ApiResponse(true, res));
	}
}
