package rt.view.console;

import rt.infrastructure.config.PropertyHandler;
import rt.infrastructure.notifier.Notifier;
import rt.model.notification.Notification;
import rt.presenter.Presenter;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsoleUI implements View {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ParserPresenter parserPresenter;
    private StoragePresenter storagePresenter;
    private RecorderPresenter recorderPresenter;
    private AnalyzerPresenter analyzerPresenter;

    @Override
    public void setPresenters(Presenter presenter1, Presenter presenter2, Presenter presenter3, Presenter presenter4) {
        this.parserPresenter = (ParserPresenter) presenter1;
        this.storagePresenter = (StoragePresenter) presenter2;
        this.recorderPresenter = (RecorderPresenter) presenter3;
        if (presenter4 instanceof AnalyzerPresenter) {
            this.analyzerPresenter = (AnalyzerPresenter) presenter4;
        } else {
            this.analyzerPresenter = null;
        }
    }

    @Override
    public void startInteractions() {
        printMenu();
        try {
            while (true) {
                String[] args = {"", "", "", ""};
                String[] userCommand = ConsoleInput.readLine().split(" ", 4);
                System.arraycopy(userCommand, 0, args, 0, userCommand.length);
                switch (args[0]) {
                    case "show" -> {
                        show();
                    }
                    case "load" -> {
                        load(args[1], args[2], args[3]);
                    }
                    case "find" -> {
                        find(new String[]{args[1], args[2], args[3]});
                    }
                    case "class" -> {
                        classify();
                    }
                    case "write" -> {
                        write(args[1]);
                    }
                    case "clear" -> {
                        clear();
                    }
                    case "stop" -> {
                        stopParser();
                        return;
                    }
                    case "logout" -> {
                        logout();
                        return;
                    }
                    case "menu" -> {
                        printMenu();
                    }
                    default -> {
                        System.out.println("неизвестная команда");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Исключение в консольном потоке: " + e.getMessage());
        }
    }

    @Override
    public void startNotificationListener() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Notification notification = Notifier.getInstance().getNotification();
                    print(notification.getMessage(), notification.needNextLine());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @Override
    public void stopNotificationListener() {
        executor.shutdownNow();
    }

    @Override
    public void show() {
        parserPresenter.show();
    }

    @Override
    public void load(String folderIDString, String dateFromString, String dateToString) {
        parserPresenter.load(folderIDString, dateFromString, dateToString);
    }

    @Override
    public void find(String[] args) {
        storagePresenter.find(args);
    }

    @Override
    public void classify() {
        if (analyzerPresenter != null) {
            analyzerPresenter.classify();
        } else {
            print("Анализатор выключен", true);
        }
    }

    @Override
    public void write(String value) {
        recorderPresenter.write(value);
    }

    @Override
    public void clear() {
        storagePresenter.clear();
    }

    @Override
    public void stopParser() {
        parserPresenter.close();
    }

    @Override
    public void logout() {
        parserPresenter.logout();
    }

    private void printMenu() {
        System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator()
                + "*          Команды для управления программой:" + System.lineSeparator()
                + "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator()
                + "* menu - показать меню" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* show - посмотреть список папок" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* команды для загрузки сообщений строятся по схеме:" + System.lineSeparator()
                + "* load X DD.MM.YYYY DD.MM.YYYY" + System.lineSeparator()
                + "* вместо Х может быть:" + System.lineSeparator()
                + "* - число для обозначения номера папки для загрузки сообщений только из указанной папки" + System.lineSeparator()
                + "* - all для загрузки сообщений из всех пабликов" + System.lineSeparator()
                + "* вместо DD.MM.YYYY может быть указана дата в данном формате" + System.lineSeparator()
                + "* - если даты не указаны вообще, то будет загружено примерно " + PropertyHandler.getMessagesToDownload() + " сообщ. с канала" + System.lineSeparator()
                + "* - если указана одна дата, то загрузятся сообщения с начала указанного дня до текущего момента" + System.lineSeparator()
                + "* - если указано две даты, то загрузятся сообщения с начала первого указанного дня до конца второго указанного дня" + System.lineSeparator()
                + "* первый параметр (Х) можно не указывать, если далее нет дат" + System.lineSeparator()
                + "* параметры в команде load пишутся через один пробел" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* команды для поиска сообщений строятся по схеме:" + System.lineSeparator()
                + "* find HOW WHERE WHAT" + System.lineSeparator()
                + "* вместо HOW может быть:" + System.lineSeparator()
                + "* - and для поиска сообщений, удовлетворяющих всем параметрам" + System.lineSeparator()
                + "* - or для поиска сообщений, удовлетворяющих хотя бы одному условию" + System.lineSeparator()
                + "* - not для поиска сообщений, не удовлетворяющих заданным условиям" + System.lineSeparator()
                + "* вместо WHERE может быть:" + System.lineSeparator()
                + "* - topic для поиска по тематикам" + System.lineSeparator()
                + "* - text для поиска по тексту" + System.lineSeparator()
                + "* вместо WHAT нужно указать необходимые слова через один пробел" + System.lineSeparator()
                + "* при поиске по категориям можно слитно с категорией через дефис указать процент соответствия текста данной категории" + System.lineSeparator()
                + "* например: find and topic экономика-50 политика-60" + System.lineSeparator()
                + "* параметры в команде find пишутся через один пробел" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* class - определить тематики сообщений (найдёт только те, на которые натренирован анализатор)" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* write - записать в файл" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* clear - удалить загруженное" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* stop - выход (авторизация сохранена)" + System.lineSeparator()
                + "* * * * * * * * * * * * * *" + System.lineSeparator()
                + "* logout - выйти из аккаунта" + System.lineSeparator()
                + "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator());
    }

    @Override
    public void print(String text, boolean needNextLine) {
        if (needNextLine) {
            System.out.println(text);
        } else {
            System.out.print(text + "\r");
        }
    }

    @Override
    public String askParameter(String who, String question) {
        return ConsoleInput.askParameter(who, question);
    }
}
