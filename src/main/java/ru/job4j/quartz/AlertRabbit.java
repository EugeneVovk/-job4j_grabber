package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
 * <p>
 * В проекте агрегатор будет использоваться база данных.
 * Открыть и закрывать соединение с базой накладно.
 * Чтобы этого избежать коннект к базе будет создаваться при старте.
 * Объект коннект будет передаваться в Job.
 * Quartz создает объект Job, каждый раз при выполнении работы.
 * <p>
 * Каждый запуск работы вызывает конструктор.
 * Чтобы в объект Job иметь общий ресурс нужно использовать JobExecutionContext.
 * При создании Job мы указываем параметры data. В них мы передаем ссылку на store.
 * В нашем примере store это ArrayList.
 * <p>
 * Чтобы получить объекты из context, используется следующий вызов.
 * List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
 * После выполнение работы в списке будут две даты.
 * Объект store является общим для каждой работы.
 * <p>
 * 1. Доработайте класс AlertRabbit.
 * * Добавьте в файл rabbit.properties настройки для базы данных.
 * 2. Создайте sql schema с таблицей rabbit и полем created_date.
 * 3. При старте приложения создайте connect к базе и передайте его в Job.
 * 4. В Job сделайте запись в таблицу, когда выполнена Job.
 * 5. Весь main должен работать 10 секунд.
 * 6. Закрыть коннект нужно в блоке try-with-resources.
 */
public class AlertRabbit {

    public static void main(String[] args) {
        Properties properties = readFile();
        try (Connection connection = connect(properties)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(
                            Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection connection =
                    (Connection) context.getJobDetail().getJobDataMap().get("store");
            try (PreparedStatement ps = connection.prepareStatement(
                    "insert into rabbit(created_date) values(?);")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
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

    public static Connection connect(Properties properties) throws SQLException, ClassNotFoundException {
        Class.forName(properties.getProperty("driver"));
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
    }
}