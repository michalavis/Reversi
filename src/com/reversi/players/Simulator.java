package com.reversi.players;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reversi.board.BoardMatrix;
import com.reversi.board.GameBoard;

public class Simulator {
	
	private int[][] cellsValue = {
            {24,  -14,  6,  6,  6,  6,  -14, 24},
            {-14, -24, -4, -3, -3, -4, -24, -14},
            { 6,  -4,  7,  4,  4,  7,  -4,  6},
            { 6,  -3,  4,  0,  0,  4,  -3,  6},
            { 6,  -3,  4,  0,  0,  4,  -3,  6},
            { 6,  -4,  7,  4,  4,  7,  -4,  6},
            {-14, -24, -4, -3, -3, -4, -24, -14},
            {24,  -14,  6,  6,  6,  6,  -14, 24}
    };

	private GameBoard gameBoard;
	private Agent agent;
	private MoveAnalyzer analyzer;

	private UUID actualParent;
	private Map<ValueCellCoordinates, Integer> boardCellsValueForPlayer = Maps.newHashMap();

	public Simulator(GameBoard presentGameBoard, Agent agentAlgorithm) {
		gameBoard = presentGameBoard;
		agent = agentAlgorithm;
		analyzer = new MoveAnalyzer(presentGameBoard, agentAlgorithm);
	}

	public int getCellsValue(int i, int j) {
		return cellsValue[i][j];
	}

	public void setActualParent(UUID actualParent) {
		this.actualParent = actualParent;
	}

	public void TryToMakeBestMove(Map<BoardMatrix, Optional<Player>> boardStatus) {
		Map<BoardMatrix, Optional<Player>> possiblePlayerMoves = analyzer
				.getAllPossiblePlayerMoves(boardStatus);
		possiblePlayerMoves.entrySet().stream()
				.forEach(possibleMoves -> simulateBestMove(possibleMoves, boardStatus));
	}

	private void simulateBestMove(Entry<BoardMatrix, Optional<Player>> freeCell,
			Map<BoardMatrix, Optional<Player>> boardStatus) {
		BoardMatrix cell = freeCell.getKey();
		int moveValue = getCellsValue(cell.getCoordinateX(), cell.getCoordinateY());
		if (Optional.ofNullable(actualParent).isPresent()) {
			moveValue += getParentValue();
		}
		ValueCellCoordinates cellValueCoordinate = new ValueCellCoordinates(cell);
		cellValueCoordinate.setParentId(actualParent);
		agent.getBoardCellsValueForAgent().put(cellValueCoordinate, moveValue);
		agent.estimateNextMove(cellValueCoordinate, boardStatus);
	}

	private Integer getParentValue() {
		Optional<ValueCellCoordinates> parent = agent.getBoardCellsValueForAgent().keySet().stream()
				.filter(key -> key.getId().equals(actualParent))
				.findFirst();
		return parent.map(presentParent -> agent.getBoardCellsValueForAgent().get(presentParent)).orElse(0);
	}

	private boolean isDeepEnough() {
		Optional<UUID> parent = Optional.ofNullable(actualParent);
		Optional<ValueCellCoordinates> parentCellValueCoordinate = parent
				.map(presentParent -> getNextParent(presentParent))
				.orElse(Optional.empty());
		Optional<UUID> rootParent = parentCellValueCoordinate.map(cell -> Optional.ofNullable(cell.getParentId()))
				.orElse(Optional.empty());
		return agent.getPlayerTurn().equals(agent.getAgentColor()) || !rootParent.isPresent();
	}

	private Optional<ValueCellCoordinates> getNextParent(UUID presentParent) {
		return agent.getBoardCellsValueForAgent().keySet().stream()
				.filter(key -> key.getId().equals(presentParent))
				.findFirst();
	}

	public void fillBoardAndmakeNextMove(ValueCellCoordinates analyzedCell,
			Map<BoardMatrix, Optional<Player>> copyBoardStatus) {
		BoardMatrix cellCoordinate = analyzedCell.getCoordinate();
		analyzer.setBoardToSimulate(copyBoardStatus);
		analyzer.setPlayersToConvert(Maps.newHashMap());
		analyzer.findPresentRivalNeighbours(cellCoordinate).entrySet().stream()
				.forEach(entry -> findCellsToConvert(entry.getKey(), cellCoordinate));
		analyzer.getPlayersToConvert().entrySet().stream().forEach(this::convert);
		analyzer.getSpecificCell(cellCoordinate)
				.ifPresent(cell -> cell.setValue(Optional.of(agent.getPlayerTurn())));
		if (isDeepEnough()) {
			makeNextMove(analyzedCell);
		}
	}

	private void findCellsToConvert(BoardMatrix neighbourPosition, BoardMatrix cellPosition) {
		analyzer.setCoordinateToMove(neighbourPosition, cellPosition);
		analyzer.setPlayersInRow(Lists.newArrayList());
		findCellsToConvertIfPlayerRivalPresent(neighbourPosition);
	}
	
	private void findCellsToConvertIfPlayerRivalPresent(BoardMatrix neighbourPosition) {
		if (isPlayerRivalPresentInRow(neighbourPosition) && isNoEmptyCellInRow()) {
			addPlayerToConvert();
		}
	}

	private boolean isPlayerRivalPresentInRow(BoardMatrix neighbourPosition) {
		return IntStream.range(0, gameBoard.getBoardSize())
				.map(indexToMove -> addPlayerInRowToConvert(indexToMove, neighbourPosition))
				.anyMatch(indexToMove -> analyzer.isThereAnyRivalInRow(indexToMove, neighbourPosition));
	}
	
	private Integer addPlayerInRowToConvert(int indexToMove, BoardMatrix neighbourPosition) {
		Optional<Entry<BoardMatrix, Optional<Player>>> nextCell = analyzer
				.getSpecificCell(analyzer.getNextCellCordinate(indexToMove, neighbourPosition));
		nextCell.filter(cell -> !analyzer.isPlayerRival(cell))
				.ifPresent(cell -> analyzer.getPlayersInRow().add(cell.getKey()));
		return indexToMove;
	}
	
	private boolean isNoEmptyCellInRow() {
		return analyzer.getPlayersInRow().stream()
				.map(cell -> analyzer.getSpecificCell(cell))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.noneMatch(cell -> !cell.getValue().isPresent());
	}

	private void addPlayerToConvert() {
		analyzer.getPlayersInRow().stream()
			.map(neighbourCell -> analyzer.getSpecificCell(neighbourCell))
			.forEach(cell -> cell.ifPresent(this::setColorAndAddToConvert));
	}

	private void setColorAndAddToConvert(Entry<BoardMatrix, Optional<Player>> cell) {
		analyzer.getPlayersToConvert().put(cell.getKey(), Optional.of(Player.builder()
				.withGridSize(gameBoard.getGridSize())
				.withColor(agent.getPlayerTurn().getColor())
				.build()));
	}

	private void convert(Entry<BoardMatrix, Optional<Player>> playerToConvert) {
		Entry<BoardMatrix, Optional<Player>> cell = analyzer.getSpecificCell(playerToConvert.getKey()).get();
		cell.setValue(playerToConvert.getValue());
	}

	public void makeNextMove(ValueCellCoordinates analyzedCell) {
		agent.changeSimulationPlayer();
		if (!agent.getPlayerTurn().getColor().equals(agent.getAgentColor())) {
			simulateRivalMove(analyzedCell);
		} else {
			simulateAgentMove();
		}
	}

	private void simulateRivalMove(ValueCellCoordinates analyzedCell) {
		Map<BoardMatrix, Optional<Player>> copyBoardStatus = analyzer.getBoardStatus();
		Map<BoardMatrix, Optional<Player>> possiblePlayerMoves = analyzer
				.getAllPossiblePlayerMoves(copyBoardStatus);
		if (!possiblePlayerMoves.isEmpty()) {
			possiblePlayerMoves.entrySet().stream().forEach(this::setCellValues);
			Entry<ValueCellCoordinates, Integer> bestRivalMove = boardCellsValueForPlayer.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.get();
			Integer value = agent.getBoardCellsValueForAgent().get(analyzedCell) - bestRivalMove.getValue();
			agent.getBoardCellsValueForAgent().put(analyzedCell, value);
			boardCellsValueForPlayer = Maps.newHashMap();
			fillBoardAndmakeNextMove(bestRivalMove.getKey(), copyBoardStatus);
		}
	}

	private void setCellValues(Entry<BoardMatrix, Optional<Player>> entry) {
		BoardMatrix key = entry.getKey();
		int cellValue = getCellsValue(key.getCoordinateX(), key.getCoordinateY());
		boardCellsValueForPlayer.put(new ValueCellCoordinates(key), cellValue);
	}

	private void simulateAgentMove() {
		Map<BoardMatrix, Optional<Player>> copyBoardStatus = analyzer.getBoardStatus();
		Map<BoardMatrix, Optional<Player>> possiblePlayerMoves = analyzer.getAllPossiblePlayerMoves(copyBoardStatus);
		possiblePlayerMoves.entrySet().stream()
				.forEach(possibleMoves -> simulateBestMove(possibleMoves, copyBoardStatus));
	}
}
