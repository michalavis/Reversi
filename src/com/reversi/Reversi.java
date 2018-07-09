package com.reversi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.reversi.board.GameBoard;
import com.reversi.board.GameBoardStandard;
import com.reversi.logic.GameLogic;
import com.reversi.logic.GameLogicStandard;
import com.reversi.players.Player;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Reversi {

	private static final String RUNNING_GAME_MESSAGE = "The game is already running. If you want start again click restart button.";
	private static final String RESTART_GAME_CONFIRMATION_MESSAGE = "The game is running do you realy want start again?";

	private GameBoard gameBoard;
	private GameLogic gameLogic;
	private BorderPane root;
	private HBox hbox = new HBox();
	private VBox vbox = new VBox();
	private boolean startedGame = false;
	private CompletableFuture<Player> completableFuture;

	public Reversi(GameBoardStandard gameBoardStandard, GameLogicStandard gameLogicStandard) {
		gameBoard = gameBoardStandard;
		gameLogic = gameLogicStandard;
	}

	public VBox getVbox() {
		return vbox;
	}

	public CompletableFuture<Player> getCompletableFuture() {
		return completableFuture;
	}

	public void startGame(Stage stage) {
		root = new BorderPane();
		createTopBox();
		createLeftBox(stage);
		createGameBoard();
		createStage(stage);
	}

	private HBox createTopBox() {
		hbox.setPadding(new Insets(20, 20, 20, 20));
		Text title = new Text("Reversi 1.2 created by Micha³ Sokolonicki");
		title.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().add(title);
		root.setTop(hbox);
		return hbox;
	}

	private void createLeftBox(Stage stage) {
		vbox.setPadding(new Insets(35));
		vbox.setSpacing(8);
		vbox.setAlignment(Pos.CENTER);
		root.setLeft(vbox);
		createWhitePointsPanel();
		createBlackPointsPanel();
		createPlayerTurnPanel();
		createStartButton();
		createRestartButton(stage);
	}

	private void createWhitePointsPanel() {
		Text textWhitePlayer = new Text("WHITE player points:");
		TextField whitePalyerPoints = new TextField();
		whitePalyerPoints.setDisable(true);
		whitePalyerPoints.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
		whitePalyerPoints.setText("2");
		vbox.getChildren().add(0, textWhitePlayer);
		vbox.getChildren().add(1, whitePalyerPoints);
	}

	private void createBlackPointsPanel() {
		Text textBlackPlayer = new Text("BLACK player points:");
		vbox.getChildren().add(2, textBlackPlayer);
		TextField blackPalyerPoints = new TextField();
		blackPalyerPoints.setDisable(true);
		blackPalyerPoints.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
		blackPalyerPoints.setText("2");
		vbox.getChildren().add(3, blackPalyerPoints);
	}

	private void createPlayerTurnPanel() {
		Separator separator = new Separator();
		vbox.getChildren().add(4, separator);
		Text playerTurnLabel = new Text("Player turn:");
		vbox.getChildren().add(5, playerTurnLabel);
		TextField playerTurn = new TextField();
		playerTurn.setDisable(true);
		playerTurn.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
		playerTurn.setText("BLACK player");
		vbox.getChildren().add(6, playerTurn);
	}

	private void createStartButton() {
		Separator separator = new Separator();
		vbox.getChildren().add(7, separator);
		TextField divider = new TextField();
		divider.setVisible(false);
		vbox.getChildren().add(8, divider);
		createStartGameAction();
	}

	private void createStartGameAction() {
		if (!startedGame) {
			gameLogic.initGameLogic(gameBoard);
			startedGame = Boolean.TRUE;
			mouseClickedOnBoard();
		} else {
			createRunningGameInfo();
		}
	}
	
	private void mouseClickedOnBoard() {
		gameBoard.getGridPane().addEventHandler(MouseEvent.MOUSE_CLICKED, pointCliked -> {
			completableFuture = CompletableFuture
					.completedFuture(gameLogic.handleMouseClickedEvent(pointCliked))
					.thenApply(player -> {
						if (player.getColor().equals(Color.WHITE)) {
							Timeline timeLine = new Timeline(new KeyFrame(Duration.millis(300), ev -> {
								gameLogic.makeAgentMove();
							}));
							timeLine.play();
						}
						return player;
					});
		});
	}

	private void createRunningGameInfo() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information");
		alert.setHeaderText(null);
		alert.setContentText(RUNNING_GAME_MESSAGE);
		alert.show();
	}

	private void createRestartButton(Stage stage) {
		Button restartGame = new Button();
		restartGame.setText("Re-start Game");
		vbox.getChildren().add(9, restartGame);
		createRestartGameAction(restartGame, stage);
	}

	private void createRestartGameAction(Button restartGame, Stage stage) {
		restartGame.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText(null);
				alert.setContentText(RESTART_GAME_CONFIRMATION_MESSAGE);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					stage.close();
					restart();
				}
			}
		});
	}

	private void restart() {
		Platform.runLater(() -> {
			try {
				new InitialGame().start(new Stage());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}

	private void createGameBoard() {
		GridPane boardGame = gameBoard.fillBoard(this);
		root.setCenter(boardGame);
	}

	private void createStage(Stage stage) {
		stage.setTitle("Reversi");
		Scene scene = new Scene(root, 1000, 600);
		stage.setScene(scene);
		stage.show();
	}
}
