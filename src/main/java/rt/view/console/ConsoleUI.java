package rt.view.console;

import rt.infrastructure.config.ParsingPropertiesHandler;
import rt.infrastructure.notifier.Notifier;
import rt.model.preset.PresetDTO;
import rt.view.View;

public class ConsoleUI extends View {

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
                        showFoldersAndChannels();
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
                    case "set" -> {
                        setMaxAmount(args[1]);
                    }
                    case "request" -> {
                        showPresets();
                    }
                    case "use" -> {
                        usePreset(args[1] + " " + args[2] + " " + args[3]);
                    }
                    case "rename" -> {
                        invokeRenamePresetMethod(args[1] + " " + args[2] + " " + args[3]);
                    }
                    case "remove" -> {
                        removePresetByName(args[1]);
                    }
                    default -> {
                        System.out.println("неизвестная команда");
                    }
                }
            }
        } catch (
                Exception e) {
            System.err.println("Исключение в консольном потоке: " + e.getMessage());
        }
    }

    private void showFoldersAndChannels() {
        showFoldersIDsAndNames();
        showChannelsIDsAndNames();
    }

    private void showFoldersIDsAndNames() {
        StringBuilder builder = new StringBuilder();
        serviceManager.getFoldersIDsAndNames()
                .forEach((k, v) -> builder
                        .append(k).append(": ").append(v)
                        .append(System.lineSeparator()));
        print(builder.toString());
    }

    private void showChannelsIDsAndNames() {
        StringBuilder builder = new StringBuilder();
        serviceManager.getChannelsIDsAndNames()
                .forEach((k, v) -> builder
                        .append(k).append(": ").append(v)
                        .append(System.lineSeparator()));
        print(builder.toString());
    }

    private void printMenu() {
        System.out.println(
                "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator()
                        + "*          Команды для управления программой:" + System.lineSeparator()
                        + "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* menu - показать меню" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* show - посмотреть список папок" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* команды для загрузки сообщений строятся по схеме:" + System.lineSeparator()
                        + "* load X DD.MM.YYYY DD.MM.YYYY" + System.lineSeparator()
                        + "* вместо Х может быть:" + System.lineSeparator()
                        + "* - номера папок или каналов через запятую без пробелов для загрузки сообщений только из указанных источников" + System.lineSeparator()
                        + "* - all для загрузки сообщений из всех пабликов" + System.lineSeparator()
                        + "* вместо DD.MM.YYYY может быть указана дата в данном формате" + System.lineSeparator()
                        + "* - если даты не указаны вообще, то будет загружено примерно " + ParsingPropertiesHandler.getMessagesToDownload() + " сообщ. с канала" + System.lineSeparator()
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
                        + "* write - записать в файл все загруженные сообщения" + System.lineSeparator()
                        + "* write x - записать в файл только отобранные через поиск сообщения (при наличии)" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* clear - удалить загруженное" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* set X - установить количество сообщений для загрузки из одного канала, если загружать без указания даты (вместо X число от 100 до 5000)" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* request - посмотреть последний запрос" + System.lineSeparator()
                        + "* use X - использовать запрос с указанным именем (вместо Х ввести имя запроса)" + System.lineSeparator()
                        + "* rename X >> Y - переименовать запрос с указанным именем (вместо Х ввести старое имя, вместо Y - новое имя запроса)" + System.lineSeparator()
                        + "* remove X - удалить запрос с указанным именем (вместо Х ввести имя удаляемого запроса)" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* stop - выход (авторизация сохранена)" + System.lineSeparator()
                        + "* * * * * * * * * * * * * *" + System.lineSeparator()
                        + "* logout - выйти из аккаунта" + System.lineSeparator()
                        + "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + System.lineSeparator());
    }

    @Override
    public void print(String text) {
        System.out.println(text);
    }

    @Override
    public void showPresets() {
        StringBuilder sb = new StringBuilder();
        for (PresetDTO preset : serviceManager.getPresets()) {
            String name = preset.name();
            String folders = String.join(", ", preset.folders());
            String channels = String.join(", ", preset.channels());
            String startString = preset.start();
            String endString = preset.end();
            sb
                    .append(name).append(": ")
                    .append(folders).append(" ")
                    .append(channels).append(" ")
                    .append(startString).append(" ")
                    .append(endString).append(System.lineSeparator());
        }
        print(sb.toString()
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" ?\n ?", "\n")
                .trim());
    }

    private void invokeRenamePresetMethod(String input) {
        String[] names = input.split(">>");
        if (names.length < 2) {
            Notifier.getInstance().addNotification("Не хватает параметров для переименования");
            return;
        }
        renamePreset(names[0].trim(), names[1].trim());
    }

    @Override
    public String askParameter(String who, String question) {
        return ConsoleInput.askParameter(who, question);
    }
}