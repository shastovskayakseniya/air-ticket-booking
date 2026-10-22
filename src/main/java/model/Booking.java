package model;

public class Booking {
    private final Passenger passenger;
    private final Flight flight;
    private final int seatNumber;

    public Booking(Passenger passenger, Flight flight, int seatNumber) {
        this.passenger = passenger;
        this.flight = flight;
        this.seatNumber = seatNumber;
    }

    public Passenger getPassenger() { return passenger; }
    public Flight getFlight() { return flight; }
    public int getSeatNumber() { return seatNumber; }

    @Override
    public String toString() {
        return "Booking{" + passenger.getName()
                + " | " + flight.getFlightNumber()
                + " | seat " + seatNumber + "}";
    }
}