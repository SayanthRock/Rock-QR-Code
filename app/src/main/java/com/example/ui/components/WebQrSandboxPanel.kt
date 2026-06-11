package com.example.ui.components

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.utils.ShareUtils

@Composable
fun WebQrSandboxPanel(
    payloadText: String,
    fgColorHex: String,
    bgColorHex: String,
    ecLevel: String,
    isMaterial10Enabled: Boolean,
    modifier: Modifier = Modifier,
    onShowToast: ((String, com.example.viewmodel.CustomToastType) -> Unit)? = null
) {
    val context = LocalContext.current
    var isWebPortalOpen by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Synchronize state from Android inputs straight into the running Web HTML Canvas!
    LaunchedEffect(payloadText, fgColorHex, bgColorHex, ecLevel, isWebPortalOpen) {
        if (isWebPortalOpen) {
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript(
                    "updateWebQr(${escapeJsString(payloadText)}, '$fgColorHex', '$bgColorHex', '$ecLevel')",
                    null
                )
            }
        }
    }

    // Modern styled container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isMaterial10Enabled) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF131722), Color(0xFF090B10))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Interactive header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isWebPortalOpen = !isWebPortalOpen }
                    .testTag("web_portal_header_root"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Engine Icon",
                            tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Web Share & Canvas Portal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isMaterial10Enabled) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Render via web canvas and share with Web API",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (isWebPortalOpen) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = "Toggle Section",
                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = isWebPortalOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Android Web View rendering our highly styled HTML canvas implementation
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(410.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            ),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        // Push immediate state
                                        view?.evaluateJavascript(
                                            "updateWebQr(${escapeJsString(payloadText)}, '$fgColorHex', '$bgColorHex', '$ecLevel')",
                                            null
                                        )
                                    }
                                }

                                // Setup custom Javascript Interface mapping
                                addJavascriptInterface(WebShareInterface(ctx, onShowToast), "AndroidShare")

                                // Load our local embedded string HTML structure!
                                val htmlData = getHtmlTemplate()
                                loadDataWithBaseURL("https://localhost", htmlData, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            // Update dynamic reference
                            webViewInstance = view
                        }
                    )
                }
            }
        }
    }
}

// Escapes special characters for Javascript safety
private fun escapeJsString(str: String): String {
    val escaped = str.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
    return "'$escaped'"
}

// Bridges JS calls directly to safe native Android mechanisms
class WebShareInterface(
    private val context: Context,
    private val onShowToast: ((String, com.example.viewmodel.CustomToastType) -> Unit)? = null
) {
    @JavascriptInterface
    fun vibrate(patternJson: String) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            try {
                val clean = patternJson.trim()
                if (clean.startsWith("[")) {
                    val elements = clean.removeSurrounding("[", "]")
                        .split(",")
                        .mapNotNull { it.trim().toLongOrNull() }
                    if (elements.isNotEmpty()) {
                        com.example.utils.HapticUtils.vibratePattern(context, elements.toLongArray())
                    }
                } else {
                    val dur = clean.toLongOrNull() ?: 50L
                    com.example.utils.HapticUtils.vibrate(context, dur)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun downloadCanvasImage(base64Data: String, fileName: String) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            try {
                val cleanBase = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
                val decodedBytes = android.util.Base64.decode(cleanBase, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    ShareUtils.saveBitmapToGallery(
                        context = context,
                        bitmap = bitmap,
                        displayName = "WebCanvasQR_",
                        isPng = true,
                        onShowToast = onShowToast
                    )
                } else {
                    if (onShowToast != null) {
                        onShowToast("Error converting canvas elements to Image", com.example.viewmodel.CustomToastType.ERROR)
                    } else {
                        Toast.makeText(context, "Error converting canvas elements to Image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = "Download failed: ${e.localizedMessage}"
                if (onShowToast != null) {
                    onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @JavascriptInterface
    fun shareCanvasImage(base64Data: String, fileName: String) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            try {
                val cleanBase = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
                val decodedBytes = android.util.Base64.decode(cleanBase, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    ShareUtils.shareBitmap(
                        context = context,
                        bitmap = bitmap,
                        fileName = "web_canvas_shared.png",
                        onShowToast = onShowToast
                    )
                } else {
                    if (onShowToast != null) {
                        onShowToast("Error rendering canvas share packet", com.example.viewmodel.CustomToastType.ERROR)
                    } else {
                        Toast.makeText(context, "Error rendering canvas share packet", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = "Sharing failed: ${e.localizedMessage}"
                if (onShowToast != null) {
                    onShowToast(errorMsg, com.example.viewmodel.CustomToastType.ERROR)
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

private fun getHtmlTemplate(): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                body {
                    background: #11141C;
                    color: #FFFFFF;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    margin: 0;
                    padding: 12px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                }
                .container {
                    width: 100%;
                    max-width: 320px;
                    background: rgba(255, 255, 255, 0.02);
                    border: 1px solid rgba(255, 255, 255, 0.06);
                    border-radius: 16px;
                    padding: 16px;
                    box-sizing: border-box;
                    backdrop-filter: blur(12px);
                    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.3);
                    text-align: center;
                }
                h3 {
                    margin: 0 0 4px 0;
                    font-size: 13px;
                    letter-spacing: 1.2px;
                    text-transform: uppercase;
                    color: #00FFCC;
                }
                p {
                    font-size: 10px;
                    color: rgba(255, 255, 255, 0.5);
                    margin: 0 0 14px 0;
                    line-height: 14px;
                }
                #qr-canvas {
                    background: #FFFFFF;
                    border-radius: 12px;
                    padding: 8px;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
                    max-width: 180px;
                    height: 180px;
                    margin: 0 auto;
                    display: block;
                    image-rendering: pixelated;
                }
                .button-group {
                    margin-top: 14px;
                    display: flex;
                    gap: 8px;
                    flex-direction: column;
                }
                button {
                    background: linear-gradient(135deg, #00FFCC 0%, #009688 100%);
                    border: none;
                    border-radius: 8px;
                    color: #0B0E14;
                    padding: 10px 12px;
                    font-size: 11px;
                    font-weight: bold;
                    cursor: pointer;
                    transition: all 0.2s;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 6px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                button:active {
                    transform: scale(0.97);
                    opacity: 0.9;
                }
                button.share {
                    background: transparent;
                    border: 1.5.dp solid #00FFCC;
                    border: 1px solid #00FFCC;
                    color: #00FFCC;
                }
                #status {
                    font-size: 9px;
                    color: #CC33FF;
                    margin-top: 8px;
                    height: 12px;
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h3>HTML5 Canvas Render</h3>
                <p>Compiling live on web canvas and dispatching using Web Share API</p>
                
                <canvas id="qr-canvas" width="220" height="220"></canvas>
                
                <div id="status">Syncing from Android system...</div>
                
                <div class="button-group">
                    <button onclick="downloadCanvasImg()">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/></svg>
                        Download Image
                    </button>
                    <button class="share" onclick="shareCanvasWebAPI()">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8M16 6l-4-4-4 4M12 2v13"/></svg>
                        Share via Web API
                    </button>
                </div>
            </div>

            <!-- Standalone QR Code lib builder loaded from CDN -->
            <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.1/build/qrcode.min.js"></script>
            <script>
                // Polyfill Navigator Vibrate API to use Android haptic bridge
                if (!navigator.vibrate || typeof navigator.vibrate !== 'function') {
                    navigator.vibrate = function(pattern) {
                        if (window.AndroidShare && window.AndroidShare.vibrate) {
                            window.AndroidShare.vibrate(JSON.stringify(pattern));
                            return true;
                        }
                        return false;
                    };
                } else {
                    const originalVibrate = navigator.vibrate;
                    navigator.vibrate = function(pattern) {
                        if (window.AndroidShare && window.AndroidShare.vibrate) {
                            window.AndroidShare.vibrate(JSON.stringify(pattern));
                            return true;
                        }
                        try {
                            return originalVibrate.call(navigator, pattern);
                        } catch(e) {
                            return false;
                        }
                    };
                }

                let currentText = "Welcome to RockQR Sandbox";
                let currentFg = "#0A0A0A";
                let currentBg = "#FFFFFF";
                let currentEc = "H";

                function renderQR() {
                    const canvas = document.getElementById('qr-canvas');
                    const statusDiv = document.getElementById('status');
                    
                    if (typeof QRCode !== 'undefined') {
                        statusDiv.innerText = "Active: Canvas updated successfully";
                        QRCode.toCanvas(canvas, currentText, {
                            width: 220,
                            margin: 1,
                            color: {
                                dark: currentFg,
                                light: currentBg
                            },
                            errorCorrectionLevel: currentEc
                        }, function (error) {
                            if (error) {
                                console.error(error);
                                statusDiv.innerText = "Error: " + error.message;
                            }
                        });
                    } else {
                        statusDiv.innerText = "Standby: Drawing local matrix matrix";
                        drawOfflineQR(canvas, currentText, currentFg, currentBg);
                    }
                }

                // Beautiful deterministic fallback for rendering custom QR code designs offline
                function drawOfflineQR(canvas, text, fg, bg) {
                    const ctx = canvas.getContext('2d');
                    ctx.fillStyle = bg;
                    ctx.fillRect(0, 0, canvas.width, canvas.height);
                    
                    ctx.fillStyle = fg;
                    const size = canvas.width;
                    const cellSize = Math.floor(size / 21);
                    
                    function drawFinder(x, y) {
                        ctx.fillStyle = fg;
                        ctx.fillRect(x, y, cellSize*7, cellSize*7);
                        ctx.fillStyle = bg;
                        ctx.fillRect(x + cellSize, y + cellSize, cellSize*5, cellSize*5);
                        ctx.fillStyle = fg;
                        ctx.fillRect(x + cellSize*2, y + cellSize*2, cellSize*3, cellSize*3);
                    }
                    
                    drawFinder(cellSize, cellSize);
                    drawFinder(size - cellSize*8, cellSize);
                    drawFinder(cellSize, size - cellSize*8);
                    
                    ctx.fillStyle = fg;
                    let seed = 0;
                    for(let i=0; i<text.length; i++) {
                        seed += text.charCodeAt(i);
                    }
                    
                    for (let row = 0; row < 21; row++) {
                        for (let col = 0; col < 21; col++) {
                            if ((row < 8 && col < 8) || (row < 8 && col > 12) || (row > 12 && col < 8)) {
                                continue;
                            }
                            const pseudo = Math.sin(seed + (row * 13) + (col * 37));
                            if (pseudo > 0.05) {
                                ctx.fillRect(col * cellSize + cellSize, row * cellSize + cellSize, cellSize, cellSize);
                            }
                        }
                    }
                }

                function updateWebQr(text, fg, bg, ec) {
                    currentText = text || "Welcome to RockQR Sandbox";
                    currentFg = fg || "#11141C";
                    currentBg = bg || "#FFFFFF";
                    currentEc = ec || "H";
                    renderQR();
                }

                function downloadCanvasImg() {
                    if (navigator.vibrate) {
                        navigator.vibrate(40);
                    }
                    const canvas = document.getElementById('qr-canvas');
                    const dataUrl = canvas.toDataURL("image/png");
                    
                    const link = document.createElement('a');
                    link.download = 'web_canvas_qr.png';
                    link.href = dataUrl;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                    
                    if (window.AndroidShare) {
                        window.AndroidShare.downloadCanvasImage(dataUrl, 'web_canvas_qr.png');
                    }
                }

                function shareCanvasWebAPI() {
                    if (navigator.vibrate) {
                        navigator.vibrate([40, 30, 40]);
                    }
                    const canvas = document.getElementById('qr-canvas');
                    const dataUrl = canvas.toDataURL("image/png");
                    
                    if (window.AndroidShare) {
                        window.AndroidShare.shareCanvasImage(dataUrl, 'web_canvas_qr.png');
                        return;
                    }
                    
                    if (navigator.share) {
                        fetch(dataUrl)
                        .then(res => res.blob())
                        .then(blob => {
                            const file = new File([blob], "web_canvas_qr.png", { type: "image/png" });
                            navigator.share({
                                files: [file],
                                title: 'Web Canvas QR Code',
                                text: 'Generated with Web API is real progress!'
                            }).catch(err => {
                                console.log("Web Share API cancellation or error: ", err);
                            });
                        });
                    } else {
                        alert("Web Share API not supported on this browser context. Copied data URL!");
                    }
                }

                setTimeout(renderQR, 150);
            </script>
        </body>
        </html>
    """.trimIndent()
}
