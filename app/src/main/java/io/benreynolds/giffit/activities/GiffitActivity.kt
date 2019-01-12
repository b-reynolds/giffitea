package io.benreynolds.giffit.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.benreynolds.giffit.R
import io.benreynolds.giffit.fragments.ImageSelectionFragment

class GiffitActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      supportFragmentManager
        .beginTransaction()
        .add(R.id.clRoot, ImageSelectionFragment())
        .commit()
    }
  }
}
