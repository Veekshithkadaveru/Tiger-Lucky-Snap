package app.krafted.tigerluckysnap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String = "",
    val score: Int,
    val gameMode: String,
    val timestamp: Long = System.currentTimeMillis()
)
