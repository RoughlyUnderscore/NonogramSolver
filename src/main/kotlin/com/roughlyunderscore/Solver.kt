package com.roughlyunderscore

import com.roughlyunderscore.model.Board
import com.roughlyunderscore.utils.text

fun main(args: Array<out String>) {
  val _board = Board(
    scale = 15 to 15,

    /*rowClues = listOf(
      listOf(3), listOf(2, 1), listOf(3, 2), listOf(4, 4), listOf(4, 2, 2),
      listOf(6, 2), listOf(6, 3), listOf(6, 3), listOf(9), listOf(3, 3)
    ),

    columnClues = listOf(
      listOf(6), listOf(8), listOf(10), listOf(1, 7), listOf(1, 5),
      listOf(8), listOf(3, 2), listOf(1, 4), listOf(7), listOf(5)
    )*/

    rowClues = listOf(
      listOf(3), listOf(2, 2), listOf(2, 2), listOf(1, 1, 1), listOf(3, 3, 2),
      listOf(1, 1, 1, 1, 2), listOf(1, 1, 3, 1, 2), listOf(4, 1, 4), listOf(10), listOf(3, 1, 1, 3),
      listOf(1, 1, 1, 1, 3), listOf(1, 1, 1), listOf(11), listOf(11), listOf(7)
    ),

    columnClues = listOf(
      listOf(2, 2, 1), listOf(3, 1), listOf(11), listOf(2, 1, 2, 2), listOf(2, 3, 1, 2),
      listOf(1, 8), listOf(2, 3, 1, 2), listOf(2, 1, 1, 2), listOf(13), listOf(2, 3),
      listOf(3, 3), listOf(2, 1, 2), listOf(2, 2, 2), listOf(2, 1, 2), listOf(2, 1, 1)
    )
  )
    /*.crosses(1) { "1,6:7" }
    .crosses(2) { "1,5,7:9" }
    .crosses(3) { "4:5" }
    .crosses(5) { "8" }
    .crosses(6) { "7,8" }
    .crosses(8) { "7" }
    .crosses(10) { "6,10" }*/

  /*val board = Board(
    scale = 10 to 10,

    rowClues = listOf(
      listOf(2), listOf(8), listOf(10), listOf(2, 2, 2), listOf(2, 3, 3),
      listOf(4, 4), listOf(2, 4, 2), listOf(3, 1, 3), listOf(8, 1), listOf(8)
    ),

    columnClues = listOf(
      listOf(7), listOf(9), listOf(2, 1, 3), listOf(2, 3, 2), listOf(5, 1, 2),
      listOf(5, 4), listOf(2, 2, 2), listOf(2, 2, 3), listOf(7, 1), listOf(7)
    )
  )*/

  try {
    _board.solve()
  } catch (ex: IllegalStateException) {
    println("Over")
  }

  _board.board.forEach { println(it.text()) }
}