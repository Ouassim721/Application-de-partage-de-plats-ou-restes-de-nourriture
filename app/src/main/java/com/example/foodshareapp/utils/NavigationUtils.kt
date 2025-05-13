import android.app.Activity
import android.content.Intent
import com.example.foodshareapp.R
import com.example.foodshareapp.activities.MainActivity
import com.example.foodshareapp.activities.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Configure la bottom navigation avec une logique commune pour toutes les activités
 */
fun Activity.setupBottomNavigation(bottomNav: BottomNavigationView, currentItemId: Int) {
    // Définir l'élément actuel comme sélectionné
    bottomNav.selectedItemId = currentItemId

    // Configurer l'écouteur de navigation
    bottomNav.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                navigateToActivity(MainActivity::class.java)
                true
            }
           /* R.id.nav_my_dishes -> {
                navigateToActivity(MyDishesActivity::class.java)
                true
            }*/
            R.id.nav_profile -> {
                navigateToActivity(ProfileActivity::class.java)
                true
            }
            else -> false
        }
    }
}

/**
 * Fonction auxiliaire pour gérer la navigation entre les activités
 */
private fun <T : Activity> Activity.navigateToActivity(activityClass: Class<T>) {
    // Ne naviguer que si on n'est pas déjà sur cette activité
    if (this::class.java != activityClass) {
        val intent = Intent(this, activityClass)
        // Éviter les instances multiples d'une même activité
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        // Animation de transition facultative
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}