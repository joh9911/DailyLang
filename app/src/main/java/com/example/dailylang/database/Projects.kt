package com.example.dailylang.database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

@Entity
data class Projects (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "default_language") val defaultLanguage: String?,
    @ColumnInfo(name = "learning_language") val learningLanguage: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "date")  val timestamp: Long,

    )

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Projects::class,
            parentColumns = ["uid"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyExpressions(
    @PrimaryKey(autoGenerate = true) val exId: Int,
    val expression: String,
    val translation: String,
    val projectId: Int
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Projects::class,
            parentColumns = ["uid"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val vocaId: Int,
    val voca: String?,
    val meaning: String?,
    val projectId: Int,
)

@Dao
interface ProjectDao {


    @Query("SELECT * FROM Projects")
    fun getAllProjects(): List<Projects>

    @Transaction // Project와 연결된 DailyExpressions을 가져오기 위해 Transaction 어노테이션 사용
    @Query("SELECT * FROM DailyExpressions WHERE projectId = :projectId")
    fun getDailyExpressionsForProject(projectId: Int): List<DailyExpressions>

    // 특정 프로젝트의 ID로 Vocabulary를 가져오는 메소드
    @Transaction // Project와 연결된 Vocabulary를 가져오기 위해 Transaction 어노테이션 사용
    @Query("SELECT * FROM Vocabulary WHERE projectId = :projectId")
    fun getVocabularyForProject(projectId: Int): List<Vocabulary>

    // 프로젝트를 추가하는 메소드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertProject(project: Projects): Long

    // 프로젝트를 삭제하는 메소드
    @Delete
     fun deleteProject(project: Projects)

    // 일일 표현식을 추가하는 메소드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertDailyExpression(expression: DailyExpressions)

    // 일일 표현식을 삭제하는 메소드
    @Delete
     fun deleteDailyExpression(expression: DailyExpressions)

    // 어휘를 추가하는 메소드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertVocabulary(vocabulary: Vocabulary)

    // 어휘를 삭제하는 메소드
    @Delete
     fun deleteVocabulary(vocabulary: Vocabulary)

    @Query("DELETE FROM Projects")
    fun deleteAll()
}