package pro.sky.telegrambot.sheduled;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.dto.NotificationTaskGetActualNowMessage;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ScheduledMethods {
    private final Logger logger = LoggerFactory.getLogger(ScheduledMethods.class);
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Scheduled(cron = "0 0/1 * * * *")
    private void SearchMassageByDate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTaskGetActualNowMessage> taskList;
        taskList = notificationTaskRepository.getMessageForNowDate(now);
        sendMessageToUserChat(taskList);
    }

    private void sendMessageToUserChat(List<NotificationTaskGetActualNowMessage> taskList) {
        if (!taskList.isEmpty()) {
            for (var element : taskList) {
                SendMessage message = new SendMessage(element.getId_chat(), element.getMessage());
                telegramBot.execute(message);
                logger.debug("message for user chat {} send", element.getId_chat());
            }
        }
    }
}
