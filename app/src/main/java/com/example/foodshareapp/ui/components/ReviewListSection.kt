package com.example.foodshareapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Review(
    val reviewerName: String,
    val comment: String,
    val rating: Float
)

val sampleReviews = listOf(
    Review("Sanae", "Très bon plat, merci !", 5f),
    Review("Youssef", "Livraison rapide, bon goût", 4.5f),
    Review("Imane", "Un peu salé pour moi, mais globalement top", 4f)
)

@Composable
fun ReviewListSection(reviews: List<Review> = sampleReviews) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reviews) { review ->
            ReviewCard(review)
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = review.reviewerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.comment)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Note : ${review.rating} ⭐")
        }
    }
}
