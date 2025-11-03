package com.roughlyunderscore.model

import com.roughlyunderscore.utils.detectCompletedBoundaryClues
import com.roughlyunderscore.utils.detectInsufficientChains
import com.roughlyunderscore.utils.fillDeterministicRows
import com.roughlyunderscore.utils.swapRowsAndColumns
import com.roughlyunderscore.utils.text

class Board(
  /**
   * X*Y scale of the board (rows * columns)
   */
  val scale: Pair<Int, Int>,

  /**
   * The clues for each row, starting with the topmost one.
   *
   * Example:
   * - [[2, 7], [6], [7], [1, 1]]
   */
  val rowClues: List<List<Int>>,

  /**
   * The clues for each column, starting with the leftmost one.
   *
   * Example: see [rowClues]
   */
  val columnClues: List<List<Int>>,
) {
  val rows = scale.first
  val cols = scale.second
  
  /**
   * This property indicates whether there are any empty cells
   * on the board (not crossed or filled).
   */
  val isSolved
    get() = board.all { row -> row.none { it.state == CellState.EMPTY } }
  
  /**
   * The cells array stores an array of rows.
   */
  val board = Array(rows) {
    Array(cols) { Cell() }
  }

  /**
   * Fills a row with crosses at specific positions.
   * The positions and the row are indexed starting with 1.
   * Returns itself for a builder-esque pattern.
   *
   * Syntax example:
   * - "3:5,7,9" (fills cells 3, 4, 5, 7, 9 in given row)
   */
  fun crosses(row: Int, value: () -> String): Board {
    value().split(",").forEach { part ->
      val range = if (":" in part) part.split(":").let { bounds ->
        (bounds[0].toInt()..bounds[1].toInt()).toList()
      } else listOf(part.toInt())

      for (cell in range)
        board[row - 1][cell - 1].state = CellState.CROSSED
    }

    return this
  }

  /**
   * Iteratively goes through the board, filling in as many cells
   * as possible in this given iteration, until the board is solved
   * or no more cells can be filled deterministically.
   */
  fun solve(): Int {
    var iterations = 0
    println("Started solving")
    while (!isSolved) {
      if (iterations > 20) throw IllegalStateException("Too much")

      iterations++
      println("\n------\nIteration $iterations")

      /*println("Empty cells: ${board.sumOf { row -> row.count { it.state == CellState.EMPTY } }}")
      println("Board state:")
      board.forEach { println(it.text()) }
      println("")*/

      var changesDetected = false

      changesDetected = changesDetected or trySolvingRows(board)

      // Swap rows and columns and try again
      println("Swapping columns and rows")
      var transposedBoard = board.swapRowsAndColumns()
      changesDetected = changesDetected or trySolvingRows(transposedBoard, true)

      // Swap rows and columns back and refill the board
      transposedBoard = transposedBoard.swapRowsAndColumns()
      for (row in 1..rows) {
        for (col in 1..cols) {
          board[row - 1][col - 1] = transposedBoard[row - 1][col - 1]
        }
      }
      
      // If no changes have been performed at all during this iteration,
      // throw an exception and exit.
      if (!changesDetected) {
        throw IllegalStateException("The board could not be filled any further")
      }
    }

    return iterations
  }

  private fun trySolvingRows(targetBoard: Array<Array<Cell>>, swapped: Boolean = false): Boolean {
    var changesDetected = false
    for (idx in 1..(if (swapped) cols else rows)) {
      val row = targetBoard[idx - 1]
      val clues = (if (swapped) columnClues else rowClues)[idx - 1]

      println("Row before detections: ${row.text()}")
      changesDetected = changesDetected or row.detectInsufficientChains(clues)
      println("Row after insufficient chains: ${row.text()}")
      changesDetected = changesDetected or row.fillDeterministicRows(clues)
      println("Row after deterministic filling: ${row.text()}")
      changesDetected = changesDetected or row.detectCompletedBoundaryClues(clues)
      println("Row after boundary clues: ${row.text()}")

      println()
    }

    return changesDetected
  }
}
