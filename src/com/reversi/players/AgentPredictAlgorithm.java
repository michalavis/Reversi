package com.reversi.players;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;
import com.reversi.logic.GamePlay;

import javafx.scene.paint.Color;

public class AgentPredictAlgorithm implements Agent {

	private Map<ValueCellCoordinates, Integer> boardCellsValueForAgent;
	private GamePlay gamePlay;
	private GameBoard gameBoard;
	private Color agentColor = Color.WHITE;
	private Player playerTurn;

	private Simulator simulator;

	@Override
	public Map<ValueCellCoordinates, Integer> getBoardCellsValueForAgent() {
		return boardCellsValueForAgent;
	}

	@Override
	public Color getAgentColor() {
		return agentColor;
	}

	@Override
	public Player getPlayerTurn() {
		return playerTurn;
	}

	@Override
	public void changeSimulationPlayer() {
		if (playerTurn.getColor().equals(Color.BLACK)) {
			playerTurn = Player.builder().withGridSize(gameBoard.getGridSize()).withColor(Color.WHITE).build();
		} else {
			playerTurn = Player.builder().withGridSize(gameBoard.getGridSize()).withColor(Color.BLACK).build();
		}
	}

	@Override
	public void setGameSatus(GamePlay presentGamePlay, GameBoard presentGameBoard) {
		gamePlay = presentGamePlay;
		gameBoard = presentGameBoard;
		playerTurn = Player.builder().withGridSize(presentGameBoard.getGridSize()).withColor(Color.WHITE).build();
	}

	@Override
	public void makeMove() {
		simulator = new Simulator(gameBoard, this);
		boardCellsValueForAgent = Maps.newHashMap();
		Map<BoardMatrix, Optional<Player>> copyBoardStatus = Maps.newHashMap();
		copyBoardStatus.putAll(gameBoard.getBoardStatus());
		simulator.TryToMakeBestMove(copyBoardStatus);
			
		synchronized (gameBoard) {
			Entry<ValueCellCoordinates, Integer> highestValueResult = getBoardCellsValueForAgent().entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.get();
			Entry<ValueCellCoordinates, Integer> moveToAdd = getCellToAdd(highestValueResult);
			gamePlay.setPlayerToAdd(moveToAdd.getKey().getCoordinate(), Player.builder()
					.withGridSize(gameBoard.getGridSize())
					.withColor(gamePlay.getPlayer().getColor()).build());
		}
	}
	
	private Entry<ValueCellCoordinates, Integer> getCellToAdd(Entry<ValueCellCoordinates, Integer> valueResult) {
		Optional<Entry<ValueCellCoordinates, Integer>> parent = getBoardCellsValueForAgent().entrySet().stream()
				.filter(entry -> isParent(entry, valueResult))
				.findFirst();
		return parent.map(presentParent -> getCellToAdd(presentParent))
				.orElse(valueResult);
	}

	private boolean isParent(Entry<ValueCellCoordinates, Integer> entry, Entry<ValueCellCoordinates, Integer> highestValueResult) {
		Optional<UUID> parent = Optional.ofNullable(highestValueResult.getKey().getParentId());
		return parent.map(parentId -> entry.getKey().getId().equals(parentId))
				.orElse(false);
	}

	@Override
	public void estimateNextMove(ValueCellCoordinates analyzedCell, Map<BoardMatrix, Optional<Player>> boardStatus) {
		simulator = new Simulator(gameBoard, this);
		simulator.setActualParent(analyzedCell.getId());
		Map<BoardMatrix, Optional<Player>> copyBoardStatus = Maps.newHashMap();
		copyBoardStatus.putAll(boardStatus);
		simulator.fillBoardAndmakeNextMove(analyzedCell, copyBoardStatus);
	}
}
