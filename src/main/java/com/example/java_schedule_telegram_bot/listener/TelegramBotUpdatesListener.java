package com.example.java_schedule_telegram_bot.listener;

import com.example.java_schedule_telegram_bot.entity.NotificationTask;
import com.example.java_schedule_telegram_bot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private final Pattern pattern = Pattern.compile("(?<dateTime>\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s(?<messageText>.+)");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                long chatId = update.message().chat().id();

                logger.info("Получено сообщение от чата {}: {}", chatId, messageText);

                if ("/start".equals(messageText)) {
                    SendMessage message = new SendMessage(chatId, "Привет! Отправь мне данные в формате: 01.01.2026 20:00 Задача");
                    telegramBot.execute(message);
                    continue;
                }

                Matcher matcher = pattern.matcher(messageText);

                if (matcher.matches()) {
                    String dateTimeStr = matcher.group("dateTime");
                    String taskText = matcher.group("messageText");

                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

                        NotificationTask task = new NotificationTask();
                        task.setChatId(chatId);
                        task.setMessage(taskText);
                        task.setDateTime(dateTime);

                        notificationTaskRepository.save(task);

                        logger.info("Задача успешно сохранена в БД для чата {}", chatId); // Логируем успех
                        telegramBot.execute(new SendMessage(chatId, "Задача успешно запланирована! 👍"));

                    } catch (Exception e) {
                        logger.error("Ошибка при обработке даты или сохранении в БД", e);
                        telegramBot.execute(new SendMessage(chatId, "Произошла ошибка при разборе даты. Попробуйте еще раз."));
                    }
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}