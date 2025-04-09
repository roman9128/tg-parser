package rt.view.console;

import rt.model.utils.PropertyHandler;
import rt.presenter.Presenter;
import rt.view.View;

public class ConsoleUI implements View {
    private Presenter presenter;

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
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
                        find(args[1] + " " + args[2] + " " + args[3]);
                    }
                    case "write" -> {
                        write(args[1]);
                    }
                    case "clear" -> {
                        clear();
                    }
                    case "stop" -> {
                        stop();
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
    public void show() {
        presenter.show();
    }

    @Override
    public void load(String folderIDString, String dateFromString, String dateToString) {
        presenter.load(folderIDString, dateFromString, dateToString);
    }

    @Override
    public void find(String argsAsString) {
        presenter.find(argsAsString);
    }

    @Override
    public void write(String value) {
        presenter.write(value);
    }

    @Override
    public void clear() {
        presenter.clear();
    }

    @Override
    public void stop() {
        presenter.stop();
    }

    @Override
    public void logout() {
        presenter.logout();
    }

    private void printMenu() {
        System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator()
                + "*          Команды для управления программой:" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* menu - показать меню" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* show - посмотреть список папок" + System.lineSeparator()
                + "*" + System.lineSeparator()
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
                + "* все слова, параметры в команде load пишутся через один пробел" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* find X - найти сообщения с хотя бы одним из указанных слов, вместо Х указать необходимые слова через один пробел" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* write - записать в файл" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* clear - удалить загруженное" + System.lineSeparator()
                + "*" + System.lineSeparator()
                + "* stop - выход (авторизация сохранена)" + System.lineSeparator()
                + "*" + System.lineSeparator()
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
