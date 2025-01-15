package com.example.smartwatermanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.smartwatermanagement.ui.theme.SmartWaterManagementTheme
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class SignUpScreen : ComponentActivity() {

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

        BASE_URL = getString(R.string.ip_address);
        setContent {
            SmartWaterManagementTheme {
                val context = LocalContext.current

                var username by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
                var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }


                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Smart Water Management System",
                                    color = colorResource(id = R.color.white),
                                    style = TextStyle(fontSize = 17.sp, textAlign = TextAlign.Start)
                                )
                            },
                            backgroundColor = colorResource(id = R.color.dark_blue),
                            contentColor = colorResource(id = R.color.white),
                            modifier = Modifier.height(62.dp)
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.light_sky_blue))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Create Account",
                                style = TextStyle(
                                    color = colorResource(id = R.color.dark_blue),
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Username TextField
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                placeholder = { Text("Type your username...", style = TextStyle(fontSize = 12.sp)) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(id = R.color.dark_blue),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = colorResource(id = R.color.dark_blue),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = colorResource(id = R.color.dark_blue)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Email TextField
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                placeholder = { Text("Type your email address...", style = TextStyle(fontSize = 12.sp)) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(id = R.color.dark_blue),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = colorResource(id = R.color.dark_blue),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = colorResource(id = R.color.dark_blue)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 3.dp)
                            )

                            // Password TextField
                            // Password TextField
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                placeholder = { Text("Type your password...") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (isPasswordVisible)
                                        painterResource(id = R.drawable.eye)
                                    else
                                        painterResource(id = R.drawable.show)

                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            painter = image,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Set the icon size
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(id = R.color.dark_blue),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = colorResource(id = R.color.dark_blue),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = colorResource(id = R.color.dark_blue)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )

                            // Confirm Password TextField
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                placeholder = { Text("Type your password again...") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (isConfirmPasswordVisible)
                                        painterResource(id = R.drawable.eye)
                                    else
                                        painterResource(id = R.drawable.show)

                                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                        Icon(
                                            painter = image,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Set the icon size
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(id = R.color.dark_blue),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = colorResource(id = R.color.dark_blue),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = colorResource(id = R.color.dark_blue)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                                    Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                                } else if (password != confirmPassword) {
                                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                } else {
                                    registerUser(username, email, password, context)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Transparent
                            ),
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
                                    text = "Sign Up",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 18.sp,
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

    private fun registerUser(username: String, email: String, password: String, context: android.content.Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.registerUser(username, email, password)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful){
                        Toast.makeText(context,"Register Successful", Toast.LENGTH_SHORT).show()


                        val intent = Intent(context, MainActivity::class.java)
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
                                val message = jsonObject.get("message")?.asString

                                // Handle the error based on status and message
                                if (status == "error") {
                                    if (message != null) {
                                        Log.e("ErrorResponse", "Error: $message")
                                        // Show message to the user
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("ErrorResponse", "Unknown error occurred")
                                    }
                                } else {
                                    Toast.makeText(context, "Please Try again", Toast.LENGTH_SHORT).show()

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

    override fun onBackPressed() {
        finish()
    }



}
