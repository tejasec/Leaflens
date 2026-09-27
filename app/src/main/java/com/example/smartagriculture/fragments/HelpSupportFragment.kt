package com.example.smartagriculture.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R

class HelpSupportFragment : Fragment(R.layout.fragment_help_support) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.llEmail)?.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:nilesh.singh0032@gmail.com")
            }
            startActivity(Intent.createChooser(emailIntent, "Send email"))
        }

        view.findViewById<LinearLayout>(R.id.llPhone)?.setOnClickListener {
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/919801960622")
            }
            startActivity(whatsappIntent)
        }
    }
}
