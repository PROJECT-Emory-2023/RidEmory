package com.projectpandas.ridemory.util.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    public User getUserByEmail(String email);

    public User getUserById(ObjectId id);
}
