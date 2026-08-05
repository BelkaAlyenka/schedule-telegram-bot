package com.example.java_schedule_telegram_bot.repository;

import com.example.java_schedule_telegram_bot.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    @Query("SELECT task FROM NotificationTask task WHERE task.dateTime = :dateTime")
    List<NotificationTask> findAllByDateTime(@Param("dateTime") LocalDateTime dateTime);
}