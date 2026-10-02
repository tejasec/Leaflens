package com.example.smartagriculture.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.SchemesAdapter
import com.example.smartagriculture.model.Scheme
import com.example.smartagriculture.repository.SchemesRepository
import kotlinx.coroutines.launch

class SchemesFragment : Fragment(R.layout.fragment_schemes) {

    private lateinit var schemesRepository: SchemesRepository
    private var allSchemes: List<Scheme> = emptyList()
    private var adapter: SchemesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        schemesRepository = SchemesRepository(requireContext())

        val rvSchemes = view.findViewById<RecyclerView>(R.id.rvSchemes)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoadingSchemes)
        val etSearch = view.findViewById<EditText>(R.id.etSearchSchemes)

        rvSchemes.layoutManager = LinearLayoutManager(requireContext())

        val onSchemeClick: (Scheme) -> Unit = { scheme ->
            val bundle = Bundle().apply {
                putSerializable("scheme", scheme)
            }
            findNavController().navigate(R.id.action_schemesFragment_to_schemeDetailFragment, bundle)
        }

        // Show progress and fetch schemes via Repository (Online -> Room DB -> Offline Fallback)
        pbLoading?.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            allSchemes = schemesRepository.getSchemes()
            pbLoading?.visibility = View.GONE

            adapter = SchemesAdapter(allSchemes, onSchemeClick)
            rvSchemes.adapter = adapter
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase()?.trim() ?: ""
                val filtered = if (query.isEmpty()) {
                    allSchemes
                } else {
                    allSchemes.filter {
                        it.title.lowercase().contains(query) ||
                                it.description.lowercase().contains(query) ||
                                it.eligibility.lowercase().contains(query)
                    }
                }
                adapter = SchemesAdapter(filtered, onSchemeClick)
                rvSchemes.adapter = adapter
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}