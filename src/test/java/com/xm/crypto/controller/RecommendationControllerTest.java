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

import java.util.List;

import static com.xm.crypto.support.TestBuilders.priceHistory;
import static com.xm.crypto.support.TestBuilders.priceRangeDetails;
import static com.xm.crypto.support.TestConstants.*;
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