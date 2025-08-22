package `in`.devh.ai_ze

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.devh.ai_ze.ui.theme.AiZeTheme

class TextClassifierService : Service() {
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        showOverlay()
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlay()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        overlayView = ComposeView(this).apply {
            setContent {
                AiZeTheme {
                    TransparentBottomNavbar(
                        onRephrase = { handleAction("Rephrase") },
                        onModifyTone = { handleAction("Modify tone") },
                        onFixGrammar = { handleAction("Fix grammar") },
                        onSuggestReply = { handleAction("Suggest reply") },
                        onDismiss = { hideOverlay() }
                    )
                }
            }
        }

        try {
            windowManager.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
        stopSelf()
    }

    private fun handleAction(action: String) {
        Toast.makeText(this, "$action - Will be implemented", Toast.LENGTH_SHORT).show()

        hideOverlay()
    }
}

@Composable
fun TransparentBottomNavbar(
    onRephrase: () -> Unit,
    onModifyTone: () -> Unit,
    onFixGrammar: () -> Unit,
    onSuggestReply: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Main action tiles
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionTile(
                        icon = Icons.Default.Edit,
                        title = "Rephrase",
                        onClick = onRephrase,
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.Settings,
                        title = "Modify tone",
                        onClick = onModifyTone,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionTile(
                        icon = Icons.Default.Check,
                        title = "Fix grammar",
                        onClick = onFixGrammar,
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "Suggest reply",
                        onClick = onSuggestReply,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dismiss button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = onDismiss,
                modifier = Modifier.size(48.dp),
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
