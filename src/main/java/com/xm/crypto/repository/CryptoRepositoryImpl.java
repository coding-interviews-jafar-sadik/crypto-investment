package com.xm.crypto.repository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.xm.crypto.dto.PriceSnapshot;
import com.xm.crypto.exceptions.GenericApplicationRuntimeException;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
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
import java.time.ZoneId;
import java.util.List;

import static java.lang.Long.parseLong;

@Repository
public class CryptoRepositoryImpl implements CryptoRepository {

    @Override
    public List<String> getSupportedSymbols() {
        return List.of("BTC", "DOGE", "ETH", "LTC", "XRP");
    }

    @Override
    public Flux<PriceSnapshot> loadFullPriceHistory(String symbol) {
        try (InputStream inputStream = getResourceURL(symbol).openStream(); Reader reader = new InputStreamReader(inputStream)) {
            CSVReader csvReader = new CSVReader(reader);
            return Flux.fromStream(csvReader.readAll().stream()
                    .skip(1)
                    .map(entry -> new PriceSnapshot(toLocalDateTime(parseLong(entry[0])), new BigDecimal(entry[2])))
            );
        } catch (IOException | CsvException e) {
            throw new GenericApplicationRuntimeException(e);
        }
    }

    private URL getResourceURL(String cryptoSymbol) {
        String resourcePath = "prices/" + cryptoSymbol + "_values.csv";
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new UnknownSymbolRuntimeException("Resource doesn't exist: " + "prices/" + cryptoSymbol + "_values.csv");
        }
        return resource;
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

}
