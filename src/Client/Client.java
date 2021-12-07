package Client;


import Model.Reservation;
import UI.UI;
import Utils.Demultiplexer;
import Utils.TaggedConnection;

import java.net.Socket;
import java.util.Map;

public class Client extends User{

    private Map<String, Reservation> reservations;

    public Map<String, Reservation> getReserves(){
        return reservations;
    }

    public void addReserve(Reservation r){
        reservations.put(r.getID(), r);
    }

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer dm = new Demultiplexer(new TaggedConnection(s));
        dm.start();

        UI.clientMenu();


    }
}
