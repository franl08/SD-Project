package Client;

import Exceptions.NoNoticesException;
import Utils.Colors;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that allows storing notices for a client
 */
public class Notices {

    /**
     * Allows locking the resources
     */
    public Lock l;
    /**
     * Notices stored
     */
    public Queue<String> notices;

    /**
     * Empty constructor
     */
    public Notices() {
        this.l = new ReentrantLock();
        this.notices = new PriorityQueue<>();
    }

    /**
     * Adds a notice
     * @param s Notice
     */
    public void addNotice(String s) {
        l.lock();
        try {
            this.notices.add(s);
        } finally {
            l.unlock();
        }
    }

    /**
     * Formats all the notices in a string
     * @return Formatted string
     * @throws NoNoticesException There are no notices in the queue
     */
    public String displayPendingNotices() throws NoNoticesException {

        l.lock();
        try {
            if (this.notices.isEmpty()) throw new NoNoticesException();
            else {
                StringBuilder s = new StringBuilder();
                for (String n : this.notices) {
                    if (n.equals("Error"))
                        s.append(Colors.ANSI_RED + " -> * Reservation could not be made.\n").append(Colors.ANSI_RESET);
                    else
                        s.append(Colors.ANSI_PURPLE + " -> * Reservation made with success. Code is " + Colors.ANSI_RESET).append(n).append("\n" );

                }
                this.notices.clear();
                return s.toString();
            }
        } finally {
            l.unlock();
        }
    }
}
