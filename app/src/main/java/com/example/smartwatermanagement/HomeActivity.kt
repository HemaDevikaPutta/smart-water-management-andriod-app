package com.example.smartwatermanagement

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

class HomeActivity : ComponentActivity() {
    private lateinit var BASE_URL: String


    val waterLevelService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val currentTemparatureLevelService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val currentHumidityLevelService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val checkLeakageLevelService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BASE_URL = getString(R.string.ip_address)
        setContent {
            val context = LocalContext.current // Access the context for Toast

            val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("email", "") ?: "" // Default to empty string if not found
            val savedPassword = sharedPreferences.getString("password", "") ?: "" // Default to empty string if not found
            var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility

            Log.e("Saved email",savedEmail);

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(), // Fill the width of the TopAppBar
                                verticalAlignment = Alignment.CenterVertically, // Align text and icon vertically
                                horizontalArrangement = Arrangement.Start // Align text to the left
                            ) {
                                Text(
                                    text = "Smart Water Management System",
                                    color = Color.White,
                                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        actions = {
                            Icon(
                                painter = painterResource(id = R.drawable.logout), // Replace with your icon resource
                                contentDescription = "Water Icon",
                                tint = Color.White, // Set icon color
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        showDialog = true

                                    }// Adjust size of the icon
                            )
                        },
                        backgroundColor = colorResource(id = R.color.dark_blue),
                        contentColor = colorResource(id = R.color.white),
                        modifier = Modifier.height(62.dp)
                    )
                }

            )








            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.light_sky_blue))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Controlled spacing between rows
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), // Outer padding for the row
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp), // Corner radius
                            elevation = 4.dp, // Shadow for the card
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 15.dp, end = 15.dp, bottom = 30.dp)
                                .height(150.dp)
                                .clickable {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val email = savedEmail
                                            val password = savedPassword
                                            val authHeader = "Basic " + Base64.encodeToString(
                                                "$email:$password".toByteArray(),
                                                Base64.NO_WRAP
                                            )
                                            val response = waterLevelService.getCurrentWaterLevel(
                                                authHeader = authHeader
                                            )
                                            withContext(Dispatchers.Main) {
                                                if(response.isSuccessful){
                                                    if (response.body()?.status == "NORMAL" || response.body()?.status == "HIGH" || response.body()?.status == "LOW") {
                                                        val intent = Intent(context, CurentWaterLevelActivity::class.java)
                                                        intent.putExtra("level", response.body()?.level)
                                                        context.startActivity(intent)
                                                    } else {
                                                        Toast.makeText(context, "No Water Levels found for this user id", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Water Level Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Error", e.message.toString())
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Failed to connect: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        }
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 30.dp),
                                contentAlignment = Alignment.Center // Center the content within the box
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, // Align the icon and text vertically
                                    horizontalArrangement = Arrangement.Center, // Center the content horizontally
                                    modifier = Modifier.fillMaxWidth() // Take full width of the box
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.water_level_new), // Replace with your icon resource
                                        contentDescription = "Water Level Icon",
                                        modifier = Modifier.size(54.dp), // Adjust the size of the icon
                                        tint = colorResource(id = R.color.dark_blue) // Optional: set the color of the icon
                                    )
                                    Spacer(modifier = Modifier.width(30.dp)) // Space between the icon and the text
                                    Text(
                                        text = "Water Level",
                                        style = TextStyle(
                                            color = colorResource(id = R.color.dark_blue),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }


                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 15.dp, end = 15.dp, bottom = 30.dp)
                                .height(150.dp)
                                .clickable {

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val email = savedEmail
                                            val password = savedPassword
                                            val authHeader = "Basic " + Base64.encodeToString(
                                                "$email:$password".toByteArray(),
                                                Base64.NO_WRAP
                                            )
                                            val response =
                                                currentTemparatureLevelService.getCurrentTemprature(
                                                    authHeader = authHeader
                                                )
                                            withContext(Dispatchers.Main) {
                                                if(response.isSuccessful){
                                                    if (response.body()?.temperature != null) {

                                                        val intent = Intent(
                                                            context,
                                                            CurrentTemperatureActivity::class.java
                                                        )
                                                        intent.putExtra("level", response.body()?.temperature) // Make sure this is passing a valid temperature value.
                                                        context.startActivity(intent)

                                                        // You can navigate to another screen or process user data here
                                                    }
                                                    else{
                                                        Toast.makeText(context, "No Temperature found for this user id", Toast.LENGTH_SHORT).show()

                                                    }

                                                }
                                                else{
                                                    Toast.makeText(context, "Temperature Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Error", e.message.toString())
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Failed to connect: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        }
                                    }

                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 30.dp),
                                contentAlignment = Alignment.Center // Center the content within the box
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, // Align the icon and text vertically
                                    horizontalArrangement = Arrangement.Center, // Center the content horizontally
                                    modifier = Modifier.fillMaxWidth() // Take full width of the box
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.thermometer_new), // Replace with your icon resource
                                        contentDescription = "Temperature Level Icon",
                                        modifier = Modifier.size(54.dp), // Adjust the size of the icon
                                        tint = colorResource(id = R.color.dark_blue) // Optional: set the color of the icon
                                    )
                                    Spacer(modifier = Modifier.width(30.dp)) // Space between the icon and the text
                                    Text(
                                        text = "Temperature",
                                        style = TextStyle(
                                            color = colorResource(id = R.color.dark_blue),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp), // Corner radius
                            elevation = 4.dp, // Shadow for the card
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 15.dp, end = 15.dp, bottom = 30.dp)
                                .height(150.dp)
                                .clickable {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val email = savedEmail
                                            val password = savedPassword
                                            val authHeader = "Basic " + Base64.encodeToString(
                                                "$email:$password".toByteArray(),
                                                Base64.NO_WRAP
                                            )
                                            val response =
                                                currentHumidityLevelService.getCurrentHumidity(
                                                    authHeader = authHeader
                                                )
                                            withContext(Dispatchers.Main) {
                                                if(response.isSuccessful){
                                                    if (response.body()?.humidity != null) {

                                                        val intent = Intent(
                                                            context,
                                                            CurrentHumidityActivity::class.java
                                                        )
                                                        intent.putExtra("level", response.body()?.humidity)
                                                        context.startActivity(intent)
                                                        // You can navigate to another screen or process user data here
                                                    }
                                                    else{
                                                        Toast.makeText(context, "No Humidity found for this user id", Toast.LENGTH_SHORT).show()

                                                    }

                                                }
                                                else{
                                                    Toast.makeText(context, "Humidity Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Error", e.message.toString())
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Failed to connect: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        }
                                    }

                                } // Set a fixed height for all cards
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 30.dp),
                                contentAlignment = Alignment.Center // Center the content within the box
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, // Align the icon and text vertically
                                    horizontalArrangement = Arrangement.Center, // Center the content horizontally
                                    modifier = Modifier.fillMaxWidth() // Take full width of the box
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.humidity_new), // Replace with your icon resource
                                        contentDescription = "Humidity Level Icon",
                                        modifier = Modifier.size(54.dp), // Adjust the size of the icon
                                        tint = colorResource(id = R.color.dark_blue) // Optional: set the color of the icon
                                    )
                                    Spacer(modifier = Modifier.width(30.dp)) // Space between the icon and the text
                                    Text(
                                        text = "Humidity",
                                        style = TextStyle(
                                            color = colorResource(id = R.color.dark_blue),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 15.dp, end = 15.dp, bottom = 30.dp)
                                .height(150.dp).clickable{
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val email = savedEmail
                                            val password = savedPassword
                                            val authHeader = "Basic " + Base64.encodeToString(
                                                "$email:$password".toByteArray(),
                                                Base64.NO_WRAP
                                            )

                                            withContext(Dispatchers.Main) {
                                                val response = checkLeakageLevelService.getCheckLeakage(authHeader)
                                                Log.d("Response", response.toString())

                                                if (response.isSuccessful) {
                                                    // Access the list of humidity records from the response
                                                    val humidityDataList = response.body()

                                                    if (humidityDataList != null && humidityDataList.isNotEmpty()) {
                                                        // Get the most recent humidity value (last item in the list)
                                                        val lastHumidity = humidityDataList.last()

                                                        val humidityValue = lastHumidity.humidity
                                                        val message: String
                                                        if (humidityValue > 60) {
                                                            message = "Warning: Possible water leakage detected!"
                                                        } else {
                                                            message =  "Humidity is normal, no leakage detected."
                                                        }

                                                        // Proceed with your intent and pass the message
                                                        val intent = Intent(context, LeakageActivity::class.java)
                                                        intent.putExtra("level", message)
                                                        context.startActivity(intent)
                                                    } else {
                                                        // Handle case if no humidity data is available
                                                        Toast.makeText(context, "No leakage levels found to this user id ", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    // Handle failure of the API response
                                                    Toast.makeText(context, "Leakage Level Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Error", e.message.toString())

                                                // Handle non-HTTP exceptions
                                                Toast.makeText(context, "Failed to connect to server: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }


                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 30.dp,
                                        end = 30.dp,
                                        top = 30.dp,
                                        bottom = 30.dp
                                    ),
                                contentAlignment = Alignment.Center // Center the content within the box
// Inner padding for content
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, // Align the icon and text vertically
                                    horizontalArrangement = Arrangement.Center, // Center the content horizontally
                                    modifier = Modifier.fillMaxWidth() // Take full width of the box
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.leakage_new), // Replace with your icon resource
                                        contentDescription = "Check Leakage",
                                        modifier = Modifier.size(54.dp), // Adjust the size of the icon
                                        tint = colorResource(id = R.color.dark_blue) // Optional: set the color of the icon
                                    )
                                    Spacer(modifier = Modifier.width(22.dp)) // Space between the icon and the text
                                    Text(
                                        text = "Check Leakage",
                                        style = TextStyle(
                                            color = colorResource(id = R.color.dark_blue),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }
                    }






                }

            }
            // Alert Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(text = "Logout Confirmation")
                    },
                    text = {
                        Text(text = "Are you sure you want to logout?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.clear()  // Clear all the data in SharedPreferences
                                editor.apply()  // Apply changes asynchronously

                                // Navigate to the LoginActivity (or any other screen)
                                val intent = Intent(context, MainActivity::class.java)
                                startActivity(intent)
                                finish()  // Optionally finish current activity

                                showDialog = false                            }
                        ) {
                            Text(text = "Yes", color = colorResource(id = R.color.dark_blue))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text(text = "No",color = colorResource(id = R.color.dark_blue))
                        }
                    }
                )
            }

        }
    }

    override fun onBackPressed() {
        finish()
    }
}

