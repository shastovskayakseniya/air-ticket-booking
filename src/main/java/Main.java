import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import com.sun.net.httpserver.HttpServer;
import model.Booking;
import model.Flight;
import model.Passenger;
import repository.InMemorySeatRepository;
import repository.SeatRepository;
import service.BookingService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        // 1. Создаем реестр метрик
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // 2. Передаем его в сервис
        SeatRepository repository = new InMemorySeatRepository();
        BookingService bookingService = new BookingService(repository, registry);

        // 3. Запускаем HTTP-сервер на порту 8081
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/metrics", exchange -> {
            String response = registry.scrape(); // Получаем метрики в формате Prometheus
            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер метрик запущен на http://localhost:8081/metrics");

        // 4. Выполняем операции для генерации метрик
        Flight flight = new Flight("SU-1234", "Moscow", "London", 10);
        Passenger ivan = new Passenger(1, "Ivan");

        bookingService.bookSeat(ivan, flight, 5); // Успешно
        bookingService.bookSeat(ivan, flight, 6);
        bookingService.bookSeat(ivan, flight, 7);// Новая операция
        try { bookingService.cancelBooking(flight, 8); } catch (Exception e) {} // Ошибка
        bookingService.cancelBooking(flight, 5); // Успешно

        System.out.println("Операции выполнены. Нажмите Enter для выхода...");
        System.in.read(); // Держим сервер запущенным
        server.stop(0);
    }
}