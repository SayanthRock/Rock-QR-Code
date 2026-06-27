package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassTheme
import com.example.utils.HapticUtils
import com.example.utils.ShareUtils
import com.example.viewmodel.QRViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GenerateScreen(
  viewModel: QRViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val themeConfig = LiquidGlassTheme.LocalConfig.current
  val primaryColor = themeConfig.primaryColor

  val inputType by viewModel.inputType.collectAsState()
  val inputText by viewModel.inputText.collectAsState()
  val inputTitle by viewModel.inputTitle.collectAsState()
  val wifiSsid by viewModel.wifiSsid.collectAsState()
  val wifiPassword by viewModel.wifiPassword.collectAsState()
  val wifiSecurity by viewModel.wifiSecurity.collectAsState()
  val contactName by viewModel.contactName.collectAsState()
  val contactPhone by viewModel.contactPhone.collectAsState()
  val contactEmail by viewModel.contactEmail.collectAsState()
  val selectedQrColor by viewModel.selectedQrColor.collectAsState()
  val qrBitmap by viewModel.generatedQrBitmap.collectAsState()
  val hasResult by viewModel.hasGeneratedResult.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp)
      .padding(top = 16.dp, bottom = 120.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Rock QR Generator",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      fontFamily = FontFamily.SansSerif,
      modifier = Modifier.fillMaxWidth().testTag("generate_title")
    )

    Text(
      text = "Create clean QR codes for text, links, Wi-Fi, and contacts.",
      fontSize = 13.sp,
      color = Color.White.copy(alpha = 0.58f),
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp)
    )

    AnimatedContent(
      targetState = hasResult && qrBitmap != null,
      transitionSpec = {
        slideInVertically { it / 2 } + fadeIn() togetherWith slideOutVertically { -it / 2 } + fadeOut()
      },
      label = "generate_screen_state"
    ) { showResult ->
      if (showResult && qrBitmap != null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
              width = 1.dp,
              color = primaryColor.copy(alpha = 0.35f),
              shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "QR Code Ready",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
          )

          Spacer(modifier = Modifier.height(18.dp))

          Box(
            modifier = Modifier
              .size(240.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color.White)
              .border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Image(
              bitmap = qrBitmap!!.asImageBitmap(),
              contentDescription = "Generated QR Code",
              modifier = Modifier.fillMaxSize().testTag("generated_qr_image")
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                HapticUtils.vibrate(context, 40)
                ShareUtils.saveBitmapToGallery(
                  context = context,
                  bitmap = qrBitmap!!,
                  displayName = "RockQR_",
                  onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                )
              },
              modifier = Modifier.weight(1f).height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Download, contentDescription = "Download")
              Spacer(modifier = Modifier.width(6.dp))
              Text("Download", fontSize = 13.sp)
            }

            Button(
              onClick = {
                HapticUtils.vibrate(context, 30)
                ShareUtils.shareBitmap(
                  context = context,
                  bitmap = qrBitmap!!,
                  fileName = "rock_qr_code.png",
                  onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                )
              },
              modifier = Modifier.weight(1f).height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share Image", tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share", color = Color.White, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              HapticUtils.vibrate(context, 30)
              val rawContent = when (viewModel.inputType.value) {
                "WIFI" -> viewModel.wifiSsid.value
                "CONTACT" -> viewModel.contactName.value.ifEmpty { viewModel.contactPhone.value }
                else -> viewModel.inputText.value
              }
              val title = viewModel.inputTitle.value.ifEmpty { "Shared Code" }
              val type = viewModel.inputType.value
              val webLink = "https://sayanthrock.github.io/Rock-QR-Code/share?content=${Uri.encode(rawContent)}&type=$type&title=${Uri.encode(title)}"
              val intent = Intent(Intent.ACTION_SEND).apply {
                this.type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Import Rock QR Code: $title")
                putExtra(Intent.EXTRA_TEXT, "Rock QR Code: $webLink")
              }
              context.startActivity(Intent.createChooser(intent, "Share Companion Web Link"))
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Link,
              contentDescription = "Share Link",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Share Companion Web Link",
              color = MaterialTheme.colorScheme.primary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedButton(
            onClick = {
              HapticUtils.vibrate(context, 20)
              viewModel.resetGenerator()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "New Code")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create New QR", fontSize = 13.sp)
          }
        }
      } else {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Select QR Type",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.58f),
            modifier = Modifier.padding(bottom = 10.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val types = listOf(
              Triple("TEXT", Icons.Default.TextFields, "Text"),
              Triple("URL", Icons.Default.Link, "Link"),
              Triple("WIFI", Icons.Default.Wifi, "Wi-Fi"),
              Triple("CONTACT", Icons.Default.ContactMail, "Contact")
            )

            types.forEach { (typeKey, icon, label) ->
              TypeChip(
                label = label,
                icon = icon,
                selected = inputType == typeKey,
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f).testTag("type_chip_$typeKey"),
                onClick = {
                  HapticUtils.vibrate(context, 20)
                  viewModel.inputType.value = typeKey
                }
              )
            }
          }

          RockTextField(
            value = inputTitle,
            onValueChange = { viewModel.inputTitle.value = it },
            label = "Title (optional)",
            primaryColor = primaryColor,
            modifier = Modifier.fillMaxWidth().testTag("input_field_title")
          )

          Spacer(modifier = Modifier.height(12.dp))

          when (inputType) {
            "URL" -> RockTextField(
              value = inputText,
              onValueChange = { viewModel.inputText.value = it },
              label = "Website URL",
              placeholder = "example.com",
              primaryColor = primaryColor,
              modifier = Modifier.fillMaxWidth().testTag("input_field_content")
            )

            "WIFI" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              RockTextField(
                value = wifiSsid,
                onValueChange = { viewModel.wifiSsid.value = it },
                label = "Network SSID",
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().testTag("input_field_ssid")
              )
              RockTextField(
                value = wifiPassword,
                onValueChange = { viewModel.wifiPassword.value = it },
                label = "Password",
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().testTag("input_field_password")
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf("WPA", "WEP", "nopass").forEach { security ->
                  val selected = wifiSecurity == security
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (selected) primaryColor.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f))
                      .border(
                        width = 1.dp,
                        color = if (selected) primaryColor.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(10.dp)
                      )
                      .clickable {
                        HapticUtils.vibrate(context, 15)
                        viewModel.wifiSecurity.value = security
                      }
                      .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = if (security == "nopass") "None" else security,
                      color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            "CONTACT" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              RockTextField(
                value = contactName,
                onValueChange = { viewModel.contactName.value = it },
                label = "Full Name",
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().testTag("input_field_contact_name")
              )
              RockTextField(
                value = contactPhone,
                onValueChange = { viewModel.contactPhone.value = it },
                label = "Phone Number",
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().testTag("input_field_contact_phone")
              )
              RockTextField(
                value = contactEmail,
                onValueChange = { viewModel.contactEmail.value = it },
                label = "Email Address",
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().testTag("input_field_contact_email")
              )
            }

            else -> RockTextField(
              value = inputText,
              onValueChange = { viewModel.inputText.value = it },
              label = "Text Content",
              primaryColor = primaryColor,
              singleLine = false,
              modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("input_field_content")
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = "QR Color",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.58f),
            modifier = Modifier.padding(bottom = 10.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            viewModel.colorOptions.forEach { option ->
              QrColorChip(
                color = Color(option.primaryColor),
                selected = selectedQrColor.name == option.name,
                onClick = {
                  HapticUtils.vibrate(context, 15)
                  viewModel.selectedQrColor.value = option
                }
              )
            }
          }

          Button(
            onClick = {
              HapticUtils.vibrate(context, 50)
              viewModel.generateQR()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("generate_code_button"),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp)
          ) {
            Icon(Icons.Default.Tune, contentDescription = "Generate QR")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Generate QR Code",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TypeChip(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  primaryColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (selected) primaryColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
      .border(
        width = 1.dp,
        color = if (selected) primaryColor.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
      )
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) primaryColor else Color.White.copy(alpha = 0.6f),
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
      )
    }
  }
}

@Composable
private fun RockTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  primaryColor: Color,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  singleLine: Boolean = true
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    placeholder = placeholder?.let { { Text(it) } },
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = TextFieldDefaults.colors(
      focusedTextColor = Color.White,
      unfocusedTextColor = Color.White,
      focusedContainerColor = Color.White.copy(alpha = 0.03f),
      unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
      focusedLabelColor = primaryColor,
      unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
      focusedIndicatorColor = primaryColor,
      unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
    ),
    singleLine = singleLine
  )
}

@Composable
private fun QrColorChip(
  color: Color,
  selected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(42.dp)
      .clip(CircleShape)
      .background(color)
      .clickable(onClick = onClick)
      .border(
        width = 3.dp,
        color = if (selected) Color.White else Color.Transparent,
        shape = CircleShape
      )
      .border(
        width = 1.dp,
        color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.2f),
        shape = CircleShape
      )
  )
}
