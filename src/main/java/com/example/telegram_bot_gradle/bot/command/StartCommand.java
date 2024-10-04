package com.example.telegram_bot_gradle.bot.command;


import com.example.telegram_bot_gradle.database.service.UserAppService;
import com.example.telegram_bot_gradle.database.userModel.UserApp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * Обработка команды начала работы с ботом
 */
@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {

    private final UserAppService userAppService;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());
        InputFile photo = new InputFile(String.valueOf("https://www.litefinance.org/ru/blog/for-beginners/kak-torvovat-krypto-valyutoy/stoit-li-vkladyvat-v-bitcoin/"));
        sendPhoto.setPhoto(photo);
        answer.setText("""
                Привет! Данный бот помогает отслеживать стоимость биткоина.
                Поддерживаемые команды:
                 /get_price - Получить стоимость биткоина
                 /subscribe [число] - Подписывает пользователя на стоимость биткоина
                 /get_subscription - Вывод информации об имеющейся подписке
                 /unsubscribe - Удаление активной подписки
                """);
        try {
            absSender.execute(sendPhoto);
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
        UserApp userApp = UserApp.builder().id(message.getChat().getUserName()).chatId((message.getChatId())).build();
        userAppService.save(userApp);

    }
}