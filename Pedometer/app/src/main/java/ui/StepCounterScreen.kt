package ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun PreviewStepScreen() {
    StepCounterScreen(
        steps = 5000,
        goal = 10000,
        calories = 200,
        distance = 3.5f,
        durationSeconds = 2700L,
        isTracking = false,
        onToggle = {}
    )
}

@Composable
fun StepCounterScreen(
    steps: Int,
    goal: Int = 1000,
    calories: Int = 0,
    distance: Float = 0f,
    durationSeconds: Long = 0L,
    isTracking: Boolean,
    onToggle: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (steps.toFloat() / goal).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000)
    )

    val pace = if (distance > 0.01f) {
        val totalMinutes = durationSeconds / 60.0
        val paceDecimal = totalMinutes / distance
        val min = paceDecimal.toInt()
        val sec = ((paceDecimal - min) * 60).toInt()
        String.format("%d'%02d\"", min, sec)
    } else {
        "0'00\""
    }

    val durationText = if (durationSeconds >= 3600) {
        String.format(
            "%02d:%02d:%02d",
            durationSeconds / 3600,
            (durationSeconds % 3600) / 60,
            durationSeconds % 60
        )
    } else {
        String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("今日步数", fontSize = 18.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                CircularProgress(progress = animatedProgress)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = steps.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("/ $goal", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("平均配速", pace)
                StatItem("总计时长", durationText)
                StatItem("消耗千卡", "$calories kcal")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color.Red else Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = if (isTracking) "停止运动" else "开始运动",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun CircularProgress(progress: Float) {
    val strokeWidth = 20.dp
    Canvas(modifier = Modifier.size(220.dp)) {
        val strokePx = strokeWidth.toPx()
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        val topLeft = Offset(strokePx / 2, strokePx / 2)

        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokePx)
        )

        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = title, fontSize = 12.sp, color = Color.Gray)
    }
}
