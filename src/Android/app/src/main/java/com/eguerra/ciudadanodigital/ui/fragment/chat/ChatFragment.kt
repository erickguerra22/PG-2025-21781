package com.eguerra.ciudadanodigital.ui.fragment.chat

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel
import com.eguerra.ciudadanodigital.databinding.FragmentChatBinding
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.activity.ChatViewModel
import com.eguerra.ciudadanodigital.ui.activity.LoadingViewModel
import com.eguerra.ciudadanodigital.ui.activity.MainActivity
import com.eguerra.ciudadanodigital.ui.adapters.MessageListAdapter
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment(), MessageListAdapter.MessageListener {
    private lateinit var binding: FragmentChatBinding
    private val messageViewModel: MessageViewModel by viewModels()
    private val mainChatViewModel: ChatViewModel by activityViewModels()
    private val loadingViewModel: LoadingViewModel by activityViewModels()
    private var isMessagesRecyclerUp: Boolean = false
    private var messageAdapter: MessageListAdapter? = null
    private var unassignedMessages: MutableList<MessageModel> = mutableListOf()
    private var localChatId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMessagesRecycler()
        setListeners()
        setObservers()
    }

    private fun setupMessagesRecycler() {
        messageAdapter = MessageListAdapter(mutableListOf(), this)
        binding.chatFragmentMessagesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
                reverseLayout = false
            }
            adapter = messageAdapter
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainChatViewModel.selectedChatId.collectLatest { chatId ->
                    if (chatId != null) {
                        if (localChatId == null || localChatId != chatId) {
                            if (localChatId != null) {
                                hideMessagesRecycler()
                                unassignedMessages = mutableListOf()
                            }
                            localChatId = chatId
                            unassignedMessages = mutableListOf()
                            mainChatViewModel.getMessages(chatId, 50, null, false)
                        }
                    } else {
                        localChatId = chatId
                        if (unassignedMessages.isEmpty())
                            hideMessagesRecycler()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            messageViewModel.createMessageEvent.collect { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.chatFragmentMessageTextInput.isEnabled = false
                    }

                    is Status.Success -> {
                        binding.chatFragmentMessageEditText.setText("")
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        val message = result.value

                        showMessagesRecycler()

                        if (message.chatId == null) unassignedMessages.add(message)

                        messageAdapter?.addMessage(message)
                        binding.chatFragmentMessagesRecycler.post {
                            val itemCount = messageAdapter?.itemCount ?: 0
                            if (itemCount > 0) {
                                binding.chatFragmentMessagesRecycler.smoothScrollToPosition(
                                    itemCount - 1
                                )
                            }
                        }
                        messageViewModel.getResponse(
                            question = message.content, localChatId
                        )
                    }

                    is Status.Error -> {
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        showToast(result.error, requireContext())
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            messageViewModel.getResponseEvent.collect { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.chatFragmentMessageTextInput.isEnabled = false
                        loadingViewModel.showLoadingDialog()
                    }

                    is Status.Success -> {
                        binding.chatFragmentMessageEditText.setText("")
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        val (message, newChat) = result.value

                        mainChatViewModel.selectChat(message.chatId?.toString())
                        if (newChat) {
                            for (m in unassignedMessages) {
                                messageViewModel.assignMessage(
                                    messageId = m.messageId.toString(),
                                    chatId = message.chatId.toString()
                                )
                            }
                        }

                        showMessagesRecycler()

                        messageAdapter?.addMessage(message)
                        binding.chatFragmentMessagesRecycler.post {
                            val itemCount = messageAdapter?.itemCount ?: 0
                            if (itemCount > 0) {
                                binding.chatFragmentMessagesRecycler.smoothScrollToPosition(
                                    itemCount - 1
                                )
                            }
                        }
                        loadingViewModel.hideLoadingDialog()
                    }

                    is Status.Error -> {
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        showToast(result.error, requireContext())
                        loadingViewModel.hideLoadingDialog()
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            messageViewModel.assignMessageEvent.collect { result ->
                println("RESULT: $result")
                when (result) {
                    is Status.Error -> {
                        showToast(result.error, requireContext())
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainChatViewModel.getChatMessagesStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.chatFragmentMessageEditText.setText("")
                        binding.chatFragmentMessageTextInput.isEnabled = false
                    }

                    is Status.Success -> {
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        val messages = result.value

                        if (messages.isNotEmpty()) {
                            showMessagesRecycler()
                            messageAdapter?.setMessages(messages)

                            val recycler = binding.chatFragmentMessagesRecycler
                            val layoutManager = recycler.layoutManager as LinearLayoutManager
                            layoutManager.stackFromEnd = true

                            if (!recycler.canScrollVertically(-1)) {
                                layoutManager.scrollToPositionWithOffset(
                                    messages.size - 1, Int.MIN_VALUE
                                )
                            }
                        } else {
                            hideMessagesRecycler()
                        }
                    }

                    is Status.Error -> {
                        binding.chatFragmentMessageTextInput.isEnabled = true
                        hideMessagesRecycler()
                        if (result.code != 404) showToast(result.error, requireContext())
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hideMessagesRecycler() {
        messageAdapter?.setMessages(emptyList())
        binding.chatFragmentWelcomeTextView.isVisible = true
        binding.chatFragmentLogoImageView.isVisible = true
        isMessagesRecyclerUp = false
    }

    private fun showMessagesRecycler() {
        if (!isMessagesRecyclerUp) {
            isMessagesRecyclerUp = true
            binding.chatFragmentMessagesRecycler.isVisible = true
            binding.chatFragmentLogoImageView.isVisible = false
            binding.chatFragmentWelcomeTextView.isVisible = false
        }
    }

    private fun setListeners() {
        binding.apply {
            chatFragmentMessageTextInput.setEndIconOnClickListener {
                val content = chatFragmentMessageEditText.text
                if (content?.trim().isNullOrBlank()) return@setEndIconOnClickListener
                sendMessage(content.toString())
            }

            chatFragmentPanelImageButton.setOnClickListener {
                (requireActivity() as MainActivity).toggleSidePanel()
            }
        }
    }

    private fun sendMessage(content: String) {
        messageViewModel.newMessage(
            content = content, chatId = localChatId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter = null
    }

    override fun onReferencesRequested(message: MessageModel) {
        val referencesText = message.reference?.split(";;;")?.map { r ->
            "• ${r.trim()}"
        }
        val tiempoRespuesta =
            if (message.responseTime != null) "\n\nTiempo de respuesta: ${message.responseTime / 1000}s" else ""

        AlertDialog.Builder(requireContext()).setTitle("Detalles de la respuesta")
            .setMessage("Referencias:\n${referencesText?.joinToString("\n")}$tiempoRespuesta")
            .setPositiveButton("Cerrar", null).show()
    }

    override fun onQuestionSelected(question: String) {
        messageViewModel.newMessage(content = question, chatId = localChatId)
    }
}