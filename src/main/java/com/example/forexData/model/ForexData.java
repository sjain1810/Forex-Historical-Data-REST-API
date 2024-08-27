package com.example.forexData.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class ForexData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "The unique identifier for the data entry")
    private Long id;

    @Schema(description = "The currency pair (e.g., INREUR=X)")
    private String currencyPair;

    @Schema(description = "The date of the data entry")
    private LocalDate date;

    @Schema(description = "The opening price")
    @Column(precision = 10, scale = 5)
    private BigDecimal open;

    @Schema(description = "The highest price during the period")
    @Column(precision = 10, scale = 5)
    private BigDecimal high;

    @Schema(description = "The lowest price during the period")
    @Column(precision = 10, scale = 5)
    private BigDecimal low;

    @Schema(description = "The closing price")
    @Column(precision = 10, scale = 5)
    private BigDecimal close;

    @Schema(description = "The adjusted closing price")
    @Column(precision = 10, scale = 5)
    private BigDecimal adjClose;

    @Schema(description = "The trading volume")
    @Column(precision = 20, scale = 5)
    private BigDecimal volume;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(BigDecimal adjClose) {
        this.adjClose = adjClose;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "ForexData{" +
                "id=" + id +
                ", currencyPair='" + currencyPair + '\'' +
                ", date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", adjClose=" + adjClose +
                ", volume=" + volume +
                '}';
    }
}