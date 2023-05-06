package com.xm.crypto.repository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceSnapshot;
import com.xm.crypto.enums.SupportedCryptocurrencies;
import com.xm.crypto.exceptions.GenericApplicationRuntimeException;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

@Slf4j
@Repository
public class CryptoRepositoryImpl implements CryptoRepository {

    @Value("${price-data-path}")
    private String priceDataPath;

    @Override
    public List<String> getSupportedSymbols() {
        return Arrays.stream(SupportedCryptocurrencies.values())
                .map(Enum::toString)
                .collect(Collectors.toList());
    }

    @Override
    public Flux<PriceSnapshot> loadPriceHistory(String symbol, DateRange dateRange) {
        int tableHeaderRow = 1;
        int timestampColumn = 0;
        int priceColumn = 2;

        try (InputStream inputStream = getResourceURL(symbol).openStream(); Reader reader = new InputStreamReader(inputStream)) {
            CSVReader csvReader = new CSVReader(reader);
            return Flux.fromStream(csvReader.readAll().stream()
                    .skip(tableHeaderRow)
                    .map(csv -> new PriceSnapshot(toLocalDateTime(parseLong(csv[timestampColumn])), new BigDecimal(csv[priceColumn])))
                    .filter(priceSnapshot -> dateRange.isWithinRange(priceSnapshot.getTimestamp()))
            );
        } catch (IOException | CsvException e) {
            log.warn("Failed to load price history for symbol '{}'", symbol, e);
            throw new GenericApplicationRuntimeException(e);
        }
    }

    private URL getResourceURL(String cryptoSymbol) {
        String resourcePath = priceDataPath + cryptoSymbol.toUpperCase() + "_values.csv";
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            log.info("Resource doesn't exist: " + resourcePath);
            throw new UnknownSymbolRuntimeException(cryptoSymbol);
        }
        return resource;
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }
}
