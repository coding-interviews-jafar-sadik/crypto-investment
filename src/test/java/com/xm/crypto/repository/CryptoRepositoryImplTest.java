package com.xm.crypto.repository;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static com.xm.crypto.support.TestBuilders.priceSnapshot;
import static com.xm.crypto.support.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CryptoRepositoryImplTest {

    @Autowired
    CryptoRepository repository;

    @Test
    void shouldReturnListOfSupportedCryptocurrencies() {
        assertThat(repository.getSupportedSymbols()).containsExactlyInAnyOrder(
                BTC, DOGE, ETH, LTC, XRP
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {BTC, DOGE, ETH, LTC, XRP})
    void shouldReturnPriceHistoryForEachSupportedCrypto(String symbol) {
        StepVerifier.create(repository.loadPriceHistory(symbol, DateRange.unbounded()))
                .thenConsumeWhile(priceSnapshot -> true)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldReturnFullPriceHistoryForCrypto() {
        StepVerifier.create(repository.loadPriceHistory(BTC, DateRange.unbounded()))
                .expectNext(priceSnapshot("2022-01-01T05:00", "46813.21"))
                .expectNext(priceSnapshot("2022-01-01T08:00", "46979.61"))
                .expectNext(priceSnapshot("2022-01-01T11:00", "47143.98"))
                .expectNext(priceSnapshot("2022-01-09T19:40", "46871.09"))
                .expectNext(priceSnapshot("2022-01-09T19:40:10", "46871.10"))
                .expectNext(priceSnapshot("2022-01-09T19:40:20", "46871.11"))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldNarrowDownPriceHistoryByDateRange() {
        LocalDate DATE_2022_01_08 = LocalDate.parse("2022-01-08");
        LocalDate DATE_2022_01_10 = LocalDate.parse("2022-01-10");

        StepVerifier.create(repository.loadPriceHistory(BTC, DateRange.of(DATE_2022_01_08, DATE_2022_01_10)))
                .expectNext(priceSnapshot("2022-01-09T19:40", "46871.09"))
                .expectNext(priceSnapshot("2022-01-09T19:40:10", "46871.10"))
                .expectNext(priceSnapshot("2022-01-09T19:40:20", "46871.11"))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldReturnEmptyPriceHistoryWhenAllEntriesAreOutsideAGivenDateRange() {
        LocalDate DATE_2021_12_30 = LocalDate.parse("2021-12-30");

        StepVerifier.create(repository.loadPriceHistory(BTC, DateRange.of(DateRange.MIN, DATE_2021_12_30)))
                .expectComplete()
                .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {DOGE, LTC})
    void shouldHandleEmptyPriceHistory(String symbol) {
        StepVerifier.create(repository.loadPriceHistory(symbol, DateRange.unbounded()))
                .expectComplete()
                .verify();
    }

    @Test
    void unknownSymbolTriggersRuntimeException() {
        assertThatThrownBy(() -> repository.loadPriceHistory("unknown_symbol", DateRange.unbounded()))
                .isInstanceOf(UnknownSymbolRuntimeException.class);
    }
}