package com.eguerra.ciudadanodigital.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel
import com.eguerra.ciudadanodigital.helpers.DocumentType
import com.eguerra.ciudadanodigital.helpers.extractFileExtension
import com.eguerra.ciudadanodigital.helpers.getDocumentType

class DocumentListAdapter(
    private var dataSet: MutableList<DocumentModel>, private val operationListener: DocumentListener
) : RecyclerView.Adapter<DocumentListAdapter.ViewHolder>() {

    interface DocumentListener {
        fun onItemClicked(document: DocumentModel)

        fun onRemoveDocument(document: DocumentModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val documentTitle: TextView = view.findViewById(R.id.documentItem_title)
        val documentAuthor: TextView = view.findViewById(R.id.documentItem_author)
        val documentYear: TextView = view.findViewById(R.id.documentItem_year)
        val documentPreview: ImageView = view.findViewById(R.id.documentItem_documentPreview)

        val deleteButton: ImageButton = itemView.findViewById(R.id.documentItem_deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = R.layout.item_document
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document = dataSet[position]
        val url = document.documentUrl

        holder.documentTitle.text = document.title
        holder.documentAuthor.text = document.author
        holder.documentYear.text = document.year.toString()

        holder.itemView.setOnClickListener {
            operationListener.onItemClicked(document)
        }

        val fileExtension = extractFileExtension(url)
        val documentType = getDocumentType(fileExtension)

        holder.documentPreview.visibility = View.VISIBLE

        when (documentType) {
            DocumentType.IMAGE -> {
                Glide.with(holder.itemView.context).load(url).centerCrop()
                    .placeholder(R.drawable.document_placeholder)
                    .error(R.drawable.document_placeholder).into(holder.documentPreview)
            }

            DocumentType.PDF -> {
                holder.documentPreview.setImageResource(R.drawable.pdf_icon)
            }

            DocumentType.WORD -> {
                holder.documentPreview.setImageResource(R.drawable.word_icon)
            }

            DocumentType.TEXT -> {
                holder.documentPreview.setImageResource(R.drawable.text_icon)
            }

            DocumentType.POWERPOINT -> {
                holder.documentPreview.setImageResource(R.drawable.powerpoint_icon)
            }

            DocumentType.UNKNOWN -> {
                holder.documentPreview.setImageResource(R.drawable.document_placeholder)
            }

            else -> {
                holder.documentPreview.setImageResource(R.drawable.document_placeholder)
            }
        }

        holder.deleteButton.setOnClickListener {
            operationListener.onRemoveDocument(document)
        }
    }

    override fun getItemCount() = dataSet.size

    fun addDocument(document: DocumentModel) {
        dataSet.add(document)
        notifyItemInserted(dataSet.size - 1)
    }

    fun setDocuments(documents: List<DocumentModel>) {
        dataSet.clear()
        dataSet.addAll(documents)
        notifyDataSetChanged()
    }
}
