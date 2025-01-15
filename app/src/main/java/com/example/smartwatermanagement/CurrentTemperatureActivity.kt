package com.example.smartwatermanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CurrentTemperatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve temperature level as Float
        val temperatureLevel = intent.getFloatExtra("level", 0.0f)
        Log.e("Temperature", "Received temperature level: $temperatureLevel")

        setContent {
            val context = LocalContext.current // Access the context for Toast

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
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
                            backgroundColor = colorResource(id = R.color.dark_blue)
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "Temperature Level",
                                color = colorResource(id = R.color.dark_blue),
                                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 50.dp)
                            )
                            TemperatureLevelDisplay(temperatureLevel = temperatureLevel)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(context, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as Activity).finish() // Optional: close the current activity to remove it from the stack

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
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

    override fun onBackPressed() {
        finish()
    }
}

@Composable
fun TemperatureLevelDisplay(temperatureLevel: Float) {
    // Ensure temperatureLevel is within the expected range
    val minTemp = 0.0f
    val maxTemp = 100.0f

    // Map the temperature to a progress range between 0.0 and 1.0
    val progress = ((temperatureLevel - minTemp) / (maxTemp - minTemp)).coerceIn(0.0f, 1.0f)

    // Log the values to verify the calculation
    Log.d("TemperatureLevelDisplay", "Temperature: $temperatureLevel, Progress: $progress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        SemicircularTemperatureProgressIndicator(
            progress = progress,
            modifier = Modifier.size(150.dp),
            backgroundColor = Color.LightGray,
            progressColor = colorResource(id = R.color.dark_blue),
            strokeWidth = 17.dp
        )
        Image(
            painter = painterResource(id = R.drawable.thermometer_new), // Replace with your actual drawable resource ID
            contentDescription = "Temperature Icon",
            modifier = Modifier.size(50.dp) // You can adjust the icon size here
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Temperature",
            fontSize = 20.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format("%.1f°C", temperatureLevel),
            fontSize = 28.sp,
            color = Color.Black
        )

        // Add the temperature icon below the semicircle
        Spacer(modifier = Modifier.height(16.dp)) // Add some spacing between the icon and the temperature text

    }
}


@Composable
fun SemicircularTemperatureProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    progressColor: Color = Color.Blue,
    strokeWidth: Dp = 8.dp
) {
    Canvas(modifier = modifier) {
        val startAngle = 180f // Start the arc from the middle of the top
        val sweepAngle = 180f // Sweep a half circle (180 degrees)
        val progressSweepAngle = sweepAngle * progress // Calculate the progress sweep angle

        // Draw background semicircle (static)
        drawArc(
            color = backgroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Draw the progress semicircle (dynamic based on progress)
        drawArc(
            color = progressColor,
            startAngle = startAngle,
            sweepAngle = progressSweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
