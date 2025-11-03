package com.roughlyunderscore.utils

import com.roughlyunderscore.model.Cell

fun Array<Array<Cell>>.swapRowsAndColumns(): Array<Array<Cell>> {
  val (rows, cols) = size to this[0].size
  return Array(cols) { i ->
    Array(rows) { j ->
      this[j][i]
    }
  }
}