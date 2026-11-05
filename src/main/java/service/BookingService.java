package service;

import model.Booking;
import model.Flight;
import model.Passenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.SeatRepository;

public class BookingService {
    //  Создаем Logger
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final SeatRepository seatRepository;

    public BookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public Booking bookSeat(Passenger passenger, Flight flight, int seatNumber) {
        // INFO: Начало операции
        log.info("INFO: Запрос на бронирование: пассажир {}, рейс {}, место {}",
                passenger.getName(), flight.getFlightNumber(), seatNumber);

        if (seatNumber < 1 || seatNumber > flight.getTotalSeats()) {
            // ERROR: Некорректные данные
            log.error("Ошибка бронирования: некорректный номер места {}", seatNumber);
            throw new IllegalArgumentException("Некорректный номер места: " + seatNumber);
        }

        if (seatNumber == 13) {
            log.error("Ошибка бронирования: место 13 недоступно");
            throw new IllegalArgumentException("Место 13 не доступно для бронирования");
        }

        if (!seatRepository.isSeatAvailable(flight, seatNumber)) {
            // WARN: Место занято (не критическая ошибка системы, но проблема для пользователя)
            log.warn("Место {} уже занято на рейсе {}", seatNumber, flight.getFlightNumber());
            throw new IllegalStateException("Место " + seatNumber + " уже забронировано на рейс "
                    + flight.getFlightNumber());
        }

        boolean alreadyBooked = seatRepository.findBookingsByFlight(flight).stream()
                .anyMatch(b -> b.getPassenger().equals(passenger));
        if (alreadyBooked) {
            log.warn("Пассажир {} уже имеет бронь на рейс {}", passenger.getName(), flight.getFlightNumber());
            throw new IllegalStateException("Пассажир " + passenger.getName() + " уже имеет бронь на рейс "
                    + flight.getFlightNumber());
        }

        Booking booking = new Booking(passenger, flight, seatNumber);
        seatRepository.bookSeat(flight, booking);

        // INFO: Успешное завершение
        log.info("Бронирование успешно создано: место {}", seatNumber);
        return booking;
    }

    // НОВЫЙ МЕТОД: Отмена бронирования
    public void cancelBooking(Flight flight, int seatNumber) {
        log.info("Запрос на отмену бронирования: рейс {}, место {}", flight.getFlightNumber(), seatNumber);

        if (seatRepository.isSeatAvailable(flight, seatNumber)) {
            log.warn("Попытка отменить несуществующую бронь на рейс {}, место {}", flight.getFlightNumber(), seatNumber);
            throw new IllegalStateException("Бронь на место " + seatNumber + " не найдена");
        }

        seatRepository.cancelBooking(flight, seatNumber);
        log.info("Бронирование на место {} успешно отменено", seatNumber);
    }

    public int getAvailableSeatsCount(Flight flight) {
        return flight.getTotalSeats() - seatRepository.countBookedSeats(flight);
    }
}