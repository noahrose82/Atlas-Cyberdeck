package com.noahrose.pocketlab.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noahrose.pocketlab.R

@Composable
fun AtlasSplashScreen() {

    val context =
        LocalContext.current

    /*
     * Atlas startup Easter egg.
     *
     * The MediaPlayer is created when the splash
     * enters composition and released when the
     * splash leaves composition.
     *
     * This prevents the sound from replaying
     * during normal recomposition.
     */
    DisposableEffect(Unit) {

        val mediaPlayer =
            MediaPlayer.create(
                context,
                R.raw.atlas_meow
            )

        mediaPlayer?.start()

        onDispose {

            mediaPlayer?.run {

                if (isPlaying) {
                    stop()
                }

                release()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 48.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.atlas_cyberdeck_emblem
                ),
                contentDescription = "Atlas Cyberdeck emblem",
                modifier = Modifier.size(230.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "ATLAS",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "CYBERDECK",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "PORTABLE LINUX WORKSPACE",
                color = Color.White,
                fontSize = 12.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "CYBERSECURITY TOOLKIT",
                color = Color.White,
                fontSize = 12.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Image(
                painter = painterResource(
                    id = R.drawable.atlas_cyberdeck_splash
                ),
                contentDescription = "Atlas Cyberdeck platform",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "ATLAS LABS",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )
        }
    }
}