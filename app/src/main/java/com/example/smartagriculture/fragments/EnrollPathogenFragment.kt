package com.example.smartagriculture.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentEnrollPathogenBinding
import com.example.smartagriculture.ml.FeatureEmbeddingExtractor
import com.example.smartagriculture.quality.QualityGate
import com.example.smartagriculture.repository.FewShotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen for On-Device Few-Shot Pathogen Enrollment (MOD-04, Feature 6).
 * Allows extension workers and farmers to register a novel crop disease using 3 to 5 reference photos.
 */
class EnrollPathogenFragment : Fragment(R.layout.fragment_enroll_pathogen) {

    private var binding: FragmentEnrollPathogenBinding? = null
    private val capturedBitmaps = mutableListOf<Bitmap>()

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { addCapturedBitmap(it) }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(it)
            if (bitmap != null) {
                addCapturedBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Failed to decode selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnrollPathogenBinding.bind(view)

        setupListeners()

        // Handle initial image if passed from ScanLowConfidenceFragment
        val initialUriStr = arguments?.getString("initialImageUri")
        if (!initialUriStr.isNullOrBlank() && capturedBitmaps.isEmpty()) {
            try {
                val uri = Uri.parse(initialUriStr)
                val bitmap = loadBitmapFromUri(uri)
                if (bitmap != null) {
                    addCapturedBitmap(bitmap)
                    Toast.makeText(requireContext(), "Imported scan as Shot 1. Add 2 more photos to teach disease.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        renderPhotoSlots()
        validateForm()
    }

    private fun setupListeners() {
        binding?.btnBack?.setOnClickListener {
            if (!findNavController().navigateUp()) {
                findNavController().popBackStack()
            }
        }

        binding?.btnAddCamera?.setOnClickListener {
            if (capturedBitmaps.size >= 5) {
                Toast.makeText(requireContext(), "Maximum of 5 photos reached.", Toast.LENGTH_SHORT).show()
            } else {
                cameraLauncher.launch(null)
            }
        }

        binding?.btnAddGallery?.setOnClickListener {
            if (capturedBitmaps.size >= 5) {
                Toast.makeText(requireContext(), "Maximum of 5 photos reached.", Toast.LENGTH_SHORT).show()
            } else {
                galleryLauncher.launch("image/*")
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding?.etPathogenName?.addTextChangedListener(textWatcher)
        binding?.etCropSpecies?.addTextChangedListener(textWatcher)

        binding?.btnRegisterPathogen?.setOnClickListener {
            registerPathogen()
        }
    }

    private fun addCapturedBitmap(bitmap: Bitmap) {
        if (capturedBitmaps.size >= 5) {
            Toast.makeText(requireContext(), "Maximum of 5 photos reached.", Toast.LENGTH_SHORT).show()
            return
        }

        // Evaluate Image Quality Gate
        val quality = QualityGate.validateImage(bitmap)
        if (!quality.isValid) {
            Toast.makeText(requireContext(), "Quality Alert: ${quality.feedbackMessage}", Toast.LENGTH_SHORT).show()
        }

        capturedBitmaps.add(bitmap)
        renderPhotoSlots()
        validateForm()
    }

    private fun removeBitmapAt(index: Int) {
        if (index in 0 until capturedBitmaps.size) {
            capturedBitmaps.removeAt(index)
            renderPhotoSlots()
            validateForm()
        }
    }

    private fun renderPhotoSlots() {
        val b = binding ?: return
        val count = capturedBitmaps.size

        b.tvShotCountBadge.text = "$count / 5"
        b.tvShotCountStatus.text = if (count < 3) {
            "$count of 5 photos added (Need at least ${3 - count} more to calculate centroid)"
        } else {
            "✓ $count photos ready. Minimum requirements met for centroid calculation."
        }

        val slots = listOf(
            Triple(b.cardSlot1, b.ivSlot1, b.btnDeleteSlot1),
            Triple(b.cardSlot2, b.ivSlot2, b.btnDeleteSlot2),
            Triple(b.cardSlot3, b.ivSlot3, b.btnDeleteSlot3),
            Triple(b.cardSlot4, b.ivSlot4, b.btnDeleteSlot4),
            Triple(b.cardSlot5, b.ivSlot5, b.btnDeleteSlot5),
        )

        val placeholders = listOf(
            b.tvSlot1Placeholder,
            b.tvSlot2Placeholder,
            b.tvSlot3Placeholder,
            b.tvSlot4Placeholder,
            b.tvSlot5Placeholder,
        )

        for (i in 0 until 5) {
            val (_, iv, btnDel) = slots[i]
            val placeholder = placeholders[i]

            if (i < count) {
                iv.setImageBitmap(capturedBitmaps[i])
                iv.visibility = View.VISIBLE
                btnDel.visibility = View.VISIBLE
                placeholder.visibility = View.GONE
                btnDel.setOnClickListener { removeBitmapAt(i) }
            } else {
                iv.setImageDrawable(null)
                iv.visibility = View.GONE
                btnDel.visibility = View.GONE
                placeholder.visibility = View.VISIBLE
            }
        }
    }

    private fun validateForm() {
        val b = binding ?: return
        val name = b.etPathogenName.text?.toString()?.trim() ?: ""
        val isValid = name.isNotBlank() && capturedBitmaps.size >= 3

        b.btnRegisterPathogen.isEnabled = isValid
        b.btnRegisterPathogen.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun registerPathogen() {
        val b = binding ?: return
        val pathogenName = b.etPathogenName.text?.toString()?.trim() ?: ""
        val cropSpecies = b.etCropSpecies.text?.toString()?.trim() ?: ""

        if (pathogenName.isBlank()) {
            Toast.makeText(requireContext(), "Please provide a pathogen name.", Toast.LENGTH_SHORT).show()
            return
        }

        if (capturedBitmaps.size < 3) {
            Toast.makeText(requireContext(), "At least 3 photos are required to teach a disease.", Toast.LENGTH_SHORT).show()
            return
        }

        val fullClassName = if (cropSpecies.isNotBlank()) {
            "$cropSpecies - $pathogenName"
        } else {
            pathogenName
        }

        b.btnRegisterPathogen.isEnabled = false
        b.pbRegistering.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val embeddings = capturedBitmaps.map { bitmap ->
                        FeatureEmbeddingExtractor.extractEmbedding(bitmap)
                    }

                    val dao = AppDatabase.getDatabase(requireContext().applicationContext).prototypeDao()
                    val repository = FewShotRepository(dao)
                    repository.enrollPathogen(fullClassName, embeddings)
                }

                Toast.makeText(
                    requireContext(),
                    "✓ '$fullClassName' taught successfully for offline recognition!",
                    Toast.LENGTH_LONG
                ).show()

                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to teach disease: ${e.message}", Toast.LENGTH_LONG).show()
                b.btnRegisterPathogen.isEnabled = true
                b.pbRegistering.visibility = View.GONE
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val context = requireContext()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
