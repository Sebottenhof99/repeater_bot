package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage;
import com.pengrad.telegrambot.request.ForwardMessages;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.MessageIdsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class RepeatMessageTask implements Runnable {

    private final List<Long> groupIds;
    private final TelegramBot bot;
    private final long chatId;
    private final BlockingQueue<Message> messages;

    public RepeatMessageTask(List<Long> groupIds, TelegramBot bot, long chatId, BlockingQueue<Message> message) {
        this.bot = bot;
        this.groupIds = groupIds;
        this.chatId = chatId;
        this.messages = message;
    }

    @Override
    public void run() {
        List<Long> failedToNotifyGroups = new ArrayList<>();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        try {
            int[] list = messages.stream().map(MaybeInaccessibleMessage::messageId).mapToInt(v -> v).sorted().toArray();
            if (list.length == 0) {
                bot.execute(new SendMessage(chatId, "Can not forward message"));
            }

            Optional<Message> first = messages.stream().findFirst();
            for (Long groupId : groupIds) {
                scheduledExecutorService.schedule(() -> {
                    ForwardMessages request = new ForwardMessages(groupId, first.get().from().id(), list);
                    MessageIdsResponse execute = bot.execute(request);
                    if (!execute.isOk()) {
                        failedToNotifyGroups.add(groupId);
                    }
                }, 2, TimeUnit.SECONDS).get();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch ( ExecutionException e) {
            bot.execute(new SendMessage(chatId, "Can not forward message"));
            throw new RuntimeException(e);
        }finally {
            scheduledExecutorService.shutdown();
        }

        if (!failedToNotifyGroups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Groups with following ids could not be notified:");
            failedToNotifyGroups.stream().map(String::valueOf).forEach(s -> sb.append(s).append("\n"));
            bot.execute(new SendMessage(chatId, sb.toString()));
        } else {
            bot.execute(new SendMessage(chatId, "Message is published in all groups"));
        }
    }
}
