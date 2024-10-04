package com.example.telegram_bot_gradle.database.service;

import com.example.telegram_bot_gradle.database.repository.UserAppRepository;
import com.example.telegram_bot_gradle.database.userModel.UserApp;
import com.example.telegram_bot_gradle.service.CryptoCurrencyService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Data
public class UserAppService {

    private final UserAppRepository repository;
    private Double bitCoinPrice;

    private final CryptoCurrencyService service;

    @PostConstruct
    private void print() {
        this.findAll().forEach(System.out::println);
    }

    public List<UserApp> findAll() {
        return repository.findAll();

    }

    public void save(UserApp userApp) {
        if (findAll().stream().noneMatch(userApp1 -> Objects.equals(userApp1.getChatId(), userApp.getChatId()))) {
            String id = userApp.getId() + UUID.randomUUID().toString().substring(0, 4);
            userApp.setId(id);
            log.info(MessageFormat.format("userApp with ID {0} registered", userApp.getChatId()));
            repository.save(userApp);
        } else log.info("This userApp  is already registered");
    }

    public void update(UserApp userApp) {
        Optional<UserApp> optional = repository.findAll().stream().filter(user -> Objects.equals(user.getChatId(), userApp.getChatId())).findFirst();
        UserApp userAppUpdate = optional.orElseThrow();
        userAppUpdate.setBitCoinPrice(userApp.getBitCoinPrice());
        repository.save(userAppUpdate);
    }

    public String GetSubscription(Long chatId) {
        Optional<UserApp> optional = repository.findAll().stream().filter(user -> Objects.equals(user.getChatId(), chatId)).findFirst();
        var text = " Вы подписались  на стоимость bitcoin %s USD.";
        return optional.orElseThrow().getBitCoinPrice() == null ? "Активные подписки отсутствуют" : String.format(text, optional.get().getBitCoinPrice());
    }

    public void reset(Long chatId) {
        Optional<UserApp> optional = repository.findAll().stream().filter(userApp1 -> Objects.equals(userApp1.getChatId(), chatId)).findFirst();
        UserApp userAppUpdate = optional.orElseThrow();
        userAppUpdate.setBitCoinPrice(null);
        repository.save(userAppUpdate);
    }

    @PostConstruct
    public void setDate() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                this.setBitCoinPrice(service.getBitcoinPrice());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 0, 2, TimeUnit.MINUTES);
    }


    private void command(Long chatId) {
        String formattedText;
        var text = "Date %s now.";
        formattedText = String.format(text, LocalDate.now());

    }

}
