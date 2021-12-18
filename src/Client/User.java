package Client;

import UI.*;
import Utils.Demultiplexer;
import Utils.TaggedConnection;

import java.net.Socket;
import java.util.Map;

public abstract class User {
    private String username;
    private String email;
    private String fullName;
    private String password;

    public User(String username, String email, String fullName, String password){
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getEmail(){
        return this.email;
    }

    public String getFullName(){
        return this.fullName;
    }

    public abstract void run();

    public static boolean loggingIn(Socket s, Demultiplexer dm, TaggedConnection tc, Map.Entry<String,String> credentialsPair) {
        try {
            String username = credentialsPair.getKey();
            String password = credentialsPair.getValue();

            dm.send(0, (username.length() + username + password.length() + password).getBytes());

            byte[] validAutentication = dm.receive(0);
            char valid = (char) validAutentication[0];

            return valid == '1';

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public abstract User clone();

    /*
    public static void main(String[] args) {

        UI ui = new UI();
        String option = ui.clientMenu();

        User u = switch (Integer.parseInt(option)) {
            case 1 -> new Client();
            case 2 -> new Admin();
            default -> null;
        };

        if (u != null)
            u.run();
        else
            System.out.println("Invalid option");

    }
    */
}
