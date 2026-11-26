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
        log.info("INFO: Запрос на бронирование: пассажир {}, рейс {}, место {}", passenger.getName(), flight.getFlightNumber(), seatNumber);

        if (seatNumber < 1 || seatNumber > flight.getTotalSeats()) {
            log.error("ERROR: Ошибка бронирования: некорректный номер места {}", seatNumber);
            throw new IllegalArgumentException("Некорректный номер места: " + seatNumber);
        }

        // НОВОЕ БИЗНЕС-ПРАВИЛО (Вариант 13)
        if (seatNumber == 13) {
            log.error("ERROR: Ошибка бронирования: место 13 недоступно");
            throw new IllegalArgumentException("Место 13 не доступно для бронирования");
        }

        if (!seatRepository.isSeatAvailable(flight, seatNumber)) {
            log.warn("WARN: Место {} уже занято на рейсе {}", seatNumber, flight.getFlightNumber());
            throw new IllegalStateException("Место " + seatNumber + " уже забронировано на рейс " + flight.getFlightNumber());
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