package com.example.alphaacemobile.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname

import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var notification: SharedPreferences

    private var currentTokenCount = 0
    private var previousTokenCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotification()
        binding = ActivityMainBinding.inflate(layoutInflater)
        Thread.sleep(1000)
        val  splashScreen = installSplashScreen()
        setContentView(binding.root)

        Log.d("MAIN_URL", "${Hostname.BASE_URL}")

        /*  SharedPreference  */
        notification = this.getSharedPreferences("notification", Context.MODE_PRIVATE)
        var checkNotification = notification.getString("statusNotification", "").toString()

        // Setting bottom nav with nav controller
        val bottomNavigationView = binding.bottomNavigationView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    var toastview:View = LayoutInflater.from(applicationContext).inflate(R.layout.custom_toast, null)
                    var toast = Toast(applicationContext)
                    toast.view = toastview
                    toast.duration = Toast.LENGTH_LONG
                    toast.show()
                    //Toast.makeText(this, "Loading. Please wait", Toast.LENGTH_SHORT).show()
                    bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.navigate(R.id.homeFragment)
                        bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = true
                    }, 4000)
                }
                R.id.searchFragment -> {
                    var toastview:View = LayoutInflater.from(applicationContext).inflate(R.layout.custom_toast, null)
                    var toast = Toast(applicationContext)
                    toast.view =toastview
                    toast.duration = Toast.LENGTH_LONG
                    toast.show()
                    //Toast.makeText(this, "Loading. Please wait", Toast.LENGTH_SHORT).show()
                    bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.navigate(R.id.searchFragment)
                        bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = true
                    }, 4000)
                }
                R.id.moreFragment -> {
                    var toastview:View = LayoutInflater.from(applicationContext).inflate(R.layout.custom_toast, null)
                    var toast = Toast(applicationContext)
                    toast.view =toastview
                    toast.duration = Toast.LENGTH_LONG
                    toast.show()
                    //Toast.makeText(this, "Loading. Please wait", Toast.LENGTH_SHORT).show()
                    bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = false
                    bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.navigate(R.id.moreFragment)
                        bottomNavigationView.menu.findItem(R.id.homeFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.searchFragment).isEnabled = true
                        bottomNavigationView.menu.findItem(R.id.moreFragment).isEnabled = true
                    }, 4000)
                }
                else -> {
                    true
                }
            }
        }

        if (checkNotification == "1"){
            /*  1st Scan  */
            val url = Hostname.BASE_URL + "get_notification_news"
            val requestQueue = Volley.newRequestQueue(this)
            val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener { response ->

                    Log.d("MAIN_RESPONSE_PREVIOUS", response)

                    val jsonObject = JSONObject(response)
                    if (jsonObject.get("response").equals("Approved")){
                        var count = jsonObject.get("count").toString().toInt()
                        currentTokenCount = count
                        previousTokenCount = count
                    } else {
                        Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener { error ->
                    Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                })
            stringRequest.retryPolicy = DefaultRetryPolicy(
                5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue.add(stringRequest)
        }

        // Setup action bar (title and more)
        setupActionBarWithNavController(navController)
    }

    /*  2nd Scan  */
    override fun onResume() {
        super.onResume()
        /*  SharedPreference  */
        notification = this.getSharedPreferences("notification", Context.MODE_PRIVATE)
        var checkNotification = notification.getString("statusNotification", "").toString()

        if (checkNotification == "1"){
            val updateTimer = Timer()

            val getUpdates = object : TimerTask() {
                override fun run() {

                    Log.d("MAIN_PREVIOUS", "$previousTokenCount" )
                    Log.d("MAIN_CURRENT", "$currentTokenCount" )

                    if (previousTokenCount != currentTokenCount){
                        pushNotification()
                    }

                    checkUpdates()

                    previousTokenCount = currentTokenCount
                }
            }
            updateTimer.schedule(getUpdates, 60000, 60000)
        }
    }

    val CHANNEL_ID = "1000"

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationTitle = "Notification Title"
            val notificationDesc = "Notification Description"
            val notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, notificationTitle, notificationImportance).apply {
                description = notificationDesc
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun pushNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Coincise News Notification Update")
            .setContentText("There is a new news gathered.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun checkUpdates() {
        val url = Hostname.BASE_URL + "get_notification_news"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->

                Log.d("MAIN_RESPONSE_CURRENT", response)

                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Approved")){
                    var count = jsonObject.get("count").toString().toInt()
                    currentTokenCount = count
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        stringRequest.retryPolicy = DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(stringRequest)
    }

    // For Back Button of ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}