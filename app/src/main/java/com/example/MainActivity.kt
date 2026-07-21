package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import android.Manifest
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.provider.ContactsContract
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.ClientSubscription
import com.example.data.ClientSubscriptionRepository
import com.example.ui.IPTVViewModel
import com.example.ui.IPTVViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ClientSubscriptionRepository(database.clientSubscriptionDao())
        
        // Initialize daily notification scheduling
        val prefsInit = getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE)
        val notifEnabled = prefsInit.getBoolean("daily_notification_enabled", true)
        val notifHour = prefsInit.getInt("daily_notification_hour", 9)
        val notifMinute = prefsInit.getInt("daily_notification_minute", 0)
        NotificationReceiver.scheduleDailyNotification(this, notifHour, notifMinute, notifEnabled)
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE) }
            var currentTheme by remember {
                mutableStateOf(
                    try {
                        AppTheme.valueOf(prefs.getString("selected_theme", AppTheme.DEFAULT.name) ?: AppTheme.DEFAULT.name)
                    } catch (e: Exception) {
                        AppTheme.DEFAULT
                    }
                )
            }
            
            MyApplicationTheme(appTheme = currentTheme) {
                IPTVApp(
                    repository = repository,
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        prefs.edit().putString("selected_theme", newTheme.name).apply()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPTVApp(
    repository: ClientSubscriptionRepository,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val viewModel: IPTVViewModel = viewModel(
        factory = IPTVViewModelFactory(repository)
    )
    
    val context = LocalContext.current
    var onPhonePickedCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    
    // WhatsApp chooser state
    var showWhatsAppChooser by remember { mutableStateOf(false) }
    var selectedPhoneNumber by remember { mutableStateOf("") }
    var whatsAppMessageToSend by remember { mutableStateOf("") }
    var onWhatsAppSentCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    val sendWhatsApp: (String, String, () -> Unit) -> Unit = { phone, msg, onSent ->
        val formattedPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        
        val isWhatsappInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        
        val isWhatsappBusinessInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        
        if (isWhatsappInstalled && isWhatsappBusinessInstalled) {
            selectedPhoneNumber = formattedPhone
            whatsAppMessageToSend = msg
            onWhatsAppSentCallback = onSent
            showWhatsAppChooser = true
        } else if (isWhatsappInstalled) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}")
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
                onSent()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors du lancement de WhatsApp", Toast.LENGTH_SHORT).show()
            }
        } else if (isWhatsappBusinessInstalled) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}")
                setPackage("com.whatsapp.w4b")
            }
            try {
                context.startActivity(intent)
                onSent()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors du lancement de WhatsApp Business", Toast.LENGTH_SHORT).show()
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}")
            }
            try {
                context.startActivity(intent)
                onSent()
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp n'est pas installé. Message copié !", Toast.LENGTH_LONG).show()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("IPTV Message", msg)
                clipboard.setPrimaryClip(clip)
            }
        }
    }
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            contactUri?.let { uri ->
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                try {
                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex >= 0) {
                                val number = cursor.getString(numberIndex)
                                onPhonePickedCallback?.invoke(number)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erreur lors de la récupération du contact", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    val triggerContactPicker: ((String) -> Unit) -> Unit = { callback ->
        onPhonePickedCallback = callback
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }
    
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val toContactSubscriptions by viewModel.toContactSubscriptions.collectAsStateWithLifecycle()
    val expiredSubscriptions by viewModel.expiredSubscriptions.collectAsStateWithLifecycle()
    val contactedSubscriptions by viewModel.contactedSubscriptions.collectAsStateWithLifecycle()
    val noPhoneSubscriptions by viewModel.noPhoneSubscriptions.collectAsStateWithLifecycle()
    val allFilteredSubscriptions by viewModel.allFilteredSubscriptions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf("home") }
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<ClientSubscription?>(null) }
    
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var parsedSubscriptionsToImport by remember { mutableStateOf<List<ClientSubscription>>(emptyList()) }
    var importFileName by remember { mutableStateOf("") }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            val parsedList = parseImportedFile(context, it)
            if (parsedList.isNotEmpty()) {
                importFileName = fileName
                parsedSubscriptionsToImport = parsedList
                showImportConfirmDialog = true
            } else {
                Toast.makeText(context, "Aucun client valide trouvé dans le fichier. Assurez-vous d'utiliser un format CSV ou texte valide.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    val totalCount = subscriptions.size
    val toContactCount = toContactSubscriptions.size
    val expiredCount = expiredSubscriptions.size
    val contactedCount = contactedSubscriptions.size
    val noPhoneCount = noPhoneSubscriptions.size

    val trigger10DaysSubscriptions = remember(subscriptions) {
        subscriptions.filter {
            val days = it.getRemainingDays()
            days == 10 && !it.hasNotified10Days &&
                    !it.status.equals("Expired", ignoreCase = true) &&
                    !it.remainingTimeRaw.equals("Expired", ignoreCase = true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && trigger10DaysSubscriptions.isNotEmpty()) {
            trigger10DaysSubscriptions.forEach { sub ->
                triggerSystemNotificationForSubscription(context, sub)
                viewModel.updateSubscription(sub.copy(hasNotified10Days = true))
            }
        }
    }

    LaunchedEffect(trigger10DaysSubscriptions) {
        if (trigger10DaysSubscriptions.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    trigger10DaysSubscriptions.forEach { sub ->
                        triggerSystemNotificationForSubscription(context, sub)
                        viewModel.updateSubscription(sub.copy(hasNotified10Days = true))
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                trigger10DaysSubscriptions.forEach { sub ->
                    triggerSystemNotificationForSubscription(context, sub)
                    viewModel.updateSubscription(sub.copy(hasNotified10Days = true))
                }
            }
        }
    }

    if (currentScreen == "settings") {
        SettingsScreen(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            onBack = { currentScreen = "home" }
        )
    } else {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IPTV Manager",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Gestion & Expiration d'Abonnements",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setSearchQuery("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Réinitialiser la recherche"
                        )
                    }
                    IconButton(
                        onClick = {
                            currentScreen = "settings"
                        },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search textfield
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Rechercher un client (Login ou Téléphone)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to add new subscriber
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_client_button_top"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nouveau client",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nouveau client",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Button to import subscribers
                OutlinedButton(
                    onClick = { importFileLauncher.launch("*/*") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("import_csv_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Importer (Excel/CSV)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tabs setup
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("À Contacter")
                            if (toContactCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                    Text(toContactCount.toString(), color = Color.White)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_to_contact")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Contactés")
                            if (contactedCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(contactedCount.toString(), color = Color.White)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_contacted")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sans Tél.")
                            if (noPhoneCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                    Text(noPhoneCount.toString(), color = Color.White)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_no_phone")
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Expirés")
                            if (expiredCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(expiredCount.toString(), color = Color.White)
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_expired")
                )
                Tab(
                    selected = selectedTabIndex == 4,
                    onClick = { selectedTabIndex = 4 },
                    text = { Text("Tous") },
                    modifier = Modifier.testTag("tab_all_clients")
                )
            }

            // Display list according to tab
            val currentList = when (selectedTabIndex) {
                0 -> toContactSubscriptions
                1 -> contactedSubscriptions
                2 -> noPhoneSubscriptions
                3 -> expiredSubscriptions
                else -> allFilteredSubscriptions
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedTabIndex) {
                                0 -> Icons.Default.CheckCircle
                                1 -> Icons.Default.Info
                                2 -> Icons.Default.Phone
                                3 -> Icons.Default.Warning
                                else -> Icons.Default.Search
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTabIndex) {
                                0 -> "Aucun client à contacter pour l'instant !"
                                1 -> "Aucun client marqué comme contacté."
                                2 -> "Aucun client classé sans numéro de téléphone."
                                3 -> "Aucun abonnement expiré."
                                else -> "Aucun client trouvé."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (selectedTabIndex == 0 && searchQuery.isEmpty()) {
                            Text(
                                text = "Les abonnements actifs qui expirent entre aujourd'hui (0 jour) et 10 jours apparaîtront ici.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp) // buffer for FAB
                ) {
                    items(currentList, key = { it.id }) { client ->
                        SubscriptionCard(
                            client = client,
                            onMarkContacted = { viewModel.markAsContacted(client) },
                            onMarkUncontacted = { viewModel.markAsUncontacted(client) },
                            onMarkNoPhone = { viewModel.markAsNoPhone(client) },
                            onRemoveNoPhone = { viewModel.removeNoPhoneStatus(client) },
                            onSavePhone = { phone -> viewModel.markAsHasPhone(client, phone) },
                            onEdit = { subscriptionToEdit = client },
                            onDelete = { viewModel.deleteSubscription(client) },
                            onPickContact = triggerContactPicker,
                            onSendWhatsApp = sendWhatsApp,
                            onMarkExpired = { viewModel.markAsExpired(client) }
                        )
                    }
                }
            }
        }

        // Add/Edit dialogues
        if (showImportConfirmDialog) {
            var clearExisting by remember { mutableStateOf(false) }
            
            Dialog(
                onDismissRequest = { showImportConfirmDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = true, onClick = { showImportConfirmDialog = false })
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clickable(enabled = false) {}
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Importer des clients",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Fichier : $importFileName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "${parsedSubscriptionsToImport.size} abonnement(s) client(s) détecté(s).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Aperçu des 5 premiers clients :",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(parsedSubscriptionsToImport.take(5)) { client ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = client.login,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (client.phoneNumber.isNotEmpty()) client.phoneNumber else "Pas de téléphone",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { clearExisting = !clearExisting }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = clearExisting,
                                    onCheckedChange = { clearExisting = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Remplacer l'intégralité des clients existants",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showImportConfirmDialog = false }
                                ) {
                                    Text("Annuler")
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = {
                                        viewModel.importSubscriptions(parsedSubscriptionsToImport, clearExisting)
                                        Toast.makeText(context, "${parsedSubscriptionsToImport.size} client(s) importé(s) avec succès !", Toast.LENGTH_LONG).show()
                                        showImportConfirmDialog = false
                                    }
                                ) {
                                    Text("Confirmer l'import")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditSubscriptionDialog(
                onDismiss = { showAddDialog = false },
                onPickContact = triggerContactPicker,
                onSave = { login, pass, remTime, days, status, phone ->
                    viewModel.addSubscription(login, pass, remTime, days, status, phone)
                    showAddDialog = false
                }
            )
        }

        subscriptionToEdit?.let { client ->
            AddEditSubscriptionDialog(
                subscription = client,
                onDismiss = { subscriptionToEdit = null },
                onPickContact = triggerContactPicker,
                onSave = { login, pass, remTime, days, status, phone ->
                    viewModel.updateSubscription(
                        client.copy(
                            login = login,
                            password = pass,
                            remainingTimeRaw = remTime,
                            daysRaw = days,
                            status = status,
                            phoneNumber = phone,
                            hasNotified10Days = false
                        )
                    )
                    subscriptionToEdit = null
                }
            )
        }

        if (showWhatsAppChooser) {
            AlertDialog(
                onDismissRequest = { showWhatsAppChooser = false },
                title = {
                    Text(
                        text = "Choisir WhatsApp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Sélectionnez l'application à utiliser :",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Button(
                            onClick = {
                                showWhatsAppChooser = false
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$selectedPhoneNumber&text=${Uri.encode(whatsAppMessageToSend)}")
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(intent)
                                    onWhatsAppSentCallback?.invoke()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur lors du lancement de WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp Personnel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showWhatsAppChooser = false
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$selectedPhoneNumber&text=${Uri.encode(whatsAppMessageToSend)}")
                                    setPackage("com.whatsapp.w4b")
                                }
                                try {
                                    context.startActivity(intent)
                                    onWhatsAppSentCallback?.invoke()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur lors du lancement de WhatsApp Business", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF075E54),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp Business", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showWhatsAppChooser = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}
}

@Composable
fun StatsPanel(
    toContactCount: Int,
    expiredCount: Int,
    contactedCount: Int,
    noPhoneCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "À Contacter",
            count = toContactCount.toString(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Expirés",
            count = expiredCount.toString(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Contactés",
            count = contactedCount.toString(),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Sans Tél.",
            count = noPhoneCount.toString(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Total",
            count = totalCount.toString(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubscriptionCard(
    client: ClientSubscription,
    onMarkContacted: () -> Unit,
    onMarkUncontacted: () -> Unit,
    onMarkNoPhone: () -> Unit,
    onRemoveNoPhone: () -> Unit,
    onSavePhone: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPickContact: ((String) -> Unit) -> Unit,
    onSendWhatsApp: (String, String, () -> Unit) -> Unit,
    onMarkExpired: () -> Unit
) {
    val context = LocalContext.current
    val daysRemaining = client.getRemainingDays()
    
    // Determine card background and border colors based on expiration urgency
    val isExpired = daysRemaining < 0 || client.status.equals("Expired", ignoreCase = true)
    val isExpiringSoon = daysRemaining in 0..10
    
    // Check if subscription needs to be contacted and is still active (daysRemaining >= 0)
    val isToContactAndActive = !client.isContacted && 
                               !client.noPhoneFound && 
                               !isExpired && 
                               daysRemaining <= 10

    val prefs = remember { context.getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE) }
    val themeName = prefs.getString("selected_theme", "DEFAULT") ?: "DEFAULT"
    val isDarkTheme = when (themeName) {
        "COSMIC_DARK" -> true
        "GOLDEN_TWILIGHT" -> true
        "DEFAULT" -> isSystemInDarkTheme()
        else -> false
    }
    // High-contrast green depending on theme
    val greenColor = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)

    val urgencyBorderColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        isExpiringSoon -> Color(0xFFE65100) // Deep Orange
        else -> Color.Transparent
    }
    
    val remainingText = when {
        isExpired -> "EXPIRÉ"
        daysRemaining == 0 -> "Expire aujourd'hui"
        daysRemaining == 1 -> "Expire demain"
        daysRemaining == -999 -> "Expiré"
        else -> "Expire dans $daysRemaining jours"
    }

    val remainingColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        isExpiringSoon -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }

    var isExpanded by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf(client.phoneNumber) }
    var isEditingPhone by remember { mutableStateOf(client.phoneNumber.isEmpty()) }

    // Message formats
    val whatsAppMessage = "chère cliente, votre abonnement IPTV expire le ${client.remainingTimeRaw}."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("client_card_${client.login}"),
        shape = RoundedCornerShape(12.dp),
        border = if (urgencyBorderColor != Color.Transparent) {
            BorderStroke(1.5.dp, urgencyBorderColor)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Main Compact Header (Always Visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Username (Login)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Quick status indicator dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                when {
                                    isToContactAndActive -> greenColor
                                    isExpired -> MaterialTheme.colorScheme.error
                                    isExpiringSoon -> Color(0xFFE65100)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = client.login,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isToContactAndActive) greenColor else Color.Unspecified,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Renommer l'utilisateur",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Expiration (in Red) and Expand Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = client.remainingTimeRaw,
                        color = Color.Red, // Expiration date in Red as requested
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Réduire" else "Développer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded Details Section
            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Remaining days / urgent text block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = remainingText,
                        color = remainingColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Status badges
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (client.isContacted) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Contacté", style = MaterialTheme.typography.bodySmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        } else if (client.noPhoneFound) {
                            SuggestionChip(
                                onClick = onRemoveNoPhone,
                                label = { Text("Sans Tél.", style = MaterialTheme.typography.bodySmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Password row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mot de passe: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = client.password,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("IPTV Password", client.password)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Mot de passe copié !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier mot de passe", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Phone number edit or save block
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (isEditingPhone) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    placeholder = { Text("Ex: 212600000000") },
                                    label = { Text("Téléphone") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f).testTag("phone_input_${client.login}"),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            onPickContact { pickedNumber ->
                                                // Clean up and format picked phone number if needed, or set directly
                                                phoneInput = pickedNumber.replace(" ", "").replace("-", "")
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ContactPhone,
                                                contentDescription = "Importer de Contacts",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        if (phoneInput.isNotEmpty()) {
                                            onSavePhone(phoneInput)
                                            isEditingPhone = false
                                            onSendWhatsApp(phoneInput, whatsAppMessage) {
                                                onMarkContacted()
                                            }
                                        } else {
                                            Toast.makeText(context, "Veuillez entrer un numéro", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("save_phone_button_${client.login}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Enregistrer", tint = MaterialTheme.colorScheme.primary)
                                }
                                if (client.phoneNumber.isNotEmpty()) {
                                    IconButton(onClick = { isEditingPhone = false }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Annuler")
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = client.phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { isEditingPhone = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row (WhatsApp, SMS, Marquer Contacté, etc.)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary actions: Edit & Delete
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_client_button_${client.login}").size(36.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier client", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_client_button_${client.login}").size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer client", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Primary triggers: WhatsApp & Contact states
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Sans numéro" quick tag if phone is empty
                        if (client.phoneNumber.isEmpty() && !client.noPhoneFound) {
                            Button(
                                onClick = onMarkNoPhone,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("nophone_button_${client.login}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("No Tél.", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Option to reclassify clients without phone back with phone clients
                        if (client.noPhoneFound) {
                            Button(
                                onClick = onRemoveNoPhone,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("remove_nophone_button_${client.login}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reclasser avec Tél.", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Contact button if phone is present
                        if (client.phoneNumber.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onSendWhatsApp(client.phoneNumber, whatsAppMessage) {
                                        onMarkContacted()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366) // WhatsApp Green
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("whatsapp_button_${client.login}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }

                        // Done/Not done toggle
                        if (client.isContacted) {
                            OutlinedButton(
                                onClick = onMarkUncontacted,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Relancer", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (client.phoneNumber.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onMarkContacted,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("contacted_button_${client.login}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Contacté", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Expired button to manually mark as expired
                        if (!client.status.equals("Expired", ignoreCase = true)) {
                            OutlinedButton(
                                onClick = onMarkExpired,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("expired_button_${client.login}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Expiré", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionDialog(
    subscription: ClientSubscription? = null,
    onDismiss: () -> Unit,
    onPickContact: ((String) -> Unit) -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var login by remember { mutableStateOf(subscription?.login ?: "") }
    var password by remember { mutableStateOf(subscription?.password ?: "") }
    var expirationDate by remember { mutableStateOf(subscription?.remainingTimeRaw ?: "") }
    var phone by remember { mutableStateOf(subscription?.phoneNumber ?: "") }
    var status by remember { mutableStateOf(subscription?.status ?: "Activated") }

    val isEdit = subscription != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = true, onClick = onDismiss)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .imePadding()
                    .systemBarsPadding()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Text(
                        text = if (isEdit) "Modifier l'Abonnement" else "Ajouter un Abonnement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        label = { Text("Nom d'utilisateur / Login Client") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Expiration inputs & quick buttons
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = expirationDate,
                            onValueChange = { expirationDate = it },
                            label = { Text("Expiration (Format: M/d/yy H:mm)") },
                            placeholder = { Text("Ex: 3/19/27 14:46") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Quick duration buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sdf = SimpleDateFormat("M/d/yy H:mm", Locale.US)
                            
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, 30)
                                    expirationDate = sdf.format(cal.time)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+30 jours", style = MaterialTheme.typography.bodySmall)
                            }
                            
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.YEAR, 1)
                                    expirationDate = sdf.format(cal.time)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+12 mois", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone (Ex: 212600000000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                onPickContact { pickedNumber ->
                                    phone = pickedNumber.replace(" ", "").replace("-", "")
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = "Importer de Contacts",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Status choice row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Statut d'abonnement :", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = status == "Activated",
                                onClick = { status = "Activated" },
                                label = { Text("Activé") }
                            )
                            FilterChip(
                                selected = status == "Expired",
                                onClick = { status = "Expired" },
                                label = { Text("Expiré") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons placed inside the scrollable column to prevent cutoffs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Annuler")
                        }
                        Button(
                            onClick = {
                                if (login.isEmpty() || expirationDate.isEmpty()) {
                                    // Show quick warning
                                } else {
                                    val daysString = if (status == "Expired") {
                                        "Expired"
                                    } else {
                                        subscription?.daysRaw ?: "30 days"
                                    }
                                    onSave(login, password, expirationDate, daysString, status, phone)
                                }
                            }
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    Text("Bonjour $name !")
}

fun triggerSystemNotificationForSubscription(context: Context, subscription: ClientSubscription) {
    val channelId = "iptv_expiry_channel"
    val channelName = "Échéances IPTV"
    val notificationId = subscription.id // Unique ID per subscription

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications d'expiration d'abonnements IPTV"
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Create an intent to open MainActivity when clicked
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = android.app.PendingIntent.getActivity(
        context,
        subscription.id,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Échéance IPTV dans 10 jours !")
        .setContentText("L'abonnement de ${subscription.login} expire le ${subscription.remainingTimeRaw}.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    try {
        notificationManager.notify(notificationId, builder.build())
    } catch (e: SecurityException) {
        // Permission might not be granted
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "Fichier inconnu"
}

fun parseImportedFile(context: Context, uri: Uri): List<ClientSubscription> {
    val result = mutableListOf<ClientSubscription>()
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = mutableListOf<String>()
        var line: String? = reader.readLine()
        while (line != null) {
            lines.add(line!!)
            line = reader.readLine()
        }
        reader.close()
        inputStream.close()

        if (lines.isEmpty()) return emptyList()

        // Detect separator: count occurrences of ',' and ';' in the first 5 lines
        var commaCount = 0
        var semicolonCount = 0
        for (i in 0 until minOf(5, lines.size)) {
            commaCount += lines[i].count { it == ',' }
            semicolonCount += lines[i].count { it == ';' }
        }
        val separator = if (semicolonCount > commaCount) ";" else ","

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue
            
            val parts = trimmed.split(separator).map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 2) {
                val login = parts[0]
                val password = parts[1]
                
                // If this is a header line, skip it
                if (login.equals("login", ignoreCase = true) || login.equals("identifiant", ignoreCase = true) || login.equals("nom", ignoreCase = true) || login.equals("username", ignoreCase = true)) {
                    continue
                }

                val remainingTime = if (parts.size >= 3 && parts[2].isNotEmpty()) parts[2] else "30 days"
                val days = if (parts.size >= 4 && parts[3].isNotEmpty()) parts[3] else "30 days"
                val status = if (parts.size >= 5 && parts[4].isNotEmpty()) parts[4] else "Activated"
                val expirationDays = if (parts.size >= 6 && parts[5].isNotEmpty()) parts[5] else ""
                val phone = if (parts.size >= 7 && parts[6].isNotEmpty()) parts[6] else {
                    var foundPhone = ""
                    for (p in parts.drop(2)) {
                        if (p.startsWith("+") || (p.length in 8..15 && p.all { it.isDigit() || it == ' ' || it == '-' })) {
                            foundPhone = p.replace(" ", "").replace("-", "")
                            break
                        }
                    }
                    foundPhone
                }

                result.add(
                    ClientSubscription(
                        login = login,
                        password = password,
                        remainingTimeRaw = remainingTime,
                        daysRaw = days,
                        status = status,
                        expirationDaysRaw = expirationDays.replace(" days", "").trim(),
                        phoneNumber = phone
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE) }
    
    var dailyNotifEnabled by remember {
        mutableStateOf(prefs.getBoolean("daily_notification_enabled", true))
    }
    var dailyNotifHour by remember {
        mutableStateOf(prefs.getInt("daily_notification_hour", 9))
    }
    var dailyNotifMinute by remember {
        mutableStateOf(prefs.getInt("daily_notification_minute", 0))
    }

    val updateNotificationSettings = { enabled: Boolean, hour: Int, minute: Int ->
        dailyNotifEnabled = enabled
        dailyNotifHour = hour
        dailyNotifMinute = minute
        
        prefs.edit().apply {
            putBoolean("daily_notification_enabled", enabled)
            putInt("daily_notification_hour", hour)
            putInt("daily_notification_minute", minute)
            apply()
        }
        
        NotificationReceiver.scheduleDailyNotification(context, hour, minute, enabled)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card with a greeting and description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personnalisation Visuelle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choisissez l'un de nos thèmes premium pour adapter l'application à vos préférences ou à votre environnement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Thèmes disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Theme options list
            AppTheme.values().forEach { theme ->
                val isSelected = currentTheme == theme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(theme) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onThemeChange(theme) },
                                modifier = Modifier.testTag("theme_radio_${theme.name}")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (theme.isDark) "Mode Sombre" else "Mode Clair",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Theme colors preview dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colors = when (theme) {
                                AppTheme.DEFAULT -> listOf(Color(0xFF6650A4), Color(0xFF625B71), Color(0xFFEADBFF))
                                AppTheme.COSMIC_DARK -> listOf(Color(0xFFB09FFF), Color(0xFF82B1FF), Color(0xFF0F0D1E))
                                AppTheme.MODERN_LIGHT -> listOf(Color(0xFF005FAF), Color(0xFF006874), Color(0xFFF8F9FA))
                                AppTheme.GOLDEN_TWILIGHT -> listOf(Color(0xFFFFB74D), Color(0xFFFFD54F), Color(0xFF1A1412))
                                AppTheme.ZEN_FOREST -> listOf(Color(0xFF2E7D32), Color(0xFF689F38), Color(0xFFF1F8E9))
                                AppTheme.OCEAN_BLUE -> listOf(Color(0xFF0288D1), Color(0xFF0097A7), Color(0xFFE1F5FE))
                            }
                            
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                        .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notifications de Rappel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Rappel quotidien",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Résumer les clients à relancer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = dailyNotifEnabled,
                            onCheckedChange = { isChecked ->
                                updateNotificationSettings(isChecked, dailyNotifHour, dailyNotifMinute)
                            },
                            modifier = Modifier.testTag("daily_notif_switch")
                        )
                    }

                    if (dailyNotifEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Heure de notification",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Hour selection
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = {
                                            val newHour = if (dailyNotifHour == 23) 0 else dailyNotifHour + 1
                                            updateNotificationSettings(dailyNotifEnabled, newHour, dailyNotifMinute)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Augmenter l'heure")
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d", dailyNotifHour),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val newHour = if (dailyNotifHour == 0) 23 else dailyNotifHour - 1
                                            updateNotificationSettings(dailyNotifEnabled, newHour, dailyNotifMinute)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuer l'heure")
                                    }
                                }

                                Text(
                                    text = ":",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Minute selection
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = {
                                            val newMinute = if (dailyNotifMinute >= 59) 0 else dailyNotifMinute + 1
                                            updateNotificationSettings(dailyNotifEnabled, dailyNotifHour, newMinute)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Augmenter les minutes")
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d", dailyNotifMinute),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val newMinute = if (dailyNotifMinute <= 0) 59 else dailyNotifMinute - 1
                                            updateNotificationSettings(dailyNotifEnabled, dailyNotifHour, newMinute)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuer les minutes")
                                    }
                                }
                            }
                            
                            Text(
                                text = "La notification sera déclenchée tous les jours à ${String.format("%02d:%02d", dailyNotifHour, dailyNotifMinute)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Information and credits section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "À Propos de l'Application",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "IPTV Manager v1.2.0\nCette application intelligente vous permet de centraliser, suivre et gérer efficacement les abonnements de vos clients, d'anticiper les expirations et d'automatiser les relances via WhatsApp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


