package lucianowlp.com.simplestodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.rememberDrawerState
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
    REPORT
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
    val tasks = remember { mutableStateListOf<TaskItem>() }
    var currentScreenName by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }
    val currentScreen = AppScreen.valueOf(currentScreenName)
    var showCompletedOnly by rememberSaveable { mutableStateOf(false) }
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val pendingTasks = tasks
        .filter { !it.completed }
        .sortedByDescending { it.dueDate }
    val completedTasks = tasks
        .filter { it.completed }
        .sortedByDescending { it.completedDate ?: LocalDate.MIN }
    val visibleTasks = if (showCompletedOnly) completedTasks else pendingTasks

    val total = tasks.size
    val done = tasks.count { it.completed }
    val pending = total - done
    val overdue = tasks.count { !it.completed && it.dueDate.isBefore(today) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Início") },
                            selected = currentScreen == AppScreen.HOME,
                            onClick = {
                                currentScreenName = AppScreen.HOME.name
                                drawerScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Relatório") },
                            selected = currentScreen == AppScreen.REPORT,
                            onClick = {
                                currentScreenName = AppScreen.REPORT.name
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
                                    if (currentScreen == AppScreen.HOME) {
                                        "Tarefas"
                                    } else {
                                        "Relatório"
                                    }
                                )
                            },
                            actions = {
                                TextButton(
                                    onClick = { drawerScope.launch { drawerState.open() } }
                                ) {
                                    Text("Menu")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (currentScreen == AppScreen.HOME) {
                            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                                Text("+")
                            }
                        }
                    }
                ) { paddingValues ->
                    if (currentScreen == AppScreen.HOME) {
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
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                        )
                    } else {
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
        Card(modifier = Modifier.fillMaxWidth()) {
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
                        text = if (showCompletedOnly) {
                            "Exibindo concluídas"
                        } else {
                            "Exibindo pendentes"
                        },
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
            text = if (showCompletedOnly) {
                "Tarefas concluídas"
            } else {
                "Tarefas pendentes"
            },
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
                modifier = Modifier.fillMaxWidth(),
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
        Card(modifier = Modifier.fillMaxWidth()) {
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

        Card(modifier = Modifier.fillMaxWidth()) {
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
fun TaskRow(
    task: TaskItem,
    outputFormatter: DateTimeFormatter,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (String, LocalDate) -> Unit
) {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    var title by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf(LocalDate.now().format(inputFormatter)) }
    var formError by remember { mutableStateOf<String?>(null) }

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
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text("Data limite (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    val parsedDate = runCatching {
                        LocalDate.parse(dueDateText, inputFormatter)
                    }.getOrNull()

                    when {
                        title.isBlank() -> formError = "Informe uma descrição para a tarefa."
                        parsedDate == null -> formError = "Data inválida. Use o formato yyyy-MM-dd."
                        else -> onCreateTask(title.trim(), parsedDate)
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
