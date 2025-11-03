package com.roughlyunderscore.utils

import com.roughlyunderscore.model.Cell
import com.roughlyunderscore.model.CellState

typealias Chain = Pair<Int, Int>
typealias Row = Array<Cell>

/**
 * Returns a string representation of this row.
 */
fun Row.text() = this.joinToString("", "[", "]")

/**
 * Returns a string representation of this chain.
 */
fun Chain.text() = "(${this.first}:${this.second})"

/**
 * Splits this row into a list of chains.
 *
 * Visual example:
 * - "X..X.XX..X", EMPTY, CROSSED -> [(2, 3), (5, 5), (8, 9)]
 * - "X..X.XX..X", CROSSED, EMPTY -> [(1, 1), (4, 4), (6, 7), (10, 10)]
 *
 */
fun Row.rowIntoChains(chainsOfState: CellState, breakAtState: CellState): List<Chain> {
  val result = mutableListOf<Chain>()

  var latest = first().state
  var startPos = 1
  var current = mutableListOf<Cell>()
  this.forEachIndexed { idx, cell ->
    val state = cell.state
    if (state == chainsOfState) {
      if (latest != chainsOfState) startPos = idx + 1
      current.add(cell)
    }

    if (latest == chainsOfState && state == breakAtState) {
      result.add(startPos to idx)
      current = mutableListOf()
    }

    latest = state
  }

  if (current.isNotEmpty()) result.add(startPos to this.size)

  return result
}

/**
 * Fills the cells in this row from and to the given positions (assumes that
 * the positions are 1-indexed, but can be toggled) with the provided cell state.
 */
fun Row.fillFromTo(from: Int, to: Int, with: CellState, indexationStart: Int = 1) {
  for (idx in from..to) {
    this[idx - indexationStart].state = with
  }
}

/**
 * Checks the first and last clue of the row and sees whether
 * any cells can be immediately crossed out.
 * For example, if the first clue is "4" and the first
 * uncrossed chain of cells if of length 3, then they
 * can be immediately crossed out.
 *
 * Additionally, if any chain is smaller of size than the
 * smallest provided clue, crosses out that chain.
 *
 * Visual example:
 * [4] "XX...X...." -> "XXXXXX...."
 */
fun Row.detectInsufficientChains(clues: List<Int>): Boolean {
  var changesDetected = false

  val (firstClue, lastClue) = clues.first() to clues.last()
  val chains = rowIntoChains(CellState.EMPTY, CellState.CROSSED)
  if (chains.isNotEmpty()) {
    val (firstChainObject, lastChainObject) = chains.first() to chains.last()
    val (firstChainStart, firstChainEnd) = firstChainObject
    val (lastChainStart, lastChainEnd) = lastChainObject

    println("Row ${text()} with clues $clues has chains ${chains.map { it.text() }}")

    if (firstChainEnd - firstChainStart + 1 < firstClue) {
      fillFromTo(firstChainStart, firstChainEnd, CellState.CROSSED)
      changesDetected = true
    }

    if (lastChainEnd - lastChainStart + 1 < lastClue) {
      fillFromTo(lastChainStart, lastChainEnd, CellState.CROSSED)
      changesDetected = true
    }
  }

  val smallestClue = clues.min()
  for (chainObject in chains) {
    val (chainStart, chainEnd) = chainObject
    if (chainEnd - chainStart + 1 < smallestClue) {
      fillFromTo(chainStart, chainEnd, CellState.CROSSED)
      changesDetected = true
    }
  }

  return changesDetected
}

/**
 * Checks whether any rows can be filled deterministically,
 * provided the clues, the initial crossed cells, and the board size.
 * Fills any such cells.
 *
 * Visual examples:
 * - 10, [----------]
 * - 6, 3, [----------]
 * - 6, 3, [------X---]
 * - 4, 2, 2, [-------X--]
 * - 4, 4, [----XX----]
 */
fun Row.fillDeterministicRows(clues: List<Int>): Boolean {
  var changesDetected = false

  /**
   * Cross chains will mess up with the calculations,
   * so take them out of the equation
   */
  val crossChains = rowIntoChains(CellState.CROSSED, CellState.EMPTY)
    .size

  val crosses = count { it.state == CellState.CROSSED }


  // 10,      [----------] -> 10 + (1 - 1) + 0 - 0 = 10
  // 6, 3,    [----------] -> 9  + (2 - 1) + 0 - 0 = 10
  // 6, 3,    [------X---] -> 9  + (2 - 1) + 1 - 1 = 10
  // 4, 2, 2, [-------X--] -> 8  + (3 - 1) + 1 - 1 = 10
  // 4, 4,    [----XX----] -> 8  + (2 - 1) + 2 - 1 = 10
  if (clues.sum() + (clues.size - 1) + crosses - crossChains == size) {
    changesDetected = true
    for (cell in this) {
      if (cell.state == CellState.EMPTY) cell.state = CellState.FILLED
    }
  }

  return changesDetected
}