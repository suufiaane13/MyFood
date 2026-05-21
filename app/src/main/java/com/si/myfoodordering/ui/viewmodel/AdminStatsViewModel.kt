package com.si.myfoodordering.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.myfoodordering.data.model.PlatPopularite
import com.si.myfoodordering.data.repository.AvisRepository
import com.si.myfoodordering.data.repository.OrderRepository
import com.si.myfoodordering.data.repository.PlatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PopulariteSort { PAR_COMMANDES, PAR_NOTE }

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val avisRepository: AvisRepository,
    private val platRepository: PlatRepository,
) : ViewModel() {

    private val _topPlats = MutableStateFlow<List<PlatPopularite>>(emptyList())
    val topPlats: StateFlow<List<PlatPopularite>> = _topPlats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sort = MutableStateFlow(PopulariteSort.PAR_COMMANDES)
    val sort: StateFlow<PopulariteSort> = _sort.asStateFlow()

    private var rawData: List<PlatPopularite> = emptyList()

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val platsDeferred = async { platRepository.loadMenu().first }
                val orderItemsDeferred = async { orderRepository.getAllOrderItems() }
                val avisDeferred = async { avisRepository.getAllAvis() }

                val plats = platsDeferred.await()
                val orderItems = orderItemsDeferred.await()
                val avis = avisDeferred.await()

                val orderCountByPlat = orderItems
                    .groupBy { it.plat_id }
                    .mapValues { (_, items) -> items.sumOf { it.quantite } }

                val avisByPlat = avis
                    .groupBy { it.plat_id }
                    .mapValues { (_, list) ->
                        val avg = list.map { it.note }.average()
                        Pair(if (avg.isNaN()) 0.0 else avg, list.size)
                    }

                rawData = plats.map { plat ->
                    val id = plat.id ?: 0
                    PlatPopularite(
                        plat = plat,
                        nbCommandes = orderCountByPlat[id] ?: 0,
                        noteMoyenne = avisByPlat[id]?.first ?: 0.0,
                        nbAvis = avisByPlat[id]?.second ?: 0
                    )
                }.filter { it.nbCommandes > 0 || it.noteMoyenne > 0 }

                applySort()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSort(sort: PopulariteSort) {
        _sort.value = sort
        applySort()
    }

    private fun applySort() {
        _topPlats.value = when (_sort.value) {
            PopulariteSort.PAR_COMMANDES -> rawData.sortedByDescending { it.nbCommandes }
            PopulariteSort.PAR_NOTE      -> rawData.sortedByDescending { it.noteMoyenne }
        }.take(5)
    }
}
