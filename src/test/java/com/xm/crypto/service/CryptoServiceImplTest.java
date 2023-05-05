package com.xm.crypto.service;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import static com.xm.crypto.utils.PriceSnapshotTestUtil.priceHistory;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
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
        when(repository.getSupportedSymbols()).thenReturn(List.of("BTC"));
        when(repository.loadFullPriceHistory("BTC")).thenReturn(
                priceHistory(1f, 0.5f, 10.5f, 5f, 4f, 8f, 10f)
        );

        StepVerifier.create(cryptoService.getPriceDetails("BTC"))
                .expectNext(new PriceRangeDetails("BTC", ONE, TEN, new BigDecimal("0.5"), new BigDecimal("10.5")))
                .verifyComplete();
    }

    @Test
    void shouldRankCryptosDescendingByNormalizedRange() {
        when(repository.getSupportedSymbols()).thenReturn(List.of("BTC", "ETH", "LTC", "XRP"));
        when(repository.loadFullPriceHistory("BTC")).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadFullPriceHistory("ETH")).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));
        when(repository.loadFullPriceHistory("LTC")).thenReturn(priceHistory(9f, 9.5f, 10f));
        when(repository.loadFullPriceHistory("XRP")).thenReturn(priceHistory(1f, 2f, 4f, 2f));

        StepVerifier.create(cryptoService.rankCryptos())
                .expectNextMatches(symbol("XRP"))
                .expectNextMatches(symbol("ETH"))
                .expectNextMatches(symbol("LTC"))
                .expectNextMatches(symbol("BTC"))
                .verifyComplete();
    }

    private Predicate<PriceRangeDetails> symbol(String symbol) {
        return e -> e.getSymbol().equals(symbol);
    }

    @Test
    void shouldFailForUnknownCrypto() {
        assertThatThrownBy(() -> cryptoService.getPriceDetails("unknown_symbol"))
                .isInstanceOf(UnknownSymbolRuntimeException.class);
    }

}