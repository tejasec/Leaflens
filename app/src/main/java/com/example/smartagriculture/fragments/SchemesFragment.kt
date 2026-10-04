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

    private var schemesRepository: SchemesRepository? = null
    private var allSchemes: List<Scheme> = emptyList()
    private var adapter: SchemesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeContext = context ?: return
        val repository = SchemesRepository(safeContext)
        schemesRepository = repository

        val rvSchemes = view.findViewById<RecyclerView>(R.id.rvSchemes)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoadingSchemes)
        val etSearch = view.findViewById<EditText>(R.id.etSearchSchemes)

        rvSchemes?.layoutManager = LinearLayoutManager(safeContext)

        val onSchemeClick: (Scheme) -> Unit = { scheme ->
            try {
                val bundle = Bundle().apply {
                    putSerializable("scheme", scheme)
                }
                findNavController().navigate(R.id.action_schemesFragment_to_schemeDetailFragment, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Show progress and fetch schemes via Repository (Online -> Room DB -> Offline Fallback)
        pbLoading?.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            allSchemes = try {
                repository.getSchemes()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            pbLoading?.visibility = View.GONE

            adapter = SchemesAdapter(allSchemes, onSchemeClick)
            rvSchemes?.adapter = adapter
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase()?.trim() ?: ""
                val filtered = if (query.isEmpty()) {
                    allSchemes
                } else {
                    allSchemes.filter { scheme ->
                        val titleMatch = scheme.title?.lowercase()?.contains(query) == true
                        val descMatch = scheme.description?.lowercase()?.contains(query) == true
                        val eligMatch = scheme.eligibility?.lowercase()?.contains(query) == true
                        titleMatch || descMatch || eligMatch
                    }
                }
                adapter = SchemesAdapter(filtered, onSchemeClick)
                rvSchemes?.adapter = adapter
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}