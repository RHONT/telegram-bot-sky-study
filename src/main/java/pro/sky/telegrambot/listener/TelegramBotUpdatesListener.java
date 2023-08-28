package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.dto.NotificationTaskGetActualNowMessage;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    static final Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {


        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String inputMessage = update.message().text();
            Long idUser = update.message().chat().id();

            logger.debug("Chat user: {}, text message: {}", idUser, inputMessage);

            if (inputMessage.equals("/start")) {
                SendMessage message = new SendMessage(idUser, "И тебе привет");
                telegramBot.execute(message);
                return;
            }

            // reaction on write user affairs
            // format 01.01.2023 Мое дело
            if (checkForBuissy(idUser, inputMessage)) return;

            telegramBot.execute(new SendMessage(idUser, "Я не знаю, что ты от меня хочешь"));

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    private boolean checkForBuissy(Long idUser, String inputMessage) {
        String dateTime;
        String message;

        Matcher matcher = pattern.matcher(inputMessage);
        if (matcher.matches()) {
            dateTime = matcher.group(1);
            message = matcher.group(3);
        } else {
            return false;
        }

        LocalDateTime parseDate;
        try {
            if (dateTime != null) {
                parseDate = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            } else throw new NullPointerException();

        } catch (DateTimeParseException | NullPointerException e) {
            telegramBot.execute(new SendMessage(idUser, "Ошибка ввода даты"));
            logger.debug("User chat: {} input incorrect date format : {}", idUser, dateTime);
            return true;
        }

        notificationTaskRepository.save(new NotificationTask(
                idUser,
                parseDate,
                message)
        );

        telegramBot.execute(new SendMessage(idUser,"Заметка записана!"));

        logger.debug("User notification successfully save. User chat: {}", idUser);

        return true;
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

    @Scheduled(cron = "0 0/1 * * * *")
    private void SearchMassageByDate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTaskGetActualNowMessage> taskList;
        taskList = notificationTaskRepository.getMessageForNowDate(now);
        sendMessageToUserChat(taskList);
    }

}


