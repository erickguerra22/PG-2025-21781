package com.eguerra.ciudadanodigital.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.ChatModel
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel
import com.eguerra.ciudadanodigital.data.repository.ChatRepository
import com.eguerra.ciudadanodigital.data.repository.MessageRepository
import com.eguerra.ciudadanodigital.ui.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val repositoryMessage: MessageRepository
) : ViewModel() {

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId = _selectedChatId.asStateFlow()

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
    }

    private val _getUserChatsStateFlow: MutableStateFlow<Status<List<ChatModel>>> =
        MutableStateFlow(Status.Default())
    val getUserChatsStateFlow: StateFlow<Status<List<ChatModel>>> = _getUserChatsStateFlow

    fun getChats(remote: Boolean = false) {
        _getUserChatsStateFlow.value = Status.Loading()
        viewModelScope.launch {
            when (val result = repository.getUserChats(
                remote = remote
            )) {
                is Resource.Success -> {
                    _getUserChatsStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _getUserChatsStateFlow.value =
                        Status.Error(
                            result.code ?: 500,
                            result.message ?: "Ocurrió un error al obtener los mensajes."
                        )
                }
            }
        }
    }

    private val _getChatMessagesStateFlow: MutableStateFlow<Status<List<MessageModel>>> =
        MutableStateFlow(Status.Default())
    val getChatMessagesStateFlow: StateFlow<Status<List<MessageModel>>> = _getChatMessagesStateFlow

    fun getMessages(chatId: String, limit: Int?, offset: Int?, remote: Boolean = false) {
        _getChatMessagesStateFlow.value = Status.Loading()
        viewModelScope.launch {
            when (val result = repositoryMessage.getChatMessages(
                chatId = chatId,
                limit = limit,
                offset = offset,
                remote = remote
            )) {
                is Resource.Success -> {
                    _getChatMessagesStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _getChatMessagesStateFlow.value =
                        Status.Error(
                            result.code ?: 500,
                            result.message ?: "Ocurrió un error al obtener los mensajes."
                        )
                }
            }
        }
    }
}