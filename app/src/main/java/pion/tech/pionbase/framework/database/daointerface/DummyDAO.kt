package pion.tech.pionbase.framework.database.daointerface

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pion.tech.pionbase.framework.database.entities.DummyEntity

@Dao
interface DummyDAO {

    @Insert
    fun insert(vararg scale: DummyEntity)

    @Update
    fun update(vararg string: DummyEntity)

    @Delete
    fun delete(scale: DummyEntity)

    @Query("SELECT * FROM ${DummyEntity.TABLE_NAME} ORDER BY ${DummyEntity.ID} DESC")
    fun getAllDummies(): List<DummyEntity>?

}