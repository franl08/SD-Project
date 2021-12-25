package Server;

import Client.User;
import Model.Model;
import Utils.Demultiplexer;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class Server {

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(12345);
        Model model = new Model();
        // TODO: Se o servidor existir fetch contas

        while(true) {
            Socket s = ss.accept();
            TaggedConnection connection = new TaggedConnection(s);

            Runnable worker = () -> {

                try (connection) {

                    while (true) {

                        Frame f = connection.receive();
                        boolean isClient = f.isClient == '1';

                        if (f.tag == 0) { // Login attempt

                            String answer = "0";

                            if (model.checkAutentication(f.username, new String(f.data)))
                                answer = "1";

                            connection.send(f.tag, f.isClient, "", answer.getBytes());

                        } else if (f.tag == 1) { // Registration attempts

                            String username = f.username;
                            String password = new String(f.data);

                            String answer = "0";

                            if (!model.lookupUser(username) && isClient) {
                                answer = "1"; // Success

                                // Criar cliente

                            } else if (!isClient)
                                answer = "2"; // Admin can't perform registration

                            connection.send(f.tag, f.isClient, "", answer.getBytes());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            };

            Thread t = new Thread(worker);
            t.start();


        }
    }
}
