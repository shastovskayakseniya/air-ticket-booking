import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import com.sun.net.httpserver.HttpServer;
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
        bookingService.bookSeat(ivan, flight, 7);
        bookingService.bookSeat(ivan, flight, 8);
        bookingService.bookSeat(ivan, flight, 9);
        bookingService.bookSeat(ivan, flight, 10);
        Passenger oleg = new Passenger(3, "Oleg");
        Passenger olga = new Passenger(4, "Olga");
        bookingService.bookSeat(ivan, flight, 4);
        try {
            bookingService.cancelBooking(flight, 9); // Пытаемся отменить то, чего нет
        } catch (Exception e) {}

        bookingService.cancelBooking(flight, 6);

        try {
            bookingService.cancelBooking(flight, 10); // Пытаемся отменить то, чего нет
        } catch (Exception e) {}

        try { bookingService.cancelBooking(flight, 8); } catch (Exception e) {} // Ошибка
        bookingService.cancelBooking(flight, 5); // Успешно


        Flight newFlight = new Flight("SU-9999", "Minsk", "Paris", 20);
        Passenger maria = new Passenger(5, "Maria");
        bookingService.bookSeat(maria, newFlight, 1);
       // Passenger pavel = new Passenger(6, "Pavel");
       // bookingService.bookSeat(pavel, newFlight, 2);



        try {
            bookingService.cancelBooking(newFlight, 15);
        } catch (Exception e) {}

        try {
            bookingService.cancelBooking(newFlight, 16);
        } catch (Exception e) {}




        System.out.println("Операции выполнены. Нажмите Enter для выхода...");
        System.in.read(); // Держим сервер запущенным
        server.stop(0);
    }
}