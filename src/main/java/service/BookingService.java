package service;

import model.Booking;
import model.Flight;
import model.Passenger;
import repository.SeatRepository;

public class BookingService {

    private final SeatRepository seatRepository;

    public BookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public Booking bookSeat(Passenger passenger, Flight flight, int seatNumber) {
        if (seatNumber < 1 || seatNumber > flight.getTotalSeats()) {
            throw new IllegalArgumentException(
                    "Некорректный номер места: " + seatNumber);
        }

        if (!seatRepository.isSeatAvailable(flight, seatNumber)) {
            throw new IllegalStateException(
                    "Место " + seatNumber + " уже забронировано на рейс "
                            + flight.getFlightNumber());
        }

        boolean alreadyBooked = seatRepository.findBookingsByFlight(flight).stream()
                .anyMatch(b -> b.getPassenger().equals(passenger));
        if (alreadyBooked) {
            throw new IllegalStateException(
                    "Пассажир " + passenger.getName()
                            + " уже имеет бронь на рейс " + flight.getFlightNumber());
        }

        Booking booking = new Booking(passenger, flight, seatNumber);
        seatRepository.bookSeat(flight, booking);
        return booking;
    }

    public int getAvailableSeatsCount(Flight flight) {
        return flight.getTotalSeats() - seatRepository.countBookedSeats(flight);
    }
}