package com.example.telegram_bot_gradle.database.userModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Statistics")
@Builder
public class UserApp {

    @Id
    private String id;
    private Double bitCoinPrice;
    private Long chatId;

}
