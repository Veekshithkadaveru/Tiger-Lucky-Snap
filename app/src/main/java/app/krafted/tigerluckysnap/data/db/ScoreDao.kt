package app.krafted.tigerluckysnap.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE gameMode = :mode ORDER BY score DESC LIMIT 10")
    fun getTopScores(mode: String): Flow<List<ScoreEntity>>
}
