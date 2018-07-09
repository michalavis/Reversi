package com.reversi.players;

import java.util.Map;
import java.util.Optional;

import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;
import com.reversi.logic.GamePlay;

import javafx.scene.paint.Color;

public interface Agent {

	public void setGameSatus(GamePlay gamePlay, GameBoard gameBoard);

	public void makeMove();

	public Map<ValueCellCoordinates, Integer> getBoardCellsValueForAgent();

	public Color getAgentColor();
	public Player getPlayerTurn();

	public void changeSimulationPlayer();

	public void estimateNextMove(ValueCellCoordinates cellValueCoordinate,
			Map<BoardMatrix, Optional<Player>> simulatedBoardStatus);

}
