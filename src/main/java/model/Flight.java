package model;

public class Flight {
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final int totalSeats;

    public Flight(String flightNumber, String origin, String destination, int totalSeats) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.totalSeats = totalSeats;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public int getTotalSeats() { return totalSeats; }

    @Override
    public String toString() {
        return "Flight{" + flightNumber + ": " + origin + " -> " + destination + "}";
    }
}