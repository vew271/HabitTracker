package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.*

class CountDownActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        workManager = WorkManager.getInstance(this)
        viewModel.currentTimeString.observe(this,{
            if(it != null) {
                findViewById<TextView>(R.id.tv_count_down).text = it
            }
        })
        viewModel.setInitialTime(habit.minutesFocus)

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            updateButtonState(true)
            viewModel.startTimer()
            viewModel.eventCountDownFinish.observe(this,{
                if(it){
                    updateButtonState(false)
                    val myData = Data.Builder()
                        .putInt(HABIT_ID, habit.id)
                        .putString(HABIT_TITLE, habit.title)
                        .build()
                    val myConstraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                    val myRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                        .setInputData(myData)
                        .setConstraints(myConstraints)
                        .addTag(NOTIFICATION_CHANNEL_ID)
                        .build()
                    workManager.enqueueUniqueWork(NOTIF_UNIQUE_WORK, ExistingWorkPolicy.REPLACE, myRequest)
                }
            })
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            updateButtonState(false)
            viewModel.resetTimer()
            workManager.cancelUniqueWork(NOTIF_UNIQUE_WORK)
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}