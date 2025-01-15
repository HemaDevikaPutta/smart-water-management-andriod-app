package com.example.smartwatermanagement

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeakageActivity : ComponentActivity() {

    private var showDialog = false // Declare showDialog for handling logout dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        // Registering for the result of the email activity
        val emailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // After returning from the email app, navigate to HomeActivity
                CoroutineScope(Dispatchers.Main).launch {
                    val intent = Intent(this@LeakageActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Close current activity after starting HomeActivity
                }
            }
        }

        setContent {
            val message = intent.getStringExtra("level")
            Log.e("message", message.toString())
            val context = LocalContext.current

            if (message == "Warning: Possible water leakage detected!") {
                sendNotification(
                    context,
                    title = "Warning!",
                    message = "Possible water leakage detected!"
                )
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "Smart Water Management System",
                                    color = Color.White,
                                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        backgroundColor = colorResource(id = R.color.dark_blue),
                        contentColor = colorResource(id = R.color.white),
                        modifier = Modifier.height(62.dp)
                    )
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.light_sky_blue)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (message == "Warning: Possible water leakage detected!") {
                                Image(
                                    painter = painterResource(id = R.drawable.alert_icon),
                                    contentDescription = "Alert Icon",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(bottom = 16.dp)
                                )
                            }

                            Text(
                                text = message ?: "No message",
                                color = colorResource(id = R.color.dark_blue),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (message == "Warning: Possible water leakage detected!") {
                                    val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
                                    val savedEmail = sharedPreferences.getString("email", "") ?: "" // Default to empty string if not found
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:")
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf("$savedEmail"))
                                        putExtra(Intent.EXTRA_SUBJECT, "Warning")
                                        putExtra(Intent.EXTRA_TEXT, "Possible water leakage detected!")
                                    }

                                    emailIntent.setPackage("com.google.android.gm") // Use Gmail app
                                    try {
                                        emailLauncher.launch(emailIntent) // Launch email intent and wait for result
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(
                                            context,
                                            "No email app found to send this email.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val intent = Intent(context, HomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    (context as Activity).finish() // Optional: close the current activity to remove it from the stack

                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .align(Alignment.BottomCenter),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                colorResource(id = R.color.light_blue),
                                                colorResource(id = R.color.light_green_color)
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Go To Home",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "my_channel_id"
        val notificationId = 1

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.alert_icon) // Ensure this resource exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
                Log.d("Notification", "Notification sent with ID: $notificationId")
            }
        } catch (e: Exception) {
            Log.e("NotificationError", "Failed to send notification: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Notification Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for notifications"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
