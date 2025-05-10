package org.example.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Command;
import org.example.StateEnum;
import org.example.model.Group;
import org.example.model.State;
import org.example.projections.GroupView;
import org.example.projections.StateColumnView;
import org.example.repository.StateRepository;
import org.example.repository.UserGroupRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final UserGroupRepository userGroupRepository;

    @Transactional
    public SendMessage handleInputMessage(Message inputMessage) throws TelegramApiException {
        String currentDateTime = getCurrentDateTime();
        upsertUser(inputMessage.getFrom(), currentDateTime);
        StateEnum stateEnum = getStateEnum(inputMessage.getFrom().getId());

        SendMessage sendMessage;
        switch (stateEnum) {
            case MAIN_MENU -> sendMessage = processMessageOnMainMenu(inputMessage);
            default -> sendMessage = processMessageOnStart(inputMessage, true);
        }

        return sendMessage;
    }

    public static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";


    private String getCurrentDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        return dateTimeFormatter.format(LocalDateTime.now());
    }

    public void upsertUser(
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
        LOGGER.debug("changed rows in the users table: {}", nChangedRows);
    }

    public StateEnum getStateEnum(Long userId) {
        Optional<StateColumnView> stateColumnOnly = stateRepository.findFirst1ByUserIdOrderByIdDesc(userId);
        if (stateColumnOnly.isEmpty()) {
            return StateEnum.START;
        }

        StateEnum stateEnum;
        try {
            stateEnum = StateEnum.valueOf(stateColumnOnly.get().getStateEnum());
        } catch (IllegalArgumentException e) {
            return StateEnum.ERROR;
        }

        return stateEnum;
    }

    private SendMessage processMessageOnMainMenu(Message inputMessage) {
        String inputText = inputMessage.getText();
        if (Command.CREATE_NEW_GROUP.equals(inputText)) {
            return processCommandCreateNewGroup(inputMessage, true);
        }

        if (Command.JOIN_GROUP_BY_ID.equals(inputText)) {
            return processCommandJoinGroupById(inputMessage,  true);
        }

        List<GroupView> groupsView = userGroupRepository.findGroupsByUserId(inputMessage.getFrom().getId());
        for (GroupView groupView : groupsView) {
            Group group = groupView.getGroup();
            if (inputText.contains(group.getId())) {
                return processCommandSelectExistingGroup(inputMessage, group, true);
            }
        }

        return processMessageOnStart(inputMessage, false);
    }

    private SendMessage processCommandCreateNewGroup(Message inputMessage, boolean isCorrectInput) {
        changeState(inputMessage, StateEnum.CREATE_NEW_GROUP, null);

        return SendMessage.builder().
                text(addPrefixIncorrectInput(isCorrectInput, "Enter new group name")).
                replyMarkup(createDefaultButtonsMarkup(List.of("Back"))).
                chatId(inputMessage.getChatId().toString()).
                build();

    }

    private SendMessage processCommandJoinGroupById(
            Message inputMessage,
            boolean isCorrectInput
    ) {
        changeState(inputMessage, StateEnum.JOIN_GROUP_BY_ID, null);

        return SendMessage.builder().
                text(addPrefixIncorrectInput(isCorrectInput, "Enter groupId")).
                replyMarkup(createDefaultButtonsMarkup(List.of("Back"))).
                chatId(inputMessage.getChatId().toString()).
                build();
    }

    private SendMessage processCommandSelectExistingGroup(
            Message inputMessage,
            Group group,
            boolean isCorrectInput
    ) {
        changeState(inputMessage, StateEnum.SELECT_EXISTING_GROUP, group);

        return SendMessage.builder().
                replyMarkup(createDefaultButtonsMarkup(List.of("Back"))).
                chatId(inputMessage.getChatId().toString()).
                build();
    }

    private SendMessage processMessageOnStart(Message inputMessage, boolean isCorrectInput) {
        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(Command.CREATE_NEW_GROUP);
        buttonNames.add(Command.JOIN_GROUP_BY_ID);

        List<GroupView> groupViews = userGroupRepository.findGroupsByUserId(inputMessage.getFrom().getId());
        for (GroupView groupView : groupViews) {
            Group group = groupView.getGroup();
            buttonNames.add(group.getName() +
                    "/n Admin:" + group.getAdmin() + ", Created at " + group.getCreatedAt() +
                    "/n" + group.getId()
            );
        }

        changeState(inputMessage, StateEnum.MAIN_MENU, null);

        return SendMessage.builder().
                text(addPrefixIncorrectInput(isCorrectInput, "Join group by id or create new group")).
                replyMarkup(createDefaultButtonsMarkup(buttonNames)).
                chatId(inputMessage.getChatId().toString()).
                build();
    }

    private ReplyKeyboardMarkup createDefaultButtonsMarkup(List<String> buttonNames) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row;
        for (String buttonName : buttonNames) {
            row = new KeyboardRow();
            row.add(buttonName);
            keyboard.add(row);
        }

        return new ReplyKeyboardMarkup(keyboard);
    }

    private String addPrefixIncorrectInput(boolean isCorrectInput, String outputText) {
        return isCorrectInput ? outputText : " Incorrect input /n" + outputText;
    }

    private void changeState(
            Message inputMessage,
            StateEnum stateEnum,
            @Nullable Group group
    ) {
        State state = State.builder().
                userId(inputMessage.getFrom().getId()).
                stateEnum(stateEnum.toString()).
                chatId(inputMessage.getChatId()).
                userInput(inputMessage.getText()).
                group(group).
                build();
        stateRepository.save(state);
    }

    private void processError() {

    }
}
