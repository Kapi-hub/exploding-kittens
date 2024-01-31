package network.server;

import java.io.PrintWriter;
import java.util.Scanner;

public class ServerTUI {

    /** The PrintWriter to write messages to */
    private PrintWriter console;

    /**
     * Constructs a new ServerTUI. Initializes the console.
     */
    public ServerTUI() {
        console = new PrintWriter(System.out, true);
    }

    public void showMessage(String message) {
        console.println("> " + message);
    }

    public String getString(String question) {
        console.println(question + ": ");
        Scanner in = new Scanner(System.in);
        return (in.nextLine());
    }

    public int getInt(String question) {
        int number = 0;
        boolean valid = false;

        while (!valid) {
            try {
                console.println(question + ": ");
                Scanner in = new Scanner(System.in);
                number = Integer.parseInt(in.nextLine());
                valid = true;
            } catch (NumberFormatException e){
                console.println("Insert a number!" + e.getMessage());
                getInt(question);
            }
        }
        return number;
    }
}
