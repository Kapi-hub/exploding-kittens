package network.client;

import network.exceptions.ExitProgram;
import network.exceptions.InvalidClientMove;
import network.exceptions.ServerUnavailableException;
import network.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ClientTUI {
    private Client client;
    private Boolean hasWrittenToTerminal = false;

    /* ************************************
                  CONSTRUCTOR
    ************************************ */

    public ClientTUI(Client client) {
        this.client = client;
    }

    /* ************************************
               GETTERS AND SETTERS
    ************************************ */

    public Boolean getHasWrittenToTerminal() {
        return hasWrittenToTerminal;
    }

    /* ************************************
                    GENERAL
    ************************************ */

    /**
     * Listens for user input and redirects it to
     * {@link #handleUserInput(String)}.
     * @throws ServerUnavailableException if could not
     * send the userInput to the {@link #handleUserInput(String)}
     */
    public void start()
            throws ServerUnavailableException,
            RuntimeException,
            InvalidClientMove {
        Scanner in = new Scanner(System.in);
        while (true){
            synchronized (client.getLock()) {
                String userInput = in.nextLine();
                try {
                    this.handleUserInput(userInput);
                } catch (ServerUnavailableException | ExitProgram e) {
                    client.doAbort();
                }
            }

        }
    }

    public void handleUserInput(String input)
            throws ExitProgram, ServerUnavailableException, InvalidClientMove {
        String[] split = input.split(" ");

        String protocolCommand = split[0].toLowerCase();
        switch (protocolCommand) {
            case "play":
                client.doPlay(input);
                break;
            case "insert":
                client.doInsert(split[1]);
                break;
            case "draw":
                client.doDraw(); // split[1] = guestName
                break;
            case "abort":
                client.doAbort();
                break;
            case "private":
                client.doPrivateMessage(split[1]);
                break;
            case "chat":
                // Create ArrayList from Array, build new string without
                // "chat command.
                List<String> list = new ArrayList<>(Arrays.asList(split));
                list.remove("chat");
                client.doBroadcast(String.join(" ", list));
                break;
            default:
                this.printMessage("Invalid command");
                this.printMessage("Type >> ");
                break;
        }
    }

    public void printMessage(String message) {
        System.out.print('\n' + message);
    }

    public void printMessageFromServer(String message) {
        System.out.print("\nSERVER: " + message);
    }
}
