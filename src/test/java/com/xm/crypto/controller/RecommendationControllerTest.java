package com.xm.crypto.controller;

import com.xm.crypto.dto.PriceRangeDetails;
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
import java.util.List;

import static com.xm.crypto.utils.PriceSnapshotTestUtil.priceHistory;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationControllerTest {

    @MockBean
    private CryptoRepository repository;

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldCalculateDetailsForRequestedCrypto() {
        when(repository.getSupportedSymbols()).thenReturn(List.of("BTC"));
        when(repository.loadFullPriceHistory("BTC")).thenReturn(priceHistory(1f, 20.5f, 0.5f, 10f));

        PriceRangeDetails response = webClient.get().uri("/cryptos/BTC")
                .exchange().expectBody(PriceRangeDetails.class).returnResult().getResponseBody();

        assertThat(response).usingRecursiveComparison().isEqualTo(
                new PriceRangeDetails("BTC", ONE, TEN, new BigDecimal("0.5"), new BigDecimal("20.5"))
        );
    }

    @Test
    void shouldRankCryptosByNormalizedRange() {
        when(repository.getSupportedSymbols()).thenReturn(List.of("BTC", "ETH", "LTC"));
        when(repository.loadFullPriceHistory("BTC")).thenReturn(priceHistory(1f, 1f, 1f, 1f));
        when(repository.loadFullPriceHistory("ETH")).thenReturn(priceHistory(1f, 2f, 1.5f, 2f));
        when(repository.loadFullPriceHistory("LTC")).thenReturn(priceHistory(9f, 9.5f, 10f));

        List<PriceRangeDetails> response = webClient.get().uri("/cryptos")
                .exchange().expectBody(new ParameterizedTypeReference<List<PriceRangeDetails>>() {
                }).returnResult().getResponseBody();

        assertThat(response).hasSize(3).containsSequence(
                new PriceRangeDetails("ETH", ONE, new BigDecimal("2"), ONE, new BigDecimal("2")),
                new PriceRangeDetails("LTC", new BigDecimal("9"), new BigDecimal("10"), new BigDecimal("9"), new BigDecimal("10")),
                new PriceRangeDetails("BTC", ONE, ONE, ONE, ONE)
        );
    }

    @Test
    void expectHTTP200_when_referringKnownCryptoSymbol() {
        when(repository.getSupportedSymbols()).thenReturn(List.of("BTC"));
        when(repository.loadFullPriceHistory("BTC")).thenReturn(Flux.empty());

        webClient.get().uri("/cryptos/BTC")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Test
    void expectHTTP404_when_referringUnknownCryptoSymbol() {
        webClient.get().uri("/cryptos/UNKNOWN")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

}