package com.reversi.players;

import java.util.UUID;

import com.reversi.board.BoardMatrix;

public class ValueCellCoordinates {
	private BoardMatrix coordinate;
	private UUID Id;
	private UUID parentId;

	public ValueCellCoordinates(BoardMatrix coordinate) {
		this.coordinate = coordinate;
		Id = UUID.randomUUID();
	}

	public BoardMatrix getCoordinate() {
		return coordinate;
	}

	public UUID getId() {
		return Id;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}
}
