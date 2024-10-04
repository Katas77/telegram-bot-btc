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


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {
    private final String botUsername;
    private final UserAppService userAppService;
    String[] commands = {"/unsubscribe", "/get_price", "/subscribe ", " /get_subscription", "/unsubscribe", "/start"};

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList, CryptoCurrencyService service, UserAppService userAppService
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
        System.out.println(update.getMessage().getText());

        String text = """
                Ошибка отправки сообщения!!!
                           
                 Данный бот поддерживаемые команды:
                 /get_price - Получить стоимость биткоина
                 /subscribe [число] - Подписывает пользователя на стоимость биткоина
                 /get_subscription - Вывод информации об имеющейся подписке
                 /unsubscribe - Удаление активной подписки
                """;
        boolean startsWith = false;
        for (String s : commands) {
            if (update.getMessage().getText().startsWith(s)) {
                startsWith = true;
                break;
            }
        }
        if (!startsWith) {
            sendMessage(update.getMessage().getChatId(), text);
        }
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }


    @PostConstruct
    private void sendMessageTimer() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::valid, 0, 10, TimeUnit.MINUTES);
    }

    private void valid() {
        var text = "Пора покупать, стоимость биткоина  %s ";
        for (UserApp userApp : userAppService.findAll()) {
            if (userApp.getBitCoinPrice() != null && userAppService.getBitCoinPrice() != null && userApp.getBitCoinPrice() >=  userAppService.getBitCoinPrice()) {
                String formattedText = String.format(text, userAppService.getBitCoinPrice());
                sendMessage(userApp.getChatId(), formattedText);
            }
        }
    }


}
