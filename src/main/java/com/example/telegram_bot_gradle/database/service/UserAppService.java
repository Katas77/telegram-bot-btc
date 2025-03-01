package com.example.telegram_bot_gradle.database.service;

import com.example.telegram_bot_gradle.database.repository.UserAppRepository;
import com.example.telegram_bot_gradle.database.userModel.UserApp;
import com.example.telegram_bot_gradle.service.CryptoCurrencyService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Data
public class UserAppService {

    private final UserAppRepository repository;
    private final CryptoCurrencyService service;
    private Double bitCoinPrice;

    public UserAppService(UserAppRepository repository, CryptoCurrencyService service) {
        this.repository = repository;
        this.service = service;
    }

    @PostConstruct
    private void init() {
        logUsers();
        scheduleBitcoinPriceUpdates();
    }

    private void logUsers() {
        findAll().forEach(userApp -> log.info("Registered UserApp: {}", userApp));
    }

    public List<UserApp> findAll() {
        return repository.findAll();
    }

    public void save(UserApp userApp) {
        if (isUserNotRegistered(userApp)) {
            String id = createUniqueId(userApp);
            userApp.setId(id);
            log.info("UserApp with ID {} registered", userApp.getChatId());
            repository.save(userApp);
        } else {
            log.info("This UserApp is already registered");
        }
    }

    private boolean isUserNotRegistered(UserApp userApp) {
        return findAll().stream()
                .noneMatch(existingUser -> Objects.equals(existingUser.getChatId(), userApp.getChatId()));
    }

    private String createUniqueId(UserApp userApp) {
        return userApp.getId() + UUID.randomUUID().toString().substring(0, 4);
    }

    public void update(UserApp userApp) {
        UserApp existingUserApp = findUserByChatId(userApp.getChatId());
        existingUserApp.setBitCoinPrice(userApp.getBitCoinPrice());
        repository.save(existingUserApp);
    }

    public String getSubscription(Long chatId) {
        UserApp userApp = findUserByChatId(chatId);
        Double price = userApp.getBitCoinPrice();
        return
                price == null ? "Активные подписки отсутствуют" :
                String.format("Вы подписались на стоимость Bitcoin: %s USD.", price);
    }

    public void reset(Long chatId) {
        UserApp userApp = findUserByChatId(chatId);
        userApp.setBitCoinPrice(null);
        repository.save(userApp);
    }

    private UserApp findUserByChatId(Long chatId) {
        return findAll().stream()
                .filter(user -> Objects.equals(user.getChatId(), chatId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void scheduleBitcoinPriceUpdates() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateBitcoinPrice, 0, 2, TimeUnit.MINUTES);
    }

    private void updateBitcoinPrice() {
        try {
            setBitCoinPrice(service.getBitcoinPrice());
        } catch (IOException e) {
            log.error("Ошибка получения цены биткоина", e);
        }
    }

}
