package com.rensdev.juar4m.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> Flow<T>?.collectAsStateOrFallback(fallback: T): State<T> {
	return this?.collectAsState(initial = fallback)
		?: remember { mutableStateOf(fallback) }
}
