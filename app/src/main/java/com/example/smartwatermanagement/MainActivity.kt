package com.example.smartwatermanagement

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {
    private lateinit var BASE_URL: String



    // Retrofit API service instance
    private val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Replace with your local machine's IP address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BASE_URL = getString(R.string.ip_address)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        createNotificationChannel(this)


        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isPasswordVisible by remember { mutableStateOf(false) }
            val context = LocalContext.current // Access the context for Toast
            val scrollState = rememberScrollState()
            val ipAddress = getLocalIpAddress(context)
            Log.e("IP Address", ipAddress)

            // Retrieve saved login details from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("email", "") ?: "" // Default to empty string if not found
            val savedPassword = sharedPreferences.getString("password", "") ?: "" // Default to empty string if not found

            // Update the state if saved credentials exist
            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.light_sky_blue)) // Background color
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp).verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Bottom, // Align items to the top
                    horizontalAlignment = Alignment.CenterHorizontally // Align items horizontally in the center
                ) {
                    Text(
                        text = "Smart Water Management System",
                        style = TextStyle(
                            color = colorResource(id = R.color.dark_blue),
                            fontSize = 23.sp,
                            fontWeight = FontWeight(700)
                        ),
                        modifier = Modifier
                            .fillMaxWidth() // Ensure the text takes up the full width
                            .padding(start = 30.dp, end = 30.dp) // Add padding to the left and right
                            .align(Alignment.CenterHorizontally), // Center the text horizontally
                        textAlign = TextAlign.Center // Ensure text alignment is centered even when wrapping
                    )


                    Spacer(modifier = Modifier.weight(1f))

                    // Logo Image - Adjust image to fill screen width and height dynamically
                    val imageHeight = 300.dp // Fixed height, can change dynamically based on screen size if needed
                    Image(
                        painter = painterResource(id = R.drawable.water),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .fillMaxWidth() // Make image fill the width of the screen
                            .height(imageHeight) // Dynamic height for the image
                            .clip(RoundedCornerShape(8.dp)) // Optionally clip to rounded corners
                            .padding(top = 0.dp) // Add some space above the image
                    )

                    Spacer(modifier = Modifier.weight(1f))


                    // Welcome Text
                    Text(
                        text = "Welcome to Smart Water Management",
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp) // Add padding to space out the text
                    )

                    // Email TextField
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp),
                        value = email,
                        onValueChange = { newValue -> email = newValue },
                        label = { Text("Email") },
                        placeholder = { Text("Type your email...") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.dark_blue),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = colorResource(id = R.color.dark_blue),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = colorResource(id = R.color.dark_blue)
                        )
                    )

                    // Password TextField
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp, bottom = 8.dp),
                        value = password,
                        onValueChange = { newValue -> password = newValue },
                        label = { Text("Password") },
                        placeholder = { Text("Type your password...") },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.dark_blue),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = colorResource(id = R.color.dark_blue),
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = colorResource(id = R.color.dark_blue)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    painter = painterResource(id = if (isPasswordVisible) R.drawable.eye else R.drawable.show),
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    modifier = Modifier.size(20.dp) // Adjust the size as needed

                                )
                            }
                        }
                    )

                    // Sign Up Button (Above "Create New Account")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .padding(top = 16.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        colorResource(id = R.color.light_blue),
                                        colorResource(id = R.color.light_green_color)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Button(
                            onClick = {
                                when {
                                    email.isEmpty() || password.isEmpty() -> {
                                        Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        loginUser(email, password, context)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp)), // Ensures the gradient respects rounded corners
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Transparent // Makes the button background transparent
                            ),
                            contentPadding = PaddingValues(0.dp) // Removes padding to align the gradient background
                        )
                        {
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
                                    text = "Sign Up",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }


                    // "Create New Account" Row at the bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), // Align Row to the bottom
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Don't have an account?",
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.dark_blue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create New Account",
                            color = colorResource(id = R.color.light_blue),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                val intent = Intent(context, SignUpScreen::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun loginUser(email: String, password: String, context: android.content.Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = api.loginUser(loginRequest)  // Call API with the raw JSON request body

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                        val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("email", email)
                            putString("password", password)
                            apply() // Save the changes asynchronously
                        }
                        // Show notification
                        sendNotification(
                            context,
                            title = "Welcome!",
                            message = "You have successfully logged in as $email."
                        )

                        val intent = Intent(context, HomeActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("password", password)
                        context.startActivity(intent)
                        (context as Activity).finish()
                        // You can navigate to another screen or process user data here
                    } else {
                        try {
                            // Example API response handling
                            val errorResponse = response?.errorBody()?.string()

                            // Ensure errorResponse is not null
                            if (errorResponse != null) {
                                val gson = Gson()
                                val jsonObject = gson.fromJson(errorResponse, JsonObject::class.java)

                                // Check if the response contains "status" and "message" keys
                                val status = jsonObject.get("status")?.asString
                                val message = jsonObject.get("error")?.asString

                                // Handle the error based on status and message
                                if (message!=null) {
                                    if (message != null) {
                                        Log.e("ErrorResponse", "Error: $message")
                                        // Show message to the user
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("ErrorResponse", "Unknown error occurred")
                                    }
                                } else {
                                    Toast.makeText(context, "Authentication Failed! Please enter valid email id", Toast.LENGTH_SHORT).show()

                                }
                            } else {
                                Log.e("ErrorResponse", "No error response body received.")
                            }
                        } catch (jsonException: JsonSyntaxException) {
                            // Handle JSON parsing error
                            Log.e("ErrorResponse", "Failed to parse error response: ${jsonException.message}")
                        }
                    }
                }
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error", e.message.toString())

                    // Check if the exception is a retrofit HTTP exception
                    if (e is retrofit2.HttpException) {
                        try {
                            // Extract the error response from the exception
                            val errorResponse = e.response()?.errorBody()?.string()
                            val gson = Gson()
                            val jsonObject = gson.fromJson(errorResponse, JsonObject::class.java)
                            val status = jsonObject.get("status").asString

                            if (status == "error") {
                                val errorMessage = jsonObject.get("message").asString
                                Log.e("ErrorMessage", errorMessage)

                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            } else {
                                // Handle unexpected status
                                Toast.makeText(context, "Unexpected response: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (jsonException: JsonSyntaxException) {
                            // Handle any JSON parsing exceptions
                            Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Handle non-HTTP exceptions
                        Toast.makeText(context, "Failed to connect to server: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("ServiceCast")
    fun getLocalIpAddress(context: Context): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return Formatter.formatIpAddress(ipAddress)
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

    private fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "my_channel_id"
        val notificationId = 1

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Ensure this resource exists
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

    private fun checkAndNotify(context: Context, value: String?) {
        if (!value.isNullOrEmpty()) {
            sendNotification(
                context,
                title = "Value Found",
                message = "The value '$value' is present!"
            )
        } else {
            Log.d("Notification", "No value present, notification not sent.")
        }
    }
    override fun onBackPressed() {
        finish()
    }
}
