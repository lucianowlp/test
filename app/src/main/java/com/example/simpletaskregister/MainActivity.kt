package lucianowlp.com.simplestodo

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TaskRegisterApp()
            }
        }
    }
}

enum class AppScreen {
    HOME,
    REPORT,
    SETTINGS
}

data class TaskItem(
    val id: String,
    val title: String,
    val dueDate: LocalDate,
    val completed: Boolean,
    val completedDate: LocalDate?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRegisterApp() {
    val context = LocalContext.current
    val initialTasks = remember { TaskStorage.loadTasks(context) }
    val tasks = remember { mutableStateListOf<TaskItem>().apply { addAll(initialTasks) } }

    var currentScreenName by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }
    val currentScreen = AppScreen.valueOf(currentScreenName)
    var showCompletedOnly by rememberSaveable { mutableStateOf(false) }
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }

    val initialReminder = remember { ReminderScheduler.getReminderTime(context) }
    var reminderHour by rememberSaveable { mutableStateOf(initialReminder.first) }
    var reminderMinute by rememberSaveable { mutableStateOf(initialReminder.second) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val outputFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        ReminderScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
    }

    val pendingTasks = tasks.filter { !it.completed }.sortedByDescending { it.dueDate }
    val completedTasks = tasks
        .filter { it.completed }
        .sortedByDescending { it.completedDate ?: LocalDate.MIN }
    val visibleTasks = if (showCompletedOnly) completedTasks else pendingTasks

    val total = tasks.size
    val done = tasks.count { it.completed }
    val pending = total - done
    val overdue = tasks.count { !it.completed && it.dueDate.isBefore(today) }

    fun persistTasks() {
        TaskStorage.saveTasks(context, tasks.toList())
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Simple Todo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Navegação",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NavigationDrawerItem(
                            label = { Text("Início") },
                            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = null) },
                            selected = currentScreen == AppScreen.HOME,
                            onClick = {
                                currentScreenName = AppScreen.HOME.name
                                drawerScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Relatório") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null
                                )
                            },
                            selected = currentScreen == AppScreen.REPORT,
                            onClick = {
                                currentScreenName = AppScreen.REPORT.name
                                drawerScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Configurações") },
                            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
                            selected = currentScreen == AppScreen.SETTINGS,
                            onClick = {
                                currentScreenName = AppScreen.SETTINGS.name
                                drawerScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        AppScreen.HOME -> "Tarefas"
                                        AppScreen.REPORT -> "Relatório"
                                        AppScreen.SETTINGS -> "Configurações"
                                    }
                                )
                            },
                            actions = {
                                IconButton(onClick = { drawerScope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Abrir menu"
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (currentScreen == AppScreen.HOME) {
                            ExtendedFloatingActionButton(
                                text = { Text("Nova tarefa") },
                                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                                onClick = { showAddTaskDialog = true }
                            )
                        }
                    }
                ) { paddingValues ->
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                tasks = visibleTasks,
                                showCompletedOnly = showCompletedOnly,
                                onShowCompletedChange = { showCompletedOnly = it },
                                outputFormatter = outputFormatter,
                                onCheckedChange = { task, checked ->
                                    val index = tasks.indexOfFirst { it.id == task.id }
                                    if (index >= 0) {
                                        tasks[index] = task.copy(
                                            completed = checked,
                                            completedDate = if (checked) LocalDate.now() else null
                                        )
                                        persistTasks()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(16.dp)
                            )
                        }

                        AppScreen.REPORT -> {
                            ReportScreen(
                                total = total,
                                done = done,
                                pending = pending,
                                overdue = overdue,
                                completedTasks = completedTasks,
                                outputFormatter = outputFormatter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(16.dp)
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                reminderHour = reminderHour,
                                reminderMinute = reminderMinute,
                                onPickTime = { hour, minute ->
                                    reminderHour = hour
                                    reminderMinute = minute
                                },
                                onSave = {
                                    ReminderScheduler.saveReminderTime(
                                        context = context,
                                        hour = reminderHour,
                                        minute = reminderMinute
                                    )
                                    ReminderScheduler.scheduleDailyReminder(
                                        context = context,
                                        hour = reminderHour,
                                        minute = reminderMinute
                                    )
                                },
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onCreateTask = { title, dueDate ->
                tasks.add(
                    TaskItem(
                        id = "${System.currentTimeMillis()}-${tasks.size}",
                        title = title,
                        dueDate = dueDate,
                        completed = false,
                        completedDate = null
                    )
                )
                persistTasks()
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun HomeScreen(
    tasks: List<TaskItem>,
    showCompletedOnly: Boolean,
    onShowCompletedChange: (Boolean) -> Unit,
    outputFormatter: DateTimeFormatter,
    onCheckedChange: (TaskItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Filtro de tarefas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (showCompletedOnly) "Exibindo concluídas" else "Exibindo pendentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Concluídas")
                    Checkbox(
                        checked = showCompletedOnly,
                        onCheckedChange = onShowCompletedChange
                    )
                }
            }
        }

        Text(
            text = if (showCompletedOnly) "Tarefas concluídas" else "Tarefas pendentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (tasks.isEmpty()) {
            Text(
                text = "Nenhuma tarefa para o filtro selecionado.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        outputFormatter = outputFormatter,
                        onCheckedChange = { checked -> onCheckedChange(task, checked) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportScreen(
    total: Int,
    done: Int,
    pending: Int,
    overdue: Int,
    completedTasks: List<TaskItem>,
    outputFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Resumo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Total: $total")
                Text("Concluídas: $done")
                Text("Pendentes: $pending")
                Text("Vencidas: $overdue")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Concluídas mais recentes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (completedTasks.isEmpty()) {
                    Text("Ainda não há tarefas concluídas.")
                } else {
                    completedTasks.take(5).forEach { task ->
                        val completedText = task.completedDate?.format(outputFormatter) ?: "-"
                        Text("• ${task.title} (concluída em: $completedText)")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    reminderHour: Int,
    reminderMinute: Int,
    onPickTime: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reminderText = String.format("%02d:%02d", reminderHour, reminderMinute)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Lembrete diário",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Horário atual: $reminderText")

                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> onPickTime(hour, minute) },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    }
                ) {
                    Text("Escolher horário")
                }

                Text(
                    text = "O app envia lembrete das tarefas pendentes para o dia atual.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(onClick = onRequestNotificationPermission) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Permitir notificações")
                    }
                }

                Button(onClick = onSave) {
                    Text("Salvar configurações")
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskItem,
    outputFormatter: DateTimeFormatter,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (task.completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Data limite: ${task.dueDate.format(outputFormatter)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = onCheckedChange
                )
            }

            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (task.completed) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Filled.DateRange
                    },
                    contentDescription = null,
                    tint = if (task.completed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = if (task.completed && task.completedDate != null) {
                        "Concluída em: ${task.completedDate.format(outputFormatter)}"
                    } else {
                        "Status: não concluída"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (String, LocalDate) -> Unit
) {
    val context = LocalContext.current
    val outputFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var formError by remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar nova tarefa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descrição da tarefa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = selectedDate.format(outputFormatter),
                    onValueChange = {},
                    label = { Text("Data limite") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDatePicker() },
                    trailingIcon = {
                        IconButton(onClick = { openDatePicker() }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Selecionar data"
                            )
                        }
                    }
                )
                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        formError = "Informe uma descrição para a tarefa."
                    } else {
                        onCreateTask(title.trim(), selectedDate)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
