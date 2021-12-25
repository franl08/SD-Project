package Client;


import Model.Reservation;
import Utils.Demultiplexer;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.Map;

public class Client extends User{

    private Demultiplexer dm;
    private Socket s;
    private BufferedReader input;

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

    }

}
