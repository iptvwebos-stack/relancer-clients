package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClientSubscription
import com.example.data.ClientSubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IPTVViewModel(private val repository: ClientSubscriptionRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    val subscriptions: StateFlow<List<ClientSubscription>> = repository.allSubscriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Filtered lists for the tabs
    val toContactSubscriptions: StateFlow<List<ClientSubscription>> = subscriptions
        .combine(searchQuery) { list, query ->
            list.filter {
                !it.isContacted && !it.noPhoneFound &&
                !it.status.equals("Expired", ignoreCase = true) &&
                !it.remainingTimeRaw.equals("Expired", ignoreCase = true) &&
                it.getRemainingDays() <= 10 &&
                (query.isEmpty() || it.login.contains(query, ignoreCase = true) || it.phoneNumber.contains(query))
            }.sortedWith(compareBy<ClientSubscription> { it.getRemainingDays() }.thenBy { it.login })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiredSubscriptions: StateFlow<List<ClientSubscription>> = subscriptions
        .combine(searchQuery) { list, query ->
            list.filter {
                (it.status.equals("Expired", ignoreCase = true) || it.remainingTimeRaw.equals("Expired", ignoreCase = true)) &&
                (query.isEmpty() || it.login.contains(query, ignoreCase = true) || it.phoneNumber.contains(query))
            }.sortedWith(compareBy<ClientSubscription> { it.getRemainingDays() }.thenBy { it.login })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactedSubscriptions: StateFlow<List<ClientSubscription>> = subscriptions
        .combine(searchQuery) { list, query ->
            list.filter {
                it.isContacted &&
                (query.isEmpty() || it.login.contains(query, ignoreCase = true) || it.phoneNumber.contains(query))
            }.sortedByDescending { it.contactDate ?: 0L }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noPhoneSubscriptions: StateFlow<List<ClientSubscription>> = subscriptions
        .combine(searchQuery) { list, query ->
            list.filter {
                it.noPhoneFound &&
                (query.isEmpty() || it.login.contains(query, ignoreCase = true) || it.phoneNumber.contains(query))
            }.sortedWith(compareBy<ClientSubscription> { it.getRemainingDays() }.thenBy { it.login })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFilteredSubscriptions: StateFlow<List<ClientSubscription>> = subscriptions
        .combine(searchQuery) { list, query ->
            if (query.isEmpty()) {
                list.sortedBy { it.login }
            } else {
                list.filter {
                    it.login.contains(query, ignoreCase = true) ||
                    it.password.contains(query, ignoreCase = true) ||
                    it.phoneNumber.contains(query)
                }.sortedBy { it.login }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSubscription(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(subscription)
        }
    }

    fun markAsContacted(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    isContacted = true,
                    noPhoneFound = false,
                    contactDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun markAsUncontacted(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    isContacted = false,
                    contactDate = null
                )
            )
        }
    }

    fun markAsNoPhone(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    noPhoneFound = true,
                    isContacted = false
                )
            )
        }
    }

    fun removeNoPhoneStatus(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    noPhoneFound = false
                )
            )
        }
    }

    fun markAsHasPhone(subscription: ClientSubscription, phoneNumber: String) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    phoneNumber = phoneNumber,
                    noPhoneFound = false
                )
            )
        }
    }

    fun deleteSubscription(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.delete(subscription)
        }
    }

    fun markAsExpired(subscription: ClientSubscription) {
        viewModelScope.launch {
            repository.update(
                subscription.copy(
                    status = "Expired"
                )
            )
        }
    }

    fun importSubscriptions(subscriptionsList: List<ClientSubscription>, clearExisting: Boolean) {
        viewModelScope.launch {
            if (clearExisting) {
                repository.deleteAll()
            }
            repository.insertAll(subscriptionsList)
        }
    }

    fun addSubscription(login: String, pass: String, remainingTime: String, days: String, status: String, phone: String = "") {
        viewModelScope.launch {
            repository.insert(
                ClientSubscription(
                    login = login,
                    password = pass,
                    remainingTimeRaw = remainingTime,
                    daysRaw = days,
                    status = status,
                    expirationDaysRaw = days.replace(" days", "").trim(),
                    phoneNumber = phone
                )
            )
        }
    }
}

class IPTVViewModelFactory(private val repository: ClientSubscriptionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IPTVViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IPTVViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
