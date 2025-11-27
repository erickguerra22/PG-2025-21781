package com.eguerra.ciudadanodigital.ui.fragment.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel
import com.eguerra.ciudadanodigital.data.repository.MessageRepository
import com.eguerra.ciudadanodigital.ui.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MessageRepository
) : ViewModel() {

    // Usar SharedFlow para eventos de un solo uso
    private val _createMessageEvent: MutableSharedFlow<Status<MessageModel>> =
        MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
    val createMessageEvent: SharedFlow<Status<MessageModel>> = _createMessageEvent

    fun newMessage(content: String, chatId: String?) {
        viewModelScope.launch {
            _createMessageEvent.emit(Status.Loading())
            when (val result = repository.createMessage(
                content = content,
                chatId = chatId
            )) {
                is Resource.Success -> {
                    _createMessageEvent.emit(Status.Success(result.data.first))
                }

                else -> {
                    _createMessageEvent.emit(
                        Status.Error(
                            result.code ?: 500,
                            result.message ?: "Ocurrió un error al enviar el mensaje."
                        )
                    )
                }
            }
        }
    }

    private val _getResponseEvent: MutableSharedFlow<Status<Pair<MessageModel, Boolean>>> =
        MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
    val getResponseEvent: SharedFlow<Status<Pair<MessageModel, Boolean>>> = _getResponseEvent

    fun getResponse(question: String, chatId: String?) {
        viewModelScope.launch {
            _getResponseEvent.emit(Status.Loading())
            when (val result = repository.getResponse(
                question = question,
                chatId = chatId
            )) {
                is Resource.Success -> {
                    _getResponseEvent.emit(
                        Status.Success(Pair(result.data.first, result.data.third))
                    )
                }

                else -> {
                    _getResponseEvent.emit(
                        Status.Error(
                            result.code ?: 500,
                            result.message ?: "Ocurrió un error al obtener la respuesta."
                        )
                    )
                }
            }
        }
    }

    private val _assignMessageEvent: MutableSharedFlow<Status<Boolean>> =
        MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
    val assignMessageEvent: SharedFlow<Status<Boolean>> = _assignMessageEvent

    fun assignMessage(messageId: String, chatId: String) {
        viewModelScope.launch {
            _assignMessageEvent.emit(Status.Loading())
            when (val result = repository.assignMessage(
                messageId = messageId,
                chatId = chatId
            )) {
                is Resource.Success -> {
                    _assignMessageEvent.emit(Status.Success(true))
                }

                else -> {
                    _assignMessageEvent.emit(
                        Status.Error(
                            result.code ?: 500,
                            result.message ?: "Ocurrió un error al asignar el mensaje."
                        )
                    )
                }
            }
        }
    }
}