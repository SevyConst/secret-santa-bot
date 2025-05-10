package org.example;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramService telegramService;

    @Value( "${telegram.telegramBotToken}" )
    private String telegramBotToken;

    private TelegramClient telegramClient;

    @PostConstruct
    public void init() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
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
            try {
                telegramClient.execute(telegramService.handleInputMessage(inputMessage));
            } catch (TelegramApiException e) {
                LOGGER.error("Can't send respond", e);
            }
        }
    }
}
