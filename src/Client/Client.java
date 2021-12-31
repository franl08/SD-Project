package Client;

import Utils.Demultiplexer;
import Utils.TaggedConnection;
import Utils.Colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {

        Socket s = new Socket("localhost", 12345);
        Demultiplexer dm = new Demultiplexer(new TaggedConnection(s));

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        dm.start();

        boolean quit = false;
        while (!quit) {

            /*

                    Initial Menu: Login + Registration

             */
            System.out.println(Colors.ANSI_GREEN + "\n" +
                    "\n" +
                    "  ____                         _ _                  _    _      _ _            \n" +
                    " |  _ \\ ___  ___  ___  ___  __| (_)_ __   __ _     / \\  (_)_ __| (_)_ __   ___ \n" +
                    " | |_) / _ \\/ __|/ _ \\/ _ \\/ _` | | '_ \\ / _` |   / _ \\ | | '__| | | '_ \\ / _ \\\n" +
                    " |  _ <  __/\\__ \\  __/  __/ (_| | | | | | (_| |  / ___ \\| | |  | | | | | |  __/\n" +
                    " |_| \\_\\___||___/\\___|\\___|\\__,_|_|_| |_|\\__, | /_/   \\_\\_|_|  |_|_|_| |_|\\___|\n" +
                    "                                         |___/                                 \n" +
                    "\n\n" + Colors.ANSI_RESET);

            System.out.println(Colors.ANSI_PURPLE + "******************* Welcome *******************\n" + Colors.ANSI_RESET);
            System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Login.");
            System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Registration.");
            System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
            System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

            String option = input.readLine();
            if (option.equals("1")) { // Login

                /*

                        Login Menu

                 */

                System.out.println(Colors.ANSI_GREEN + "\n********** Login **********\n" + Colors.ANSI_RESET);
                System.out.print(Colors.ANSI_YELLOW + "Insert email: " + Colors.ANSI_RESET);
                String username = input.readLine();
                System.out.print(Colors.ANSI_YELLOW + "Insert password: " + Colors.ANSI_RESET);
                String password = input.readLine();

                dm.send(0, username, password.getBytes());

                String answerLogin = new String(dm.receive(0));
                if (answerLogin.equals("Success")) {

                    boolean homeMenuQuit = false;
                    while (!homeMenuQuit) {

                        if (username.equals("admin")) {

                            /*

                                    Admin home menu

                             */

                            System.out.println(Colors.ANSI_GREEN + "\n********** Admin Home Menu **********\n" + Colors.ANSI_RESET);
                            System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Add flight.");
                            System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Close day.");
                            System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
                            System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

                            option = input.readLine();
                            if (option.equals("1")) {

                                // Add flight

                            } else if (option.equals("2")) {

                                // Close day

                            } else if (option.equals("0"))
                                homeMenuQuit = true;
                            else
                                System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);

                        } else {

                            /*

                                    Client home menu

                             */

                            System.out.println(Colors.ANSI_GREEN + "\n********** Home Menu **********\n" + Colors.ANSI_RESET);
                            System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Make a reservation.");
                            System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Get existing flights.");
                            System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
                            System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

                            option = input.readLine();
                            if (option.equals("1")) {

                                // Make a reservation

                            } else if (option.equals("2")) {

                                // Get existing flights

                            } else if (option.equals("0"))
                                homeMenuQuit = true;
                            else
                                System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);
                        }
                    }

                } else
                    System.out.println("Unknown credentials");

            } else if (option.equals("2")) {

                /*

                        Registration Menu

                 */

                System.out.println(Colors.ANSI_GREEN + "\n********** Registration **********\n" + Colors.ANSI_RESET);
                System.out.print(Colors.ANSI_YELLOW + "Insert email: " + Colors.ANSI_RESET);
                String username = input.readLine();
                System.out.print(Colors.ANSI_YELLOW + "Insert password: " + Colors.ANSI_RESET);
                String password = input.readLine();

                dm.send(1, username, password.getBytes());

                String answerLogin = new String(dm.receive(1));
                if (answerLogin.equals("Success"))
                    System.out.println(Colors.ANSI_PURPLE + "\nAccount successfully created." + Colors.ANSI_RESET);
                else
                    System.out.println(Colors.ANSI_RED + "\nEmail already taken." + Colors.ANSI_RESET);

            } else if (option.equals("0")) {

                /*

                        Quiting program

                 */

                quit = true;
                System.out.println(Colors.ANSI_PURPLE + "\nExiting app..." + Colors.ANSI_RESET);
                dm.close();

            } else
                System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);

        }

    }

}
