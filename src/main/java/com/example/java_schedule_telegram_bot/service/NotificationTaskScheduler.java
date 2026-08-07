package com.example.java_schedule_telegram_bot.service;

import com.example.java_schedule_telegram_bot.entity.NotificationTask;
import com.example.java_schedule_telegram_bot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskScheduler {

    private final Logger logger = LoggerFactory.getLogger(NotificationTaskScheduler.class);

    @Autowired
    private NotificationTaskRepository repository;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void sendScheduledNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> tasksToSend = repository.findAllByDateTime(currentMinute);

        for (NotificationTask task : tasksToSend) {
            try {
                SendMessage message = new SendMessage(task.getChatId(), "⏰ Напоминание о задаче: " + task.getMessage());
                telegramBot.execute(message);

                repository.delete(task);

            } catch (Exception e) {
                logger.error("Не удалось отправить напоминание для ID: " + task.getId(), e);
            }
        }
    }
}
