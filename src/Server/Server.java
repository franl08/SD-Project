package Server;

import Model.Model;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(12345);
        Model model = new Model();

        while(true) {
            Socket s = ss.accept();
            TaggedConnection connection = new TaggedConnection(s);

            Runnable worker = () -> {

                try (connection) {
                    Frame f = connection.receive();
                    int tag = f.tag;
                    byte[] data = f.data;

                    ByteBuffer buffer = ByteBuffer.allocate(data.length);
                    int lengthUsername = buffer.getInt();
                    byte[] username = new byte[lengthUsername];
                    buffer.get(username, 0, lengthUsername);

                    int lengthPassword = buffer.getInt();
                    byte[] password = new byte[lengthPassword];
                    buffer.get(password, 0, lengthPassword);

                    byte[] answer = new byte[1];

                    if (model.checkAutentication(Arrays.toString(username), Arrays.toString(password))) {
                        answer[0] = (byte) '1';
                    } else {
                        answer[0] = (byte) '0';
                    }

                    connection.send(f.tag, answer);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            };

            Thread t = new Thread(worker);
            t.start();


        }
    }
}
