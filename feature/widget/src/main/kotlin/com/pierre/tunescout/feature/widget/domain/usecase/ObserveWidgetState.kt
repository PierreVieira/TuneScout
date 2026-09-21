package com.pierre.tunescout.feature.widget.domain.usecase

import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import kotlinx.coroutines.flow.Flow

fun interface ObserveWidgetState {
    operator fun invoke(): Flow<WidgetState>
}
