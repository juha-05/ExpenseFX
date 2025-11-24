package com.example.ReFX.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ReFX.data.entity.Expense2;

import java.util.List;

@Dao
public interface ExpenseDao2 {

    // -------------------------
    // INSERT
    // -------------------------
    @Insert
    long insertExpense(Expense2 expense);

    // -------------------------
    // SELECT 전체
    // -------------------------
    @Query("SELECT * FROM expense ORDER BY id DESC")
    List<Expense2> getAllExpenses();

    // -------------------------
    // SELECT 날짜별 (spendDate = 'YYYY. MM. DD')
    // -------------------------
    @Query("SELECT * FROM expense WHERE spendDate = :spendDate ORDER BY id DESC")
    List<Expense2> getExpensesByDate(String spendDate);

    // -------------------------
    // SELECT 단일 조회 (수정 화면용)
    // -------------------------
    @Query("SELECT * FROM expense WHERE id = :id LIMIT 1")
    Expense2 getExpenseById(int id);

    // -------------------------
    // UPDATE (수정)
    // -------------------------
    @Update
    void updateExpense(Expense2 expense);

    // -------------------------
    // DELETE (id 기반 개별 삭제)
    // -------------------------
    @Query("DELETE FROM expense WHERE id = :id")
    void deleteById(int id);
}
