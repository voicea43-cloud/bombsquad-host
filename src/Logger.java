import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Logger {
    public static synchronized void log(String type, String message) {
        System.out.println("[" + type + "] " + message);
    }
}
