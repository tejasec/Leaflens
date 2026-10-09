package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.ChatMessageAdapter
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.model.ChatMessage
import com.example.smartagriculture.network.GeminiService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class AskCropDoctorBottomSheet : BottomSheetDialogFragment() {

    private var diseaseName: String = "Tomato Early Blight"
    private var scientificName: String = "Alternaria solani"
    private var confidence: Int = 87
    private var organicCare: String = ""
    private var chemicalCare: String = ""
    private var scanId: Long = 0L
    private var chatHistoryJson: String? = null

    var onChatUpdated: ((List<ChatMessage>) -> Unit)? = null

    private lateinit var chatAdapter: ChatMessageAdapter
    private var rvChat: RecyclerView? = null
    private var pbSending: ProgressBar? = null
    private var btnSend: ImageButton? = null
    private var etInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diseaseName = arguments?.getString(ARG_DISEASE_NAME) ?: "Tomato Early Blight"
        scientificName = arguments?.getString(ARG_SCIENTIFIC_NAME) ?: "Alternaria solani"
        confidence = arguments?.getInt(ARG_CONFIDENCE, 87) ?: 87
        organicCare = arguments?.getString(ARG_ORGANIC_CARE) ?: ""
        chemicalCare = arguments?.getString(ARG_CHEMICAL_CARE) ?: ""
        scanId = arguments?.getLong(ARG_SCAN_ID, 0L) ?: 0L
        chatHistoryJson = arguments?.getString(ARG_CHAT_HISTORY_JSON)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_ask_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSubtitle = view.findViewById<TextView>(R.id.tvBottomSheetSubtitle)
        tvSubtitle?.text = "Diagnosed: $diseaseName ($confidence% confidence)"

        view.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
            dismiss()
        }

        rvChat = view.findViewById(R.id.rvChat)
        pbSending = view.findViewById(R.id.pbSending)
        btnSend = view.findViewById(R.id.btnSendMessage)
        etInput = view.findViewById(R.id.etInputMessage)

        chatAdapter = ChatMessageAdapter()
        rvChat?.layoutManager = LinearLayoutManager(requireContext())
        rvChat?.adapter = chatAdapter

        // Pre-load existing consultation history or display initial greeting
        val savedMessages = parseChatHistory(chatHistoryJson)
        if (savedMessages.isNotEmpty()) {
            chatAdapter.setMessages(savedMessages)
        } else {
            val initialGreeting = "Hello! I am your Crop Doctor AI Assistant. Your crop has been diagnosed with $diseaseName ($scientificName) at $confidence% confidence. Ask me any question about spray intervals, organic fertilizers, dosages, or preventative measures!"
            chatAdapter.addMessage(ChatMessage(initialGreeting, isUser = false))
        }

        // Quick Suggestion Chips
        view.findViewById<TextView>(R.id.chipSprayIntervals)?.setOnClickListener {
            sendQuestion("What are the recommended spray intervals for $diseaseName?")
        }
        view.findViewById<TextView>(R.id.chipOrganicRemedies)?.setOnClickListener {
            sendQuestion("What organic fertilizers and natural remedies should I use?")
        }
        view.findViewById<TextView>(R.id.chipPrevention)?.setOnClickListener {
            sendQuestion("How can I prevent $diseaseName from spreading or recurring next season?")
        }

        btnSend?.setOnClickListener {
            val text = etInput?.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) {
                sendQuestion(text)
                etInput?.setText("")
            }
        }
    }

    private fun sendQuestion(userText: String) {
        chatAdapter.addMessage(ChatMessage(userText, isUser = true))
        rvChat?.smoothScrollToPosition(chatAdapter.itemCount - 1)
        persistMessages()

        pbSending?.visibility = View.VISIBLE
        btnSend?.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val doctorReply = GeminiService.askCropDoctor(
                    diseaseName = diseaseName,
                    scientificName = scientificName,
                    confidence = confidence,
                    userQuestion = userText,
                    organicCare = organicCare,
                    chemicalCare = chemicalCare
                )
                chatAdapter.addMessage(ChatMessage(doctorReply, isUser = false))
                rvChat?.smoothScrollToPosition(chatAdapter.itemCount - 1)
                persistMessages()
            } catch (_: Exception) {
                chatAdapter.addMessage(ChatMessage("For $diseaseName, apply copper-based fungicide at 7-10 day intervals and spray neem oil for organic protection.", isUser = false))
                persistMessages()
            } finally {
                pbSending?.visibility = View.GONE
                btnSend?.visibility = View.VISIBLE
            }
        }
    }

    private fun persistMessages() {
        val allMessages = chatAdapter.getMessages()
        onChatUpdated?.invoke(allMessages)
        if (scanId > 0) {
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(requireContext().applicationContext)
                    val json = Gson().toJson(allMessages)
                    db.scanHistoryDao().updateChatHistory(scanId, json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun parseChatHistory(json: String?): List<ChatMessage> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            sheet.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    companion object {
        private const val ARG_DISEASE_NAME = "diseaseName"
        private const val ARG_SCIENTIFIC_NAME = "scientificName"
        private const val ARG_CONFIDENCE = "confidence"
        private const val ARG_ORGANIC_CARE = "organicCare"
        private const val ARG_CHEMICAL_CARE = "chemicalCare"
        private const val ARG_SCAN_ID = "scanId"
        private const val ARG_CHAT_HISTORY_JSON = "chatHistoryJson"

        fun newInstance(
            diseaseName: String,
            scientificName: String,
            confidence: Int,
            organicCare: String = "",
            chemicalCare: String = "",
            scanId: Long = 0L,
            chatHistoryJson: String? = null
        ): AskCropDoctorBottomSheet {
            return AskCropDoctorBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_DISEASE_NAME, diseaseName)
                    putString(ARG_SCIENTIFIC_NAME, scientificName)
                    putInt(ARG_CONFIDENCE, confidence)
                    putString(ARG_ORGANIC_CARE, organicCare)
                    putString(ARG_CHEMICAL_CARE, chemicalCare)
                    putLong(ARG_SCAN_ID, scanId)
                    putString(ARG_CHAT_HISTORY_JSON, chatHistoryJson)
                }
            }
        }
    }
}
