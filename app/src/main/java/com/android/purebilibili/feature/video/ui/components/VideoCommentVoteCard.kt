package com.android.purebilibili.feature.video.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.resolveAccessibleContainerColors
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.data.model.response.ReplyVoteCard
import com.android.purebilibili.feature.dynamic.components.DynamicVoteDialog
import kotlin.math.roundToInt
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

@Composable
fun VideoCommentVoteCard(
    card: ReplyVoteCard,
    modifier: Modifier = Modifier,
) {
    if (card.voteId <= 0L || card.options.isEmpty()) return

    var displayedCard by remember(card) { mutableStateOf(card) }
    var selectedOption by remember(card.voteId) { mutableIntStateOf(-1) }
    var showVoteDialog by remember(card.voteId) { mutableStateOf(false) }
    val totalVotes = displayedCard.options.sumOf { it.count }.coerceAtLeast(0L)

    AppSurface(
        modifier = modifier,
        shape = AppShapes.container(ContainerLevel.Card),
        color = AppSurfaceTokens.surfaceContainer(),
        contentColor = AppContentColors.primaryText(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = displayedCard.title,
                style = COUITheme.textStyles.headline1,
                fontWeight = FontWeight.SemiBold,
                color = AppContentColors.primaryText(),
            )
            AppText(
                text = if (displayedCard.myVoteOption != null) {
                    "${displayedCard.count} 人参与 · 已投票"
                } else {
                    "${displayedCard.count} 人参与 · 选择选项投票"
                },
                style = COUITheme.textStyles.body1,
                color = AppContentColors.secondaryText(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                displayedCard.options.forEachIndexed { position, option ->
                    val selected = displayedCard.myVoteOption == option.idx
                    val (containerColor, contentColor) = when (position % 3) {
                        0 -> AppSurfaceTokens.primaryContainer() to AppSurfaceTokens.onPrimaryContainer()
                        1 -> AppSurfaceTokens.secondaryContainer() to AppSurfaceTokens.onSecondaryContainer()
                        else -> AppSurfaceTokens.surfaceContainerHigh() to AppContentColors.primaryText()
                    }
                    val optionColors = resolveAccessibleContainerColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                        backgroundColor = AppSurfaceTokens.surfaceContainer(),
                        fallbackContentColors = listOf(AppContentColors.primaryText(), Color.Black, Color.White),
                    )
                    AppSurface(
                        onClick = {
                            selectedOption = option.idx.toInt()
                            showVoteDialog = true
                        },
                        modifier = Modifier
                            .widthIn(min = 144.dp, max = 240.dp)
                            .heightIn(min = 72.dp),
                        shape = AppShapes.container(ContainerLevel.MediaCover),
                        color = optionColors.containerColor,
                        contentColor = optionColors.contentColor,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            AppText(
                                text = if (selected) "✓ ${option.desc}" else option.desc,
                                style = COUITheme.textStyles.body1,
                                fontWeight = FontWeight.Medium,
                                color = optionColors.contentColor,
                            )
                            AppText(
                                text = if (totalVotes > 0L) {
                                    "${(option.count * 100.0 / totalVotes).roundToInt()}%"
                                } else {
                                    "0%"
                                },
                                style = COUITheme.textStyles.body1,
                                color = optionColors.contentColor,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showVoteDialog) {
        DynamicVoteDialog(
            voteId = card.voteId,
            dynamicId = "",
            onDismiss = { showVoteDialog = false },
            initialOptionIndex = selectedOption,
            onVoteSuccess = { result ->
                displayedCard = displayedCard.copy(
                    count = result.join_num.toLong(),
                    options = displayedCard.options.map { option ->
                        option.copy(count = result.options.firstOrNull { it.opt_idx.toLong() == option.idx }
                            ?.cnt?.toLong() ?: option.count)
                    },
                    myVoteOption = result.my_votes.firstOrNull()?.toLong(),
                )
            },
        )
    }
}
