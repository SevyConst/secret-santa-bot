package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final CredentialsProperties credentialsProperties;
    private final TelegramClient telegramClient;
    private final TelegramService telegramService;

    @Autowired
    public TelegramBot(CredentialsProperties credentialsProperties,
                       TelegramService telegramService
    ) {
        this.credentialsProperties = credentialsProperties;
        telegramClient = new OkHttpTelegramClient(getBotToken());
        this.telegramService = telegramService;
    }

    @Override
    public String getBotToken() {
        return credentialsProperties.getTelegramBotToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() &&
                update.getMessage().isUserMessage() &&
                update.getMessage().hasText()
        ) {
            Message inputMessage = update.getMessage();
            LOGGER.info("UserId: {}, userName: {}, chatId: {}, message: {}",
                    inputMessage.getFrom().getId(),
                    inputMessage.getFrom().getUserName(),
                    inputMessage.getChatId(),
                    inputMessage.getText()
            );
            telegramService.handleInputMessage(inputMessage);
        }
    }

}
