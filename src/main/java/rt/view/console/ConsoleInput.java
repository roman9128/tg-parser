package rt.view.console;

import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConsoleInput {

    private static final Object LOCK = new Object();
    private static InputStreamReader scanner = null;

    public static String askParameter(String displayName, String question) {
        synchronized (LOCK) {
            Console console = System.console();
            if (console != null) {
                return console.readLine("%s: %s: ", displayName, question);
            } else {
                if (scanner == null) {
                    scanner = new InputStreamReader(System.in);
                }
                System.out.printf("%s: %s: ", displayName, question);
                try {
                    return interruptibleReadLine(scanner);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static String readLine() {
        synchronized (LOCK) {
            Console console = System.console();
            if (console != null) {
//                Charset consoleCharset = console.charset();
//                System.out.println("Кодировка консоли, определенная Java: " + consoleCharset.name());
//
//                String line = console.readLine();
//                System.out.println("Строка, считанная из консоли: " + line);
//
//                byte[] bytes = line.getBytes(consoleCharset);
//
//                String utf8String = new String(bytes, StandardCharsets.UTF_8);
//                System.out.println("Строка в UTF-8: " + utf8String);
//                return  utf8String;
                return console.readLine();
            } else {
                if (scanner == null) {
                    scanner = new InputStreamReader(System.in);
                }
                try {
                    return interruptibleReadLine(scanner);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String interruptibleReadLine(Reader reader) throws InterruptedException, IOException {
        Pattern line = Pattern.compile("^(.*)\\R");
        Matcher matcher;
        boolean interrupted;
        StringBuilder result = new StringBuilder();
        int chr = -1;
        do {
            if (reader.ready()) {
                chr = reader.read();
            }
            if (chr > -1) {
                result.append((char) chr);
            }
            matcher = line.matcher(result.toString());
            interrupted = Thread.interrupted();
        } while (!interrupted && !matcher.matches());
        if (interrupted) {
            throw new InterruptedException();
        }
        return (matcher.matches() ? matcher.group(1) : "");
    }
}