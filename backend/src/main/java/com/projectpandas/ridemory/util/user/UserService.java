package com.projectpandas.ridemory.util.user;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projectpandas.ridemory.util.dto.EmailDto;
import com.projectpandas.ridemory.util.dto.UserUpdateDto;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User createUser(EmailDto email) {
        User user = userRepository.getUserByEmail(email.getEmail());
        if (user != null) {
            logger.warn("Tried creating {}, but {} already exists.", user, user);
            return null;
        }

        try {
            user = User.createNew(email.getEmail());
            userRepository.save(user);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update a user profile. Not entirely complete, but lays the framework for
     * future updates.
     *
     * @param userUpdates updates to the user
     * @return updated user
     */
    public User updateUser(String id, UserUpdateDto userUpdates) {
        User user = userRepository.getUserById(new ObjectId(id));
        if (userUpdates.getFirstName() != null)
            user.setFirstName(userUpdates.getFirstName());

        userRepository.save(user);
        return user;
    }
}
