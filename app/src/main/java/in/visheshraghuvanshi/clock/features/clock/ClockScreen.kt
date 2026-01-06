package `in`.visheshraghuvanshi.clock.features.clock

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.clock.features.clock.components.WorldClockCard
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("world_clock_prefs", Context.MODE_PRIVATE) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600
    val isWideScreen = isLandscape || isTablet
    val fabPaddingBottom = if (isWideScreen) 16.dp else 100.dp
    val listContentPadding = if (isWideScreen) 16.dp else 140.dp

    val worldClocks = remember {
        val savedSet = prefs.getStringSet("cities", setOf("America/New_York", "Europe/London")) ?: emptySet()
        mutableStateListOf(*savedSet.toTypedArray())
    }

    fun saveCities() {
        prefs.edit().putStringSet("cities", worldClocks.toSet()).apply()
    }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000L)
        }
    }

    var showAddCitySheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCitySheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = fabPaddingBottom)
            ) {
                Icon(Icons.Rounded.Public, contentDescription = "Add City")
            }
        }
    ) { innerPadding ->

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    HeroClockDisplay(currentTime)
                }

                VerticalDivider(
                    modifier = Modifier
                        .padding(vertical = 48.dp)
                        .padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "World Clock",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(worldClocks, key = { it }) { zoneId ->
                            WorldClockCard(
                                zoneId = zoneId,
                                onRemove = {
                                    worldClocks.remove(zoneId)
                                    saveCities()
                                }
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = listContentPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeroClockDisplay(currentTime)
                    }
                }

                item {
                    Text(
                        text = "World Clock",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                items(worldClocks, key = { it }) { zoneId ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        WorldClockCard(
                            zoneId = zoneId,
                            onRemove = {
                                worldClocks.remove(zoneId)
                                saveCities()
                            }
                        )
                    }
                }
            }
        }

        if (showAddCitySheet) {
            AddCitySheet(
                onDismiss = { showAddCitySheet = false },
                onCitySelected = { zoneId ->
                    if (!worldClocks.contains(zoneId)) {
                        worldClocks.add(zoneId)
                        saveCities()
                    }
                    showAddCitySheet = false
                }
            )
        }
    }
}

@Composable
fun HeroClockDisplay(currentTime: LocalDateTime) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-4).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCitySheet(
    onDismiss: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allZones = remember {
        ZoneId.getAvailableZoneIds()
            .filter { it.contains("/") && !it.startsWith("Etc") && !it.startsWith("System") }
            .sorted()
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredZones by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) allZones else allZones.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "Select City",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city or country") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredZones) { zone ->
                    val parts = zone.split("/")
                    val region = parts.first()
                    val city = parts.last().replace("_", " ")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCitySelected(zone) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = city,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = region,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    if (filteredZones.last() != zone) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}