# Crypto Investment

## Development notes
The project was created using `OpenJDK Runtime Environment Temurin-11.0.15+10` and `Gradle 7.6.1` 
with a compatible Spring Boot version `2.7.11`.

It can be built directly with Gradle wrapper `./gradlew build` and then run `java -jar build/libs/crypto-recommendation-service.jar`.

Another option is to run the most recent image from Docker Hub `docker run --pull always -p 8080:8080 dzafarsadik/crypto-recommendation-service:latest`

You will be able to access the application with web browser or even command line using tools like for example HTTPie: 
```
http :8080/cryptos/BTC
http :8080/cryptos
http get localhost:8080/cryptos limit==1
http get localhost:8080/cryptos limit==1 from_date==2022-01-01 to_date==2022-01-01
```

Swagger documentation can be accessed [here](http://localhost:8080/swagger-ui.html#/recommendation-controller)

## Description
It’s time for XM developers to invest their salaries on cryptos. The problem is that we have no idea of cryptos, so we are feeling a little bit afraid which crypto to choose. But is this actually a problem? Of course not! We are developers and we always implement solutions for all the problems we face.
For this one, we decided to build a recommendation service. Initially, we will build something simple and through iterations we are going to transform it to a gold miner.
In the CRYPTO_NAME_values.csv (e.g. BTC_values.csv) you can find one month’s prices for one crypto in USD. The file has the following format:

| timestamp        | symbol | price    |
|------------------|--------|----------|
| 1641000960000000 | BTC    | 46813.21 |

There are separate files for each crypto.

## Requirements for the recommendation service
- Reads all the prices from the csv files
- Calculates oldest/newest/min/max for each crypto for the whole month
- Exposes an endpoint that will return a descending sorted list of all the cryptos,
comparing the normalized range (i.e. (max-min)/min)
- Exposes an endpoint that will return the oldest/newest/min/max values for a requested
crypto
- Exposes an endpoint that will return the crypto with the highest normalized range for a
specific day

## Things to consider
- Documentation is our best friend, so it will be good to share one for the endpoints
- Initially the cryptos are only five, but what if we want to include more? Will the
recommendation service be able to scale?
- New cryptos pop up every day, so we might need to safeguard recommendations service
endpoints from not currently supported cryptos
- For some cryptos it might be safe to invest, by just checking only one month's time
frame. However, for some of them it might be more accurate to check six months or even a year. Will the recommendation service be able to handle this?

## Extra mile for recommendation service (optional)
- In XM we run everything on Kubernetes, so containerizing the recommendation service will add great value
- Malicious users will always exist, so it will be really beneficial if at least we can rate limit them (based on IP)
