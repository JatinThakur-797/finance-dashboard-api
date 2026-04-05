package com.auth.repository;

import com.auth.entities.FinancialRecord;
import com.auth.entities.RecordType;
import com.auth.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialRecordRepo extends JpaRepository<FinancialRecord, UUID> {

    // For filtering records
    List<FinancialRecord> findByCreatedBy(User createdBy);
    List<FinancialRecord> findByType(RecordType type);
    List<FinancialRecord> findByCategory(String category);
    List<FinancialRecord> findByDateBetween(LocalDate start, LocalDate end);
    List<FinancialRecord> findByTypeAndCategory(RecordType type, String category);

    // Get totals
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.type = :type")
    Double getTotalByType(@Param("type") RecordType type);

    @Query("SELECT f.category, SUM(f.amount) FROM FinancialRecord f GROUP BY f.category")
    List<Object[]> getCategorySummary();

    @Query("SELECT f FROM FinancialRecord f ORDER BY f.date DESC")
    List<FinancialRecord> getRecentRecords(Pageable pageable); // DB-level limit

    // For Monthly Trends
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', f.date, '%Y-%m'),
               SUM(CASE WHEN f.type = 'INCOME' THEN f.amount ELSE 0 END),
               SUM(CASE WHEN f.type = 'EXPENSE' THEN f.amount ELSE 0 END)
        FROM FinancialRecord f
        GROUP BY FUNCTION('DATE_FORMAT', f.date, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', f.date, '%Y-%m')
    """)
    List<Object[]> getMonthlyTrends();
}