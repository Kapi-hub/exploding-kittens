package model;

import java.util.ArrayList;
import java.util.Arrays;

public class Player {
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND = 8;
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE = 7;
    protected ArrayList<Card> handOfCards;
    protected String name;

    /* ************************************
              GETTERS & SETTERS
    ************************************ */

    public ArrayList<Card> getHand() {
        return this.handOfCards;
    }

    public String getName() {
        return name;
    }
/* ************************************
                CONSTRUCTORS
    ************************************ */

    public Player() {
        this.handOfCards = new ArrayList<>();
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    /* ************************************
                    METHODS
    ************************************ */

//    public Card doMove() {
//
//
//    }

    @Override
    public String toString() {
        return (this.name + ": ");
    }
}
