package Utils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allows connecting the client with the server
 */
public class Demultiplexer {

    /**
     * Tagged connection
     */
    private TaggedConnection tc;
    /**
     * Lock
     */
    private ReentrantLock l = new ReentrantLock();
    /**
     * Map with the frame values
     */
    private Map<Integer, FrameValue> map = new HashMap<>();
    /**
     * Exception
     */
    private IOException exception = null;

    /**
     * Represents a frame value
     */
    private class FrameValue {
        int waiters = 0;
        Queue<byte[]> queue = new ArrayDeque<>();
        Condition c = l.newCondition();

        public FrameValue() {

        }
    }

    /**
     * Constructor
     * @param conn Tagged Connection
     */
    public Demultiplexer(TaggedConnection conn) {
        this.tc = conn;
    }

    /**
     * Starts the demultiplexer
     */
    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame frame = tc.receive();
                    l.lock();
                    try {
                        FrameValue fv = map.get(frame.tag);
                        if (fv == null) {
                            fv = new FrameValue();
                            map.put(frame.tag, fv);
                        }
                        fv.queue.add(frame.data);
                        fv.c.signal();
                    } finally {
                        l.unlock();
                    }
                }
            }
            catch (IOException e) {
                exception = e;
            }
        }).start();
    }

    /**
     * Sends data through a tagged connection
     * @param tag Tag
     * @param username User ID of the client
     * @param data Data
     * @throws IOException Tagged connection I/O error
     */
    public void send(int tag, String username, byte[] data) throws IOException {
        tc.send(tag, username, data);
    }

    /**
     * Allows receiving data
     * @param tag Tag
     * @return Data in bytes
     * @throws IOException I/O Exception
     * @throws InterruptedException Interrupted Exception
     */
    public byte[] receive(int tag) throws IOException, InterruptedException {
        l.lock();
        FrameValue fv;
        try {
            fv = map.get(tag);
            if (fv == null) {
                fv = new FrameValue();
                map.put(tag, fv);
            }
            fv.waiters++;
            while(true) {
                if(! fv.queue.isEmpty()) {
                    fv.waiters--;
                    byte[] reply = fv.queue.poll();
                    if (fv.waiters == 0 && fv.queue.isEmpty())
                        map.remove(tag);
                    return reply;
                }
                if (exception != null) {
                    throw exception;
                }
                fv.c.await();
            }
        }
        finally {
            l.unlock();
        }
    }


    /**
     * Closes a tagged connection
     * @throws IOException I/O error closing
     */
    public void close() throws IOException {
        tc.close();
    }
}
