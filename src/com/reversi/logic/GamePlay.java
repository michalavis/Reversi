package com.reversi.logic;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;
import com.reversi.players.Player;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public class GamePlay {

	private Entry<BoardMatrix, Optional<Player>> playerToAdd;
	private Map<BoardMatrix, Optional<Player>> playersToConvert;
	private Map<BoardMatrix, Optional<Player>> presentRivalNeighbours;
	private Pair<Integer, Integer> coordinateToMove;
	private List<BoardMatrix> playersInRow;
	private Player playerTurn;
	private GameBoard gameBoard;

	public GamePlay(GameBoard currentGameBoard, Color color) {
		gameBoard = currentGameBoard;
		playerTurn = Player.builder().withGridSize(currentGameBoard.getGridSize()).withColor(color).build();
	}

	public Entry<BoardMatrix, Optional<Player>> getPlayerToAdd() {
		return playerToAdd;
	}

	public void setPlayerToAdd(BoardMatrix clickedCell, Player player) {
		this.playerToAdd = new AbstractMap.SimpleEntry<BoardMatrix, Optional<Player>>(clickedCell, Optional.of(player));
	}

	public Map<BoardMatrix, Optional<Player>> getPlayersToConvert() {
		return playersToConvert;
	}

	public void setPlayersToConvert(Map<BoardMatrix, Optional<Player>> playersToConvert) {
		this.playersToConvert = playersToConvert;
	}

	public Map<BoardMatrix, Optional<Player>> getPresentRivalNeighbours() {
		return presentRivalNeighbours;
	}

	public List<BoardMatrix> getPlayersInRow() {
		return playersInRow;
	}

	public Pair<Integer, Integer> getCoordinateToMove() {
		return coordinateToMove;
	}

	public void setCoordinateToMove(BoardMatrix neighbourPosition, BoardMatrix clickedCellPosition) {
		int coordX = clickedCellPosition.getCoordinateX() - neighbourPosition.getCoordinateX();
		int coordY = clickedCellPosition.getCoordinateY() - neighbourPosition.getCoordinateY();
		coordinateToMove = new Pair<>(coordX, coordY);
	}

	public void setPlayersInRow(List<BoardMatrix> playersInRow) {
		this.playersInRow = playersInRow;
	}

	public Player getPlayer() {
		return playerTurn;
	}

	public void setPlayer(Player player) {
		this.playerTurn = player;
	}

	public Map<BoardMatrix, Optional<Player>> findPresentRivalNeighbours(BoardMatrix clickedCellPosition) {
		return gameBoard.getNeighbours(clickedCellPosition).entrySet().stream()
				.filter(isPlayerPresent())
				.filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerPresent() {
		return entry -> entry.getValue().isPresent();
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerRival() {
		return entry -> !entry.getValue().get().getColor().equals(playerTurn.getColor());
	}

	public BoardMatrix getNextCellCordinate(int indexToMove, BoardMatrix neighbourCoord) {
		return BoardMatrix.builder()
				.withCoordinateX(neighbourCoord.getCoordinateX() - indexToMove * getCoordinateToMove().getKey())
				.withCoordinateY(neighbourCoord.getCoordinateY() - indexToMove * getCoordinateToMove().getValue())
				.build();
	}

	public Map<BoardMatrix, Optional<Player>> getAllFreeRivalNeighboursCell() {
		Map<BoardMatrix, Optional<Player>> freeRivalNeighbours = Maps.newHashMap();
		getPlayerRival().entrySet().stream().forEach(cell -> findFreeRivalNeighbours(cell, freeRivalNeighbours));
		return freeRivalNeighbours;
	}

	public Map<BoardMatrix, Optional<Player>> getPlayerRival() {
		return gameBoard.getBoardStatus().entrySet().stream()
				.filter(isPlayerPresent()).filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private void findFreeRivalNeighbours(Entry<BoardMatrix, Optional<Player>> cell,
			Map<BoardMatrix, Optional<Player>> freePlayerNeighbours) {
		gameBoard.getNeighbours(cell.getKey()).entrySet().stream().filter(isPlayerNotPresent())
				.forEach(freeCell -> getAllFreeCells(freeCell, freePlayerNeighbours));
	}

	private Predicate<? super Entry<BoardMatrix, Optional<Player>>> isPlayerNotPresent() {
		return entry -> !entry.getValue().isPresent();
	}

	private void getAllFreeCells(Entry<BoardMatrix, Optional<Player>> freeCell,
			Map<BoardMatrix, Optional<Player>> freePlayerNeighbours) {
		if (!freePlayerNeighbours.containsKey(freeCell.getKey())) {
			freePlayerNeighbours.put(freeCell.getKey(), freeCell.getValue());
		}
	}

	public void changeTurn() {
		if (playerTurn.getColor().equals(Color.BLACK)) {
			playerTurn = Player.builder().withGridSize(gameBoard.getGridSize()).withColor(Color.WHITE).build();
		} else {
			playerTurn = Player.builder().withGridSize(gameBoard.getGridSize()).withColor(Color.BLACK).build();
		}
	}

	public boolean isBoardFull() {
		return !gameBoard.getBoardStatus().entrySet().stream().anyMatch(isPlayerNotPresent());
	}

	public boolean isOnlyOnePlayerOnBoard() {
		Map<Boolean, List<Player>> isBlackPalyer = gameBoard.getBoardStatus().entrySet().stream()
				.filter(isPlayerPresent())
				.map(Entry::getValue)
				.map(Optional::get)
				.collect(Collectors.partitioningBy(player -> isBlack(player)));
		return isBlackPalyer.get(Boolean.TRUE).isEmpty() || isBlackPalyer.get(Boolean.FALSE).isEmpty();
	}

	private boolean isBlack(Player player) {
		return player.getColor().equals(Color.BLACK);
	}

	public Map<Color, Long> getPlayersCount() {
		return gameBoard.getBoardStatus().entrySet().stream().filter(isPlayerPresent()).map(Entry::getValue)
				.map(Optional::get).map(Player::getColor)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}
}
