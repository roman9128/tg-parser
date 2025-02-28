package rt;


import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final AtomicBoolean isWorking = new AtomicBoolean(true);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws Exception {
        Init.init();
        Log.setLogMessageHandler(0, new Slf4JLogMessageHandler());
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(PropertyHandler.getApiID(), PropertyHandler.getApiHash());
            TDLibSettings settings = TDLibSettings.create(apiToken);
            Path sessionPath = Paths.get("tdlight-session");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            ConsoleInteractiveAuthenticationData authenticationData = AuthenticationSupplier.consoleLogin();

            try (UserBot userBot = new UserBot(clientBuilder, authenticationData)) {
                initConsoleThread(userBot);
                userBot.getClient().waitForExit();
                Thread.sleep(100); // ожидание завершения соединения с TDlib
            } catch (Exception e) {
                System.out.println("Исключение в главном потоке: " + e.getMessage());
                stopConsoleThread();
            } finally {
                System.out.println("Программа завершила работу");
            }
        }
    }

    private static void initConsoleThread(UserBot userBot) {
        executor.submit(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isWorking.get()) {
                    if (userBot.isLoggedIn()) {
                        printMenu();
                        String[] args = {"", "", ""};
                        String consoleText = scanner.nextLine();
                        String[] command = consoleText.split(" ", 3);
                        System.arraycopy(command, 0, args, 0, command.length);

                        switch (args[0]) {
                            case "show" -> {
                                userBot.showFolders();
                            }
                            case "load" -> {
                                loadHistory(userBot, args[1], args[2]);
                            }
                            case "write" -> {
                                writeHistoryToFile(userBot);
                            }
                            case "stop" -> {
                                isWorking.set(false);
                                userBot.getClient().sendClose();
                            }
                            case "logout" -> {
                                userBot.getClient().logOutAsync();
                                isWorking.set(false);
                                userBot.getClient().sendClose();
                            }
                            default -> {
                                System.out.println("неизвестная команда");
                            }
                        }
                    } else {
                        Thread.sleep(2000);
                    }
                }
            } catch (Exception e) {
                System.err.println("Исключение в консольном потоке: " + e.getMessage());
            } finally {
                stopConsoleThread();
            }
        });
    }

    private static void printMenu() {
        System.out.println("_________________________________" + System.lineSeparator()
                + "|          Введи команду:" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| show - посмотреть список папок" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| команды для загрузки сообщений строятся по схеме:" + System.lineSeparator()
                + "| load X DD.MM.YYYY" + System.lineSeparator()
                + "| вместо Х может быть:" + System.lineSeparator()
                + "| - число для обозначения номера папки для загрузки сообщений только из указанной папки" + System.lineSeparator()
                + "| - all для загрузки сообщений из всех пабликов" + System.lineSeparator()
                + "| вместо DD.MM.YYYY может быть указана дата в данном формате" + System.lineSeparator()
                + "| - если дата указана, то загрузятся сообщения от указанной даты до текущего момента" + System.lineSeparator()
                + "| - если даты нет, то будет загружено не менее " + PropertyHandler.getMessagesToDownload() + " сообщ. с канала" + System.lineSeparator()
                + "| первый параметр указывать нужно, второй - по необходимости" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| write - записать в файл" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| stop - выход (авторизация сохранена)" + System.lineSeparator()
                + "| logout - выйти из аккаунта" + System.lineSeparator()
                + "|_________________________________" + System.lineSeparator());
    }

    private static void writeHistoryToFile(UserBot userBot) {
        ExecutorService writer = Executors.newSingleThreadExecutor();
        writer.execute(userBot::writeHistory);
        writer.shutdown();
    }

    private static void loadHistory(UserBot userBot, String folderIDString, String dateString) {
        ExecutorService loader = Executors.newSingleThreadExecutor();
        loader.execute(() -> userBot.loadChannelsHistory(folderIDString, dateString));
        loader.shutdown();
    }

    private static void stopConsoleThread() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            System.out.println("Консольный поток закрыт");
        } else {
            System.out.println("Была попытка закрыть консольный поток повторно");
        }
    }
}