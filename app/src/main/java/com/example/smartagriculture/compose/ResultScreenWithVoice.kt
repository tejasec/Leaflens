package com.example.smartagriculture.compose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartagriculture.R
import com.example.smartagriculture.audio.AudioDossierManager
import com.example.smartagriculture.audio.AudioState
import com.example.smartagriculture.pdf.DiagnosisPayload
import com.example.smartagriculture.pdf.WhatsAppSharer
import com.example.smartagriculture.utils.PdfReportGenerator

// Agricultural Theme Colors
val LeafLensGreen = Color(0xFF2E7D32)
val PlayingRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreenWithVoice(
    payload: DiagnosisPayload,
    audioManager: AudioDossierManager
) {
    val context = LocalContext.current

    // Observe TTS playback state Flow
    val playbackState by audioManager.playbackState.collectAsState()

    // Active UI Language for Voice Readout ("en", "hi", "mr")
    var selectedLanguageCode by remember { mutableStateOf("hi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Diagnosis Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (payload.isHealthy) stringResource(R.string.status_healthy) else payload.diseaseName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (payload.isHealthy) LeafLensGreen else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stringResource(R.string.label_crop)}: ${payload.cropName} • ${stringResource(R.string.label_confidence)}: ${(payload.confidenceScore * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (!payload.isHealthy) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.label_remedy),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = LeafLensGreen
                    )
                    Text(
                        text = payload.organicRemedy,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Multilingual Audio Dossier Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = "Audio Dossier",
                        tint = LeafLensGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_listen_advisory),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Language Selection Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    LanguageChip(
                        label = "English",
                        isSelected = selectedLanguageCode == "en",
                        onClick = { selectedLanguageCode = "en" }
                    )
                    LanguageChip(
                        label = "हिंदी (Hindi)",
                        isSelected = selectedLanguageCode == "hi",
                        onClick = { selectedLanguageCode = "hi" }
                    )
                    LanguageChip(
                        label = "मराठी (Marathi)",
                        isSelected = selectedLanguageCode == "mr",
                        onClick = { selectedLanguageCode = "mr" }
                    )
                    LanguageChip(
                        label = "ગુજરાતી (Gujarati)",
                        isSelected = selectedLanguageCode == "gu",
                        onClick = { selectedLanguageCode = "gu" }
                    )
                    LanguageChip(
                        label = "తెలుగు (Telugu)",
                        isSelected = selectedLanguageCode == "te",
                        onClick = { selectedLanguageCode = "te" }
                    )
                    LanguageChip(
                        label = "தமிழ் (Tamil)",
                        isSelected = selectedLanguageCode == "ta",
                        onClick = { selectedLanguageCode = "ta" }
                    )
                }

                // Dynamic Audio Action Button (Play vs. Stop State)
                val buttonColor by animateColorAsState(
                    targetValue = if (playbackState == AudioState.PLAYING) PlayingRed else LeafLensGreen,
                    label = "AudioButtonColor"
                )

                Button(
                    onClick = {
                        if (playbackState == AudioState.PLAYING) {
                            audioManager.stopAudio()
                        } else {
                            audioManager.speakDossier(
                                cropName = payload.cropName,
                                diseaseName = payload.diseaseName,
                                confidencePct = (payload.confidenceScore * 100).toInt(),
                                organicRemedy = payload.organicRemedy,
                                languageCode = selectedLanguageCode
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(if (playbackState == AudioState.PLAYING) R.drawable.ic_stop else R.drawable.ic_volume_up),
                            contentDescription = "Toggle Audio"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (playbackState) {
                                AudioState.PLAYING -> "Stop Voice Advisory / आवाज़ रोकें"
                                AudioState.ERROR -> "Audio Unavailable / पुनः प्रयास करें"
                                else -> "Play Voice Advisory / आवाज़ सुनें"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Active Playback Indicator
                AnimatedVisibility(visible = playbackState == AudioState.PLAYING) {
                    Text(
                        text = "🔊 Reading diagnostic advisory in ${selectedLanguageCode.uppercase()}...",
                        fontSize = 12.sp,
                        color = LeafLensGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // 3. Export PDF & Share on WhatsApp
        OutlinedButton(
            onClick = {
                try {
                    val pdfFile = PdfReportGenerator.generateDiagnosticDossier(context, payload)
                    WhatsAppSharer.shareDossierViaWhatsApp(context, pdfFile, payload)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, LeafLensGreen)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share PDF",
                    tint = LeafLensGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_export_whatsapp),
                    fontWeight = FontWeight.Bold,
                    color = LeafLensGreen,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LeafLensGreen,
            selectedLabelColor = Color.White
        )
    )
}
