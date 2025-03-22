package rt.view;

import rt.model.auxillaries.PropertyHandler;
import rt.model.core.Status;
import rt.presenter.Presenter;

import java.util.Scanner;

public class ConsoleUI implements View {
    private Presenter presenter;

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void startInteractions() {
        while (true) {
            if (Status.isReadyToInteract()) {
                break;
            } else {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                String[] args = {"", "", "", ""};
                String[] userCommand = scanner.nextLine().split(" ", 4);
                System.arraycopy(userCommand, 0, args, 0, userCommand.length);
                switch (args[0]) {
                    case "show" -> {
                        show();
                    }
                    case "load" -> {
                        load(args[1], args[2], args[3]);
                    }
                    case "write" -> {
                        write();
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
                    default -> {
                        System.out.println("неизвестная команда");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Исключение в консольном потоке: " + e.getMessage());
        } finally {
            System.out.println("Консольный поток закрыт");
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
    public void write() {
        presenter.write();
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
        System.out.println("_________________________________" + System.lineSeparator()
                + "|          Введи команду:" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| show - посмотреть список папок" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| команды для загрузки сообщений строятся по схеме:" + System.lineSeparator()
                + "| load X DD.MM.YYYY DD.MM.YYYY" + System.lineSeparator()
                + "| вместо Х может быть:" + System.lineSeparator()
                + "| - число для обозначения номера папки для загрузки сообщений только из указанной папки" + System.lineSeparator()
                + "| - all для загрузки сообщений из всех пабликов" + System.lineSeparator()
                + "| вместо DD.MM.YYYY может быть указана дата в данном формате" + System.lineSeparator()
                + "| - если даты не указаны вообще, то будет загружено не менее " + PropertyHandler.getMessagesToDownload() + " сообщ. с канала" + System.lineSeparator()
                + "| - если указана одна дата, то загрузятся сообщения с начала указанного дня до текущего момента" + System.lineSeparator()
                + "| - если указано две даты, то загрузятся сообщения с начала первого указанного дня до конца второго указанного дня" + System.lineSeparator()
                + "| первый параметр (Х) можно не указывать, если далее нет дат" + System.lineSeparator()
                + "| все слова, параметры в команде load пишутся через один пробел" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| write - записать в файл" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| clear - удалить загруженное" + System.lineSeparator()
                + "|" + System.lineSeparator()
                + "| stop - выход (авторизация сохранена)" + System.lineSeparator()
                + "| logout - выйти из аккаунта" + System.lineSeparator()
                + "|_________________________________" + System.lineSeparator());
    }

    @Override
    public void print(String text) {
        System.out.println(text);
    }
}
