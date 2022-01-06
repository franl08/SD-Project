package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that allows writing the functionalities that the server made to a log file
 */
public class Log {

    /**
     * Locks the resources
     */
    private Lock l;

    /**
     * Empty constructor
     */
    public Log() {
        this.l = new ReentrantLock();
    }

    /**
     * Writes a message to the file
     * @param message Message
     */
    public void appendMessage(String message){
        l.lock();
        try {
            File f = new File(".logs.txt");
            try {
                if (!f.exists())
                    f.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
                out.write(message + "\n");
                out.flush();
                out.close();
            } catch (Exception ignored) {}
        } finally {
            l.unlock();
        }
    }

    /**
     * Places a separator in the log file
     * @param date Date and time when the server was run
     */
    public void appendSeparator(LocalDateTime date) {


        l.lock();
        try {
            File f = new File(".logs.txt");
            try {
                if (!f.exists())
                    f.createNewFile();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                String formatDateTime = date.format(formatter);

                BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
                out.write("\n-----------------------------------HF-----------------------------------\n");
                out.write("Date: " + formatDateTime + "\n\n");
                out.flush();
                out.close();

            } catch (Exception ignored) {}
        } finally {
            l.unlock();
        }
    }
}
