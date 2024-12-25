package com.daud.scrt.jwt.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daud.scrt.jwt.model.Users;
import com.daud.scrt.jwt.payload.UserSummary;
import com.daud.scrt.jwt.repositories.UserRepository;
import com.daud.scrt.jwt.security.UserPrincipal;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	/*
	 * @Autowired private ProfileImageRepositroy profileImageRepositroy;
	 */

	@GetMapping("/user/me")
	@PreAuthorize("hasRole('USER')")
	public UserSummary getCurrentUser(@RequestBody UserPrincipal currentUser) {
		return new UserSummary(currentUser.getId(), currentUser.getUsername(),
				currentUser.getName());
	}

	@PostMapping("/user/updateuser")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> updateUserProfile(@RequestBody UserPrincipal currentUser) {
		Users dbUser = userRepository.getById(currentUser.getId());
		if(dbUser!=null) {
			Users users = new Users(currentUser.getName(),currentUser.getUsername(),currentUser.getEmail(),dbUser.getPassword());
			users.setId(dbUser.getId());
			userRepository.save(users);
			
		}
		return ResponseEntity.ok("Update SuccessFully!!!!!");
	}
	
	@PostMapping("/user/updateProfileImg")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> updateUserProfileImage(@RequestBody UserPrincipal currentUser) {
		Users users = new Users();
		//ProfileImages images = new ProfileImages();
		return ResponseEntity.ok("Update SuccessFully!!!!!");
	}

	/*
	 * @GetMapping("/user/checkUsernameAvailability") public
	 * UserIdentityAvailability checkUsernameAvailability(@RequestParam(value =
	 * "username") String username) { Boolean isAvailable =
	 * !userRepository.existsByUsername(username); return new
	 * UserIdentityAvailability(isAvailable); }
	 * 
	 * @GetMapping("/user/checkEmailAvailability") public UserIdentityAvailability
	 * checkEmailAvailability(@RequestParam(value = "email") String email) { Boolean
	 * isAvailable = !userRepository.existsByEmail(email); return new
	 * UserIdentityAvailability(isAvailable); }
	 */

	@GetMapping("/user/getAllUser")
	public ResponseEntity<?> getUserProfile() {
		List<Users> userlist = userRepository.findAll();
		return ResponseEntity.ok(userlist);
	}

}
