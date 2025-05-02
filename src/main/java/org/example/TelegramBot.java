package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger logger = LogManager.getLogger(TelegramBot.class.getName());

    private final CredentialsProperties credentialsProperties;
    private final TelegramClient telegramClient;

    public TelegramBot(CredentialsProperties credentialsProperties) {
        this.credentialsProperties = credentialsProperties;
        telegramClient = new OkHttpTelegramClient(getBotToken());
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
            logger.info("UserId: {}, userName: {}, chatId: {}, message: {}",
                    inputMessage.getFrom().getId(),
                    inputMessage.getFrom().getUserName(),
                    inputMessage.getChatId(),
                    inputMessage.getText()
            );
        }
    }

}
