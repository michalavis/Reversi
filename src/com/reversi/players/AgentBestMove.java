package com.reversi.players;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;
import com.reversi.logic.GamePlay;

import javafx.scene.paint.Color;

public class AgentBestMove implements Agent {

	private GamePlay gamePlay;
	private GameBoard gameBoard;
	private Color agentColor = Color.WHITE;

	private int[][] cellsValue = {
            {99,  -8,  8,  6,  6,  8,  -8, 99},
            {-8, -24, -4, -3, -3, -4, -24, -8},
            { 8,  -4,  7,  4,  4,  7,  -4,  8},
            { 6,  -3,  4,  0,  0,  4,  -3,  6},
            { 6,  -3,  4,  0,  0,  4,  -3,  6},
            { 8,  -4,  7,  4,  4,  7,  -4,  8},
            {-8, -24, -4, -3, -3, -4, -24, -8},
            {99,  -8,  8,  6,  6,  8,  -8, 99}
    };

	public int getCellsValue(int i, int j) {
		return cellsValue[i][j];
	}

	@Override
	public void setGameSatus(GamePlay presentGamePlay, GameBoard presentGameBoard) {
		gamePlay = presentGamePlay;
		gameBoard = presentGameBoard;
	}

	@Override
	public void makeMove() {
		Map<ValueCellCoordinates, Integer> boardCellsValueForPlayer = Maps.newHashMap();
		Map<BoardMatrix, Optional<Player>> possiblePlayerMoves = getAllPossiblePlayerMoves();
		possiblePlayerMoves.entrySet().stream().forEach(entry -> {
			BoardMatrix key = entry.getKey();
			int cellValue = getCellsValue(key.getCoordinateX(), key.getCoordinateY());
			boardCellsValueForPlayer.put(new ValueCellCoordinates(key), cellValue);
		});
		Entry<ValueCellCoordinates, Integer> bestRivalMove = boardCellsValueForPlayer.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.get();
		gamePlay.setPlayerToAdd(bestRivalMove.getKey().getCoordinate(), Player.builder()
				.withGridSize(gameBoard.getGridSize())
				.withColor(gamePlay.getPlayer().getColor()).build());
	}

	public Map<BoardMatrix, Optional<Player>> getAllPossiblePlayerMoves() {
		Map<BoardMatrix, Optional<Player>> freeRivalNeighbours = Maps.newHashMap();
		getPlayerRival().entrySet().stream()
				.forEach(cell -> findFreeRivalNeighbours(cell, freeRivalNeighbours));
		return freeRivalNeighbours.entrySet().stream()
				.filter(this::getAllPossibleMoves)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public Map<BoardMatrix, Optional<Player>> getPlayerRival() {
		return gameBoard.getBoardStatus().entrySet().stream()
				.filter(isPlayerPresent())
				.filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerPresent() {
		return entry -> entry.getValue().isPresent();
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerRival() {
		return entry -> !entry.getValue().get().getColor().equals(gamePlay.getPlayer().getColor());
	}
	
	public void findFreeRivalNeighbours(Entry<BoardMatrix, Optional<Player>> cell,
			Map<BoardMatrix, Optional<Player>> freePlayerNeighbours) {
		gameBoard.getNeighbours(cell.getKey()).entrySet().stream().filter(isPlayerNotPresent())
				.forEach(freeCell -> getAllFreeCells(freeCell, freePlayerNeighbours));
	}
	
	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerNotPresent() {
		return entry -> !entry.getValue().isPresent();
	}
	
	private void getAllFreeCells(Entry<BoardMatrix, Optional<Player>> freeCell,
			Map<BoardMatrix, Optional<Player>> freePlayerNeighbours) {
		if (!freePlayerNeighbours.containsKey(freeCell.getKey())) {
			freePlayerNeighbours.put(freeCell.getKey(), freeCell.getValue());
		}
	}
	
	private boolean getAllPossibleMoves(Entry<BoardMatrix, Optional<Player>> cell) {
		Map<BoardMatrix, Optional<Player>> presentRivalNeighbours = findPresentRivalNeighbours(cell.getKey());
		return presentRivalNeighbours.entrySet().stream()
				.anyMatch(neighbour -> checkIfCellIsValid(neighbour.getKey(), cell.getKey()));
	}

	public Map<BoardMatrix, Optional<Player>> findPresentRivalNeighbours(BoardMatrix clickedCellPosition) {
		return gameBoard.getNeighbours(clickedCellPosition).entrySet().stream().filter(isPlayerPresent()).filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private boolean checkIfCellIsValid(BoardMatrix neighbourPosition, BoardMatrix possiblePosition) {
		gamePlay.setCoordinateToMove(neighbourPosition, possiblePosition);
		gamePlay.setPlayersInRow(Lists.newArrayList());
		boolean isRivalInRow = IntStream.range(0, gameBoard.getBoardSize())
				.map(indexToMove -> addPlayerInRowToConvert(indexToMove, neighbourPosition))
				.anyMatch(indexToMove -> isThereAnyRivalInRow(indexToMove, neighbourPosition));
		return isRivalInRow && isNoEmptyCellInRow();
	}
	
	private Integer addPlayerInRowToConvert(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = gameBoard
				.getSpecificCell(gamePlay.getNextCellCordinate(indexToMove, neighbourPosition));
		nextCell.filter(cell -> !isPlayerRival(cell)).ifPresent(cell -> gamePlay.getPlayersInRow().add(cell.getKey()));
		return indexToMove;
	}

	public boolean isThereAnyRivalInRow(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = gameBoard.getSpecificCell(
				gamePlay.getNextCellCordinate(indexToMove, neighbourPosition));
		return nextCell.map(this::isPlayerRival)
				.orElse(false);
	}
	
	public boolean isPlayerRival(Entry<BoardMatrix, Optional<Player>> cell) {
		return cell.getValue().filter(player -> player.getColor().equals(gamePlay.getPlayer().getColor())).isPresent();
	}

	private boolean isNoEmptyCellInRow() {
		return gamePlay.getPlayersInRow().stream()
				.noneMatch(this::isPlayerNotPresent);
	}

	private boolean isPlayerNotPresent(BoardMatrix cell) {
		Optional<Player> player = gameBoard.getSpecificCell(cell)
				.flatMap(specificCell -> specificCell.getValue());
		return !player.isPresent();
	}

	@Override
	public Map<ValueCellCoordinates, Integer> getBoardCellsValueForAgent() {
		return null;
	}

	@Override
	public Color getAgentColor() {
		return agentColor;
	}

	@Override
	public Player getPlayerTurn() {
		return null;
	}

	@Override
	public void changeSimulationPlayer() {
	}

	@Override
	public void estimateNextMove(ValueCellCoordinates cellValueCoordinate,
			Map<BoardMatrix, Optional<Player>> simulatedBoardStatus) {
	}
}
