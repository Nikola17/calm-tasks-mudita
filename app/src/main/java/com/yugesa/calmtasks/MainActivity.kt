package com.yugesa.calmtasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yugesa.calmtasks.data.FolderEntity
import com.yugesa.calmtasks.data.TaskEntity
import com.yugesa.calmtasks.domain.CalmDateTimeLabels
import com.yugesa.calmtasks.domain.ReminderTimes
import com.yugesa.calmtasks.domain.TodayPlanner
import com.yugesa.calmtasks.reminders.ReminderNotifications
import com.yugesa.calmtasks.ui.CalmTasksUiState
import com.yugesa.calmtasks.ui.CalmTasksViewModel
import com.yugesa.calmtasks.ui.Screen
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val MmdTopBarHeight = 67.dp
private val MmdHeaderIconTouch = 48.dp
private val MmdHeaderIconSize = 28.dp
private val MmdPrimaryButtonHeight = 64.dp
private val MmdIconButtonHeight = 72.dp
private val MmdControlHeight = 56.dp
private val MmdButtonRadius = 18.dp
private val MmdPrimaryRadius = 20.dp
private val MmdDividerHeight = 3.dp

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageStore.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderNotifications.ensureChannel(this)
        setContent {
            CalmTheme {
                CalmTasksApp()
            }
        }
    }
}

@Composable
private fun CalmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            background = Color.White,
            surface = Color.White,
            primary = Color.Black,
            onPrimary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black,
            outline = Color.Black,
        ),
        content = content,
    )
}

@Composable
private fun CalmTasksApp(
    viewModel: CalmTasksViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    BackHandler(enabled = state.screen != Screen.Today) {
        viewModel.back()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        val selectedDay = state.selectedDate.toString()
        val overflow = TodayPlanner.overflowToday(state.tasks, selectedDay, state.todayPriorityLimit)
        val screen = if (state.screen == Screen.Today && overflow.isNotEmpty()) Screen.FocusReview else state.screen

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = titleFor(screen, state),
                showBack = screen != Screen.Today && screen != Screen.FocusReview,
                showEdit = screen == Screen.Today,
                editMode = editMode,
                showSettings = screen == Screen.Today,
                onBack = viewModel::back,
                onEdit = { editMode = !editMode },
                onSettings = { viewModel.goTo(Screen.Settings) },
            )
            HeaderDivider()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                when (screen) {
                    Screen.Today -> TodayScreen(state, viewModel, editMode)
                    Screen.FocusReview -> FocusReviewScreen(state, viewModel)
                    Screen.AddTask -> AddTaskScreen(state, viewModel)
                    Screen.Inbox -> TaskListScreen(
                        tasks = state.tasks.filter { it.status == TaskEntity.STATUS_ACTIVE && it.plannedDate == null },
                        folders = state.folders,
                        emptyTitle = stringResource(R.string.empty_unplanned_title),
                        emptyBody = stringResource(R.string.empty_unplanned_body),
                        onOpen = { viewModel.goTo(Screen.TaskDetail(it)) },
                        onDone = viewModel::markDone,
                        onDelete = viewModel::deleteTask,
                    )
                    Screen.Folders -> FoldersScreen(state, viewModel)
                    Screen.DoneTasks -> DoneTasksScreen(state, viewModel)
                    is Screen.FolderDetail -> TaskListScreen(
                        tasks = state.tasks.filter {
                            it.status == TaskEntity.STATUS_ACTIVE && it.folderId == screen.folderId
                        },
                        folders = state.folders,
                        emptyTitle = stringResource(R.string.empty_folder_title),
                        emptyBody = stringResource(R.string.empty_folder_body),
                        onOpen = { viewModel.goTo(Screen.TaskDetail(it)) },
                        onDone = viewModel::markDone,
                        onDelete = viewModel::deleteTask,
                    )
                    is Screen.TaskDetail -> TaskDetailScreen(screen.taskId, state, viewModel)
                    Screen.Settings -> SettingsScreen(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    showBack: Boolean,
    showEdit: Boolean,
    editMode: Boolean,
    showSettings: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MmdTopBarHeight)
            .padding(start = 18.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(MmdHeaderIconTouch)) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back),
                    tint = Color.Black,
                    modifier = Modifier.size(MmdHeaderIconSize),
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showEdit) {
            IconButton(onClick = onEdit, modifier = Modifier.size(MmdHeaderIconTouch)) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.rename),
                    tint = Color.Black,
                    modifier = Modifier.size(if (editMode) 32.dp else MmdHeaderIconSize),
                )
            }
        }
        if (showSettings) {
            IconButton(onClick = onSettings, modifier = Modifier.size(MmdHeaderIconTouch)) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.Black,
                    modifier = Modifier.size(MmdHeaderIconSize),
                )
            }
        }
    }
}

@Composable
private fun HeaderDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MmdDividerHeight)
            .background(Color.Black),
    )
}

@Composable
private fun titleFor(screen: Screen, state: CalmTasksUiState): String {
    return when (screen) {
        Screen.Today -> stringResource(R.string.reminders)
        Screen.FocusReview -> stringResource(R.string.choose_focus)
        Screen.AddTask -> stringResource(R.string.add)
        Screen.Inbox -> stringResource(R.string.inbox)
        Screen.Folders -> stringResource(R.string.folders)
        Screen.DoneTasks -> stringResource(R.string.done_tasks)
        Screen.Settings -> stringResource(R.string.settings)
        is Screen.FolderDetail -> state.folders.firstOrNull { it.id == screen.folderId }?.let { folderLabel(it) }.orEmpty()
        is Screen.TaskDetail -> stringResource(R.string.task)
    }
}

@Composable
private fun TodayScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel, editMode: Boolean) {
    val selectedDay = state.selectedDate.toString()
    val todayTasks = TodayPlanner.visibleToday(state.tasks, selectedDay, state.todayPriorityLimit)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            ActionRow(viewModel)
        }
        item {
            DayNavigator(
                date = state.selectedDate,
                onPrevious = viewModel::previousDay,
                onNext = viewModel::nextDay,
            )
        }
        if (todayTasks.isEmpty()) {
            item {
                TodayEmptyState()
            }
        } else {
            items(todayTasks) { task ->
                TaskCard(
                    task = task,
                    folders = state.folders,
                    onOpen = { viewModel.goTo(Screen.TaskDetail(task.id)) },
                    onDone = { viewModel.markDone(task.id) },
                    onDelete = { viewModel.deleteTask(task.id) },
                    editMode = editMode,
                )
            }
        }
    }
}

@Composable
private fun FocusReviewScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    val selectedDay = state.selectedDate.toString()
    val tasks = TodayPlanner.activeToday(state.tasks, selectedDay)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.focus_overflow, state.todayPriorityLimit), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = { viewModel.goTo(Screen.Settings) }, modifier = Modifier.fillMaxWidth().height(MmdControlHeight), border = blackBorder(), shape = calmButtonShape()) {
            Text(stringResource(R.string.change_limit), color = Color.Black, fontWeight = FontWeight.Bold)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                val index = tasks.indexOf(task)
                ReviewTaskCard(
                    task = task,
                    folders = state.folders,
                    isFocus = index < state.todayPriorityLimit,
                    onOpen = { viewModel.goTo(Screen.TaskDetail(task.id)) },
                    onLater = { viewModel.moveLater(task.id) },
                )
            }
        }
    }
}

@Composable
private fun ActionRow(viewModel: CalmTasksViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { viewModel.goTo(Screen.AddTask) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors = blackButtonColors(),
            shape = primaryButtonShape(),
        ) {
            Text(stringResource(R.string.add_task), fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ActionIconButton(
                iconRes = R.drawable.ic_inbox,
                label = stringResource(R.string.inbox),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.goTo(Screen.Inbox) },
            )
            ActionIconButton(
                iconRes = R.drawable.ic_folder,
                label = stringResource(R.string.folders),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.goTo(Screen.Folders) },
            )
            ActionIconButton(
                iconRes = R.drawable.ic_done_tasks,
                label = stringResource(R.string.done_tasks),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.goTo(Screen.DoneTasks) },
            )
        }
    }
}

@Composable
private fun ActionIconButton(iconRes: Int, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(MmdIconButtonHeight),
        border = blackBorder(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        shape = calmButtonShape(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
            Text(label, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddTaskScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    var title by remember { mutableStateOf("") }
    var folderId by remember { mutableStateOf<Long?>(null) }
    var plannedForSelectedDay by remember { mutableStateOf(true) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf(state.selectedDate) }
    var reminderTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.task_title)) },
                singleLine = true,
            )
        }
        item {
            ToggleRow(
                title = stringResource(R.string.plan),
                options = listOf(stringResource(R.string.selected_day) to true, stringResource(R.string.inbox) to false),
                selected = plannedForSelectedDay,
                onSelected = { plannedForSelectedDay = it },
            )
        }
        item {
            FolderChooser(state.folders, folderId) { folderId = it }
        }
        item {
            ReminderChooser(
                enabled = reminderEnabled,
                reminderDate = reminderDate,
                reminderTime = reminderTime,
                onEnabledChange = { reminderEnabled = it },
                onDateChange = { reminderDate = it },
                onTimeChange = { reminderTime = it },
            )
        }
        item {
            Button(
                onClick = { viewModel.addTask(title, folderId, plannedForSelectedDay, ReminderTimes.reminderMillis(reminderEnabled, reminderDate, reminderTime)) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(MmdPrimaryButtonHeight),
                colors = blackButtonColors(),
                shape = primaryButtonShape(),
            ) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TaskDetailScreen(taskId: Long, state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    val task = state.tasks.firstOrNull { it.id == taskId }
    if (task == null) {
        EmptyState(stringResource(R.string.task_missing))
        return
    }

    var title by remember(task.id) { mutableStateOf(task.title) }
    var folderId by remember(task.id) { mutableStateOf(task.folderId) }
    var plannedForSelectedDay by remember(task.id, state.selectedDate) { mutableStateOf(task.plannedDate == state.selectedDate.toString()) }
    var reminderEnabled by remember(task.id) { mutableStateOf(task.reminderAt != null) }
    var reminderDate by remember(task.id) { mutableStateOf(dateFromMillis(task.reminderAt) ?: state.selectedDate) }
    var reminderTime by remember(task.id) { mutableStateOf(timeFromMillis(task.reminderAt) ?: LocalTime.of(9, 0)) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.task_title)) },
                singleLine = true,
            )
        }
        item {
            ToggleRow(
                title = stringResource(R.string.plan),
                options = listOf(stringResource(R.string.selected_day) to true, stringResource(R.string.inbox) to false),
                selected = plannedForSelectedDay,
                onSelected = { plannedForSelectedDay = it },
            )
        }
        item {
            FolderChooser(state.folders, folderId) { folderId = it }
        }
        item {
            ReminderChooser(
                enabled = reminderEnabled,
                reminderDate = reminderDate,
                reminderTime = reminderTime,
                onEnabledChange = { reminderEnabled = it },
                onDateChange = { reminderDate = it },
                onTimeChange = { reminderTime = it },
            )
        }
        item {
            Button(
                onClick = {
                    viewModel.updateTask(
                        task.copy(
                            title = title,
                            folderId = folderId,
                            plannedDate = if (plannedForSelectedDay) state.selectedDate.toString() else null,
                            reminderAt = ReminderTimes.reminderMillis(reminderEnabled, reminderDate, reminderTime),
                        ),
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(MmdPrimaryButtonHeight),
                colors = blackButtonColors(),
                shape = primaryButtonShape(),
            ) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedButton(onClick = { viewModel.markDone(task.id) }, modifier = Modifier.fillMaxWidth().height(MmdControlHeight), border = blackBorder(), shape = calmButtonShape()) {
                Text(stringResource(R.string.done), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedButton(onClick = { viewModel.deleteTask(task.id) }, modifier = Modifier.fillMaxWidth().height(MmdControlHeight), border = blackBorder(), shape = calmButtonShape()) {
                Text(stringResource(R.string.delete_task), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoldersScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    if (state.folders.isEmpty()) {
        IllustratedEmptyState(
            title = stringResource(R.string.empty_folders_title),
            body = stringResource(R.string.empty_folders_body),
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(state.folders) { folder ->
                val count = state.tasks.count { it.status == TaskEntity.STATUS_ACTIVE && it.folderId == folder.id }
                FolderListRow(
                    title = folderLabel(folder),
                    count = count,
                    onOpen = { viewModel.goTo(Screen.FolderDetail(folder.id)) },
                )
            }
        }
    }
}

@Composable
private fun FolderListRow(title: String, count: Int, onOpen: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
            border = BorderStroke(0.dp, Color.Transparent),
            shape = RoundedCornerShape(0.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = Color.Black, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CountBadge(count)
            }
        }
        DottedDivider()
    }
}

@Composable
private fun DoneTasksScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    val doneTasks = state.tasks.filter { it.status == TaskEntity.STATUS_DONE }.sortedByDescending { it.updatedAt }
    if (doneTasks.isEmpty()) {
        IllustratedEmptyState(
            title = stringResource(R.string.empty_done_title),
            body = stringResource(R.string.empty_done_body),
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(doneTasks) { task ->
                DoneTaskCard(
                    task = task,
                    folders = state.folders,
                    onRestore = { viewModel.restoreTask(task.id) },
                    onDelete = { viewModel.deleteDoneTask(task.id) },
                )
            }
        }
    }
}

@Composable
private fun DoneTaskCard(
    task: TaskEntity,
    folders: List<FolderEntity>,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(folderName(task.folderId, folders), style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onRestore, modifier = Modifier.weight(1f).height(48.dp), colors = blackButtonColors(), shape = chipShape()) {
                    Text(stringResource(R.string.restore), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                    Text(stringResource(R.string.delete), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        DottedDivider()
    }
}

@Composable
private fun SettingsScreen(state: CalmTasksUiState, viewModel: CalmTasksViewModel) {
    var priorityExpanded by remember { mutableStateOf(true) }
    var languageExpanded by remember { mutableStateOf(false) }
    var notificationsExpanded by remember { mutableStateOf(false) }
    var foldersExpanded by remember { mutableStateOf(false) }
    var priorityLimitText by remember(state.todayPriorityLimit) { mutableStateOf(state.todayPriorityLimit.toString()) }
    var folderName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LanguageStore.current(context)) }
    val folderNameDuplicate = folderName.isNotBlank() && folderNameTaken(folderName, state.folders)
    val priorityLimit = priorityLimitText.toIntOrNull()
    val priorityLimitValid = priorityLimit != null && priorityLimit in 1..99

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SettingsSection(
                title = stringResource(R.string.priority_limit_title),
                expanded = priorityExpanded,
                onToggle = { priorityExpanded = !priorityExpanded },
            ) {
                OutlinedTextField(
                    value = priorityLimitText,
                    onValueChange = { value -> priorityLimitText = value.filter { it.isDigit() }.take(2) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.priority_limit_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Text(stringResource(R.string.priority_limit_range), style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { viewModel.updatePriorityLimit(priorityLimit ?: state.todayPriorityLimit) },
                    enabled = priorityLimitValid && priorityLimit != state.todayPriorityLimit,
                    modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
                    colors = blackButtonColors(),
                    shape = calmButtonShape(),
                ) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.language),
                expanded = languageExpanded,
                onToggle = { languageExpanded = !languageExpanded },
            ) {
                LanguageSettingsSection(
                    selectedLanguage = currentLanguage,
                    onSelected = { languageCode ->
                        LanguageStore.set(context, languageCode)
                        currentLanguage = languageCode
                        (context as? ComponentActivity)?.recreate()
                    },
                )
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.notifications),
                expanded = notificationsExpanded,
                onToggle = { notificationsExpanded = !notificationsExpanded },
            ) {
                NotificationSettingsSection()
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.your_folders),
                expanded = foldersExpanded,
                onToggle = { foldersExpanded = !foldersExpanded },
            ) {
                SectionLabel(stringResource(R.string.add_folder))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.custom_folder_name)) },
                    singleLine = true,
                )
                if (folderNameDuplicate) {
                    Text(stringResource(R.string.folder_name_exists), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.addFolder(folderName)
                        folderName = ""
                    },
                    enabled = folderName.isNotBlank() && !folderNameDuplicate,
                    modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
                    colors = blackButtonColors(),
                    shape = calmButtonShape(),
                ) {
                    Text(stringResource(R.string.add_folder), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                state.folders.forEachIndexed { index, folder ->
                    FolderSettingsCard(
                        folder = folder,
                        folders = state.folders,
                        activeTaskCount = state.tasks.count { it.status == TaskEntity.STATUS_ACTIVE && it.folderId == folder.id },
                        canMoveUp = index > 0,
                        canMoveDown = index < state.folders.lastIndex,
                        onRename = { viewModel.renameFolder(folder.id, it) },
                        onDelete = { viewModel.deleteCustomFolder(folder.id) },
                        onMoveUp = { viewModel.moveFolderUp(folder.id) },
                        onMoveDown = { viewModel.moveFolderDown(folder.id) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
            border = blackBorder(),
            shape = calmButtonShape(),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (expanded) "-" else "+", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun FolderSettingsCard(
    folder: FolderEntity,
    folders: List<FolderEntity>,
    activeTaskCount: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val isCustom = folder.customName != null
    val currentLabel = folderLabel(folder)
    var draftName by remember(folder.id, folder.customName) { mutableStateOf(currentLabel) }
    val duplicate = isCustom && draftName.isNotBlank() && folderNameTaken(draftName, folders, folder.id)
    val changed = draftName.trim() != currentLabel

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = blackBorder(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currentLabel, fontWeight = FontWeight.Bold)
                Text(activeTaskCount.toString())
            }
            if (isCustom) {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.custom_folder_name)) },
                    singleLine = true,
                )
                if (duplicate) {
                    Text(stringResource(R.string.folder_name_exists), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Text(stringResource(R.string.delete_folder_moves_tasks), style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onRename(draftName) },
                        enabled = draftName.isNotBlank() && changed && !duplicate,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = blackButtonColors(),
                        shape = chipShape(),
                    ) {
                        Text(stringResource(R.string.rename), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                        Text(stringResource(R.string.delete), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                    Text(stringResource(R.string.move_up), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                    Text(stringResource(R.string.move_down), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun folderNameTaken(name: String, folders: List<FolderEntity>, excludedId: Long? = null): Boolean {
    val cleanName = name.trim()
    return cleanName.isNotBlank() && folders.any { folder ->
        folder.id != excludedId && folderLabel(folder).equals(cleanName, ignoreCase = true)
    }
}

@Composable
private fun LanguageSettingsSection(selectedLanguage: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        languageOptions().forEach { option ->
            ChoiceButton(
                label = option.label,
                selected = selectedLanguage == option.code,
            ) {
                onSelected(option.code)
            }
        }
    }
}

private data class LanguageOption(val code: String, val label: String)

@Composable
private fun languageOptions(): List<LanguageOption> {
    return listOf(
        LanguageOption(LanguageStore.SYSTEM, stringResource(R.string.system_language)),
        LanguageOption("en", "English"),
        LanguageOption("fr", "Français"),
        LanguageOption("pl", "Polski"),
        LanguageOption("es", "Español"),
        LanguageOption("pt", "Português"),
        LanguageOption("it", "Italiano"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("ru", "Русский"),
    )
}

@Composable
private fun NotificationSettingsSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(ReminderNotifications.areEnabled(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        enabled = ReminderNotifications.areEnabled(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = ReminderNotifications.areEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (enabled) stringResource(R.string.notifications_enabled) else stringResource(R.string.notifications_disabled),
            style = MaterialTheme.typography.bodyLarge,
        )
        val buttonText = if (enabled) {
            stringResource(R.string.open_notification_settings)
        } else {
            stringResource(R.string.enable_notifications)
        }
        OutlinedButton(
            onClick = {
                ReminderNotifications.ensureChannel(context)
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(ReminderNotifications.settingsIntent(context))
                }
            },
            modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
            border = blackBorder(),
            shape = calmButtonShape(),
        ) {
            Text(buttonText, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TaskListScreen(
    tasks: List<TaskEntity>,
    folders: List<FolderEntity>,
    emptyTitle: String,
    emptyBody: String,
    onOpen: (Long) -> Unit,
    onDone: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    if (tasks.isEmpty()) {
        IllustratedEmptyState(title = emptyTitle, body = emptyBody)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    folders = folders,
                    onOpen = { onOpen(task.id) },
                    onDone = { onDone(task.id) },
                    onDelete = { onDelete(task.id) },
                    editMode = true,
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    folders: List<FolderEntity>,
    onOpen: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit,
    editMode: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(folderName(task.folderId, folders), style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = onDone, modifier = Modifier.size(MmdHeaderIconTouch)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_done_circle),
                        contentDescription = stringResource(R.string.done),
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            task.reminderAt?.let {
                Text(
                    text = stringResource(R.string.reminder_at, reminderLabel(it)),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (editMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                        Text(stringResource(R.string.open), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                        Text(stringResource(R.string.delete), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        DottedDivider()
    }
}

@Composable
private fun ReviewTaskCard(
    task: TaskEntity,
    folders: List<FolderEntity>,
    isFocus: Boolean,
    onOpen: () -> Unit,
    onLater: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = blackBorder(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (isFocus) stringResource(R.string.kept_today) else folderName(task.folderId, folders))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f).height(48.dp), border = blackBorder(), shape = chipShape()) {
                    Text(stringResource(R.string.open), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                if (!isFocus) {
                    Button(onClick = onLater, modifier = Modifier.weight(1f).height(48.dp), colors = blackButtonColors(), shape = chipShape()) {
                        Text(stringResource(R.string.later), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderChooser(folders: List<FolderEntity>, selectedId: Long?, onSelected: (Long?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = folders.firstOrNull { it.id == selectedId }?.let { folderLabel(it) } ?: stringResource(R.string.none)

    SectionLabel(stringResource(R.string.folder))
    Box {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().height(MmdControlHeight),
            border = blackBorder(),
            shape = calmButtonShape(),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = selectedLabel,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(if (expanded) "-" else "+", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        if (expanded) {
            FolderMenuPopup(
                folders = folders,
                selectedId = selectedId,
                onDismiss = { expanded = false },
                onSelected = {
                    onSelected(it)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun FolderMenuPopup(
    folders: List<FolderEntity>,
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long?) -> Unit,
) {
    val items = listOf<Pair<String, Long?>>(stringResource(R.string.none) to null) +
        folders.map { folderLabel(it) to it.id }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
    ) {
        Surface(
            modifier = Modifier.width(320.dp),
            color = Color.White,
            contentColor = Color.Black,
            shape = RoundedCornerShape(26.dp),
            border = blackBorder(),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    val selected = selectedId == item.second
                    OutlinedButton(
                        onClick = { onSelected(item.second) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        border = BorderStroke(0.dp, Color.Transparent),
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                    ) {
                        Text(
                            text = item.first,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (index < items.lastIndex) {
                        DottedDivider(Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderChooser(
    enabled: Boolean,
    reminderDate: LocalDate,
    reminderTime: LocalTime,
    onEnabledChange: (Boolean) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleRow(
            title = stringResource(R.string.reminder),
            options = listOf(stringResource(R.string.none) to false, stringResource(R.string.reminder_set) to true),
            selected = enabled,
            onSelected = onEnabledChange,
        )
        if (enabled) {
            DayNavigator(
                date = reminderDate,
                onPrevious = {
                    if (reminderDate.isAfter(LocalDate.now())) {
                        onDateChange(reminderDate.minusDays(1))
                    }
                },
                onNext = { onDateChange(reminderDate.plusDays(1)) },
            )
            TimeNavigator(
                time = reminderTime,
                onTimeSelected = onTimeChange,
            )
        }
    }
}

@Composable
private fun <T> ToggleRow(title: String, options: List<Pair<String, T>>, selected: T, onSelected: (T) -> Unit) {
    SectionLabel(title)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { option ->
            ChoiceButton(option.first, selected == option.second, Modifier.weight(1f)) {
                onSelected(option.second)
            }
        }
    }
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier.height(MmdControlHeight), colors = blackButtonColors(), shape = calmButtonShape()) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.height(MmdControlHeight), border = blackBorder(), shape = calmButtonShape()) {
            Text(label, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DayNavigator(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onPrevious, modifier = Modifier.weight(0.8f).height(56.dp), border = blackBorder(), shape = calmButtonShape(), contentPadding = PaddingValues(0.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.back),
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = dayLabel(date),
            modifier = Modifier.weight(2.4f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        OutlinedButton(onClick = onNext, modifier = Modifier.weight(0.8f).height(56.dp), border = blackBorder(), shape = calmButtonShape(), contentPadding = PaddingValues(0.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.open),
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun TimeNavigator(time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val selectedHour = time.hour
    val selectedMinute = nearestQuarter(time.minute)
    val normalizedTime = time.withMinute(selectedMinute)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeWheelColumn(
            selected = selectedHour,
            values = wheelValues(selectedHour, 24, 1),
            formatter = { it.toString() },
            onPrevious = { onTimeSelected(normalizedTime.plusHours(-1L)) },
            onNext = { onTimeSelected(normalizedTime.plusHours(1L)) },
            onSelected = { onTimeSelected(normalizedTime.withHour(it)) },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        TimeWheelColumn(
            selected = selectedMinute,
            values = wheelValues(selectedMinute / 15, 4, 15),
            formatter = { "%02d".format(it) },
            onPrevious = { onTimeSelected(normalizedTime.plusMinutes(-15L)) },
            onNext = { onTimeSelected(normalizedTime.plusMinutes(15L)) },
            onSelected = { onTimeSelected(normalizedTime.withMinute(it)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TimeWheelColumn(
    selected: Int,
    values: List<Int>,
    formatter: (Int) -> String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_triangle_up),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }
        values.forEach { value ->
            val isSelected = value == selected
            if (isSelected) {
                OutlinedButton(
                    onClick = { onSelected(value) },
                    modifier = Modifier.width(72.dp).height(56.dp),
                    border = blackBorder(),
                    shape = calmButtonShape(),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = formatter(value),
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(value) },
                    modifier = Modifier.width(72.dp).height(38.dp),
                    border = BorderStroke(0.dp, Color.Transparent),
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = formatter(value),
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_triangle_down),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private fun wheelValues(selected: Int, itemCount: Int, multiplier: Int): List<Int> {
    return (-2..2).map { offset -> wrap(selected + offset, itemCount) * multiplier }
}

private fun nearestQuarter(minute: Int): Int {
    return ((minute + 7) / 15 * 15) % 60
}

private fun wrap(value: Int, itemCount: Int): Int {
    return ((value % itemCount) + itemCount) % itemCount
}

@Composable
private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    val dateWithDay = CalmDateTimeLabels.localizedDate(date)
    return when (date) {
        today -> stringResource(R.string.today_with_date, dateWithDay)
        today.plusDays(1) -> stringResource(R.string.tomorrow_with_date, dateWithDay)
        else -> dateWithDay
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun IllustratedEmptyState(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(todayEmptyImageRes()),
            contentDescription = null,
            modifier = Modifier.size(width = 180.dp, height = 180.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun TodayEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(todayEmptyImageRes()),
            contentDescription = null,
            modifier = Modifier.size(width = 172.dp, height = 172.dp),
        )
        Text(
            text = stringResource(R.string.empty_today_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.empty_today_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 10.dp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun todayEmptyImageRes(): Int {
    return when (LocalDate.now().dayOfYear % 3) {
        0 -> R.drawable.empty_today_1
        1 -> R.drawable.empty_today_2
        else -> R.drawable.empty_today_3
    }
}

@Composable
private fun folderLabel(folder: FolderEntity): String {
    return folder.customName?.takeIf { it.isNotBlank() } ?: stringResource(folderStringId(folder.nameKey))
}

@Composable
private fun folderName(folderId: Long?, folders: List<FolderEntity>): String {
    val folder = folders.firstOrNull { it.id == folderId }
    return if (folder == null) stringResource(R.string.no_folder) else folderLabel(folder)
}

private fun folderStringId(nameKey: String): Int {
    return when (nameKey) {
        "folder_home" -> R.string.folder_home
        "folder_work" -> R.string.folder_work
        "folder_admin" -> R.string.folder_admin
        "folder_errands" -> R.string.folder_errands
        "folder_personal" -> R.string.folder_personal
        else -> R.string.folder
    }
}

@Composable
private fun blackButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Color.Black,
    contentColor = Color.White,
    disabledContainerColor = Color.LightGray,
    disabledContentColor = Color.Black,
)

@Composable
private fun CountBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(width = if (count > 99) 48.dp else 28.dp, height = 28.dp)
            .background(Color.Black, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun DottedDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx())),
        )
    }
}

@Composable
private fun blackBorder() = BorderStroke(2.dp, Color.Black)

@Composable
private fun calmButtonShape() = RoundedCornerShape(MmdButtonRadius)

@Composable
private fun primaryButtonShape() = RoundedCornerShape(MmdPrimaryRadius)

@Composable
private fun chipShape() = RoundedCornerShape(20.dp)

private fun reminderLabel(timestamp: Long): String {
    return CalmDateTimeLabels.reminderLabel(timestamp)
}

private fun dateFromMillis(timestamp: Long?): LocalDate? {
    return timestamp
        ?.let { java.time.Instant.ofEpochMilli(it) }
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
}

private fun timeFromMillis(timestamp: Long?): LocalTime? {
    return timestamp
        ?.let { java.time.Instant.ofEpochMilli(it) }
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalTime()
        ?.withSecond(0)
        ?.withNano(0)
}
