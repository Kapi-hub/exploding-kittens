package network.model;

import network.controller.GameController;

import java.util.*;

public class Game {
    private Deck deck;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Card> discardPile;
    private int currentPlayerIndex;
    private Player currentPlayer;
    private GameController controller;

    @SuppressWarnings("FieldCanBeLocal")
    private boolean winner = false;

    private final int INITIAL_NUMBER_OF_PLAYERS;
    private int currentNumberOfPlayers;

    /* ************************************
              GETTERS & SETTERS
    ************************************ */
    public Deck getDeckObject() {
        return deck;
    }

    public int getCurrentPlayerTurnIndex() {
        return currentPlayerIndex;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Card> getDiscardPile() {
        return discardPile;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean hasWinner() {
        return this.winner;
    }

    /* ************************************
                CONSTRUCTORS
    ************************************ */

    /**
     * @param players ArrayList of players of the game
     * @requires players != null;
     */
    public Game(final ArrayList<Player> players, final GameController controller) {
        players.forEach(player -> this.players.add(player));
        INITIAL_NUMBER_OF_PLAYERS = this.players.size();
        currentNumberOfPlayers = this.players.size();
        currentPlayerIndex = (int) (Math.random() * INITIAL_NUMBER_OF_PLAYERS);
        currentPlayer = players.get(currentPlayerIndex);
        deck = new Deck();
        discardPile = new ArrayList<>();
        this.controller = controller;
    }

    /* ************************************
                    METHODS
    ************************************ */

    /**
     * Sets up the method exactly as the rules indicate.
     * First method combines STEP 1 & 2 from the rules, removing
     * the Exploding Kittens (4) and Defuses (6) cards.
     * Second method deals the right number of cards to players.
     * That is, each player gets a Defuse card. Then, depending on
     * NUMBER_OF_PLAYERS, there are inserted 1 or 2. Defuse cards back
     * in the deck. Then, each player gets 7 cards from the deck, for
     * a total of 8. Third method inserts NUMBER_OF_PLAYERS - 1
     * (such that there remains a winner) Exploding cards back into the deck.
     * The deck is then shuffled and ready to be played.
     *
     * @ensures Player's hands (player.getHand()) have 8 cards
     * (7 random + 1 DEFUSE)
     * @ensures Deck has EXPLODING cards = (numberOfPlayers - 1)
     */
    public void init() {
        this.removeExplodeAndDefuseCards();
        this.dealCardsToPlayers();
        this.insertExplodeCards();
    }

    /**
     * Combines STEP 1 & 2 from the rules, removing the Exploding Kittens (4)
     * and Defuses (6) cards.
     *
     * @ensures Deck has no more Explode / Defuse cards.
     */
    public void removeExplodeAndDefuseCards() {
        deck.getDeckArray().removeIf(card ->
                (card == Card.EXPLODE || card == Card.DEFUSE));
    }

    /**
     * Deals the right number of cards to players. That is,
     * each player gets a Defuse card. Then, depending on NUMBER_OF_PLAYERS,
     * there are inserted 1 or 2 Defuse cards back in the deck.
     * Then, each player gets 7 cards from the deck, for a total of 8.
     *
     * @ensures Each player has 8 cards && (1 or 2) Defuse cards in deck.
     */
    public void dealCardsToPlayers() {
        //noinspection DuplicatedCode - Because duplicated code is in local
        players.forEach(player -> player.getHand().add(Card.DEFUSE));

        this.deck.getDeckArray().add(Card.DEFUSE);
        if (INITIAL_NUMBER_OF_PLAYERS != 5) {
            this.deck.getDeckArray().add(Card.DEFUSE);
        }

        Collections.shuffle(deck.getDeckArray());

        for (int cardIndex = 0; cardIndex < Player.MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE; cardIndex++) {
            players.forEach(player -> player.getHand().add(deck.getLastCardAndRemove()));
        }
    }

    /**
     * Inserts INITIAL_NUMBER_OF_PLAYERS - 1 (such that there remains a winner) Exploding
     * cards back into the deck. The deck is then shuffled and ready to be played.
     *
     * @ensures Deck shuffled & ready to play
     */
    public void insertExplodeCards() {
        for (int cardIndex = 0; cardIndex < INITIAL_NUMBER_OF_PLAYERS - 1; cardIndex++) {
            deck.getDeckArray().add(Card.EXPLODE);
        }
        Collections.shuffle(deck.getDeckArray());
//        deck.getDeckArray().add(Card.EXPLODE);
    }

    /* ************************************
              COMMAND PROCESSING
    ************************************ */

    /**
     * Main method of the logic of the game, where each move that is received
     * from a client is gathered here. There are several checks implied, each
     * of them checking if the move is of a specific type (normal move /
     * 3 of a kind / 2 of a kind) or if the move adheres to some rules
     * (no longer than 5 args, 2 of a kind with different cards in composition
     * and so on).
     *
     * @param cmd != null
     */
    public void processPlay(String[] cmd) {
        Card firstCard;
        String[] splittedCmdInter = cmd[0].split("\\s+");
        String[] splittedCmd = this.getPlayersMoveSplitted(cmd);

        if (splittedCmdInter[0].equalsIgnoreCase("DRAW")) {
            processDraw();
        } else if (splittedCmd[0].equals(Card.DEFUSE.name())
                || splittedCmd[0].equals(Card.EXPLODE.name())
                || splittedCmd[0].equals(Card.NOPE.name())) {
            controller.sendPrivateMsg("You cannot play a DEFUSE or EXPLODE." +
                    " Right now, not even NOPE.", currentPlayerIndex);
        }

        // Three of a Kind
        else if (splittedCmd.length == 5) {
            if (checkThreeOfAKind(splittedCmd)) {
                this.processThreeOfAKind(splittedCmd);
            }
        }

        //Three of a Kind but no desired card specified
        else if (splittedCmd.length == 4) {
            controller.sendPrivateMsg("Did you want perhaps to play three of a kind?" +
                    "If so, format is <CARD> <CARD> <CARD> <TARGET PLAYER'S NAME>" +
                    " <DESIRED CARD>", currentPlayerIndex);
        }

        // Two of a Kind
        else if (splittedCmd.length == 3) {
            if (checkTwoOfAKind(splittedCmd)) {
                this.processTwoOfAKind(splittedCmd);
            }
        }

        // ATTACK / FAVOR / SEE THE FUTURE / SKIP / SHUFFLE
        else if (splittedCmd.length == 1 || splittedCmd.length == 2) {
            if (this.isRegularMoveValid(splittedCmd)) {
                firstCard = Card.valueOf(splittedCmd[0].toUpperCase());
                switch (firstCard) {
                    case ATTACK -> processAttackCard();
                    case FAVOR -> processFavorCard(splittedCmd[1]); // target player's name
                    case NOPE -> processNopeCard();
                    case SHUFFLE -> processShuffleCard();
                    case SKIP -> processSkipCard();
                    case FUTURE -> processFutureCard();
                    case TACOCAT, CATTERMELLON, POTATO, BEARD, RAINBOW -> processTwoOfAKind(splittedCmd);
                }
            }
        }
    }

    // Receives just cards, not the command
    // Receives "favor alex", not "PLAYMOVE favor alex"
//    no NOPE case
    private boolean isRegularMoveValid(String[] cmd) {
        if (cmd.length == 0) {
            controller.sendPrivateMsg("No cards provided",
                    currentPlayerIndex);
            return false;
        }

        if (cmd.length > 5) {
            controller.sendPrivateMsg("No move has that "
                    + "many arguments", currentPlayerIndex);
            return false;
        }

        if (cmd.length == 2 && cmd[0].equals(cmd[1])) {
            controller.sendPrivateMsg("2 of a Kind format: "
                    + "<CARD> <CARD> <TARGET PLAYER'S NAME>", currentPlayerIndex);
        }

        Card firstCard;
        String[] cmds = Arrays.copyOf(cmd, cmd.length);
        try {
            firstCard = Card.valueOf(cmd[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            controller.sendPrivateMsg("First provided card is not"
                    + " actually a card.", currentPlayerIndex);
            return false;
        }
        return switch (firstCard) {
            case FAVOR -> isFavorMoveValid(cmd);
            case ATTACK, SHUFFLE, SKIP, FUTURE -> isGenericMoveValid(cmds, firstCard);
            default -> true;
        };
    }

    // EXPECTED CORRECT FORMAT:
    // <ATTACK / SHUFFLE / SKIP / FUTURE>
    private boolean isGenericMoveValid(String[] cmd, final Card expectedCard) {
        if (cmd.length != 1) {
            controller.sendPrivateMsg("Format: play " +
                    expectedCard.name(), currentPlayerIndex);
            return false;
        }
        return true;
    }

    // EXPECTED CORRECT FORMAT:
    // <"FAVOR"> <TARGET PLAYER'S NAME>
    private boolean isFavorMoveValid(String[] cmd) {
        if (cmd.length != 2) {
            controller.sendPrivateMsg("Format: "
                    + "play <FAVOR> <Target Player's Name>", currentPlayerIndex);
            return false;
        } else if (!doesPlayerHaveCardInHand(Card.FAVOR)) {
            return false;
        } else return isTargetPlayerValid(getPlayerFromString(cmd[1]));
    }

    // EXPECTED CORRECT FORMAT:
    // <CARD> <CARD> <CARD> <TARGET PLAYER'S NAME> <DESIRED CARD>
    private boolean checkThreeOfAKind(String[] cmd) {
        if (!(cmd[0].equalsIgnoreCase(cmd[1])
                && cmd[1].equalsIgnoreCase(cmd[2]))) {
            return false;
        } else {
            if (cmd.length == 5) {

                // Check if all 3 cards + desired card are actually cards
                for (int cardIndex = 0; cardIndex < 3; cardIndex++) {
                    try {
                        Card.valueOf(cmd[cardIndex].toUpperCase());
                        Card.valueOf(cmd[4]);
                    } catch (IllegalArgumentException e) {
                        controller.sendPrivateMsg("Provided cards" +
                                " are not actually cards", currentPlayerIndex);
                        return false;
                    }
                }

                // Count number to check if player has 3 cards of same type
                int counter = 0;
                for (Card cardInHand : currentPlayer.handOfCards) {
                    if (cardInHand.name().equalsIgnoreCase(cmd[0]))
                        counter++;
                }

                // Check if, indeed, player has all 3 cards in hand
                if (counter == 3) {

                    // Check if all cards are same.
                    if (cmd[0].equalsIgnoreCase(cmd[1])
                            && cmd[1].equalsIgnoreCase(cmd[2])) {

                        // Check if target player is valid (return true if yes)
                        return isTargetPlayerValid(getPlayerFromString(cmd[3]));
                    } else {
                        controller.sendPrivateMsg("Not identical "
                                        + "cards or invalid player",
                                currentPlayerIndex);
                    }
                } else {
                    controller.sendPrivateMsg("You don't have all 3 " +
                            "cards in your hand", currentPlayerIndex);
                }
            }
        }
        // Default
        return false;
    }

    // EXPECTED CORRECT FORMAT:
    // <CARD> <CARD> <TARGET PLAYER'S NAME>
    private boolean checkTwoOfAKind(String[] cmd) {
        // Check if the 2 cards are same.
        if (!cmd[0].equalsIgnoreCase(cmd[1])) {
            return false;
        } else {
            if (cmd.length == 3) {

                // Check if the 2 cards are actually cards
                for (int cardIndex = 0; cardIndex < 2; cardIndex++) {
                    try {
                        Card.valueOf(cmd[cardIndex].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        controller.sendPrivateMsg("Provided cards " +
                                "are not actually cards", currentPlayerIndex);
                        return false;
                    }
                }

                // Count number to check if player has 2 cards of same type
                int counter = 0;
                for (Card cardInHand : currentPlayer.handOfCards) {
                    if (cardInHand.name().equalsIgnoreCase(cmd[0]))
                        counter++;
                }

                // Check if, indeed, player has all 3 cards in hand
                // It's possible that player has 3 identical cards,
                //  but decides he wants to play only 2.
                if (counter == 2 || counter == 3) {

                    // Check if target player is valid (return true if yes)
                    return isTargetPlayerValid(getPlayerFromString(cmd[2]));
                } else {
                    controller.sendPrivateMsg("You don't have the 2 " +
                            "cards in your hand", currentPlayerIndex);
                }
            }
        }
        // Default
        return false;
    }

    private void processThreeOfAKind(String[] cmd) {
        Player targetPlayer = this.getPlayerFromString(cmd[3]);

        if (targetPlayer.handOfCards.contains(Card.valueOf(cmd[4]))) {
            currentPlayer.handOfCards.add(Card.valueOf(cmd[4]));
            targetPlayer.handOfCards.remove(Card.valueOf(cmd[4]));

            controller.sendPrivateMsg("You got " + targetPlayer.getName()
                    + "'s " + cmd[4].toUpperCase() + ".", currentPlayerIndex);
            controller.sendPrivateMsg(currentPlayer.getName() +
                            " played a 3 OF A KIND on your " + cmd[4].toUpperCase(),
                    players.indexOf(targetPlayer));

        } else {
            controller.sendPrivateMsg(targetPlayer.getName() +
                    " did not have the desired card", currentPlayerIndex);
            controller.sendPrivateMsg("Your cards are put " +
                    "in the discard pile.", currentPlayerIndex);
        }
        removeFromHandAndAddToDiscardPile(Card.valueOf(cmd[0]));
        removeFromHandAndAddToDiscardPile(Card.valueOf(cmd[0]));
        removeFromHandAndAddToDiscardPile(Card.valueOf(cmd[0]));
    }

    private void processTwoOfAKind(String[] cmd) {
        if (cmd.length != 3) {
            controller.sendPrivateMsg("2 of a Kind format: "
                    + "<CARD> <CARD> <TARGET PLAYER'S NAME>", currentPlayerIndex);
        } else {
            Player targetPlayer = this.getPlayerFromString(cmd[2]);
            Card randomlyChosenCard = targetPlayer
                    .handOfCards
                    .get(new Random()
                            .nextInt(targetPlayer
                                    .handOfCards
                                    .size()));
            targetPlayer.handOfCards.remove(randomlyChosenCard);

            currentPlayer
                    .handOfCards
                    .add(randomlyChosenCard);

            removeFromHandAndAddToDiscardPile(Card.valueOf(cmd[0]));
            removeFromHandAndAddToDiscardPile(Card.valueOf(cmd[0]));

            controller.sendPrivateMsg("You got a " + randomlyChosenCard + ".",
                    currentPlayerIndex);
            controller.sendPrivateMsg("Your 2 identical cards have been" +
                    " put in the discard pile.", currentPlayerIndex);
            controller.sendPrivateMsg(currentPlayer.getName() + " got from you a " +
                    randomlyChosenCard.name() + ".", players.indexOf(targetPlayer));
        }
    }

    private boolean doesPlayerHaveCardInHand(Card card) {
        if (!currentPlayer.handOfCards.contains(card)) {
            controller.sendPrivateMsg("You don't have " +
                    card.name() + " card in your hand.", currentPlayerIndex);
            return false;
        }
        return true;
    }

    public void processAttackCard() {
        int nextPlayerIndex = (currentPlayerIndex + 1) % currentNumberOfPlayers;
        // Decrement current player's turn by 1
        // Don't forget that at each step the game moves to next player,
        //      the turnsToPlay increments by 1 already.
        currentPlayer.setTurnsToPlay(-1);

        if (!currentPlayer.hasTurnsToPlay()) {
            controller.sendPrivateMsg("Next player has to play 2 consecutive rounds",
                    currentPlayerIndex);
            players.get(nextPlayerIndex).setTurnsToPlay(+1);
        } else {
            players.get(nextPlayerIndex)
                    .setTurnsToPlay(+currentPlayer.getTurnsToPlay());
            currentPlayer.setTurnsToPlay(-currentPlayer.getTurnsToPlay());
            controller.sendPrivateMsg("You passed the turns to the next player.",
                    currentPlayerIndex);
        }
        controller.sendPrivateMsg("Previous player played an ATTACK card, and " +
                "now you have to play 3 rounds.", nextPlayerIndex);

        this.removeFromHandAndAddToDiscardPile(Card.ATTACK);
    }

    public void processFavorCard(String targetPlayerName) {
        Player targetPlayer = players.stream()
                .filter(player -> player.getName().equalsIgnoreCase(targetPlayerName))
                .findFirst()
                .orElse(null); // Must. This is how Streams work in JAVA. It is actually checked from source method

        controller.requestCardFromPlayer(
                players.indexOf(targetPlayer),
                currentPlayer.getName()
        );
    }

    public void processNopeCard() {

    }

    public void processShuffleCard() {
        controller.doBroadcast("The deck has been shuffled.");
        Collections.shuffle(this.deck.getDeckArray());
        this.removeFromHandAndAddToDiscardPile(Card.SHUFFLE);
    }

    /**
     * Processes the SKIP card whenever it is played.
     * Immediately ends one's turn without drawing a card.
     * <p>
     * If one plays a Skip card as a defense to an ATTACK card,
     * it only ends 1 of the 2 turns. 2 Skip Cards would end both turns.
     */
    public void processSkipCard() {
        if (currentPlayer.hasTurnsToPlay()) {
            currentPlayer.setTurnsToPlay(-1);
        }
        currentPlayer.getHand().remove(Card.SKIP);
        discardPile.add(Card.SKIP);
    }

    public void processFutureCard() {
        currentPlayer.getHand().remove(Card.FUTURE);
        discardPile.add(Card.FUTURE);

        controller.promptLastThreeCardsFromDeck(
                deck.getDeckArray().get(deck.getDeckArray().size() - 1),
                deck.getDeckArray().get(deck.getDeckArray().size() - 2),
                deck.getDeckArray().get(deck.getDeckArray().size() - 3)
        );
    }

    private void processDraw() {
        boolean currentPlayerHasDefuse = currentPlayer.getHand().contains(Card.DEFUSE);

        if (deck.getLastCard() == Card.EXPLODE && currentPlayerHasDefuse) {
            controller.askIndexOfReinsertingExplode();
            deck.getDeckArray().remove(Card.EXPLODE);
            this.removeFromHandAndAddToDiscardPile(Card.DEFUSE);
        } else if (deck.getLastCard() == Card.EXPLODE && !currentPlayerHasDefuse) {
            controller.announceKick(currentPlayerIndex);
            controller.doBroadcast(currentPlayer.getName() + " drew an EXPLODE, " +
                    "but did not have a DEFUSE. He/She is out.");
            this.removePlayerFromGame(currentPlayerIndex);
            checkHasWinner();
        } else if (deck.getLastCard() != Card.EXPLODE) {
            currentPlayer.getHand().add(deck.getLastCardAndRemove());
            currentPlayer.setTurnsToPlay(-1);
        }
        if (currentPlayer.hasTurnsToPlay()) {
            controller.sendPrivateMsg("You (still) have " +
                    currentPlayer.getTurnsToPlay() +
                    " round(s) to play", currentPlayerIndex);
        }
    }

    /* ********* CARD PROCESSING ********* */

    public void doInsertExplode(int cardIndex, int playerIndex) {
        this.deck.getDeckArray().add(cardIndex, Card.EXPLODE);
        controller.sendPrivateMsg("EXPLODE inserted successfully at " + cardIndex,
                playerIndex);

        if (currentPlayer.hasTurnsToPlay()) {
            currentPlayer.setTurnsToPlay(-1);
        }
    }

    /* ********* MISCELLANEOUS ********* */
    public void incrementPlayerTurnIndex() {
        currentPlayerIndex = (currentPlayerIndex + 1) % currentNumberOfPlayers;
        currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.setTurnsToPlay(+1);
    }

    public void checkHasWinner() {
        if (currentNumberOfPlayers == 1) {
//            incrementPlayerTurnIndex();
            controller.doBroadcast("YOU WON! CONGRATULATIONS!");
            System.exit(0);
        }
    }

    public void removePlayerFromGame(int playerIndexToRemove) {
        players.remove(playerIndexToRemove);
        currentNumberOfPlayers--;

        if (currentPlayerIndex >= playerIndexToRemove) {
            currentPlayerIndex--;
            if (currentPlayerIndex < 0) {
                currentPlayerIndex = currentNumberOfPlayers - 1;
            }
            currentPlayer = players.get(currentPlayerIndex);
        }
        Collections.shuffle(this.deck.getDeckArray());
        this.deck.getDeckArray().remove(Card.EXPLODE);
    }

    public void removeFromHandAndAddToDiscardPile(Card card) {
        currentPlayer.getHand().remove(card);
        discardPile.add(card);
    }

    public void doFavorResponse(String card, int targetPlayerIndex) {
        String[] splittedCmdInter = card.split("\\s+"); // On SPACE
        String[] splittedCmd = Arrays.copyOfRange(splittedCmdInter, 1,
                splittedCmdInter.length);
        splittedCmd = Arrays.stream(splittedCmd).map(String::toUpperCase)
                .toArray(String[]::new);
        Card receivedCard = Card.valueOf(splittedCmd[0]);
        currentPlayer.getHand().add(receivedCard);
        players.get(targetPlayerIndex)
                .getHand()
                .remove(receivedCard);
        this.removeFromHandAndAddToDiscardPile(Card.FAVOR);
    }

    public boolean isClientsTurn(int playerIndexWhoPlayedMove) {
        return (playerIndexWhoPlayedMove == currentPlayerIndex);
    }

    public boolean isTargetPlayerValid(Player targetPlayer) {
        if (targetPlayer == null) {
            controller.sendPrivateMsg("There is no such player."
                    , currentPlayerIndex);
            return false;
        } else if (currentPlayer.getName().
                equalsIgnoreCase(targetPlayer.getName())) {
            controller.sendPrivateMsg("You cannot play it to yourself."
                    , currentPlayerIndex);
            return false;
        }
        return true;
    }

    public Player getPlayerFromString(String playerName) {
        return (players.stream()
                .filter(player -> player.getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null)); // Must. This is how Streams work in JAVA. It is actually checked from source method
    }

    public void removeCardFromPlayerHand(String[] clientMsgSplitted) {
        Card firstCard;
        String[] splittedCmd = this.getPlayersMoveSplitted(clientMsgSplitted);

        if (splittedCmd[0].equals(Card.DEFUSE.name())
                || splittedCmd[0].equals(Card.EXPLODE.name())
                || splittedCmd[0].equals(Card.NOPE.name())) {
            controller.sendPrivateMsg("You cannot play a DEFUSE or EXPLODE." +
                    " Right now, not even NOPE.", currentPlayerIndex);
        }

        // Three of a Kind
        if (splittedCmd.length == 5) {
            if (checkThreeOfAKind(splittedCmd)) {
                this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
                this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
                this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
            }
        }

        // Two of a Kind
        if (splittedCmd.length == 3) {
            if (checkTwoOfAKind(splittedCmd)) {
                this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
                this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
            }
        }

        // ATTACK / FAVOR / SEE THE FUTURE / SKIP / SHUFFLE
        if (splittedCmd.length == 1 || splittedCmd.length == 2) {
            firstCard = Card.valueOf(splittedCmd[0].toUpperCase());
            switch (firstCard) {
                case ATTACK -> this.removeFromHandAndAddToDiscardPile(Card.ATTACK);
                case FAVOR -> this.removeFromHandAndAddToDiscardPile(Card.FAVOR); // target player's name
                case NOPE -> this.removeFromHandAndAddToDiscardPile(Card.NOPE);
                case SHUFFLE -> this.removeFromHandAndAddToDiscardPile(Card.SHUFFLE);
                case SKIP -> this.removeFromHandAndAddToDiscardPile(Card.SKIP);
                case FUTURE -> this.removeFromHandAndAddToDiscardPile(Card.FUTURE);
                case TACOCAT, CATTERMELLON, POTATO, BEARD, RAINBOW -> {
                    this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
                    this.removeFromHandAndAddToDiscardPile(Card.valueOf(splittedCmd[0]));
                }
            }
        }
    }

    public String[] getPlayersMoveSplitted(String[] cmd) {
        String fullCmd = cmd[0];
        String[] splittedCmdInter = fullCmd.split("\\s+"); // On SPACE
        String[] splittedCmd = Arrays.copyOfRange(splittedCmdInter, 1,
                splittedCmdInter.length);
        splittedCmd = Arrays.stream(splittedCmd).map(String::toUpperCase)
                .toArray(String[]::new);
        return splittedCmd;
    }
}
