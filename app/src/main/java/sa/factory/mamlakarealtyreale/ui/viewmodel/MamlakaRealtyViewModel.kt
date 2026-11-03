package sa.factory.mamlakarealtyreale.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.mamlakarealtyreale.domain.usecase.*
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyItem
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyOrder
import sa.factory.mamlakarealtyreale.model.MamlakaRealtyTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MamlakaRealtyUiState(
    val catalog: List<MamlakaRealtyItem> = emptyList(),
    val filteredCatalog: List<MamlakaRealtyItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<MamlakaRealtyItem> = emptyList(),
    val telemetry: MamlakaRealtyTelemetry? = null,
    val lastOrder: MamlakaRealtyOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class MamlakaRealtyViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MamlakaRealtyUiState())
    val uiState: StateFlow<MamlakaRealtyUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: MamlakaRealtyItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: MamlakaRealtyItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
