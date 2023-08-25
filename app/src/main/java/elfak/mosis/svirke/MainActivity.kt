package elfak.mosis.svirke

import android.content.Context
import elfak.mosis.svirke.R
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import elfak.mosis.svirke.fragments.RegisterFragment



class MainActivity : AppCompatActivity()  {

    private var lastBackPressTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }



    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackPressTime < 2000) {
            finish()
        } else {
            Toast.makeText(this,"Pritisnite back dugme ponovo da biste zatvorili aplikaciju",Toast.LENGTH_SHORT).show()
            lastBackPressTime = currentTime
        }

    }
}