package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.sky.telegrambot.dto.NotificationTaskGetActualNowMessage;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    @Query(value = "select nt.id_chat,nt.message\n" +
            "from notification_task as nt\n" +
            "where date = ?1", nativeQuery = true)
    List<NotificationTaskGetActualNowMessage> getMessageForNowDate(LocalDateTime localDateTime);
}
