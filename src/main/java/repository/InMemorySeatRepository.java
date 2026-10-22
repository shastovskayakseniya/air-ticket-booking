package repository;

import model.Booking;
import model.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemorySeatRepository implements SeatRepository {

    private final Map<String, List<Booking>> storage = new HashMap<>();

    @Override
    public boolean isSeatAvailable(Flight flight, int seatNumber) {
        List<Booking> bookings = storage.getOrDefault(
                flight.getFlightNumber(), new ArrayList<>());
        return bookings.stream()
                .noneMatch(b -> b.getSeatNumber() == seatNumber);
    }

    @Override
    public void bookSeat(Flight flight, Booking booking) {
        storage.computeIfAbsent(flight.getFlightNumber(), k -> new ArrayList<>())
                .add(booking);
    }

    @Override
    public List<Booking> findBookingsByFlight(Flight flight) {
        return new ArrayList<>(
                storage.getOrDefault(flight.getFlightNumber(), new ArrayList<>()));
    }

    @Override
    public int countBookedSeats(Flight flight) {
        return storage.getOrDefault(flight.getFlightNumber(), new ArrayList<>()).size();
    }
}