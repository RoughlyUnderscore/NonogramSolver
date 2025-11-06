package com.roughlyunderscore.model

class Cell(var state: CellState) {
  constructor() : this(CellState.EMPTY)

  override fun toString() = when (state) {
    CellState.EMPTY -> "-" //"□"
    CellState.FILLED -> "O" //"■"
    CellState.CROSSED -> "x" //"✖"
  }
}