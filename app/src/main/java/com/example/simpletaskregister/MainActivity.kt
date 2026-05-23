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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TaskRegisterScreen()
            }
        }
    }
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
fun TaskRegisterScreen() {
    val tasks = remember { mutableStateListOf<TaskItem>() }
    val today = LocalDate.now()
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var taskTitle by rememberSaveable { mutableStateOf("") }
    var dueDateText by rememberSaveable { mutableStateOf(today.format(inputFormatter)) }
    var formError by remember { mutableStateOf<String?>(null) }

    val total = tasks.size
    val done = tasks.count { it.completed }
    val pending = total - done
    val overdue = tasks.count { !it.completed && it.dueDate.isBefore(today) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Registro de Tarefas") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportCard(
                total = total,
                done = done,
                pending = pending,
                overdue = overdue
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nova tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Descrição da tarefa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dueDateText,
                        onValueChange = { dueDateText = it },
                        label = { Text("Data limite (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (formError != null) {
                        Text(
                            text = formError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            val parsedDate = runCatching {
                                LocalDate.parse(dueDateText, inputFormatter)
                            }.getOrNull()

                            when {
                                taskTitle.isBlank() -> {
                                    formError = "Informe uma descrição para a tarefa."
                                }

                                parsedDate == null -> {
                                    formError = "Data inválida. Use o formato yyyy-MM-dd."
                                }

                                else -> {
                                    tasks.add(
                                        TaskItem(
                                            id = "${System.currentTimeMillis()}-${tasks.size}",
                                            title = taskTitle.trim(),
                                            dueDate = parsedDate,
                                            completed = false,
                                            completedDate = null
                                        )
                                    )
                                    taskTitle = ""
                                    dueDateText = today.format(inputFormatter)
                                    formError = null
                                }
                            }
                        }
                    ) {
                        Text("Cadastrar tarefa")
                    }
                }
            }

            Text(
                text = "Tarefas cadastradas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (tasks.isEmpty()) {
                Text(
                    text = "Nenhuma tarefa cadastrada ainda.",
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
                            onCheckedChange = { checked ->
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) {
                                    tasks[index] = task.copy(
                                        completed = checked,
                                        completedDate = if (checked) LocalDate.now() else null
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    total: Int,
    done: Int,
    pending: Int,
    overdue: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Relatório simples",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Total: $total")
            Text("Concluídas: $done")
            Text("Pendentes: $pending")
            Text("Vencidas: $overdue")
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
            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = onCheckedChange
                )
                Spacer(modifier = Modifier.width(8.dp))
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
