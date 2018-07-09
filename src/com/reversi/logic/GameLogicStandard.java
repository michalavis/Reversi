package com.reversi.logic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;
import com.reversi.players.Agent;
import com.reversi.players.AgentPredictAlgorithm;
import com.reversi.players.Player;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameLogicStandard implements GameLogic {

	private GameBoard gameBoard;
	private GamePlay gamePlay;

	@Override
	public void initGameLogic(GameBoard currentGameBoard) {
		gameBoard = currentGameBoard;
		gamePlay = new GamePlay(currentGameBoard, Color.BLACK);
	}

	@Override
	public Player handleMouseClickedEvent(MouseEvent pointCliked) {
		int gridSize = gameBoard.getGridSize();
		BoardMatrix clickedCell = BoardMatrix.builder()
				.withCoordinateX(getClickedCoordinate(pointCliked.getX(), gridSize))
				.withCoordinateY(getClickedCoordinate(pointCliked.getY(), gridSize)).build();
		addPlayerIfValidCell(clickedCell);
		return gamePlay.getPlayer();
	}

	private int getClickedCoordinate(double d, int gridSize) {
		return (int) Math.ceil(d / gridSize) - 1;
	}

	private void addPlayerIfValidCell(BoardMatrix clickedCell) {
		if (isValidCell(clickedCell)) {
			gamePlay.setPlayerToAdd(clickedCell, Player.builder()
					.withGridSize(gameBoard.getGridSize())
					.withColor(gamePlay.getPlayer().getColor())
					.build());
			gameBoard.updateGameBoard(gamePlay.getPlayersToConvert(), gamePlay.getPlayerToAdd());
			nextRound();
		}
	}

	private boolean isValidCell(BoardMatrix clickedCell) {
		Entry<BoardMatrix, Optional<Player>> cell = gameBoard.getSpecificCell(clickedCell).get();
		if (cell.getValue().isPresent()) {
			return false;
		}
		gamePlay.setPlayersToConvert(Maps.newHashMap());
		return isValidCellToAddPlayer(cell.getKey());
	}

	private boolean isValidCellToAddPlayer(BoardMatrix clickedCellPosition) {
		Map<BoardMatrix, Optional<Player>> presentRivalNeighbours = gamePlay
				.findPresentRivalNeighbours(clickedCellPosition);
		presentRivalNeighbours.entrySet().stream()
				.forEach(entry -> findCellsToRotate(entry.getKey(), clickedCellPosition));
		return !gamePlay.getPlayersToConvert().isEmpty();
	}

	private void findCellsToRotate(BoardMatrix neighbourPosition,
			BoardMatrix clickedCellPosition) {
		gamePlay.setCoordinateToMove(neighbourPosition, clickedCellPosition);
		gamePlay.setPlayersInRow(Lists.newArrayList());
		findCellsToRotateIfPlayerRivalPresent(neighbourPosition);
	}

	private void findCellsToRotateIfPlayerRivalPresent(BoardMatrix neighbourPosition) {
		if (isPlayerRivalPresentInRow(neighbourPosition) && isNoEmptyCellInRow()) {
			addPlayerToConvert();
		}
	}

	private boolean isPlayerRivalPresentInRow(BoardMatrix neighbourPosition) {
		return IntStream.range(0, gameBoard.getBoardSize())
				.map(indexToMove -> addPlayerInRowToConvert(indexToMove, neighbourPosition))
				.anyMatch(indexToMove -> isThereAnyRivalInRow(indexToMove, neighbourPosition));
	}

	private Integer addPlayerInRowToConvert(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = gameBoard
				.getSpecificCell(gamePlay.getNextCellCordinate(indexToMove, neighbourPosition));
		nextCell.filter(cell -> !isPlayerRival(cell)).ifPresent(cell -> gamePlay.getPlayersInRow().add(cell.getKey()));
		return indexToMove;
	}

	private boolean isThereAnyRivalInRow(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = gameBoard
				.getSpecificCell(gamePlay.getNextCellCordinate(indexToMove, neighbourPosition));
		return nextCell.map(this::isPlayerRival)
				.orElse(false);
	}

	private boolean isPlayerRival(Entry<BoardMatrix, Optional<Player>> cell) {
		return cell.getValue()
				.filter(player -> player.getColor().equals(gamePlay.getPlayer().getColor())).isPresent();
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

	private void addPlayerToConvert() {
		gamePlay.getPlayersInRow().stream().map(cell -> gameBoard.getSpecificCell(cell))
				.forEach(cell -> cell.ifPresent(this::setColorAndAddToConvert));
	}

	private void setColorAndAddToConvert(Entry<BoardMatrix, Optional<Player>> cell) {
		gamePlay.getPlayersToConvert().put(cell.getKey(), Optional.of(Player.builder()
				.withGridSize(gameBoard.getGridSize())
				.withColor(gamePlay.getPlayer().getColor())
				.build()));
	}

	private void nextRound() {
		gameBoard.setPlayersCount(gamePlay.getPlayersCount());
		if (!checkEndOfTheGameConditions()) {
			continuePlaying();
		} else {
			gameBoard.showMessageAboutEndOfTheGame();
		}
	}

	private boolean checkEndOfTheGameConditions() {
		return gamePlay.isBoardFull() || gamePlay.isOnlyOnePlayerOnBoard() || isBothPlayerCannotMove();
	}

	private boolean isBothPlayerCannotMove() {
		boolean rivalCannotMove = isRivalCannotMove();
		gamePlay.changeTurn();
		boolean playerCannotMove = isRivalCannotMove();
		gamePlay.changeTurn();
		return rivalCannotMove && playerCannotMove;
	}

	private boolean isRivalCannotMove() {
		Map<BoardMatrix, Optional<Player>> allFreeRivalNeighbours = gamePlay.getAllFreeRivalNeighboursCell();
		return !allFreeRivalNeighbours.entrySet().stream()
				.anyMatch(this::isAnnyValidCell);
	}

	private boolean isAnnyValidCell(Entry<BoardMatrix, Optional<Player>> cell) {
		Map<BoardMatrix, Optional<Player>> presentRivalNeighbours = gamePlay.findPresentRivalNeighbours(cell.getKey());
		return presentRivalNeighbours.entrySet().stream()
				.anyMatch(neighbour -> checkIfCellIsValid(neighbour.getKey(), cell.getKey()));
	}

	private boolean checkIfCellIsValid(BoardMatrix neighbourPosition, BoardMatrix possiblePosition) {
		gamePlay.setCoordinateToMove(neighbourPosition, possiblePosition);
		return IntStream.range(0, gameBoard.getBoardSize())
				.anyMatch(indexToMove -> isThereAnyRivalInRow(indexToMove, neighbourPosition));
	}

	private void continuePlaying() {
		gamePlay.changeTurn();
		if (isRivalCannotMove()) {
			gamePlay.changeTurn();
		}
		gameBoard.setPlayerTurn(gamePlay.getPlayer());
	}

	@Override
	public void makeAgentMove() {
		Agent agent = new AgentPredictAlgorithm();
		agent.setGameSatus(gamePlay, gameBoard);
		agent.makeMove();
		Map<BoardMatrix, Optional<Player>> presentRivalNeighbours = gamePlay
				.findPresentRivalNeighbours(gamePlay.getPlayerToAdd().getKey());
		gamePlay.setPlayersToConvert(Maps.newHashMap());
		presentRivalNeighbours.entrySet().stream()
				.forEach(entry -> findCellsToRotate(entry.getKey(), gamePlay.getPlayerToAdd().getKey()));
		gameBoard.updateGameBoard(gamePlay.getPlayersToConvert(), gamePlay.getPlayerToAdd());
		nexeRoundAfterAgentMove();
	}

	private void nexeRoundAfterAgentMove() {
		gameBoard.setPlayersCount(gamePlay.getPlayersCount());
		if (!checkEndOfTheGameConditions()) {
			continuePlayingAfterAgentMove();
		} else {
			gameBoard.showMessageAboutEndOfTheGame();
		}
	}

	private void continuePlayingAfterAgentMove() {
		gamePlay.changeTurn();
		if (isRivalCannotMove()) {
			gamePlay.changeTurn();
			Timeline timeLine = new Timeline(new KeyFrame(Duration.millis(600), ev -> {
				makeAgentMove();
			}));
			timeLine.play();
		}
		gameBoard.setPlayerTurn(gamePlay.getPlayer());
	}
}
