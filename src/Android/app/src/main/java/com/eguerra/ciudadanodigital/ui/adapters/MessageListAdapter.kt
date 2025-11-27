package com.eguerra.ciudadanodigital.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel

class MessageListAdapter(
    private var dataSet: MutableList<MessageModel>, private val operationListener: MessageListener
) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {

    interface MessageListener {
        fun onReferencesRequested(message: MessageModel)
        fun onQuestionSelected(question: String)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val infoButton: ImageButton? = view.findViewById(R.id.itemMessageReceived_infoButton)
        val chipGroup: com.google.android.material.chip.ChipGroup? =
            view.findViewById(R.id.questionsChipGroup)
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position].source == "user") 1 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout =
            if (viewType == 1) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]
        val parts = message.content.split("::Preguntas::")
        holder.messageText.text = parts[0].trim()

        holder.chipGroup?.removeAllViews()
        val isLastAssistantMessage = (position == dataSet.size - 1 && message.source == "assistant")

        if (isLastAssistantMessage && parts.size > 1) {
            val preguntas = parts[1].trim().split("?")
                .filter { it.isNotBlank() }
                .map { "$it?".trim() }

            if (preguntas.isNotEmpty()) {
                holder.chipGroup?.visibility = View.VISIBLE
                for (pregunta in preguntas) {
                    var preguntaLimpia = pregunta.trim().split("¿")[1]
                    preguntaLimpia = "¿$preguntaLimpia"
                    val chip = com.google.android.material.button.MaterialButton(
                        holder.itemView.context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle
                    ).apply {
                        text = preguntaLimpia
                        isAllCaps = false
                        letterSpacing = 0f
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        typeface = ResourcesCompat.getFont(context, R.font.urbanist)
                        ellipsize = null
                        strokeWidth = 0
                        strokeColor = null
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin =
                                (4 * holder.itemView.context.resources.displayMetrics.density).toInt()
                            bottomMargin =
                                (4 * holder.itemView.context.resources.displayMetrics.density).toInt()
                        }

                        val verticalPadding =
                            (8 * holder.itemView.context.resources.displayMetrics.density).toInt()
                        setPadding(24, verticalPadding, 24, verticalPadding)

                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.suggested_question_background
                            )
                        )
                        setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                        cornerRadius =
                            (16 * holder.itemView.context.resources.displayMetrics.density).toInt()

                        setOnClickListener {
                            operationListener.onQuestionSelected(preguntaLimpia)
                            holder.chipGroup?.visibility = View.GONE
                        }
                    }

                    holder.chipGroup?.addView(chip)
                }
            } else {
                holder.chipGroup?.visibility = View.GONE
            }
        } else {
            holder.chipGroup?.visibility = View.GONE
        }

        if (!message.reference.isNullOrBlank() && holder.infoButton != null) {
            holder.infoButton.visibility = View.VISIBLE
            holder.infoButton.setOnClickListener {
                operationListener.onReferencesRequested(message)
            }
        } else {
            holder.infoButton?.visibility = View.GONE
        }
    }

    override fun getItemCount() = dataSet.size

    fun addMessage(message: MessageModel) {
        dataSet.add(message)
        notifyItemInserted(dataSet.size - 1)
    }

    fun setMessages(messages: List<MessageModel>) {
        dataSet.clear()
        dataSet.addAll(messages)
        notifyDataSetChanged()
    }
}
