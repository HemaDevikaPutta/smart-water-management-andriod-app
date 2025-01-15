package com.example.smartwatermanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class CurentWaterLevelActivity : ComponentActivity() {


    private lateinit var BASE_URL: String


    val getAllWaterLevelService : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    private val _waterLevels = mutableStateOf<List<ApiService.WaterLevel>>(emptyList())
    val waterLevels = _waterLevels
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BASE_URL = getString(R.string.ip_address)
        setContent {
            val context = LocalContext.current // Access the context for Toast

            val waterLevel = intent.getDoubleExtra("level", 0.0) // Default to 0.0 if no value is provided
            val sharedPreferences = context.getSharedPreferences("login_preferences", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("email", "") ?: "" // Default to empty string if not found
            val savedPassword = sharedPreferences.getString("password", "") ?: ""
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorResource(id = R.color.light_blue)
            ) {
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
                            .background(color = colorResource(id = R.color.light_sky_blue)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Water Level Graph",
                            color = colorResource(id = R.color.dark_blue),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 10.dp, top = 30.dp) // Add some space between the text and the graph
                        )

                        WaterLevelGraph(
                            waterLevel = waterLevel,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val email = savedEmail
                                        val password = savedPassword
                                        val authHeader = "Basic " + Base64.encodeToString(
                                            "$email:$password".toByteArray(),
                                            Base64.NO_WRAP
                                        )
                                        val response =
                                            getAllWaterLevelService.getAllWaterLevels(
                                                authHeader = authHeader
                                            )
                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful) {
                                                val waterresponse = response.body()
                                                if(!waterresponse.isNullOrEmpty()  && waterresponse != null){
                                                    val gson = Gson()
                                                    val json = gson.toJson(waterresponse)

                                                    val intent = Intent(context, GetAllWaterLevelActivity::class.java)
                                                    intent.putExtra("waterLevelsJson", json)
                                                    context.startActivity(intent)
                                                }
                                                else{
                                                    Toast.makeText(
                                                        context,
                                                        "No water levels found",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                            }
                                            else{
                                                Toast.makeText(
                                                    context,
                                                    "Failed to retrieve water levels: ${response.errorBody()}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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


                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp)),
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
                                    text = "Get All Water Levels",
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

    @Composable
    fun WaterLevelGraph(
        waterLevel: Double,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .background(colorResource(id = R.color.light_sky_blue))
                .padding(start = 30.dp, top = 60.dp, bottom = 30.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 40.dp, end = 40.dp)
                    ) {
                        val gridLines = 6
                        val gridSpacing = size.height / (gridLines - 1)

                        // Draw grid lines
                        for (i in 0 until gridLines) {
                            val y = i * gridSpacing
                            drawLine(
                                color = Color(0xFF00283f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Normalize water level (0.0 to 1.0)
                        // Normalize water level (0.0 to 1.0)
                        val normalizedLevel = (waterLevel / 100.0).coerceIn(0.0, 1.0).toFloat()
                        val barWidth = size.width * 0.3f
                        val barHeight = size.height * normalizedLevel

// Draw water level bar
                        drawRect(
                            color = Color(0xFF00283f),
                            topLeft = Offset(
                                (size.width - barWidth) / 2,
                                size.height - barHeight
                            ),
                            size = Size(barWidth, barHeight)
                        )

// Display exact water level (with no rounding)
// Display exact water level without any rounding
                        drawContext.canvas.nativeCanvas.apply {
                            // Show value with 2 decimal places
                            drawText(
                                String.format("%.2f", waterLevel),  // Ensure it prints exactly like 80.58
                                size.width / 2,
                                size.height - barHeight - 20.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#00283F")
                                    textSize = 40f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }

                    }

                    // Level markers
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 5 downTo 0) {
                            Text(
                                text = "${i * 20}",
                                color = colorResource(id = R.color.dark_blue),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Time display
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                    color = colorResource(id = R.color.dark_blue),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
