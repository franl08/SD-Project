package Client;


import Model.Reservation;
import UI.UI;
import Utils.Demultiplexer;
import Utils.TaggedConnection;

import java.net.Socket;
import java.util.Map;

public class Client extends User{

    private final UI ui = new UI();

    private Map<String, Reservation> reservations;

    public Map<String, Reservation> getReservations(){
        return reservations;
    }


    public Client(String username, String email, String fullName, String password) {
        super(username, email, fullName, password);
    }

    public Client(Client c){
        super(c.getUsername(), c.getEmail(), c.getFullName(), c.getPassword());
    }

    public Client clone(){
        return new Client(this);
    }

    public void addReservation(Reservation r){
        reservations.put(r.getID(), r);
    }

    public boolean removeReservation(String s){
        if(!this.reservations.containsKey(s)) return false;
        this.reservations.remove(s);
        return true;
    }

    public void run() {
        try {
            Socket s = new Socket("localhost", 12345);
            TaggedConnection tc = new TaggedConnection(s);
            Demultiplexer dm = new Demultiplexer(tc);
            dm.start();

            Map.Entry<String, String> credentialsPair = ui.login();
            boolean loggingIn = User.loggingIn(s,dm,tc,credentialsPair);
            if (loggingIn) {
                // New menu
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
