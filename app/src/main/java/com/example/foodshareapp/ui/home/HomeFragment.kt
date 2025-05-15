package com.example.foodshareapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.ui.home.PlatAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlatAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var filterLocalisation: EditText
    private var allPlats = mutableListOf<Plat>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewPlats)
        filterLocalisation = view.findViewById(R.id.filterLocalisation)

        adapter = PlatAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()

        // Listener pour filtre localité
        filterLocalisation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterByLocalisation(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadPlats()
        return view
    }

    private fun loadPlats() {
        db.collection("plats")
            .orderBy("datePublication", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Erreur : ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allPlats.clear()
                for (doc in snapshots!!) {
                    val plat = doc.toObject(Plat::class.java)
                    allPlats.add(plat)
                }
                adapter.updateData(allPlats)
            }
    }

    private fun filterByLocalisation(query: String) {
        val filtered = if (query.isEmpty()) allPlats else allPlats.filter {
            it.localisation.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }
}