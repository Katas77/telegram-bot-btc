package com.example.telegram_bot_gradle.bot.command;

import com.example.telegram_bot_gradle.database.service.UserAppService;
import com.example.telegram_bot_gradle.database.userModel.UserApp;
import com.example.telegram_bot_gradle.service.CryptoCurrencyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {
    private final String botUsername;
    private final UserAppService userAppService;
    private final String[] commands = {"/unsubscribe", "/get_price", "/subscribe", "/get_subscription", "/start"};

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList,
            CryptoCurrencyService service,
            UserAppService userAppService
    ) {
        super(botToken);
        this.botUsername = botUsername;
        this.userAppService = userAppService;
        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        String messageText = update.getMessage().getText();
        log.info("Получено сообщение: {}", messageText);

        String helpMessage = """
                Ошибка отправки сообщения!!!
                Данный бот поддерживает следующие команды:
                /get_price - Получить стоимость биткоина
                /subscribe [число] - Подписывает пользователя на стоимость биткоина
                /get_subscription - Вывод информации об имеющейся подписке
                /unsubscribe - Удаление активной подписки
                """;

        if (!isCommandSupported(messageText)) {
            sendMessage(update.getMessage().getChatId(), helpMessage);
        }
    }

    private boolean isCommandSupported(String messageText) {
        return Stream.of(commands).anyMatch(messageText::startsWith);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    @PostConstruct
    private void schedulePriceCheck() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkPrices, 0, 10, TimeUnit.MINUTES);
    }

    private void checkPrices() {
        String textTemplate = "Пора покупать, стоимость биткоина: %s";

        for (UserApp userApp : userAppService.findAll()) {
            Double desiredPrice = userApp.getBitCoinPrice();
            Double currentPrice = userAppService.getBitCoinPrice();

            if (desiredPrice != null && currentPrice != null && desiredPrice >= currentPrice) {
                String formattedText = String.format(textTemplate, currentPrice);
                sendMessage(userApp.getChatId(), formattedText);
            }
        }
    }
}
