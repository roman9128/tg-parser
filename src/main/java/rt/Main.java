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
                        System.out.println("_________________________________" + System.lineSeparator()
                                + "|       Введи команду:" + System.lineSeparator()
                                + "| stop - выход (авторизация сохранена)" + System.lineSeparator()
                                + "| logout - выйти из аккаунта" + System.lineSeparator()
                                + "| show - посмотреть список папок" + System.lineSeparator()
                                + "| load (x) - загрузить сообщения из каналов (номер папки вместо х для загрузки только из этой папки) " + System.lineSeparator()
                                + "| write - записать в файл" + System.lineSeparator()
                                + "|_________________________________" + System.lineSeparator());
                        String consoleText = scanner.nextLine();
                        String[] command = consoleText.split(" ", 2);

                        switch (command[0]) {
                            case "stop":
                                isWorking.set(false);
                                userBot.getClient().sendClose();
                                break;
                            case "logout":
                                userBot.getClient().logOutAsync();
                                isWorking.set(false);
                                userBot.getClient().sendClose();
                                break;
                            case "load":
                                if (command.length == 2) {
                                    loadHistory(userBot, command[1]);
                                    break;
                                } else {
                                    loadHistory(userBot, command[0]);
                                    break;
                                }
                            case "write":
                                writeHistoryToFile(userBot);
                                break;
                            case "show":
                                userBot.showFolders();
                                break;
                            default:
                                System.out.println("неизвестная команда");
                                break;
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

    private static void writeHistoryToFile(UserBot userBot) {
        ExecutorService writer = Executors.newSingleThreadExecutor();
        writer.execute(userBot::writeHistory);
        writer.shutdown();
    }

    private static void loadHistory(UserBot userBot, String secondArg) {
        ExecutorService loader = Executors.newSingleThreadExecutor();
        loader.execute(() -> userBot.loadChannelsHistory(secondArg));
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