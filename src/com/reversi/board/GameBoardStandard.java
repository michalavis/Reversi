package com.reversi.board;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.reversi.Reversi;
import com.reversi.players.Player;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class GameBoardStandard implements GameBoard {

	private static final String END_GAME_MESSAGE = "{0} player win with: {1} points\n"
			+ "{2} player lose with: {3} points\nIf you want start again click restart button.";

	private Reversi mainPanel;
	private static final int BOARD_SIZE = 8;
	private static final int GRID_SIZE = 60;
	private GridPane gridPane = new GridPane();
	private Map<BoardMatrix, Optional<Player>> boardStatus = Maps.newHashMap();
	private Map<Color, Long> playersCount = Maps.newHashMap();
	private static List<Pair<Integer, Integer>> neighbours = ImmutableList.of(new Pair<>(-1, -1), new Pair<>(-1, 0),
			new Pair<>(-1, 1), new Pair<>(0, -1), new Pair<>(0, 1), new Pair<>(1, -1), new Pair<>(1, 0),
			new Pair<>(1, 1));

	@Override
	public GridPane fillBoard(Reversi reversiPanel) {
		mainPanel = reversiPanel;
		setBoardConstraints();
		createBoardGrid();
		gridPane.setGridLinesVisible(true);
		return gridPane;
	}

	private void setBoardConstraints() {
		IntStream.range(0, BOARD_SIZE).forEach(i -> {
			gridPane.getRowConstraints().add(new RowConstraints(GRID_SIZE));
			gridPane.getColumnConstraints().add(new ColumnConstraints(GRID_SIZE));
		});
	}

	private void createBoardGrid() {
		IntStream.range(0, BOARD_SIZE).forEach(i -> createGridBoard(i));
		boardStatus.entrySet().forEach(entry -> createBoardGame(entry));
	}

	private void createGridBoard(int i) {
		IntStream.range(0, BOARD_SIZE).forEach(j -> createEmptyMatrixBoard(j, i));
	}

	private void createEmptyMatrixBoard(int xCoordinate, int yCooradinate) {
		boardStatus.put(BoardMatrix.builder()
				.withCoordinateX(xCoordinate)
				.withCoordinateY(yCooradinate)
				.build(),
				Optional.empty());
	}

	private void createBoardGame(Entry<BoardMatrix, Optional<Player>> entry) {
		BoardMatrix key = entry.getKey();
		setStartPlayers(entry, key);
		createStartedBoardState(entry);
	}

	private void setStartPlayers(Entry<BoardMatrix, Optional<Player>> entry, BoardMatrix key) {
		int center = BOARD_SIZE / 2;
		if (key.getCoordinateX() == center && key.getCoordinateY() == center) {
			entry.setValue(Optional.of(Player.builder()
					.withGridSize(GRID_SIZE)
					.withColor(Color.BLACK)
					.build()));
		}
		if (key.getCoordinateX() == center - 1 && key.getCoordinateY() == center - 1) {
			entry.setValue(Optional.of(Player.builder()
					.withGridSize(GRID_SIZE)
					.withColor(Color.BLACK)
					.build()));
		}
		if (key.getCoordinateX() == center - 1 && key.getCoordinateY() == center) {
			entry.setValue(Optional.of(Player.builder()
					.withGridSize(GRID_SIZE)
					.withColor(Color.WHITE)
					.build()));
		}
		if (key.getCoordinateX() == center && key.getCoordinateY() == center - 1) {
			entry.setValue(Optional.of(Player.builder()
					.withGridSize(GRID_SIZE)
					.withColor(Color.WHITE)
					.build()));
		}
	}

	private void createStartedBoardState(Entry<BoardMatrix, Optional<Player>> entry) {
		Pane pane = new Pane();
		BoardMatrix key = entry.getKey();
		pane.setStyle("-fx-background-color: #2C5431;");
		entry.getValue().ifPresent(player -> pane.getChildren().add(player));
		key.setPane(pane);
		gridPane.add(pane, key.getCoordinateX(), key.getCoordinateY());
	}

	@Override
	public void updateGameBoard(Map<BoardMatrix, Optional<Player>> playersToConvert,
			Entry<BoardMatrix, Optional<Player>> playerToAdd) {
		if (playersToConvert.isEmpty()) {
			System.out.println("");
		}
		Optional<Entry<BoardMatrix, Optional<Player>>> cell = getSpecificCell(playerToAdd.getKey());
		cell.ifPresent(boardCell -> addPlayers(boardCell, playerToAdd.getValue().get()));
		playersToConvert.entrySet().forEach(this::convertsAndRotate);
	}
	
	private void addPlayers(Entry<BoardMatrix, Optional<Player>> entry, Player player) {
		BoardMatrix key = entry.getKey();
		key.getPane().getChildren().clear();
		entry.setValue(Optional.of(player));
		Pane pane = new Pane();
		pane.getChildren().add(entry.getValue().get());
		key.setPane(pane);
		gridPane.add(pane, key.getCoordinateX(), key.getCoordinateY());
	}

	private void convertsAndRotate(Entry<BoardMatrix, Optional<Player>> playerToConvert) {
		Entry<BoardMatrix, Optional<Player>> cell = getSpecificCell(playerToConvert.getKey()).get();
		cell.setValue(playerToConvert.getValue());
		playerToConvert.getValue().ifPresent(player -> addPlayers(cell, player));
		// rotate(cell);
	}

	/*
	 * private void rotate(Entry<BoardMatrix, Optional<Player>> cell) {
	 * RotateTransition rotate = new RotateTransition(Duration.millis(300),
	 * cell.getValue().get()); rotate.setAxis(Rotate.Y_AXIS);
	 * rotate.setFromAngle(90); rotate.setToAngle(180);
	 * rotate.setInterpolator(Interpolator.LINEAR); new
	 * SequentialTransition(rotate).play(); }
	 */

	@Override
	public synchronized GridPane getGridPane() {
		return gridPane;
	}

	@Override
	public synchronized Map<BoardMatrix, Optional<Player>> getBoardStatus() {
		return boardStatus;
	}

	@Override
	public void setPlayersCount(Map<Color, Long> playersCount) {
		VBox vbox = mainPanel.getVbox();
		TextField whitePalyerPoints = (TextField) vbox.getChildren().get(1);
		if (playersCount.containsKey(Color.WHITE)) {
			whitePalyerPoints.setText(playersCount.get(Color.WHITE).toString());
		} else {
			whitePalyerPoints.setText("0");
		}
		TextField blackPalyerPoints = (TextField) vbox.getChildren().get(3);
		if (playersCount.containsKey(Color.BLACK)) {
			blackPalyerPoints.setText(playersCount.get(Color.BLACK).toString());
		} else {
			blackPalyerPoints.setText("0");
		}
		this.playersCount = playersCount;
	}

	@Override
	public void setPlayerTurn(Player player) {
		VBox vbox = mainPanel.getVbox();
		TextField playerTurn = (TextField) vbox.getChildren().get(6);
		playerTurn.setText(getColor(player.getColor()).toUpperCase() + " player");
	}

	@Override
	public int getGridSize() {
		return GRID_SIZE;
	}

	@Override
	public Map<BoardMatrix, Optional<Player>> getNeighbours(BoardMatrix cellPosition) {
		return boardStatus.entrySet().stream()
				.filter(entry -> getPossibleNeighbours(entry, cellPosition))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	@Override
	public boolean getPossibleNeighbours(Entry<BoardMatrix, Optional<Player>> entry, BoardMatrix cellPosition) {
		return neighbours.stream()
				.anyMatch(pair -> getNeighbourCells(entry.getKey(), cellPosition, pair));
	}

	private boolean getNeighbourCells(BoardMatrix key, BoardMatrix cellPosition, Pair<Integer, Integer> pair) {
		int coordX = cellPosition.getCoordinateX() + pair.getKey();
		int coordY = cellPosition.getCoordinateY() + pair.getValue();
		return key.getCoordinateX() == coordX && key.getCoordinateY() == coordY;
	}

	@Override
	public Optional<Entry<BoardMatrix, Optional<Player>>> getSpecificCell(BoardMatrix cellCoordinates) {
		return boardStatus.entrySet().stream()
				.filter(entry -> entry.getKey().equals(cellCoordinates))
				.findFirst();
	}

	@Override
	public int getBoardSize() {
		return BOARD_SIZE;
	}

	@Override
	public void showMessageAboutEndOfTheGame() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Game Over");
		alert.setHeaderText(null);
		setAlertContentText(alert);
		alert.show();
	}

	private void setAlertContentText(Alert alert) {
		Entry<Color, Long> winner = getWinner(playersCount);
		Entry<Color, Long> loser = getLoser(playersCount);
		String winnerColor = getColor(winner.getKey());
		String loserColor = getColor(loser.getKey());
		alert.setContentText(
				MessageFormat.format(END_GAME_MESSAGE, winnerColor.toUpperCase(), winner.getValue(),
						loserColor.toUpperCase(), loser.getValue()));
	}

	private Entry<Color, Long> getWinner(Map<Color, Long> playersCount) {
		return playersCount.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.get();
	}

	private Entry<Color, Long> getLoser(Map<Color, Long> playersCount) {
		return playersCount.entrySet().stream()
				.min(Map.Entry.comparingByValue())
				.get();
	}

	private String getColor(Color color) {
		return color.equals(Color.BLACK) ? "black" : "white";
	}

}
