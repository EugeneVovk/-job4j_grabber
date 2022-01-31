package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * 1. Quartz
 * Часто программе нужно выполнять действия с периодичностью.
 * Например, отправка рассылки, создания копии базы данных.
 * В Java есть библиотека позволяющая делать действия с периодичностью.
 * 1. Конфигурирование.
 * Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
 * scheduler.start();
 * Начало работы происходит с создания класса управляющего всеми работами.
 * В объект Scheduler мы будем добавлять задачи, которые хотим выполнять периодически.
 * 2. Создание задачи.
 * JobDetail job = newJob(Rabbit.class).build()
 * quartz каждый раз создает объект с типом org.quartz.Job.
 * Вам нужно создать класс реализующий этот интерфейс.
 * Внутри этого класса нужно описать требуемые действия. В нашем случае - это вывод на консоль текста.
 * public static class Rabbit implements Job { @Override
 * public void execute(JobExecutionContext context) throws JobExecutionException {
 * System.out.println("Rabbit runs here ...");
 * }
 * }
 * 3. Создание расписания.
 * SimpleScheduleBuilder times = simpleSchedule()
 * .withIntervalInSeconds(10)
 * .repeatForever();
 * Конструкция выше настраивает периодичность запуска.
 * В нашем случае, мы будем запускать задачу через 10 секунд и делать это бесконечно.
 * 4. Задача выполняется через триггер.
 * Trigger trigger = newTrigger()
 * .startNow()
 * .withSchedule(times)
 * .build();
 * Здесь можно указать, когда начинать запуск. Мы хотим сделать это сразу.
 * 5. Загрузка задачи и триггера в планировщик
 * scheduler.scheduleJob(job, trigger);
 * Запустите код и убедитесь, что программа печатает на консоль текст через 10 секунд.
 */
public class AlertRabbit {
    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(
                            Integer.parseInt(readFile().getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }

    public static Properties readFile() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
