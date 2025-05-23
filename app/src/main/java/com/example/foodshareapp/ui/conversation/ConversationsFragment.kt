package com.example.foodshareapp.ui.conversation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodshareapp.databinding.FragmentConversationsBinding
import com.example.foodshareapp.data.model.Conversation
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.example.foodshareapp.data.model.ConversationStatus
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.R


class ConversationsFragment : Fragment() {

    private lateinit var binding: FragmentConversationsBinding
    private lateinit var viewModel: ConversationsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConversationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ConversationsViewModel::class.java]

        // Tabs
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Toutes"))
        tabLayout.addTab(tabLayout.newTab().setText("Confirmées"))
        tabLayout.addTab(tabLayout.newTab().setText("Terminées"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    1 -> ConversationStatus.CONFIRMED
                    2 -> ConversationStatus.COMPLETED
                    else -> null
                }
                viewModel.filterByStatus(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // RecyclerView
        val adapter = ConversationsAdapter(emptyList()) { conversation ->
            // Ouvrir ChatFragment ou ChatActivity
            findNavController().navigate(
                R.id.action_conversationsFragment_to_chatFragment,
                bundleOf("conversationId" to conversation.id)
            )
        }

        binding.conversationsRecyclerView.adapter = adapter
        binding.conversationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observing data
        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            val newAdapter = ConversationsAdapter(conversations) { conversation ->
                findNavController().navigate(
                    R.id.action_conversationsFragment_to_chatFragment,
                    bundleOf("conversationId" to conversation.id)
                )
            }
            binding.conversationsRecyclerView.adapter = newAdapter
        }


        return binding.root
    }
}
