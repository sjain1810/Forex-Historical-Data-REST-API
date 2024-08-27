package com.example.forexData.service;

import com.example.forexData.model.ForexData;
import com.example.forexData.repository.ForexDataRepository;
import com.example.forexData.util.Period;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ForexScraperService {

    @Autowired
    private ForexDataRepository forexDataRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForexScraperService.class);

    @Scheduled(cron = "0 20 14 * * *", zone = "Asia/Kolkata") // Daily at IST 02:20 PM
    public void scrapeAndSaveWeeklyData() {
        LOGGER.info("Scheduled task: Scraping and saving weekly data for USD to INR");
        scrapeAndSaveExchangeRates("USD", "INR", Period.ONE_WEEK);
    }

    @Scheduled(cron = "0 20 14 * * 6", zone = "Asia/Kolkata") // Every Saturday at IST 02:20 PM
    public void scrapeAndSaveMonthlyData() {
        LOGGER.info("Scheduled task: Scraping and saving monthly data for USD to INR");
        scrapeAndSaveExchangeRates("USD", "INR", Period.ONE_MONTH);
    }

    @Scheduled(cron = "0 20 14 1 * *", zone = "Asia/Kolkata") // Monthly on 1st at IST 02:20 PM
    public void scrapeAndSaveQuarterlyData() {
        LOGGER.info("Scheduled task: Scraping and saving quarterly data for USD to INR");
        scrapeAndSaveExchangeRates("USD", "INR", Period.THREE_MONTHS);
        scrapeAndSaveExchangeRates("USD", "INR", Period.SIX_MONTHS);
        scrapeAndSaveExchangeRates("USD", "INR", Period.NINE_MONTHS);
    }

    @Scheduled(cron = "0 20 14 1 1 *", zone = "Asia/Kolkata") // January 1st at IST 02:20 PM
    public void scrapeAndSaveYearlyData() {
        LOGGER.info("Scheduled task: Scraping and saving yearly data for USD to INR");
        scrapeAndSaveExchangeRates("USD", "INR", Period.ONE_YEAR);
    }

    public List<ForexData> scrapeAndSaveExchangeRates(String from, String to, Period period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(period);

        List<ForexData> dataList = fetchDataFromUrl(from, to, startDate, endDate);
        if (!dataList.isEmpty()) {
            forexDataRepository.saveAll(dataList);
            LOGGER.info("Successfully saved {} records for period {}", dataList.size(), period.getValue());

        } else {
            LOGGER.warn("No valid data found for the period: {}", period.getValue());
        }
        return dataList;
    }

    private LocalDate calculateStartDate(Period period) {
        int value;
        return switch (period) {
            case ONE_WEEK -> {
                value = 1;
                yield LocalDate.now().minusWeeks(value);
            }
            case ONE_MONTH -> {
                value = 1;
                yield LocalDate.now().minusMonths(value);
            }
            case THREE_MONTHS -> {
                value = 3;
                yield LocalDate.now().minusMonths(value);
            }
            case SIX_MONTHS -> {
                value = 6;
                yield LocalDate.now().minusMonths(value);
            }
            case NINE_MONTHS -> {
                value = 9;
                yield LocalDate.now().minusMonths(value);
            }
            case ONE_YEAR -> {
                value = 1;
                yield LocalDate.now().minusYears(value);
            }
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private List<ForexData> fetchDataFromUrl(String from, String to, LocalDate startDate, LocalDate endDate) {
        List<ForexData> forexDataList = new ArrayList<>();
        String currencyPair = from + to + "=X";
        String url = "https://finance.yahoo.com/quote/" + currencyPair + "/history?p=" + currencyPair;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements rows = doc.select("table tbody tr");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

            for (Element row : rows) {
                String dateStr = row.select("td:nth-of-type(1)").text();
                String openStr = row.select("td:nth-of-type(2)").text();
                String highStr = row.select("td:nth-of-type(3)").text();
                String lowStr = row.select("td:nth-of-type(4)").text();
                String closeStr = row.select("td:nth-of-type(5)").text();
                String adjCloseStr = row.select("td:nth-of-type(6)").text();
                String volumeStr = row.select("td:nth-of-type(7)").text();

                System.out.println("Raw data: " + row.text());

                try {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    if (date.isBefore(startDate) || date.isAfter(endDate)) {
                        continue;
                    }

                    BigDecimal open = parseBigDecimal(openStr);
                    BigDecimal high = parseBigDecimal(highStr);
                    BigDecimal low = parseBigDecimal(lowStr);
                    BigDecimal close = parseBigDecimal(closeStr);
                    BigDecimal adjClose = parseBigDecimal(adjCloseStr);
                    BigDecimal volume = parseBigDecimal(volumeStr);

                    System.out.println("Parsed values: Date: " + date + ", Open: " + open + ", High: " + high + ", Low: " + low + ", Close: " + close + ", AdjClose: " + adjClose + ", Volume: " + volume);
                    if (open != null && high != null && low != null && close != null && adjClose != null && volume != null) {
                        ForexData data = new ForexData();
                        data.setCurrencyPair(currencyPair);
                        data.setDate(date);
                        data.setOpen(open);
                        data.setHigh(high);
                        data.setLow(low);
                        data.setClose(close);
                        data.setAdjClose(adjClose);
                        data.setVolume(volume);
                        forexDataList.add(data);
                    } else {
                        LOGGER.warn("Skipping row with incomplete or invalid data: {}", row.text());
                    }
                } catch (DateTimeParseException e) {
                    LOGGER.error("Failed to parse date: {}", row.select("td:nth-of-type(1)").text(), e);
                } catch (NumberFormatException e) {
                    LOGGER.error("Failed to parse numeric values: {}", row.text(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching data from URL: {}", url, e);
        }

        return forexDataList;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value)) {
            return BigDecimal.ZERO;
        }
        try {
            String sanitizedValue = value.replace(",", "").trim();
            return new BigDecimal(sanitizedValue);
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse value to BigDecimal: {}", value, e);
            return BigDecimal.ZERO;
        }
    }
}