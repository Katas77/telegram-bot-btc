package com.example.telegram_bot_gradle.database.repository;

import com.example.telegram_bot_gradle.database.userModel.UserApp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAppRepository extends MongoRepository<UserApp, String> {

}
