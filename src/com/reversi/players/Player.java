package com.reversi.players;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Player extends Circle {

	private Color color;

	public Player(PlayerBuilder builder) {
		this.color = builder.color;
		int circleCenter = builder.gridSize / 2;
		setFill(builder.color);
		setCenterX(circleCenter);
		setCenterY(circleCenter);
		setRadius(circleCenter - 6);
	}

	public Color getColor() {
		return color;
	}

	public static PlayerBuilder builder() {
		return new PlayerBuilder();
	}

	public static class PlayerBuilder extends Circle {
		private int gridSize;
		private Color color;

		public PlayerBuilder withGridSize(int size) {
			gridSize = size;
			return this;
		}

		public PlayerBuilder withColor(Color col) {
			color = col;
			return this;
		}

		public Player build() {
			return new Player(this);
		}
	}

	public Player changeColor() {
		if (color == Color.BLACK) {
			color = Color.WHITE;
		} else {
			color = Color.BLACK;
		}
		return this;
	}

}
