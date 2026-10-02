package com.example.smartagriculture.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R

class HelplineFragment : Fragment(R.layout.fragment_helpline) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardKisanCallCenter)?.setOnClickListener {
            dialPhoneNumber("18001801551")
        }

        view.findViewById<View>(R.id.cardFasalBima)?.setOnClickListener {
            dialPhoneNumber("18002005142")
        }

        view.findViewById<View>(R.id.cardEmailSupport)?.setOnClickListener {
            sendEmail("support@leaflens.ai")
        }
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(emailAddress: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailAddress")
                putExtra(Intent.EXTRA_SUBJECT, "Agronomy Support Request - Leaflens AI")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open email app", Toast.LENGTH_SHORT).show()
        }
    }
}
