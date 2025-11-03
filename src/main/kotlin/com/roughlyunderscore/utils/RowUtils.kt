package com.roughlyunderscore.utils

import com.roughlyunderscore.model.Cell
import com.roughlyunderscore.model.CellState
import com.roughlyunderscore.model.CellState.EMPTY
import com.roughlyunderscore.model.CellState.CROSSED
import com.roughlyunderscore.model.CellState.FILLED

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
fun Row.rowIntoChains(chainsOfState: List<CellState>, breakAtState: List<CellState>): List<Chain> {
  val result = mutableListOf<Chain>()

  var latest = first().state
  var startPos = indexOfFirst { it.state !in breakAtState } + 1
  var current = mutableListOf<Cell>()
  this.forEachIndexed { idx, cell ->
    val state = cell.state
    if (state in chainsOfState) {
      if (latest in breakAtState) startPos = idx + 1
      current.add(cell)
    }

    if (latest in chainsOfState && state in breakAtState) {
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
fun Row.fillFromTo(from: Int, to: Int, with: CellState, indexationStart: Int = 1) =
  replaceFromTo(from, to, with, null, indexationStart)

/**
 * Replaces the cells in this row from and to the given positions (assumes that
 * the positions are 1-indexed, but can be toggled) with the provided cell state.
 */
fun Row.replaceFromTo(from: Int, to: Int, with: CellState, replace: CellState?, indexationStart: Int = 1) {
  for (idx in from..to) {
    val cell = this[idx - indexationStart]
    if (cell.state == replace || replace == null) cell.state = with
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
  val chains = rowIntoChains(listOf(EMPTY, FILLED), listOf(CROSSED))

  if (chains.isNotEmpty()) {
    val (firstChainObject, lastChainObject) = chains.first() to chains.last()
    val (firstChainStart, firstChainEnd) = firstChainObject
    val (lastChainStart, lastChainEnd) = lastChainObject

    if (firstChainEnd - firstChainStart + 1 < firstClue) {
      fillFromTo(firstChainStart, firstChainEnd, CROSSED)
      changesDetected = true
    }

    if (lastChainEnd - lastChainStart + 1 < lastClue) {
      fillFromTo(lastChainStart, lastChainEnd, CROSSED)
      changesDetected = true
    }
  }

  val smallestClue = clues.min()
  for (chainObject in chains) {
    val (chainStart, chainEnd) = chainObject
    if (chainEnd - chainStart + 1 < smallestClue) {
      fillFromTo(chainStart, chainEnd, CROSSED)
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
  if (none { it.state == EMPTY }) return false

  var changesDetected = false

  /**
   * Cross chains will mess up with the calculations,
   * so take them out of the equation
   */
  val crossChains = rowIntoChains(listOf(CROSSED), listOf(EMPTY)).size

  val crosses = count { it.state == CROSSED }
  val initial = if (first().state == CROSSED) 1 else 0
  val last = if (last().state == CROSSED) 1 else 0


  // 10,      [----------] -> 10  + (1 - 1) + 0 - 0 + 0 + 0 = 10
  // 6, 3,    [----------] -> 9   + (2 - 1) + 0 - 0 + 0 + 0 = 10
  // 6, 3,    [------X---] -> 9   + (2 - 1) + 1 - 1 + 0 + 0 = 10
  // 4, 2, 2, [-------X--] -> 8   + (3 - 1) + 1 - 1 + 0 + 0 = 10
  // 4, 4,    [----XX----] -> 8   + (2 - 1) + 2 - 1 + 0 + 0 = 10
  // 9,       [X---------] -> 9   + (1 - 1) + 1 - 1 + 1 + 0 = 10
  // 3,       [X--OXXXXXX] -> 3   + (1 - 1) + 7 - 2 + 1 + 1 = 10
  if (clues.sum() + (clues.size - 1) + crosses - crossChains + initial + last == size) {
    println("Provided row ${text()} with clues $clues to check for deterministic filling (can be filled)")
    changesDetected = true

    // Find the first uncrossed cell and start filling from there
    val firstUncrossed = indexOfFirst { it.state != CROSSED } + 1
    var currentClueIdx = 0
    var filledCells = 0
    for (idx in firstUncrossed..size) {
      if (currentClueIdx == clues.size) break

      if (filledCells == clues[currentClueIdx]) {
        this[idx - 1].state = CROSSED
        filledCells = 0
        currentClueIdx++
      } else {
        if (this[idx - 1].state != CROSSED) {
          this[idx - 1].state = FILLED
          filledCells++
        }
      }
    }
  } else println("Provided row ${text()} with clues $clues to check for deterministic filling (can't be filled)")

  return changesDetected
}

/**
 * Checks whether either the first or the last clue is deterministically
 * completed and allows for extra crosses to be added to the board.
 * 
 * Visual example:
 * - 3, 2, [--OxxOO---] -> [--OxxOOxxx]
 */
fun Row.detectCompletedBoundaryClues(clues: List<Int>): Boolean {
  var changesDetected = false

  // Say that the leftmost and rightmost clues are equal to M and N respectively.
  val (m, n) = clues.first() to clues.last()
  
  // If there is a chain of N (or less by x<N) filled cells on the right such that
  // the amount of free cells to the right of the chain is less than M+x+1,
  // then this chain corresponds to the rightmost clue and the cells to the
  // right of it can be deterministically crossed out.
  // And vice versa: if there is a chain of M (or less by x<N) filled cells
  // on the left such that the amount of free cells to the left of the chain
  // is less than N+x+1, then this chain corresponds to the leftmost clue and
  // the cells to the left of it can be deterministically crossed out.
  val chains = rowIntoChains(listOf(FILLED), listOf(CROSSED, EMPTY))
  if (chains.isNotEmpty()) {
    val (firstChainObject, lastChainObject) = chains.first() to chains.last()
    val (_, firstChainEnd) = firstChainObject
    val (lastChainStart, _) = lastChainObject

    if (firstChainEnd < n + m && (firstChainEnd != size && this[firstChainEnd].state == CROSSED)) {
      fillFromTo(firstChainEnd - m + 1, firstChainEnd, FILLED)
      replaceFromTo(1, firstChainEnd, CROSSED, EMPTY)
      changesDetected = true
    }

    if ((size - lastChainStart) < n + m && (lastChainStart != 1 && this[lastChainStart - 2].state == CROSSED)) {
      fillFromTo(lastChainStart, lastChainStart + n - 1, FILLED)
      replaceFromTo(lastChainStart, size, CROSSED, EMPTY)
      changesDetected = true
    }
  }

  return changesDetected
}