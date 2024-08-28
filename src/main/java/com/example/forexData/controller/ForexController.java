package com.example.forexData.controller;

import com.example.forexData.model.ForexData;
import com.example.forexData.repository.ForexDataRepository;
import com.example.forexData.service.ForexScraperService;
import com.example.forexData.util.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "https://forex-historical-data-rest-api-1.onrender.com")
@RestController
@RequestMapping("/api/forex-data")
@Tag(name = "Forex Data Scraping")
public class ForexController {

    @Autowired
    private ForexScraperService forexScraperService;

    @Autowired
    private ForexDataRepository forexDataRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForexController.class);

    @Operation(
            summary = "Scrape historical exchange data and store it in the database",
            description = "This endpoint scrapes historical exchange data from Yahoo Finance and stores it in an in-memory H2 database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Data scraped and stored successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "[\n" +
                                            "  {\n" +
                                            "    \"id\": 1,\n" +
                                            "    \"currencyPair\": \"INREUR=X\",\n" +
                                            "    \"date\": \"2024-08-26\",\n" +
                                            "    \"open\": 0.0107,\n" +
                                            "    \"high\": 0.0107,\n" +
                                            "    \"low\": 0.0107,\n" +
                                            "    \"close\": 0.0107,\n" +
                                            "    \"adjClose\": 0.0107,\n" +
                                            "    \"volume\": 0\n" +
                                            "  }\n" +
                                            "]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\n\"msg\": \"Invalid input parameters\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\n\"msg\": \"Failed to scrape data\"\n}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Object> scrapeAndSaveExchangeRates(
            @RequestParam @Parameter(description = "The currency code representing the source currency to be converted.", example = "GBP") String from,
            @RequestParam @Parameter(description = "The currency code representing the target currency for conversion.", example = "INR") String to,
            @RequestParam @Parameter(description = "The timeframe for the historical data", schema = @Schema(allowableValues = {"1W", "1M", "3M", "6M", "9M", "1Y"}), example = "1W") String period) {

        LOGGER.info("Received request to scrape and save exchange rates from {} to {} for period {}", from, to, period);

        try {
            Period periodEnum = Period.fromValue(period);

            if (from.length() != 3 || to.length() != 3 ||
                    !forexScraperService.isValidCurrencyCode(from) ||
                    !forexScraperService.isValidCurrencyCode(to)) {
                String errorMessage = "Invalid input parameters";
                LOGGER.error("Invalid input parameters: from={}, to={}, period={}", from, to, period);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", errorMessage));
            }

            List<ForexData> forexData = forexScraperService.scrapeAndSaveExchangeRates(from, to, periodEnum);

            if (forexData.isEmpty()) {
                String errorMessage = "Failed to scrape data";
                LOGGER.error("Failed to scrape data: No data found for the provided parameters.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", errorMessage));
            }

            LOGGER.info("Successfully scraped and saved exchange rates for {} to {} for period {}", from, to, period);
            return ResponseEntity.ok(forexData);

        } catch (IllegalArgumentException e) {
            String errorMessage = "Invalid input parameters";
            LOGGER.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", errorMessage));
        } catch (Exception e) {
            String errorMessage = "Failed to scrape data";
            LOGGER.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", errorMessage));
        }
    }

    @GetMapping("getForex")
    public ResponseEntity<List<ForexData>> getForexData(){
        return ResponseEntity.ok(forexDataRepository.findAll());
    }
}