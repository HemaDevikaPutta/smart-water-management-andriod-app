package com.example.smartwatermanagement

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.ceil
import kotlin.math.floor

class GetAllWaterLevelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current // Access the context for Toast

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
                                    style = TextStyle(fontSize = 17.sp)
                                )
                            },
                            backgroundColor = colorResource(id = R.color.dark_blue)
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
                            text = "All Water Levels ",
                            color = colorResource(id = R.color.dark_blue),
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        val json = intent.getStringExtra("waterLevelsJson")
                        Log.e("WaterLevel",json.toString());
                        if (!json.isNullOrEmpty()) {
                            // Deserialize the JSON string into a list of WaterLevel objects
                            val gson = Gson()
                            val waterLevels: List<ApiService.WaterLevel> = gson.fromJson(
                                json,
                                object : TypeToken<List<ApiService.WaterLevel>>() {}.type
                            )

                            val levelValues: List<Float> = waterLevels.map { it.level }

                            WaterLevelLineGraph(
                                waterLevels = levelValues, // Pass the list of Float values here
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 30.dp, vertical = 30.dp)
                            )


                            // Add a button at the bottom
                            Button(
                                onClick = {
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                    (context as Activity).finish()
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
    }

    @Composable
    fun WaterLevelLineGraph(
        waterLevels: List<Float>,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier.background(colorResource(id = R.color.light_sky_blue))) {
            if (waterLevels.isEmpty()) return@Canvas

            val padding = 16.dp.toPx() // Define padding around the graph

            // Calculate min and max level dynamically
            val maxLevel = waterLevels.maxOrNull() ?: 100f
            val minLevel = waterLevels.minOrNull() ?: 0f

            val step = 20f

            val roundedMinLevel = floor(minLevel / step) * step
            val roundedMaxLevel = ceil(maxLevel / step) * step

            val numOfIds = waterLevels.size
            val spacing = (size.width - 2 * padding) / (numOfIds - 1)

            // Draw grid lines dynamically
            for (i in 0..(ceil((roundedMaxLevel - roundedMinLevel) / step).toInt())) {
                val y = size.height - padding - i * (size.height - 2 * padding) / (ceil((roundedMaxLevel - roundedMinLevel) / step).toInt())
                val level = roundedMinLevel + i * step

                drawLine(
                    color = Color.Gray,
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Grid label with formatting for decimal values
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        String.format("%.2f", level), // Format the level to 2 decimal places
                        padding - 10.dp.toPx(),
                        y + 5.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 35f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }
            }

            // Draw the line graph
            val path = Path()
            waterLevels.forEachIndexed { index, level ->
                val x = padding + index * spacing
                val y = size.height - padding - (size.height - 2 * padding) * (level - roundedMinLevel) / (roundedMaxLevel - roundedMinLevel)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF00283f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )

            // Draw points and labels dynamically
            waterLevels.forEachIndexed { index, level ->
                val x = padding + index * spacing
                val y = size.height - padding - (size.height - 2 * padding) * (level - roundedMinLevel) / (roundedMaxLevel - roundedMinLevel)

                drawCircle(
                    color = Color.Red,
                    center = Offset(x, y),
                    radius = 6.dp.toPx()
                )

                // Draw level with 2 decimal places
                drawContext.canvas.nativeCanvas.apply {
                    val levelText = String.format("%.2f", level)  // Ensure 2 decimal places
                    drawText(
                        levelText,
                        x,
                        y - 15.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // X-Axis labels dynamically
            waterLevels.forEachIndexed { index, _ ->
                val x = padding + index * spacing
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "ID ${index + 1}",
                        x,
                        size.height - padding + 20.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 35f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }


}