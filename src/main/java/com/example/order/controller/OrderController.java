package com.example.order.controller;

import com.example.order.client.DeliveryClient;
import com.example.order.client.PaymentClient;
import com.example.order.client.StockClient;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class OrderController {

    private final StockClient stockClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final WebClient webClient;
    private final String stockServiceUrl;
    private final String paymentServiceUrl;
    private final String deliveryServiceUrl;
    private final String bookServiceUrl;

    public OrderController(
            StockClient stockClient,
            PaymentClient paymentClient,
            DeliveryClient deliveryClient,
            WebClient.Builder webClientBuilder,
            @Value("${services.stock.url}") String stockServiceUrl,
            @Value("${services.payment.url}") String paymentServiceUrl,
            @Value("${services.delivery.url}") String deliveryServiceUrl,
            @Value("${services.book.url}") String bookServiceUrl) {
        this.stockClient = stockClient;
        this.paymentClient = paymentClient;
        this.deliveryClient = deliveryClient;
        this.webClient = webClientBuilder.build();
        this.stockServiceUrl = stockServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
        this.bookServiceUrl = bookServiceUrl;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/order";
    }

    @GetMapping("/order")
    public String order(Model model) {
        long start = System.nanoTime();

        String stock = stockClient.stock();
        String payment = paymentClient.payment();
        String delivery = deliveryClient.delivery();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        model.addAttribute("results", List.of(stock, payment, delivery));
        model.addAttribute("elapsedMs", elapsedMs);

        return "index";
    }

    @GetMapping("/books")
    public String books(Model model) {
        long start = System.nanoTime();
        
        List<BookResponse> books = Flux.fromIterable(fetchBooks())
                .collectList()
                .block();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        model.addAttribute("books", Objects.requireNonNullElse(books, List.of()));
        model.addAttribute("elapsedMs", elapsedMs);
        return "index";
    }

      private List<BookResponse> fetchBooks() {
        List<BookResponse> books = webClient
            .get()
            .uri(combineUrl(bookServiceUrl, "/books"))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<BookResponse>>() {})
            .onErrorReturn(List.of())
            .block();

        return Objects.requireNonNullElse(books, List.of());
    }


   @GetMapping("/flux")
public String flux(Model model) {
    long start = System.nanoTime();

    List<String> results = Mono.zip(
            callStep(new StepRequest(stockServiceUrl, "/stock")),
            callStep(new StepRequest(paymentServiceUrl, "/payment")),
            callStep(new StepRequest(deliveryServiceUrl, "/delivery"))
    ).map(tuple -> List.of(tuple.getT1(), tuple.getT2(), tuple.getT3()))
     .block();

    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    model.addAttribute("results", results);
    model.addAttribute("elapsedMs", elapsedMs);
    return "index";
}

  
    private String combineUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        return baseUrl + path;
    }

    private reactor.core.publisher.Mono<String> callStep(StepRequest request) {
        return webClient
                .get()
                .uri(combineUrl(request.baseUrl(), request.path()))
                .retrieve()
                .bodyToMono(String.class);
    }

    private record StepRequest(String baseUrl, String path) {}

    private record BookResponse(int id, String name, String author, int price) {}
}
