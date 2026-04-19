package app.krafted.tigerluckysnap.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.model.TigerReactionState

@Composable
fun TigerReaction(state: TigerReactionState, modifier: Modifier = Modifier) {
    // Phase C: instant image swap (tiger_react_idle/happy/excited/surprised/sad) with spring animation
    Card(modifier = modifier.size(120.dp)) {}
}
