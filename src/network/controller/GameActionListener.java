package network.controller;

import network.model.Player;

public interface GameActionListener {
    void requestCardFromPlayer(
            int targetPlayerIndex,
            String requestingPlayerName,
            CardActionCallback callback
    );
}
