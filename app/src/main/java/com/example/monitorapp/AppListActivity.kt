package com.example.monitorapp

import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.provider.Settings;


class AppListActivity : AppCompatActivity() {

    private lateinit var appListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        appListView = findViewById(R.id.appListView)

        val packageManager = packageManager
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        val appNames = mutableListOf<String>()
        for (app in installedApps) {
            appNames.add(app.applicationInfo.loadLabel(packageManager).toString())
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appNames)
        appListView.adapter = adapter

        appListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedApp = installedApps[position]
            val packageName = selectedApp.packageName

            val intent = Intent(this, AppStatsActivity::class.java)
            intent.putExtra("packageName", packageName)
            startActivity(intent)
        }
    }

    private fun hasPermission(): Boolean {
        val context = applicationContext
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun requestPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
