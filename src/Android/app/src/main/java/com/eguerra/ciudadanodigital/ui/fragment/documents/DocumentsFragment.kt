package com.eguerra.ciudadanodigital.ui.fragment.documents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel
import com.eguerra.ciudadanodigital.databinding.FragmentDocumentsBinding
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.activity.LoadingViewModel
import com.eguerra.ciudadanodigital.ui.activity.MainActivity
import com.eguerra.ciudadanodigital.ui.adapters.DocumentListAdapter
import com.eguerra.ciudadanodigital.ui.util.getFileNameFromUri
import com.eguerra.ciudadanodigital.ui.util.showConfirmationDialog
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DocumentsFragment : Fragment(), DocumentListAdapter.DocumentListener {
    private lateinit var binding: FragmentDocumentsBinding
    private val documentViewModel: DocumentsViewModel by viewModels()
    private val loadingViewModel: LoadingViewModel by activityViewModels()
    private var documentAdapter: DocumentListAdapter? = null
    private var isDocumentsRecyclerUp: Boolean = false
    private var selectedFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEvents()
        setupDocumentsRecycler()
        setObservers()
        setListeners()
    }

    private fun initEvents() {
        binding.documentsFragmentLayout.setProgressViewOffset(true, 0, 200)
    }

    override fun onStart() {
        super.onStart()
        documentViewModel.getDocuments(true)
    }

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedFileUri = result.data?.data

            selectedFileUri?.let { uri ->
                val fileName = getFileNameFromUri(requireContext(), uri)
                println("SELECTEDFILEURI: $uri")
                println("FILENAME: $fileName")

                binding.formNewDocumentFileName.text = fileName ?: "Archivo seleccionado"
            }
        }
    }

    private fun showDocumentsRecycler() {
        if (!isDocumentsRecyclerUp) {
            isDocumentsRecyclerUp = true
            binding.documentsFragmentDocumentsRecycler.isVisible = true
        }
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            documentViewModel.getDocumentsStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        loadingViewModel.showLoadingDialog()
                    }

                    is Status.Success -> {
                        val documents = result.value.second
                        if (binding.documentsFragmentLayout.isRefreshing) binding.documentsFragmentLayout.isRefreshing =
                            false

                        if (documents.isNotEmpty()) {
                            showDocumentsRecycler()
                            documentAdapter?.setDocuments(documents)
                        }
                        loadingViewModel.hideLoadingDialog()
                    }

                    is Status.Error -> {
                        documentAdapter?.setDocuments(emptyList())
                        if (result.code != 404) showToast(result.error, requireContext())
                        loadingViewModel.hideLoadingDialog()
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            documentViewModel.saveDocumentStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        loadingViewModel.showLoadingDialog()
                    }

                    is Status.Success -> {
                        val message = result.value

                        showToast(message, requireContext())
                        documentViewModel.getDocuments()
                        showNewDocumentForm(false)
                        loadingViewModel.hideLoadingDialog()
                    }

                    is Status.Error -> {
                        if (result.code != 404) showToast(result.error, requireContext())
                        loadingViewModel.hideLoadingDialog()
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            documentViewModel.deleteDocumentStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        loadingViewModel.showLoadingDialog()
                    }

                    is Status.Success -> {
                        val message = result.value

                        showToast(message, requireContext(), true)
                        documentViewModel.getDocuments(true)
                        loadingViewModel.hideLoadingDialog()
                    }

                    is Status.Error -> {
                        loadingViewModel.hideLoadingDialog()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupDocumentsRecycler() {
        documentAdapter = DocumentListAdapter(mutableListOf(), this)
        binding.documentsFragmentDocumentsRecycler.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = documentAdapter
        }
    }

    private fun setListeners() {
        binding.apply {
            documentsFragmentPanelImageButton.setOnClickListener {
                (requireActivity() as MainActivity).toggleSidePanel()
            }

            documentsFragmentLayout.setOnRefreshListener {
                documentViewModel.getDocuments()
            }

            documentsFragmentAddDocumentButton.setOnClickListener {
                showNewDocumentForm(true)
            }
            documentsFragmentOverlayView.setOnClickListener {
                showNewDocumentForm(false)
            }

            formNewDocumentAddFile.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES, arrayOf(
                            "application/pdf",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/vnd.ms-powerpoint",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            "text/plain",
                            "text/markdown"
                        )
                    )
                }
                selectFileLauncher.launch(intent)
            }
            formNewDocumentSaveDocument.setOnClickListener {
                val title = formNewDocumentTitleEditText.text.toString()
                val author = formNewDocumentAuthorEditText.text.toString()
                val year = formNewDocumentYearEditText.text.toString()

                val minAge = formNewDocumentAgeSlider.values[0].toInt()
                val maxAge = formNewDocumentAgeSlider.values[1].toInt()

                if (title.isBlank() || author.isBlank() || year.isBlank() || selectedFileUri == null) {
                    showToast("Por favor completa todos los campos", requireContext())
                    return@setOnClickListener
                }

                documentViewModel.saveDocument(title, author, year.toInt(), selectedFileUri!!, minAge, maxAge)
            }
            formNewDocumentAgeSlider.addOnChangeListener { slider, _,_->
                formNewDocumentAgeSliderPreview.text = getString(
                    R.string.ages_preview_template,
                    slider.values[0].toInt(),
                    slider.values[1].toInt()
                )
            }
        }
    }

    private fun showNewDocumentForm(show: Boolean) {
        binding.apply {
            if (show) documentsFragmentLayout.setBackgroundColor("#52555A".toColorInt())
            else documentsFragmentLayout.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.main_color
                )
            )
            documentsFragmentOverlayView.isVisible = show
            documentsFragmentFormNewDocument.isVisible = show
            documentsFragmentAddDocumentButton.isVisible = !show
        }
    }

    override fun onItemClicked(document: DocumentModel) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = document.documentUrl.toUri()
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showToast("No se pudo abrir el documento", requireContext())
        }
    }

    override fun onRemoveDocument(document: DocumentModel) {
        showConfirmationDialog(
            title = "Eliminar documento",
            description = "¿Estás seguro de que deseas eliminar este documento del corpus?",
            context = requireContext()
        ) { confirm ->
            if (confirm) {
                documentViewModel.deleteDocument(document.documentId)
            }
        }
    }
}