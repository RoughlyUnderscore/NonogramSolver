package com.roughlyunderscore.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import com.roughlyunderscore.model.CellState.CROSSED as C
import com.roughlyunderscore.model.CellState.FILLED as F
import com.roughlyunderscore.model.CellState.EMPTY as E

class BoardTest {
  @Test
  fun test_15_by_15_board() {
    val board = Board(
      scale = 15 to 15,

      rowClues = listOf(
        listOf(2), listOf(2), listOf(2), listOf(4, 2), listOf(1, 3, 5),
        listOf(1, 10), listOf(4), listOf(3), listOf(3, 8), listOf(3, 7, 2),
        listOf(1, 4, 1, 1), listOf(1, 5, 1, 2), listOf(1, 5, 1, 1), listOf(1, 5, 1, 2), listOf(2, 3, 2)
      ),

      columnClues = listOf(
        listOf(2), listOf(8), listOf(5, 1), listOf(1, 2, 3), listOf(1, 1, 6),
        listOf(1, 2, 6), listOf(3, 7), listOf(3, 6), listOf(2, 2), listOf(1, 7),
        listOf(3, 2, 1), listOf(4, 1), listOf(2, 2, 1, 1, 1), listOf(2, 2, 4, 1), listOf(1, 2, 1, 1)
      )
    )
      .crosses(1) { "2:8,10" }

    // Test initial cross placement
    assertEquals(
      board.board[0].map { cell -> cell.state },
      1*E + 7*C + 1*E + 1*C + 5*E
    ) {
      "Initially placed crosses don't match the actual state of the board"
    }

    // Test amount of iterations
    val iterations = board.solve()
    assert(iterations < 10) {
      "Too many iterations (algorithm too slow; 15x15 board, $iterations iterations)"
    }

    // Test correct solution
    assertEquals(
      board.board.map { row -> row.map { cell -> cell.state } } ,
      listOf(
        13*C + 2*F,
        12*C + 2*F + 1*C,
        11*C + 2*F + 2*C,
        4*C + 4*F + 2*C + 2*F + 3*C,
        3*C + 1*F + 2*C + 3*F + 1*C + 5*F,

        2*C + 1*F + 2*C + 10*F,
        2*C + 4*F + 9*C,
        1*C + 3*F + 11*C,
        3*F + 3*C + 8*F + 1*C,
        3*F + 1*C + 7*F + 2*C + 2*F,

        1*C + 1*F + 2*C + 4*F + 1*C + 1*F + 3*C + 1*F + 1*C,
        1*C + 1*F + 1*C + 5*F + 1*C + 1*F + 2*C + 2*F + 1*C,
        1*C + 1*F + 1*C + 5*F + 1*C + 1*F + 4*C + 1*F,
        1*C + 1*F + 1*C + 5*F + 1*C + 1*F + 2*C + 2*F + 1*C,
        1*C + 2*F + 1*C + 3*F + 2*C + 2*F + 4*C,
      )
    ) {
      "The solved board does not match the expected state"
    }
  }
}

operator fun Int.times(cellState: CellState) = List(this) { cellState }
operator fun CellState.times(int: Int) = List(int) { this }