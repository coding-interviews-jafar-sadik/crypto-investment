package com.xm.crypto.service;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;

import static com.xm.crypto.support.TestBuilders.priceHistory;
import static com.xm.crypto.support.TestBuilders.priceRangeDetails;
import static com.xm.crypto.support.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoServiceImplTest {

    CryptoService cryptoService;

    @Mock
    CryptoRepository repository;

    @BeforeEach
    void init() {
        cryptoService = new CryptoServiceImpl(repository);
    }

    @Test
    void shouldCalculatePriceRangeDetailsForGivenSymbol() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC));
        when(repository.loadPriceHistory(BTC, DateRange.unbounded())).thenReturn(
                priceHistory(1f, 0.5f, 10.5f, 5f, 4f, 8f, 10f)
        );

        StepVerifier.create(cryptoService.calculatePriceRangeDetails(BTC))
                .expectNext(priceRangeDetails(BTC, 1, 10, 0.5f, 10.5f))
                .verifyComplete();
    }

    @Test
    void shouldRankCryptosDescendingByNormalizedRange() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH, LTC, XRP));
        when(repository.loadPriceHistory(BTC, DateRange.unbounded())).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadPriceHistory(ETH, DateRange.unbounded())).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));
        when(repository.loadPriceHistory(LTC, DateRange.unbounded())).thenReturn(priceHistory(9f, 9.5f, 10f));
        when(repository.loadPriceHistory(XRP, DateRange.unbounded())).thenReturn(priceHistory(1f, 2f, 4f, 2f));

        StepVerifier.create(cryptoService.rankCryptos(Integer.MAX_VALUE, DateRange.unbounded()))
                .expectNextMatches(symbol(XRP))
                .expectNextMatches(symbol(ETH))
                .expectNextMatches(symbol(LTC))
                .expectNextMatches(symbol(BTC))
                .verifyComplete();
    }

    @Test
    void testLimitRankSizeToZero() {
        StepVerifier.create(cryptoService.rankCryptos(0, DateRange.unbounded()))
                .verifyComplete();
    }

    @Test
    void testLimitRankSizeToOne() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH));
        when(repository.loadPriceHistory(BTC, DateRange.unbounded())).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadPriceHistory(ETH, DateRange.unbounded())).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));

        StepVerifier.create(cryptoService.rankCryptos(1, DateRange.unbounded()))
                .expectNextMatches(symbol(ETH))
                .verifyComplete();
    }

    private Predicate<PriceRangeDetails> symbol(String symbol) {
        return priceRangeDetails -> priceRangeDetails.getSymbol().equals(symbol);
    }

    @Test
    void shouldFailForUnknownCrypto() {
        assertThatThrownBy(() -> cryptoService.calculatePriceRangeDetails("unknown_symbol"))
                .isInstanceOf(UnknownSymbolRuntimeException.class);
    }
}