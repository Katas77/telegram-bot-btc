package com.example.telegram_bot_gradle.bot.command;

import com.example.telegram_bot_gradle.database.service.UserAppService;
import com.example.telegram_bot_gradle.database.userModel.UserApp;
import com.example.telegram_bot_gradle.service.CryptoCurrencyService;
import com.example.telegram_bot_gradle.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;


/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {
    private final UserAppService userAppService;
    private final CryptoCurrencyService service;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        String messageText = message.getText();
        if (messageText.length() > 10 && messageText.substring(11).matches("[/s0-9.]+")) {
            try {
                sendMessage(absSender, message, "Текущая цена биткоина " + TextUtil.toString(service.getBitcoinPrice()) + " USD");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Double prize = Double.valueOf(message.getText().replaceAll("[^0-9]", ""));
            var text = "Новая подписка создана. Вы подписались  на стоимость bitcoin %s USD.";
            String formattedText = String.format(text, prize);
            sendMessage(absSender, message, formattedText);
            UserApp userApp = UserApp.builder().bitCoinPrice(prize).chatId((message.getChatId())).build();
            userAppService.update(userApp);
        } else {
            sendMessage(absSender, message, "Введите команду  в формате /subscribe желаемая стоимости биткоина.  Пример /subscribe 35500");
        }
    }

    private void sendMessage(AbsSender absSender, Message message, String text) {
        var chatIdStr = String.valueOf(message.getChatId());
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}