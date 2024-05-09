package com.example.DiamondMiner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*

class MainActivity : Activity() {

    private lateinit var highestScoreTextView: TextView
    private var highestScore = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isGameEnded = false // Declare boolean flag to track game end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        val resetButton = findViewById<Button>(R.id.reset_highest_score)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = "Highest Score: $highestScore"

        val board = findViewById<RelativeLayout>(R.id.board)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val mainMenu = findViewById<Button>(R.id.MainMenu)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<RelativeLayout>(R.id.board1)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest Score: $highestScore"

        val diamond = ImageView(this)
        val miner = ImageView(this)
        val goblin = ImageView(this) // Add goblin ImageView
        val minerSegments = mutableListOf(miner)
        val handler = Handler()
        var delayMillis = 25L
        var currentDirection = "right"
        var scorex = -1

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.VISIBLE

        resetButton.setOnClickListener {
            // Reset the highest score to 0
            highestScore = 0
            highestScoreTextView.text = " Highest Score: $highestScore"
            saveHighestScore()
        }

        newgame.setOnClickListener {
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            endGameButton.visibility = View.VISIBLE

            val minerWidth = 240 // Snake width in pixels
            val minerHeight = 240 // Snake height in pixels
            val diamondWidth = 112 // Meat width in pixels
            val diamondHeight = 294 // Meat height in pixels
            val goblinWidth = 240 // goblin width in pixels
            val goblinHeight = 240 // goblin height in pixels
            goblin.scaleX=-1f

            miner.setImageResource(R.drawable.miner)
            miner.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            miner.layoutParams = ViewGroup.LayoutParams(minerWidth, minerHeight)
            board.addView(miner)
            minerSegments.add(miner)

            var minerX = miner.x
            var minerY = miner.y

            diamond.setImageResource(R.drawable.diamond)
            diamond.setPadding(-10, -80, -10, -60) // Add padding to increase touch-sensitive area
            diamond.layoutParams = ViewGroup.LayoutParams(diamondWidth, diamondHeight)
            board.addView(diamond)

            goblin.setImageResource(R.drawable.goblin) // Assuming "goblin" is the name of your vector drawable
            goblin.layoutParams = ViewGroup.LayoutParams(goblinWidth, goblinHeight)
            board.addView(goblin)

            // Function to generate random coordinates for goblin within the board bounds
            fun generateRandomPosition(): Pair<Float, Float> {
                //val randomX = Random().nextInt(500 - goblinWidth)
                //val randomY = Random().nextInt(500 - goblinHeight)
                return Pair(400f, 550f)
            }

            // Position the goblin at a random location initially
            var (goblinX, goblinY) = generateRandomPosition()
            goblin.x = goblinX
            goblin.y = goblinY

            // Add logic to position the goblin within the board layout

            fun checkFoodCollision() {
                val diamondBounds = Rect()
                diamond.getHitRect(diamondBounds)

                for (segment in minerSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(diamondBounds, segmentBounds)) {
                        val randomX = Random().nextInt(board.width - 200)
                        val randomY = Random().nextInt(board.height - 200)

                        diamond.x = randomX.toFloat()
                        diamond.y = randomY.toFloat()



                        delayMillis--
                        scorex++
                        score2.text = "score : $scorex"

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = " Highest Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            fun checkgoblinCollision() {
                val goblinBounds = Rect()
                goblin.getHitRect(goblinBounds)

                for (segment in minerSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(goblinBounds, segmentBounds)) {
                        isGameEnded = true // End the game if goblin collision detected
                        playagain.visibility = View.VISIBLE
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.INVISIBLE
                        mainMenu.visibility = View.VISIBLE

                        return // Exit the function once collision is detected
                    }
                }
            }

            // Define a function to move the goblin
            fun movegoblin() {
                // Implement your logic to move the goblin here
                // For example, you can move it randomly or towards a specific direction
                // Here's a simple example of moving the goblin towards the miner's current position
                val dx = miner.x - goblin.x
                val dy = miner.y - goblin.y

                // Move goblin towards the miner's position
                goblin.x += dx / 340
                goblin.y += dy / 340
            }

            val goblinMovementHandler = Handler()
            val goblinMovementRunnable = object : Runnable {
                override fun run() {
                    movegoblin()
                    checkgoblinCollision()
                    goblinMovementHandler.postDelayed(this, delayMillis)
                }
            }
            // Start moving the goblin
            goblinMovementHandler.postDelayed(goblinMovementRunnable, delayMillis)

            val runnable = object : Runnable {
                override fun run() {
                    for (i in minerSegments.size - 1 downTo 1) {
                        minerSegments[i].x = minerSegments[i - 1].x
                        minerSegments[i].y = minerSegments[i - 1].y
                    }

                    when (currentDirection) {
                        "up" -> {
                            minerY -= 3
                            if (minerY < -800) {
                                minerY = 750f
                            }
                            miner.translationY = minerY
                        }
                        "down" -> {
                            minerY += 3
                            if (minerY > 1150 - miner.height) {
                                minerY = -850f
                            }
                            miner.translationY = minerY
                        }
                        "left" -> {
                            minerX -= 3
                            if (minerX < -680) {
                                minerX = 660f
                            }
                            miner.scaleX = -1f // Flip the miner horizontally
                            miner.translationX = minerX
                        }
                        "right" -> {
                            minerX += 3
                            if (minerX > 860 - miner.width) {
                                minerX = -680f
                            }
                            miner.scaleX = 1f
                            miner.translationX = minerX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    checkgoblinCollision() // Check for goblin collision
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

            upButton.setOnClickListener {
                currentDirection = "up"
            }
            downButton.setOnClickListener {
                currentDirection = "down"
            }
            leftButton.setOnClickListener {
                currentDirection = "left"
            }
            rightButton.setOnClickListener {
                currentDirection = "right"
            }
            pauseButton.setOnClickListener {
                currentDirection = "pause"
                board.visibility = View.INVISIBLE
                resume.visibility = View.VISIBLE
                endGameButton.visibility = View.VISIBLE
            }
            resume.setOnClickListener {
                currentDirection = "right"
                board.visibility = View.VISIBLE
                resume.visibility = View.INVISIBLE
            }
            endGameButton.setOnClickListener {
                // Create an AlertDialog
                val alertDialogBuilder = AlertDialog.Builder(this)

                // Set the title and message
                alertDialogBuilder.setTitle("Confirm Exit")
                alertDialogBuilder.setMessage("Are you sure you want to exit the game?")

                // Set a positive button and its click listener
                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    // Finish the current activity (exit the game)
                    finishAffinity()
                    System.exit(0)
                }

                // Set a negative button and its click listener
                alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog if "No" is clicked
                    dialog.dismiss()
                }

                // Create and show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        mainMenu.setOnClickListener {
            val Intent = Intent(this, newGame::class.java)
            startActivity(Intent)
        }

        hideSystemUI()
    }

    private fun saveHighestScore() {
        val editor = sharedPreferences.edit()
        editor.putInt("highestScore", highestScore)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
