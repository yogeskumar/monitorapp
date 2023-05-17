package com.example.monitorapp

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AppStatsActivity : AppCompatActivity() {

    private lateinit var packageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_stats)

        packageName = intent.getStringExtra("packageName") ?: ""
        title = getAppName(packageName)

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()

        val todayUsage = getAppUsage(usageStats, startTime, endTime)
        val yesterdayUsage = getYesterdayAppUsage(usageStats, startTime, endTime)
        val last7DaysUsage = getLast7DaysAppUsage(usageStats)
        val last30DaysUsage = getLast30DaysAppUsage(usageStats)
        val lastTimeUsed = getLastTimeUsed(usageStats)

        // Display the usage statistics for the selected app as required
        // For simplicity, you can display the usage stats in a TextView or any other UI component

        // Example usage:
        val statsTextView = findViewById<TextView>(R.id.statsTextView)
        statsTextView.text = "Today's Usage: ${formatUsageTime(todayUsage)}\n" +
                "Yesterday's Usage: ${formatUsageTime(yesterdayUsage)}\n" +
                "Last 7 Days' Usage: ${formatUsageTime(last7DaysUsage)}\n" +
                "Last 30 Days' Usage: ${formatUsageTime(last30DaysUsage)}\n" + "Last Time used: ${dateFormat.format(lastTimeUsed)}"
    }

    private fun getAppUsage(usageStats: List<UsageStats>, startTime: Long, endTime: Long): Long {
        var totalUsageTime = 0L
        for (stat in usageStats) {
            if (stat.packageName == packageName) {
                val usageTime = stat.totalTimeInForeground
                if (usageTime > 0 && stat.firstTimeStamp >= startTime && stat.lastTimeStamp <= endTime) {
                    totalUsageTime += usageTime
                }
            }
        }
        return totalUsageTime
    }

    private fun getYesterdayAppUsage(usageStats: List<UsageStats>, startTime: Long, endTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterdayStartTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val yesterdayEndTime = calendar.timeInMillis
        return getAppUsage(usageStats, yesterdayStartTime, yesterdayEndTime)
    }

    private fun getLast7DaysAppUsage(usageStats: List<UsageStats>): Long {
        val calendar = Calendar.getInstance()
        val todayEndTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val last7DaysStartTime = calendar.timeInMillis
        return getAppUsage(usageStats, last7DaysStartTime, todayEndTime)
    }

    // ...

    private fun getLast30DaysAppUsage(usageStats: List<UsageStats>): Long {
        val calendar = Calendar.getInstance()
        val todayEndTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val last30DaysStartTime = calendar.timeInMillis
        return getAppUsage(usageStats, last30DaysStartTime, todayEndTime)
    }

    private fun getAppName(packageName: String): String {
        val packageManager = packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return applicationInfo.loadLabel(packageManager).toString()
    }

    private fun formatUsageTime(usageTime: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(usageTime)
        val hours = TimeUnit.MILLISECONDS.toHours(usageTime)
        val minutesString = String.format("%02d", minutes % 60)
        val hoursString = String.format("%02d", hours)
        return "$hoursString:$minutesString"
    }
    private fun getLastTimeUsed(usageStats: List<UsageStats>): Long {
        var lastTimeUsed = 0L
        for (stat in usageStats) {
            if (stat.packageName == packageName && stat.lastTimeUsed > lastTimeUsed) {
                lastTimeUsed = stat.lastTimeUsed
            }
        }
        return lastTimeUsed
    }
}
