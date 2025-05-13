package com.example.foodshareapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foodshareapp.R

@Composable
fun DishListSection() {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(2) { // Fake 2 items, for mockup
            DishCard()
        }
    }
}

@Composable
fun DishCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), //sample_pasta
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )

            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp)
                    .background(Color(0xFFDEF5D4), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("🌱 Anti-gaspillage", style = MaterialTheme.typography.labelSmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pâtes aux légumes", style = MaterialTheme.typography.titleMedium)
                    Text("Par Thomas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("500m", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { /* Réserver */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Réserver", color = Color.White)
                    }
                }
            }
        }
    }
}
