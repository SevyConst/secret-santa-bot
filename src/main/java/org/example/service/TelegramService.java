package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final UserRepository userRepository;

    @Transactional
    public Optional<String> handleInputMessage(Message inputMessage) {
        String currentDateTime = getCurrentDateTime();
        upsertUser(inputMessage.getFrom(), currentDateTime);

        return Optional.empty();
    }

    public static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    private String getCurrentDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        return dateTimeFormatter.format(LocalDateTime.now());
    }

    private void upsertUser(
            org.telegram.telegrambots.meta.api.objects.User userTelegram,
            String dateTime
    ) {
        int nChangedRows = userRepository.updateAndIncrement(
                userTelegram.getId(),
                userTelegram.getUserName(),
                userTelegram.getFirstName(),
                userTelegram.getLastName(),
                dateTime
        );
        LOGGER.debug("changed rows in users table: " + nChangedRows);
    }

}
