package com.example.forexData.repository;

import com.example.forexData.model.ForexData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ForexDataRepository extends JpaRepository<ForexData, Long> {
    /**
     * Finds forex data entries by currency pair and date range.
     *
     * @param currencyPair the currency pair to filter by (e.g., "USD/EUR")
     * @param startDate    the start date for the query
     * @param endDate      the end date for the query
     * @return a list of ForexData entries within the specified date range
     */
    List<ForexData> findByCurrencyPairAndDateBetween(String currencyPair, LocalDate startDate, LocalDate endDate);
}
