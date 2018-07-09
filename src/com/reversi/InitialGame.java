package com.reversi;

import com.reversi.board.GameBoardStandard;
import com.reversi.logic.GameLogicStandard;

import javafx.application.Application;
import javafx.stage.Stage;

public class InitialGame extends Application {

	@Override
	public void start(Stage stage) {
		Reversi reversi = new Reversi(new GameBoardStandard(), new GameLogicStandard());
		reversi.startGame(stage);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
