package com.xm.crypto.service;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.repository.CryptoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CryptoServiceImpl implements CryptoService {

    private final CryptoRepository cryptoRepository;

    public CryptoServiceImpl(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public Mono<PriceRangeDetails> getPriceDetails(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        ensureValidSymbol(cryptoSymbol);
        return cryptoRepository.loadFullPriceHistory(cryptoSymbol)
                .reduce(new MutablePriceRangeDetails(), (acc, value) -> {
                    acc.minPrice = acc.hasMin() ? acc.minPrice.min(value.getPrice()) : value.getPrice();
                    acc.maxPrice = acc.hasMax() ? acc.maxPrice.max(value.getPrice()) : value.getPrice();
                    acc.oldestPrice = acc.hasOldestPrice() ? acc.oldestPrice : value.getPrice();
                    acc.newestPrice = value.getPrice();
                    return acc;
                })
                .map(it -> new PriceRangeDetails(cryptoSymbol.toUpperCase(), it.oldestPrice, it.newestPrice, it.minPrice, it.maxPrice));
    }

    private void ensureValidSymbol(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        if (!cryptoRepository.getSupportedSymbols().contains(cryptoSymbol.toUpperCase())) {
            throw new UnknownSymbolRuntimeException();
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