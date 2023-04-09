package com.example.alphaacemobile.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.ActivityAboutUsBinding
import com.example.alphaacemobile.databinding.ActivityMainBinding

class AboutUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*  Setup Action Bar Details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "View Developers"

        // Pej's LinkedIn site
        binding.llAboutUsPE.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/peja-enriquez-6a9797265/"))
            startActivity(urlIntent)
        }

        // Christian's LinkedIn site
        binding.llAboutUsCS.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/christian-shi-a70543118/"))
            startActivity(urlIntent)
        }

        // Ram's LinkedIn site
        binding.llAboutUsREG.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/ram-emerson-goria-474265265/"))
            startActivity(urlIntent)
        }

        binding.btnAboutUsFacebook.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.facebook.com/profile.php?id=100087617372375"))
            startActivity(urlIntent)
        }

        binding.btnAboutUsTwitter.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/AlphaAce_Coin"))
            startActivity(urlIntent)
        }

        binding.btnAboutUsGithub.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Oxysian"))
            startActivity(urlIntent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}