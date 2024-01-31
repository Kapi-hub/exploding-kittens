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
}
