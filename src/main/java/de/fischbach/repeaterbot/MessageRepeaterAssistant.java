package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageRepeaterAssistant {

    private final TelegramBot bot;
    private final ExecutorService executorService;
    private List<Long> groupIds = new ArrayList<>();
    private boolean isFinished = false;
    private final BlockingQueue<Message> forwardedMessage = new LinkedBlockingQueue<>();

    public MessageRepeaterAssistant(TelegramBot bot, ExecutorService executorService) {
        this.bot = bot;
        this.executorService = executorService;
    }

    public void assist(long chatId, Message message) {
        if (message.text() != null && message.text().startsWith("For groups:")) {
            processGroupIds(chatId, message.text());
            return;
        }

        if (message.forwardOrigin() != null) {
            forwardedMessage.offer(message);
            return;
        }

        if ("send".equalsIgnoreCase(message.text())) {
            executorService.submit(new RepeatMessageTask(groupIds, bot, chatId, forwardedMessage));
            isFinished = true;
            return;
        }

        if ("stop".equalsIgnoreCase(message.text())) {
            bot.execute(new SendMessage(chatId, "Current process will be aborted"));
            isFinished = true;
            return;
        }

        bot.execute(new SendMessage(chatId, "Unsupported Operation"));
    }

    private void processGroupIds(long chatId, String input) {
        String groupsString = input.substring("For groups:".length());
        String[] groupIdStrings = groupsString.split(",");
        try {
            this.groupIds = parseToLong(groupIdStrings);
            bot.execute(new SendMessage(chatId, "Please, forward message, that will be repeated as follow and then write 'send' or cancel"));
        } catch (NumberFormatException e) {
            bot.execute(new SendMessage(chatId, "All Ids must be a number"));
        }
    }

    private List<Long> parseToLong(String[] groupIds) {
        return Arrays.stream(groupIds).map(Long::parseLong).toList();
    }

    public boolean isFinished() {
        return isFinished;
    }
}
