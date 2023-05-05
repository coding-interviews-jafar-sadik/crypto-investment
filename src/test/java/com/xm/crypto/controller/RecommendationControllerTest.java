package com.xm.crypto.controller;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.dto.PriceSnapshot;
import com.xm.crypto.repository.CryptoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.xm.crypto.support.TestBuilders.priceHistory;
import static com.xm.crypto.support.TestBuilders.priceRangeDetails;
import static com.xm.crypto.support.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationControllerTest {

    private final LocalDateTime DATE_2023_03_01 = LocalDateTime.of(2023, 3, 1, 0, 0);
    private final LocalDateTime DATE_2023_03_05 = LocalDateTime.of(2023, 5, 1, 0, 0);
    private final LocalDateTime DATE_2023_03_10 = LocalDateTime.of(2023, 3, 10, 0, 0);
    private final LocalDateTime DATE_2023_03_15 = LocalDateTime.of(2023, 3, 15, 0, 0);

    @MockBean
    private CryptoRepository repository;

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldCalculatePriceRangeDetailsForRequestedCrypto() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(priceHistory(1f, 20.5f, 0.5f, 10f));

        PriceRangeDetails response = webClient.get().uri("/cryptos/BTC")
                .exchange().expectBody(PriceRangeDetails.class).returnResult().getResponseBody();

        assertThat(response).usingRecursiveComparison().isEqualTo(
                priceRangeDetails(BTC, 1f, 10f, 0.5f, 20.5f)
        );
    }

    @Test
    void shouldCalculatePriceRangeDetailsEvenWhenUsingLowerCaseSymbol() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(priceHistory(1f, 20.5f, 0.5f, 10f));

        webClient.get().uri("/cryptos/btc").exchange().expectStatus().isOk();
    }

    @Test
    void shouldRankCryptosByNormalizedRange() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH, LTC));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadFullPriceHistory(ETH)).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));
        when(repository.loadFullPriceHistory(LTC)).thenReturn(priceHistory(9f, 9.5f, 10f));

        List<PriceRangeDetails> response = webClient.get().uri("/cryptos")
                .exchange().expectBody(new ParameterizedTypeReference<List<PriceRangeDetails>>() {
                }).returnResult().getResponseBody();

        assertThat(response).hasSize(3).containsSequence(
                priceRangeDetails(ETH, 1, 2, 1, 2),
                priceRangeDetails(LTC, 9, 10, 9, 10),
                priceRangeDetails(BTC, 1, 1, 1, 1)
        );
    }

    @Test
    void shouldLimitRankedResultsToDateRange() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(Flux.fromArray(new PriceSnapshot[]{
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_05, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_10, BigDecimal.TEN)
                })
        );
        when(repository.loadFullPriceHistory(ETH)).thenReturn(Flux.fromArray(new PriceSnapshot[]{
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.TEN)
                })
        );

        List<PriceRangeDetails> response = webClient.get().uri("/cryptos?from_date=2023-03-01&to_date=2023-03-10")
                .exchange().expectBody(new ParameterizedTypeReference<List<PriceRangeDetails>>() {
                }).returnResult().getResponseBody();

        assertThat(response).hasSize(1).contains(
                priceRangeDetails(BTC, 1, 10, 1, 10)
        );
    }

    @Test
    void shouldReturnCryptoWithTheHighestNormalizedRangeForASpecificDay() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH, XRP));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(Flux.fromArray(new PriceSnapshot[]{
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.TEN),
                        new PriceSnapshot(DATE_2023_03_01, new BigDecimal(15)),
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_05, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_10, BigDecimal.ONE)
                })
        );
        when(repository.loadFullPriceHistory(ETH)).thenReturn(Flux.fromArray(new PriceSnapshot[]{
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_15, BigDecimal.TEN)
                })
        );

        when(repository.loadFullPriceHistory(XRP)).thenReturn(Flux.fromArray(new PriceSnapshot[]{
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.ONE),
                        new PriceSnapshot(DATE_2023_03_01, BigDecimal.ONE)
                })
        );

        List<PriceRangeDetails> response = webClient.get().uri("/cryptos?limit=1&from_date=2023-03-01&to_date=2023-03-01")
                .exchange().expectBody(new ParameterizedTypeReference<List<PriceRangeDetails>>() {
                }).returnResult().getResponseBody();

        assertThat(response).hasSize(1).contains(
                priceRangeDetails(BTC, 10, 1, 1, 15)
        );
    }

    @Test
    void canLimitRankSizeByOptionalQueryParameter() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC, ETH, LTC));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadFullPriceHistory(ETH)).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));
        when(repository.loadFullPriceHistory(LTC)).thenReturn(priceHistory(9f, 9.5f, 10f));

        List<PriceRangeDetails> response = webClient.get().uri("/cryptos?limit=2")
                .exchange().expectBody(new ParameterizedTypeReference<List<PriceRangeDetails>>() {
                }).returnResult().getResponseBody();

        assertThat(response).hasSize(2);
    }

    @Test
    void expectHTTP200WhenReferringKnownCryptoSymbol() {
        when(repository.getSupportedSymbols()).thenReturn(List.of(BTC));
        when(repository.loadFullPriceHistory(BTC)).thenReturn(Flux.empty());

        webClient.get().uri("/cryptos/BTC")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Test
    void expectHTTP404WhenReferringUnknownCryptoSymbol() {
        webClient.get().uri("/cryptos/UNKNOWN")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }
}