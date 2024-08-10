package com.projectpandas.ridemory.util.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectpandas.ridemory.util.dto.EmailDto;
import com.projectpandas.ridemory.util.dto.UserUpdateDto;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    /**
     * Sign up for a new account.
     *
     * @param email the email of the new user
     * @return new user
     */
    @PostMapping
    public ResponseEntity<User> signUp(@RequestBody EmailDto email) {
        User user = userService.createUser(email);
        return user == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(user);
    }

    // TODO: authentication

    /**
     * Edit account details.
     *
     * @param id the user's id
     * @param userUpdates the
     * @return
     */
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UserUpdateDto userUpdates) {
        User user = userService.updateUser(id, userUpdates);
        return user == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(user);
    }
}
