package com.reversi.players;

import java.util.List;
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

import javafx.util.Pair;

public class MoveAnalyzer {

	private GameBoard gameBoard;
	private Agent agent;

	private Map<BoardMatrix, Optional<Player>> boardStatus;

	private Map<BoardMatrix, Optional<Player>> playersToConvert;

	private Pair<Integer, Integer> coordinateToMove;
	private List<BoardMatrix> playersInRow;

	public MoveAnalyzer(GameBoard presentGameBoard, Agent agentAlgorithm) {
		gameBoard = presentGameBoard;
		agent = agentAlgorithm;
	}

	public Map<BoardMatrix, Optional<Player>> getPlayersToConvert() {
		return playersToConvert;
	}

	public void setPlayersToConvert(Map<BoardMatrix, Optional<Player>> playersToConvert) {
		this.playersToConvert = playersToConvert;
	}

	public List<BoardMatrix> getPlayersInRow() {
		return playersInRow;
	}

	public void setPlayersInRow(List<BoardMatrix> playersInRow) {
		this.playersInRow = playersInRow;
	}

	public Pair<Integer, Integer> getCoordinateToMove() {
		return coordinateToMove;
	}

	public void setCoordinateToMove(BoardMatrix neighbourPosition, BoardMatrix clickedCellPosition) {
		int coordX = clickedCellPosition.getCoordinateX() - neighbourPosition.getCoordinateX();
		int coordY = clickedCellPosition.getCoordinateY() - neighbourPosition.getCoordinateY();
		coordinateToMove = new Pair<>(coordX, coordY);
	}

	public BoardMatrix getNextCellCordinate(int indexToMove, BoardMatrix neighbourCoord) {
		return BoardMatrix.builder()
				.withCoordinateX(neighbourCoord.getCoordinateX() - indexToMove * getCoordinateToMove().getKey())
				.withCoordinateY(neighbourCoord.getCoordinateY() - indexToMove * getCoordinateToMove().getValue())
				.build();
	}

	public Map<BoardMatrix, Optional<Player>> getAllPossiblePlayerMoves(
			Map<BoardMatrix, Optional<Player>> analyzedBoardStatus) {
		boardStatus = analyzedBoardStatus;
		Map<BoardMatrix, Optional<Player>> freeRivalNeighbours = Maps.newHashMap();
		getPlayerRival().entrySet().stream().forEach(cell -> findFreeRivalNeighbours(cell, freeRivalNeighbours));
		return freeRivalNeighbours.entrySet().stream().filter(this::getAllPossibleMoves)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public Map<BoardMatrix, Optional<Player>> getPlayerRival() {
		return boardStatus.entrySet().stream().filter(isPlayerPresent()).filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerPresent() {
		return entry -> entry.getValue().isPresent();
	}

	private Predicate<Entry<BoardMatrix, Optional<Player>>> isPlayerRival() {
		return entry -> !entry.getValue().get().getColor().equals(agent.getPlayerTurn().getColor());
	}

	public void findFreeRivalNeighbours(Entry<BoardMatrix, Optional<Player>> cell,
			Map<BoardMatrix, Optional<Player>> freePlayerNeighbours) {
		getNeighbours(cell.getKey()).entrySet().stream().filter(isPlayerNotPresent())
				.forEach(freeCell -> getAllFreeCells(freeCell, freePlayerNeighbours));
	}

	public Map<BoardMatrix, Optional<Player>> getNeighbours(BoardMatrix cellPosition) {
		return boardStatus.entrySet().stream().filter(entry -> gameBoard.getPossibleNeighbours(entry, cellPosition))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

	private boolean getAllPossibleMoves(Entry<BoardMatrix, Optional<Player>> cell) {
		Map<BoardMatrix, Optional<Player>> presentRivalNeighbours = findPresentRivalNeighbours(cell.getKey());
		return presentRivalNeighbours.entrySet().stream()
				.anyMatch(neighbour -> checkIfCellIsValid(neighbour.getKey(), cell.getKey()));
	}

	public Map<BoardMatrix, Optional<Player>> findPresentRivalNeighbours(BoardMatrix clickedCellPosition) {
		return getNeighbours(clickedCellPosition).entrySet().stream().filter(isPlayerPresent()).filter(isPlayerRival())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private boolean checkIfCellIsValid(BoardMatrix neighbourPosition, BoardMatrix possiblePosition) {
		setPlayersInRow(Lists.newArrayList());
		setCoordinateToMove(neighbourPosition, possiblePosition);
		boolean isRivalInRow = IntStream.range(0, gameBoard.getBoardSize())
				.map(indexToMove -> addPlayerInRowToConvert(indexToMove, neighbourPosition))
				.anyMatch(indexToMove -> isThereAnyRivalInRow(indexToMove, neighbourPosition));
		return isRivalInRow && isNoEmptyCellInRow();
	}

	private Integer addPlayerInRowToConvert(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = gameBoard
				.getSpecificCell(getNextCellCordinate(indexToMove, neighbourPosition));
		nextCell.filter(cell -> !isPlayerRival(cell))
				.ifPresent(cell -> getPlayersInRow().add(cell.getKey()));
		return indexToMove;
	}

	public boolean isThereAnyRivalInRow(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = getSpecificCell(
				getNextCellCordinate(indexToMove, neighbourPosition));
		return nextCell.map(this::isPlayerRival).orElse(false);
	}
	
	private boolean isNoEmptyCellInRow() {
		return getPlayersInRow().stream()
				.noneMatch(this::isPlayerNotPresent);
	}

	private boolean isPlayerNotPresent(BoardMatrix cell) {
		Optional<Player> player = gameBoard.getSpecificCell(cell)
				.flatMap(specificCell -> specificCell.getValue());
		return !player.isPresent();
	}

	public Optional<Entry<BoardMatrix, Optional<Player>>> getSpecificCell(BoardMatrix cellCoordinates) {
		return boardStatus.entrySet().stream().filter(entry -> entry.getKey().equals(cellCoordinates)).findFirst();
	}

	public boolean isPlayerRival(Entry<BoardMatrix, Optional<Player>> cell) {
		return cell.getValue().filter(player -> player.getColor().equals(agent.getPlayerTurn().getColor())).isPresent();
	}

	public void setBoardToSimulate(Map<BoardMatrix, Optional<Player>> copyBoardStatus) {
		boardStatus = copyBoardStatus;
	}

	public Map<BoardMatrix, Optional<Player>> getBoardStatus() {
		return boardStatus;
	}
}
