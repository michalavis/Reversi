package com.reversi.board;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.reversi.Reversi;
import com.reversi.players.Player;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public interface GameBoard {

	public GridPane fillBoard(Reversi reversi);

	public GridPane getGridPane();

	public Map<BoardMatrix, Optional<Player>> getBoardStatus();
	public int getGridSize();

	public void updateGameBoard(Map<BoardMatrix, Optional<Player>> playersToConvert,
			Entry<BoardMatrix, Optional<Player>> playerToAdd);

	public Map<BoardMatrix, Optional<Player>> getNeighbours(BoardMatrix cellPosition);

	public boolean getPossibleNeighbours(Entry<BoardMatrix, Optional<Player>> entry, BoardMatrix cellPosition);

	public Optional<Entry<BoardMatrix, Optional<Player>>> getSpecificCell(BoardMatrix cellCoordinates);

	public int getBoardSize();

	public void showMessageAboutEndOfTheGame();

	public void setPlayersCount(Map<Color, Long> playersCount);

	public void setPlayerTurn(Player player);
}
