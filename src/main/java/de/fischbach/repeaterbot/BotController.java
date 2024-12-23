package de.fischbach.repeaterbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class BotController implements UpdatesListener {

    private final ExecutorService executor;

    private static final Map<Long, MessageRepeaterAssistant> assistants = new ConcurrentHashMap<>();
    private final TelegramBot bot;

    public BotController(TelegramBot bot, ExecutorService executor) {
        this.bot = bot;
        this.executor = executor;
    }

    @Override
    public int process(List<Update> list) {

        for (Update update : list) {
            executor.submit(() -> processUpdate(update));
        }

        return CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        Long id = update.message().chat().id();
        Chat.Type type = update.message().chat().type();
        if (!type.equals(Chat.Type.Private)) {
            return;
        }
        if (update.message().text() != null && update.message().text().equalsIgnoreCase("/start")) {
            bot.execute(new SendMessage(id, "Please provide groups id as follow - 'For groups:' and then group ids comma separated: For groups: 1,2,3,4"));
            assistants.put(id, new MessageRepeaterAssistant(bot, executor));
            return;
        }

        if (update.message().text() != null && update.message().text().equalsIgnoreCase("/cancel")) {
            assistants.remove(id);
            bot.execute(new SendMessage(id, "Current process is stopped"));
            return;
        }

        executor.submit(() -> {
            var messageRepeaterAssistant = assistants.get(id);
            if (messageRepeaterAssistant == null) {
                bot.execute(new SendMessage(id, "Please start with with bot with command /start"));
                return;
            }
            messageRepeaterAssistant.assist(id, update.message());
            if (messageRepeaterAssistant.isFinished()) {
                assistants.remove(id);
            }
        });
    }

}
