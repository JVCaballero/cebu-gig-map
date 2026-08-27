package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GigEntity
import com.example.data.GigRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavDestination {
    HOME,
    EXPLORE_MAP,
    POST_GIG,
    ADMIN,
    SAVED
}

data class PostGigFormState(
    val title: String = "",
    val category: String = "Cover Band",
    val dateText: String = "",
    val locationName: String = "Fuente Osmeña Circle",
    val posX: Float = 0.45f,
    val posY: Float = 0.42f,
    val payText: String = "₱5,000 / set",
    val contactInfo: String = "",
    val description: String = "",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)

class GigViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GigRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = GigRepository(database.gigDao())
    }

    // Navigation & UI Mode
    private val _currentDestination = MutableStateFlow(AppNavDestination.EXPLORE_MAP)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    // Dark Mode: null = system default, true = dark, false = light
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun setDarkMode(dark: Boolean?) {
        _isDarkMode.value = dark
    }

    fun toggleDarkMode(currentIsDark: Boolean) {
        _isDarkMode.value = !currentIsDark
    }

    fun navigateTo(destination: AppNavDestination) {
        _currentDestination.value = destination
    }

    // Explore / Map Feed State
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGig = MutableStateFlow<GigEntity?>(null)
    val selectedGig: StateFlow<GigEntity?> = _selectedGig.asStateFlow()

    private val _isMobileListView = MutableStateFlow(false)
    val isMobileListView: StateFlow<Boolean> = _isMobileListView.asStateFlow()

    fun toggleMobileListView() {
        _isMobileListView.value = !_isMobileListView.value
    }

    fun setMobileListView(isList: Boolean) {
        _isMobileListView.value = isList
    }

    // Raw Gigs
    val allGigs: StateFlow<List<GigEntity>> = repository.exploreGigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flaggedGigs: StateFlow<List<GigEntity>> = repository.flaggedGigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedOrAppliedGigs: StateFlow<List<GigEntity>> = repository.savedOrAppliedGigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = repository.activeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCount: StateFlow<Int> = repository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val flaggedCount: StateFlow<Int> = repository.flaggedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered explore gigs
    val filteredGigs: StateFlow<List<GigEntity>> = combine(
        allGigs,
        _selectedCategory,
        _searchQuery
    ) { gigs, category, query ->
        gigs.filter { gig ->
            val matchesCategory = (category == "All") || gig.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    gig.title.contains(query, ignoreCase = true) ||
                    gig.locationName.contains(query, ignoreCase = true) ||
                    gig.description.contains(query, ignoreCase = true) ||
                    gig.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGig(gig: GigEntity?) {
        _selectedGig.value = gig
    }

    // Interactive Dialogs
    private val _applyGigTarget = MutableStateFlow<GigEntity?>(null)
    val applyGigTarget: StateFlow<GigEntity?> = _applyGigTarget.asStateFlow()

    private val _flagGigTarget = MutableStateFlow<GigEntity?>(null)
    val flagGigTarget: StateFlow<GigEntity?> = _flagGigTarget.asStateFlow()

    fun openApplyDialog(gig: GigEntity) {
        _applyGigTarget.value = gig
    }

    fun closeApplyDialog() {
        _applyGigTarget.value = null
    }

    fun openFlagDialog(gig: GigEntity) {
        _flagGigTarget.value = gig
    }

    fun closeFlagDialog() {
        _flagGigTarget.value = null
    }

    // SnackBar / Toast events
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun submitApplication(gig: GigEntity, applicantName: String, instrument: String, note: String) {
        viewModelScope.launch {
            repository.setApplied(gig.id, true)
            closeApplyDialog()
            _toastEvent.emit("Application submitted for '${gig.title}'!")
        }
    }

    fun submitFlag(gig: GigEntity, reason: String) {
        viewModelScope.launch {
            repository.flagGig(gig.id, reason)
            closeFlagDialog()
            _toastEvent.emit("Flag submitted. Admin will review this post.")
        }
    }

    fun toggleSaveGig(gig: GigEntity) {
        viewModelScope.launch {
            repository.toggleSaved(gig.id, gig.isSaved)
            val msg = if (!gig.isSaved) "Saved '${gig.title}'" else "Removed from saved"
            _toastEvent.emit(msg)
        }
    }

    // Admin Actions
    fun approveGig(gig: GigEntity) {
        viewModelScope.launch {
            repository.clearFlags(gig.id)
            repository.setStatus(gig.id, "ACTIVE")
            _toastEvent.emit("Approved '${gig.title}'. Flags cleared.")
        }
    }

    fun hideGig(gig: GigEntity) {
        viewModelScope.launch {
            val newStatus = if (gig.status == "HIDDEN") "ACTIVE" else "HIDDEN"
            repository.setStatus(gig.id, newStatus)
            val msg = if (newStatus == "HIDDEN") "Gig hidden from public feed" else "Gig made visible"
            _toastEvent.emit(msg)
        }
    }

    fun deleteGig(gig: GigEntity) {
        viewModelScope.launch {
            repository.deleteGig(gig.id)
            _toastEvent.emit("Gig deleted successfully.")
        }
    }

    // Post a Gig Form State
    private val _postFormState = MutableStateFlow(PostGigFormState())
    val postFormState: StateFlow<PostGigFormState> = _postFormState.asStateFlow()

    fun updatePostForm(
        title: String? = null,
        category: String? = null,
        dateText: String? = null,
        locationName: String? = null,
        posX: Float? = null,
        posY: Float? = null,
        payText: String? = null,
        contactInfo: String? = null,
        description: String? = null
    ) {
        _postFormState.value = _postFormState.value.copy(
            title = title ?: _postFormState.value.title,
            category = category ?: _postFormState.value.category,
            dateText = dateText ?: _postFormState.value.dateText,
            locationName = locationName ?: _postFormState.value.locationName,
            posX = posX ?: _postFormState.value.posX,
            posY = posY ?: _postFormState.value.posY,
            payText = payText ?: _postFormState.value.payText,
            contactInfo = contactInfo ?: _postFormState.value.contactInfo,
            description = description ?: _postFormState.value.description,
            errorMessage = null
        )
    }

    fun submitNewGig(onSuccess: () -> Unit) {
        val form = _postFormState.value
        if (form.title.isBlank()) {
            _postFormState.value = form.copy(errorMessage = "Please enter an opportunity title")
            return
        }
        if (form.contactInfo.isBlank()) {
            _postFormState.value = form.copy(errorMessage = "Please provide contact info (Email/Phone)")
            return
        }

        viewModelScope.launch {
            val newGig = GigEntity(
                title = form.title.trim(),
                category = form.category,
                dateText = if (form.dateText.isNotBlank()) form.dateText else "Upcoming Date",
                locationName = if (form.locationName.isNotBlank()) form.locationName else "Cebu City Area",
                posX = form.posX,
                posY = form.posY,
                payText = if (form.payText.isNotBlank()) form.payText else "Negotiable",
                contactInfo = form.contactInfo.trim(),
                description = form.description.trim(),
                status = "ACTIVE",
                posterName = "Community Musician",
                postedTime = System.currentTimeMillis()
            )
            val newId = repository.insertGig(newGig)
            _postFormState.value = PostGigFormState() // Reset form
            _selectedGig.value = newGig.copy(id = newId)
            _toastEvent.emit("Gig posted successfully! Added to Cebu map.")
            _currentDestination.value = AppNavDestination.EXPLORE_MAP
            onSuccess()
        }
    }
}
