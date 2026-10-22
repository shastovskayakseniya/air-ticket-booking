package service;

import model.Booking;
import model.Flight;
import model.Passenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.InMemorySeatRepository;
import repository.SeatRepository;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты сервиса бронирования авиабилетов")
class BookingServiceTest {

    private BookingService service;
    private Flight flight;
    private Passenger ivan;
    private Passenger anna;

    @BeforeEach
    void setUp() {
        SeatRepository repository = new InMemorySeatRepository();
        service = new BookingService(repository);

        flight = new Flight("SU-1234", "Moscow", "London", 10);
        ivan = new Passenger(1, "Ivan");
        anna = new Passenger(2, "Anna");
    }

    @Test
    @DisplayName("Успешное бронирование свободного места")
    void testSuccessfulBooking() {
        Booking booking = service.bookSeat(ivan, flight, 5);

        assertNotNull(booking);
        assertEquals(ivan, booking.getPassenger());
        assertEquals(flight, booking.getFlight());
        assertEquals(5, booking.getSeatNumber());
    }

    @Test
    @DisplayName("Нельзя забронировать уже занятое место")
    void testBookingAlreadyTakenSeat() {
        service.bookSeat(ivan, flight, 5);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.bookSeat(anna, flight, 5)
        );
        assertTrue(ex.getMessage().contains("уже забронировано"));
    }


    @Test
    @DisplayName("Подсчёт свободных мест уменьшается после бронирования")
    void testAvailableSeatsCount() {
        assertEquals(10, service.getAvailableSeatsCount(flight));

        service.bookSeat(ivan, flight, 1);
        assertEquals(9, service.getAvailableSeatsCount(flight));

        service.bookSeat(anna, flight, 2);
        assertEquals(8, service.getAvailableSeatsCount(flight));
    }
}