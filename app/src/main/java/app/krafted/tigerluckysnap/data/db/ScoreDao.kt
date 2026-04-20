package app.krafted.tigerluckysnap.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE gameMode = :mode AND playerName != '' ORDER BY score DESC LIMIT 10")
    fun getTopScores(mode: String): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores WHERE playerName != '' ORDER BY score DESC LIMIT 20")
    fun getAllTopScores(): Flow<List<ScoreEntity>>

    @Query("DELETE FROM scores WHERE playerName = '' OR playerName IS NULL")
    suspend fun deleteUnnamedScores()
}
