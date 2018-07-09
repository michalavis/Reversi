package com.reversi.logic;

import com.reversi.board.GameBoard;
import com.reversi.players.Player;

import javafx.scene.input.MouseEvent;

public interface GameLogic {

	public void initGameLogic(GameBoard gameBoard);

	public Player handleMouseClickedEvent(MouseEvent pointCliked);

	public void makeAgentMove();
}
