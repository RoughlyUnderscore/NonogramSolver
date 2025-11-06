package com.roughlyunderscore.utils

import com.github.michaelbull.itertools.product
import com.roughlyunderscore.model.Cell
import com.roughlyunderscore.model.CellState
import com.roughlyunderscore.model.CellState.EMPTY
import com.roughlyunderscore.model.CellState.CROSSED
import com.roughlyunderscore.model.CellState.FILLED
import java.util.LinkedList
import java.util.Queue
import kotlin.math.max
import kotlin.system.exitProcess

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
    val cell = this[max(0, idx - indexationStart)]
    if (cell.state == replace || replace == null) cell.state = with
  }
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

  val (first, last) = clues.first() to clues.last()
  val second = clues.getOrElse(1) { 0 }
  val secondToLast = clues.reversed().getOrElse(1) { 0 }
  
  val chains = rowIntoChains(listOf(FILLED), listOf(CROSSED, EMPTY))

  if (chains.isNotEmpty()) {
    val (firstChainObject, lastChainObject) = chains.first() to chains.last()
    val (_, firstChainEnd) = firstChainObject
    val (lastChainStart, _) = lastChainObject

    if (firstChainEnd < first + second && (firstChainEnd != size && this[firstChainEnd].state == CROSSED)) {
      fillFromTo(firstChainEnd - first + 1, firstChainEnd, FILLED)
      replaceFromTo(1, firstChainEnd, CROSSED, EMPTY)
      changesDetected = true
    }

    if ((size - lastChainStart) < last + secondToLast && (lastChainStart != 1 && this[lastChainStart - 2].state == CROSSED)) {
      fillFromTo(lastChainStart, lastChainStart + last - 1, FILLED)
      replaceFromTo(lastChainStart, size, CROSSED, EMPTY)
      changesDetected = true
    }
  }

  return changesDetected
}

/**
 * If the row is filled, adds crosses wherever they are missing.
 *
 * Visual example:
 * - 2, [----OO----] -> [xxxxOOxxxx]
 */
fun Row.crossOutFilled(clues: List<Int>): Boolean {
  if (count { it.state == FILLED } == clues.sum()) {
    replaceFromTo(1, size, CROSSED, EMPTY)
    return true
  }

  return false
}

fun Row.intersectionMethod(clues: List<Int>): Boolean {
  if (none { it.state == EMPTY }) return false

  var changesDetected = false
  val layouts = mutableListOf<List<CellState>>()

  val cluesTotal = clues.sum()

  // Iterate through all possible cross amounts
  // E.g. for clues [6,1] there can be three cross chains (before, between, after)
  // and the total complementary amount is 10 - (6+1) = 3
  // Thus iterate from (0, 0, 0) to (3, 3, 3) and discard any invalid combinations
  val crossAmounts = clues.size + 1

  val range = (0..(size - cluesTotal)).toList()
  val exclusiveRange = (1..(size - cluesTotal)).toList()
  val iterations = (
    if (crossAmounts == 2) listOf(range.toList(), range.toList())
    else listOf(range) + List(crossAmounts - 2) { exclusiveRange } + listOf(range)
  ).product().toList()

  // println("Clues: $clues | Iterations: $iterations")
  for (product in iterations) {
    // Immediately discard products that don't satisfy the size requirement
    if (product.sum() + cluesTotal != size) continue

    val layout = Row(size) { Cell() }

    // EXAMPLE | Clues: [6, 1]; crosses: [1, 1, 1]
    // Go through [0; crossAmounts * 2)
    // If even, fill the crosses; if odd, fill the clues.
    var pos = 1
    for (i in (0 ..< crossAmounts * 2 - 1)) {
      //println("Clues: $clues; crosses: $product; idx: $i; total range: ${(0..<crossAmounts*2)}")
      val idx = i / 2
      val length: Int
      if (i % 2 == 0) {
        length = product[idx]
        // Only the first or last crosses can be of zero length
        // if (length == 0 && (idx != 0 && idx != crossAmounts - 1)) continue
        layout.fillFromTo(pos, pos + length - 1, CROSSED)
      } else {
        length = clues[idx]
        //println("Filling from $pos to ${pos + length} with FILLED")
        layout.fillFromTo(pos, pos + length - 1, FILLED)
      }

      pos += length
    }

    // Check whether the layout overlaps with any of the previously established data
    var badLayout = false
    for (idx in layout.indices) {
      val member = layout[idx].state
      val corresponding = this[idx].state
      if ((member == CROSSED && corresponding == FILLED) ||
        (member == FILLED && corresponding == CROSSED)) badLayout = true
    }

    if (badLayout) continue

    // Add this layout to the total list of layouts
    layouts.add(layout.map { it.state })
  }

  // println("Layouts: ${layouts.map { it.joinToString("") { if (it == FILLED) "O" else "x" }}}")
  // Check whether there are any cells that share common cell states among all
  // existing layouts
  for (idx in 0 ..< size) {
    val members = layouts.map { it[idx] }
    if (members.toSet().size == 1) {
      this[idx].state = members[0]
      changesDetected = true
    }
  }

  return changesDetected
}