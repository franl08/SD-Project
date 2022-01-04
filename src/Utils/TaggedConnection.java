package Utils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allows connecting the client and the server
 */
public class TaggedConnection implements AutoCloseable {

    /**
     * Data input stream
     */
    private final DataInputStream dis;
    /**
     * Data output stream
     */
    private final DataOutputStream dos;
    /**
     * Receiving lock
     */
    private final Lock rl = new ReentrantLock();
    /**
     * Sending lock
     */
    private final Lock wl = new ReentrantLock();





    /**
     * Class that represents the data through a frame
     */
    public static class Frame {
        /**
         * Tag
         */
        public final int tag;
        /**
         * Username of the client of the communication
         */
        public final String username;
        /**
         * Data exchanged
         */
        public final byte[] data;

        /**
         * Parametrized constructor
         * @param tag Tag
         * @param username User ID
         * @param data Data
         */
        public Frame(int tag, String username, byte[] data) {
            this.tag = tag;
            this.username = username;
            this.data = data; }
    }





    /**
     * Opens the data input and output stream through a socket
     * @param socket Socket
     * @throws IOException I/O exception from opening the data streams
     */
    public TaggedConnection(Socket socket) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Sends a frame
     * @param frame Frame
     * @throws IOException I/O error sending
     */
    public void send(Frame frame) throws IOException {
        try {
            wl.lock();
            this.dos.writeInt(frame.tag);
            this.dos.writeUTF(frame.username);
            this.dos.writeInt(frame.data.length);
            this.dos.write(frame.data);
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    /**
     * Sends the data
     * @param tag Tag
     * @param username User ID
     * @param data Data
     * @throws IOException I/O error sending
     */
    public void send(int tag, String username, byte[] data) throws IOException {
        this.send(new Frame(tag, username, data));
    }

    /**
     * Receives a frame
     * @return Frame received
     * @throws IOException I/O error receiving
     */
    public Frame receive() throws IOException {
        int tag;
        String username;
        byte[] data;
        try {
            rl.lock();
            tag = this.dis.readInt();
            username = this.dis.readUTF();
            int n = this.dis.readInt();
            data = new byte[n];
            this.dis.readFully(data);
        }
        finally {
            rl.unlock();
        }
        return new Frame(tag, username, data);
    }

    /**
     * Closes the connection
     * @throws IOException I/O error closing
     */
    public void close() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}
