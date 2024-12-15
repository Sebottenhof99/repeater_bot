package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageRepeaterAssistant {

    private final TelegramBot bot;
    private List<Long> groupIds = new ArrayList<>();
    private boolean isFinished = false;
    private String message;

    public MessageRepeaterAssistant(TelegramBot bot) {
        this.bot = bot;
    }

    public void assist(long chatId, String input) {
        if (input.startsWith("For groups:")) {
            processGroupIds(chatId, input);
            return;
        }

        if (input.startsWith("Message:")) {
            StringBuilder sb = new StringBuilder();
            message = input.substring("Message:".length());
            sb.append(message);
            sb.append("will be provided to groups with ids:");
            groupIds.stream().map(String::valueOf).forEach(s -> sb.append(s).append("\n"));
            sb.append("\n");
            sb.append("All right? (yes/no)");
            bot.execute(new SendMessage(chatId, sb.toString()));
            return;
        }

        if (input.equalsIgnoreCase("yes")) {

            Thread thread = new Thread(() -> {
                List<Long> unsuccessful = new ArrayList<>();
                Iterator<Long> iterator = groupIds.iterator();
                try  {
                    while (iterator.hasNext()) {
                        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                        executorService.schedule(() -> {
                            if (iterator.hasNext()) {
                                Long groupId = iterator.next();
                                SendResponse response = bot.execute(new SendMessage(groupId, message));
                                if (!response.isOk()) {
                                    unsuccessful.add(groupId);
                                }
                            }
                        }, 2, TimeUnit.SECONDS).get();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }

                if (!unsuccessful.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Groups with following ids could not be notified:");
                    unsuccessful.stream().map(String::valueOf).forEach(s -> sb.append(s).append("\n"));
                    bot.execute(new SendMessage(chatId, sb.toString()));
                }else {
                    bot.execute(new SendMessage(chatId, "Message is published in all groups"));
                }

            });
            thread.start();
            isFinished = true;
            return;

        }

        if ("no".equalsIgnoreCase(input)) {
            bot.execute(new SendMessage(chatId, "Current process will be aborted"));
            isFinished = true;
            return;
        }

        bot.execute(new SendMessage(chatId, "Unsupported Operation"));
    }

    private void processGroupIds(long chatId, String input) {
        String groupsString = input.substring("For groups:".length());
        String[] groupIds = groupsString.split(",");
        try {
            this.groupIds = parseToLong(groupIds);
            bot.execute(new SendMessage(chatId, "Please, provide message, that will be repeated as follow -  Message:<Test>"));
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
