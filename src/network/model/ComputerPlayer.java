package network.model;

import network.client.Client;
import network.exceptions.ExitProgram;
import network.exceptions.InvalidClientMove;
import network.exceptions.ServerUnavailableException;

public class ComputerPlayer extends Player {
    private Client client;
    private boolean isMyTurnOrNot;
    private boolean mustInsertBack;
    private String[] cardsInHand = null;

    public ComputerPlayer(String[] args) {
        super(args[0]);
    }

    public boolean isMyTurnOrNot() {
        return isMyTurnOrNot;
    }

    public void setMyTurnOrNot(boolean myTurnOrNot) {
        isMyTurnOrNot = myTurnOrNot;
    }

    public boolean isMustInsertBack() {
        return mustInsertBack;
    }

    public void setMustInsertBack(boolean mustInsertBack) {
        this.mustInsertBack = mustInsertBack;
    }

    public String doMove() throws ExitProgram, ServerUnavailableException, InvalidClientMove {
        for (String card : cardsInHand) {
            if (card.equals(Card.SHUFFLE.name())
            || card.equals(Card.SKIP.name())
            || card.equals(Card.ATTACK.name())
            || card.equals(Card.FUTURE.name()))
                return "play " + card;
        }
        return "draw";
    }

    public String getIndexToReinsertExplode() throws ExitProgram, ServerUnavailableException, InvalidClientMove {
        return "0";
    }

    // Return the first card from bot's hand
    public String getCardInFavorResponse() {
        return "play " + cardsInHand[1];
    }

    public void decomposeHand(String splittedMessage) {
        String malformedMsg = splittedMessage
                .replace("Your hand: [", "")
                .replace("]", "");
        cardsInHand = malformedMsg.split(", ");
    }

}
