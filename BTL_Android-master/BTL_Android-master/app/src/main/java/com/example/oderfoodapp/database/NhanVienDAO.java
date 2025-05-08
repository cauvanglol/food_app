package com.example.oderfoodapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.oderfoodapp.object.NhanVien;
import com.example.oderfoodapp.object.User;

import java.util.List;

@Dao
public interface NhanVienDAO {
    @Insert
    void insert(NhanVien nhanVien);

    @Update
    void update(NhanVien nhanVien);

    @Delete
    void delete(NhanVien nhanVien);

    @Query("SELECT * FROM tblNhanVien")
    List<NhanVien> getAll();
}
