package com.xm.crypto.service;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.repository.CryptoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CryptoServiceImpl implements CryptoService {

    private final CryptoRepository cryptoRepository;

    public CryptoServiceImpl(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public Mono<PriceRangeDetails> calculatePriceRangeDetails(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        ensureCryptoSymbolIsSupported(cryptoSymbol);
        return cryptoRepository.loadFullPriceHistory(cryptoSymbol.toUpperCase())
                .reduce(new MutablePriceRangeDetails(), (acc, value) -> {
                    acc.minPrice = acc.hasMin() ? acc.minPrice.min(value.getPrice()) : value.getPrice();
                    acc.maxPrice = acc.hasMax() ? acc.maxPrice.max(value.getPrice()) : value.getPrice();
                    acc.oldestPrice = acc.hasOldestPrice() ? acc.oldestPrice : value.getPrice();
                    acc.newestPrice = value.getPrice();
                    return acc;
                })
                .map(it -> new PriceRangeDetails(cryptoSymbol.toUpperCase(), it.oldestPrice, it.newestPrice, it.minPrice, it.maxPrice));
    }

    @Override
    public Flux<PriceRangeDetails> rankCryptos() {
        return Flux.fromIterable(cryptoRepository.getSupportedSymbols())
                .flatMap(this::calculatePriceRangeDetails)
                .sort((o1, o2) -> Float.compare(o2.normalizedRange(), o1.normalizedRange()));
    }

    private void ensureCryptoSymbolIsSupported(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        if (!cryptoRepository.getSupportedSymbols().contains(cryptoSymbol.toUpperCase())) {
            throw new UnknownSymbolRuntimeException(cryptoSymbol);
        }
    }

    private static class MutablePriceRangeDetails {
        BigDecimal oldestPrice;

        BigDecimal newestPrice;

        BigDecimal minPrice;

        BigDecimal maxPrice;

        boolean hasOldestPrice() {
            return oldestPrice != null;
        }

        boolean hasMin() {
            return minPrice != null;
        }

        boolean hasMax() {
            return maxPrice != null;
        }
    }
}
