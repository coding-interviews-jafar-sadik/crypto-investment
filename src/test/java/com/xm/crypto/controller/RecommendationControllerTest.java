package com.xm.crypto.controller;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.repository.CryptoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

import static com.xm.crypto.utils.PriceSnapshotTestUtil.priceHistory;
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
                new PriceRangeDetails(
                        "BTC",
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        new BigDecimal("0.5"),
                        new BigDecimal("20.5"))
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