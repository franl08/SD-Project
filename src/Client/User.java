package Client;

import Utils.Demultiplexer;
import Utils.TaggedConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public abstract class User {
    private String username;
    private String password;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    public String getUsername(){
        return this.username;
    }

    public abstract User clone();

    public abstract void run();

    public static void main(String[] args) throws IOException, InterruptedException {

        Socket s = new Socket("localhost", 12345);
        Demultiplexer dm = new Demultiplexer(new TaggedConnection(s));

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        dm.start();

        char typeUser = '0'; // '1' if client, '2' if admin
        int typeAuthentication = -1;

        boolean quit = false;
        while (!quit) {

            System.out.println("Welcome to Receeding Airline");
            System.out.println("1. Client authentication.");
            System.out.println("2. Admin authentication.");
            System.out.println("0. Quit.");
            System.out.print("Insert option: ");

            int option = Integer.parseInt(input.readLine());
            if (option == 1 || option == 2) {

                typeUser = (option == 1) ? '1' : '2';
                boolean turnBack = false;
                System.out.println("Login/Registration");
                System.out.println("1. Login.");
                System.out.println("2. Registration.");
                System.out.println("0. Quit.");
                typeAuthentication = Integer.parseInt(input.readLine());
                if (typeAuthentication == 1) {

                    System.out.print("Insert username: ");
                    String username = input.readLine();
                    String password = input.readLine();

                    dm.send(0, typeUser, username, password.getBytes());

                    String answerLogin = new String(dm.receive(0));
                    if (answerLogin.equals("1")) {

                        // TODO: SEPARAR PARA OS RUNS DE CADA CLASSE
                        if (typeUser == '1') {

                            // Funcionalidades de cliente

                        } else {

                            // Funcionalidades de admin

                        }

                    } else
                        System.out.println("Unknown credentials");
                }
                else if (typeAuthentication == 2) {

                } else if (typeAuthentication == 0)
                    turnBack = true;
                else {
                }


            } else if (option == 0) {
                quit = true;
                System.out.println("Exiting app.");
            } else
                System.out.println("Invalid option.");

        }

        
    }

}
