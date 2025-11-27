package com.eguerra.ciudadanodigital.ui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.entity.ChatModel
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.databinding.ActivityMainBinding
import com.eguerra.ciudadanodigital.helpers.ADMIN
import com.eguerra.ciudadanodigital.helpers.InternetStatusListener
import com.eguerra.ciudadanodigital.helpers.InternetStatusManager
import com.eguerra.ciudadanodigital.helpers.SessionManager
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.adapters.ChatListAdapter
import com.eguerra.ciudadanodigital.ui.dialogs.LoadingDialog
import com.eguerra.ciudadanodigital.ui.util.getColorFromSeed
import com.eguerra.ciudadanodigital.ui.util.showConfirmationDialog
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), InternetStatusListener, ChatListAdapter.ChatListener {

    @Inject
    lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityMainBinding
    private val mainUserViewModel: UserViewModel by viewModels()
    private val mainChatViewModel: ChatViewModel by viewModels()
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private val loadingDialog = LoadingDialog()
    private val loadingViewModel: LoadingViewModel by viewModels()
    private var chatAdapter: ChatListAdapter? = null
    private var isChatsRecyclerUp: Boolean = false
    private var isPanelVisible = false
    var isLoggingOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        isFirstLoad = savedInstanceState == null
        InternetStatusManager.addListener(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionManager.logoutEvents.collect { reason ->
                    handleLogoutAction(reason)
                }
            }
        }

        initEvents()
        setContentView(binding.root)
        setObservers()
        setListeners()
    }

    private fun initEvents() {
        mainUserViewModel.getUserData(true)
        setupChatsRecycler()
        configureNavigation()
    }

    override fun onStart() {
        super.onStart()

        if (isFirstLoad) {
            isFirstLoad = false
        }
    }

    private fun hideSidePanel() {
        val panel = binding.mainActivitySidePanelContainer
        val overlay = binding.mainActivityOverlayView

        overlay.animate().alpha(0f).setDuration(200).withEndAction { overlay.isVisible = false }
            .start()

        panel.animate().translationX(-panel.width.toFloat()).setDuration(250)
            .withEndAction { panel.isVisible = false }.start()

        isPanelVisible = false
    }

    private fun setListeners() {
        binding.apply {
            mainActivityNewChatButton.setOnClickListener {
                mainChatViewModel.selectChat(null)
                hideSidePanel()

                val currentDestination = navController.currentDestination?.id

                if (currentDestination != R.id.chatFragment) {
                    navController.navigate(R.id.chatFragment)
                }
            }

            mainActivityLogoutButton.setOnClickListener {
                showConfirmationDialog(
                    "Cerrar Sesión",
                    "¿Está seguro que desea terminar la sesión actual?",
                    this@MainActivity
                ) { confirm ->
                    if (confirm) handleLogoutAction()
                }
            }
            mainActivityOverlayView.apply {
                setOnTouchListener { v, event ->
                    val panelRect = Rect()
                    binding.mainActivitySidePanelContainer.getGlobalVisibleRect(panelRect)

                    if (panelRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        return@setOnTouchListener false
                    }

                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                        hideSidePanel()
                    }

                    true
                }

                setOnClickListener {}
            }

            mainActivityProfileButton.setOnClickListener {
                hideSidePanel()
                navController.navigate(R.id.profileFragment)
            }

            mainActivityManageFilesButton.setOnClickListener {
                hideSidePanel()
                navController.navigate(R.id.documentsFragment)
            }
        }
    }

    fun toggleSidePanel() {
        if (isPanelVisible) hideSidePanel() else showSidePanel()
        mainChatViewModel.getChats()
    }

    private fun showSidePanel() {
        val panel = binding.mainActivitySidePanelContainer
        val overlay = binding.mainActivityOverlayView

        overlay.isVisible = true
        overlay.alpha = 0f
        overlay.animate().alpha(1f).setDuration(200).start()

        panel.isVisible = true
        panel.animate().translationX(0f).setDuration(250).start()

        isPanelVisible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        InternetStatusManager.removeListener(this)
    }

    fun handleLogoutAction(message: String = "Se ha cerrado sesión correctamente") {
        if (isLoggingOut) return
        isLoggingOut = true
        mainUserViewModel.logout()

        showToast(message, this, true)

        val intent = Intent(this, UnloggedActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadingViewModel.isLoading.collectLatest { isLoading ->
                    manageLoadingComponent(isLoading)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainUserViewModel.userDataStateFlow.collectLatest { state ->
                    when (state) {
                        is UserSessionStatus.Logged -> {
                            setUpProfileActions(state.data)
                        }

                        is UserSessionStatus.NotLogged -> {
                            handleLogoutAction("No se pudo obtener la información del usuario")
                        }

                        else -> {}
                    }
                }
            }
        }
        lifecycleScope.launch {
            mainChatViewModel.getUserChatsStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {}

                    is Status.Success -> {
                        val chats = result.value

                        if (chats.isNotEmpty()) {
                            showChatsRecycler()
                            chatAdapter?.setChats(chats)
                        }
                    }

                    is Status.Error -> {
                        chatAdapter?.setChats(emptyList())
                        if (result.code != 404) showToast(result.error, this@MainActivity)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun showChatsRecycler() {
        if (!isChatsRecyclerUp) {
            isChatsRecyclerUp = true
            binding.mainActivityChatsRecycler.isVisible = true
        }
    }

    private fun manageLoadingComponent(isLoading: Boolean) {
        if (isLoading) {
            if (!loadingDialog.isAdded) {
                loadingDialog.show(supportFragmentManager, "Loading")
            }
        } else {
            if (loadingDialog.isAdded) {
                loadingDialog.dismiss()
            }
        }
    }

    override fun onInternetStatusChanged(isConnected: Boolean) {
//        showToast("Estás conectado: $isConnected", this)
    }

    private fun setupChatsRecycler() {
        chatAdapter = ChatListAdapter(mutableListOf(), this)
        binding.mainActivityChatsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }
    }

    private fun setUpProfileActions(user: UserModel) {
        val button = binding.mainActivityProfileButton
        val nameLabel = binding.mainActivityProfileName
        nameLabel.text =
            getString(R.string.username, user.names.split(" ")[0], user.lastnames.split(" ")[0])

        val initial = user.names.trim().firstOrNull()?.uppercase() ?: "?"
        val color = getColorFromSeed(seed = user.userId.toString())

        button.text = initial
        button.setBackgroundColor(color)

        if (user.role == ADMIN) binding.mainActivityManageFilesButton.isVisible = true
    }

    private fun configureNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainActivity_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onItemClicked(chat: ChatModel) {
        hideSidePanel()
        val currentDestination = navController.currentDestination?.id

        if (currentDestination != R.id.chatFragment) {
            navController.navigate(R.id.chatFragment)
        }
        mainChatViewModel.selectChat(chat.chatId.toString())
    }
}