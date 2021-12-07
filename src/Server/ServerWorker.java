package Server;

import Model.Model;
import Utils.TaggedConnection;
import Utils.TaggedConnection.Frame;

public class ServerWorker implements Runnable{

    private final TaggedConnection tc;

    public ServerWorker(TaggedConnection tc) {
        this.tc = tc;
    }

    public void run() {

        try (this.tc) {
            for (; ; ) {

                Frame f = this.tc.receive();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
