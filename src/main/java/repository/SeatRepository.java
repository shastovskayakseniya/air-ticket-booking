package repository;
import model.Booking;
import model.Flight;
import java.util.List;

public interface SeatRepository {
    boolean isSeatAvailable(Flight flight, int seatNumber);
    void bookSeat(Flight flight, Booking booking);
    void cancelBooking(Flight flight, int seatNumber); // НОВЫЙ МЕТОД
    List<Booking> findBookingsByFlight(Flight flight);
    int countBookedSeats(Flight flight);
}