package com.reversi.board;

import javafx.scene.layout.Pane;

public class BoardMatrix {

	private final int coordinateX;
	private final int coordinateY;
	private Pane pane;

	public BoardMatrix(BoardMatrixBuilder builder) {
		coordinateX = builder.coordinateX;
		coordinateY = builder.coordinateY;
	}

	public int getCoordinateX() {
		return coordinateX;
	}

	public int getCoordinateY() {
		return coordinateY;
	}

	public Pane getPane() {
		return pane;
	}

	public void setPane(Pane pane) {
		this.pane = pane;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + coordinateX;
		result = prime * result + coordinateY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoardMatrix other = (BoardMatrix) obj;
		if (coordinateX != other.coordinateX)
			return false;
		if (coordinateY != other.coordinateY)
			return false;
		return true;
	}

	public static BoardMatrixBuilder builder() {
		return new BoardMatrixBuilder();
	}

	public static class BoardMatrixBuilder {
		private int coordinateX;
		private int coordinateY;

		public BoardMatrixBuilder withCoordinateX(int givenCoordinateX) {
			coordinateX = givenCoordinateX;
			return this;
		}

		public BoardMatrixBuilder withCoordinateY(int givenCoordinateY) {
			coordinateY = givenCoordinateY;
			return this;
		}

		public BoardMatrix build() {
			return new BoardMatrix(this);
		}
	}
}
