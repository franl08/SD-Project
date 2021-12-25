package Utils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable{

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Lock rl = new ReentrantLock();
    private final Lock wl = new ReentrantLock();

    public static class Frame {
        public final int tag;
        public final char isClient;
        public final String username;
        public final byte[] data;

        public Frame(int tag, char isClient, String username, byte[] data) {
            this.tag = tag;
            this.isClient = isClient;
            this.username = username;
            this.data = data; }
    }

    public TaggedConnection(Socket socket) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(Frame frame) throws IOException {
        try {
            wl.lock();
            this.dos.writeInt(frame.tag);
            this.dos.writeChar(frame.isClient);
            this.dos.writeUTF(frame.username);
            this.dos.writeInt(frame.data.length);
            this.dos.write(frame.data);
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    public void send(int tag, char isClient, String username, byte[] data) throws IOException {
        this.send(new Frame(tag, isClient, username, data));
    }

    public Frame receive() throws IOException {
        int tag;
        char isClient;
        String username;
        byte[] data;
        try {
            rl.lock();
            tag = this.dis.readInt();
            isClient = this.dis.readChar();
            username = this.dis.readUTF();
            int n = this.dis.readInt();
            data = new byte[n];
            this.dis.readFully(data);
        }
        finally {
            rl.unlock();
        }
        return new Frame(tag, isClient, username, data);
    }

    public void close() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}
