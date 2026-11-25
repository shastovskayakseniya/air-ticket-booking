package service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import model.Booking;
import model.Flight;
import model.Passenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.SeatRepository;

public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final SeatRepository seatRepository;

    // --- МЕТРИКИ ---
    private final Counter totalOps;
    private final Counter successOps;
    private final Counter errorOps;
    private final Counter bookingCreated;
    private final Counter bookingCancelled;

    public BookingService(SeatRepository seatRepository, MeterRegistry registry) {
        this.seatRepository = seatRepository;

        // Регистрируем метрики в системе
        this.totalOps = registry.counter("application_operations_total");
        this.successOps = registry.counter("application_operations_success_total");
        this.errorOps = registry.counter("application_operations_error_total");
        // Индивидуальные метрики
        this.bookingCreated = registry.counter("booking_created_total");
        this.bookingCancelled = registry.counter("booking_cancelled_total");
    }

    public Booking bookSeat(Passenger passenger, Flight flight, int seatNumber) {
        totalOps.increment(); // Считаем операцию

        log.info("INFO: Запрос на бронирование...");
        if (seatNumber < 1 || seatNumber > flight.getTotalSeats() || seatNumber == 13) {
            errorOps.increment(); // Ошибка
            log.error("ERROR: Ошибка бронирования...");
            throw new IllegalArgumentException("Некорректный номер места");
        }

        if (!seatRepository.isSeatAvailable(flight, seatNumber)) {
            errorOps.increment(); // Ошибка
            log.warn("WARN: Место занято...");
            throw new IllegalStateException("Место уже забронировано");
        }

        Booking booking = new Booking(passenger, flight, seatNumber);
        seatRepository.bookSeat(flight, booking);

        successOps.increment(); // Успех
        bookingCreated.increment(); // Индивидуальная метрика создания
        log.info("INFO: Бронирование создано");
        return booking;
    }

    public void cancelBooking(Flight flight, int seatNumber) {
        totalOps.increment(); // Считаем операцию

        log.info("INFO: Запрос на отмену...");
        if (seatRepository.isSeatAvailable(flight, seatNumber)) {
            errorOps.increment(); // Ошибка
            log.warn("WARN: Попытка отменить несуществующую бронь...");
            throw new IllegalStateException("Бронь не найдена");
        }

        seatRepository.cancelBooking(flight, seatNumber);

        successOps.increment(); // Успех
        bookingCancelled.increment(); // Индивидуальная метрика отмены
        log.info("INFO: Бронирование отменено");
    }

    public int getAvailableSeatsCount(Flight flight) {
        return flight.getTotalSeats() - seatRepository.countBookedSeats(flight);
    }
}