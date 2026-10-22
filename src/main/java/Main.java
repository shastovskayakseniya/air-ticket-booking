import model.Booking;
import model.Flight;
import model.Passenger;
import repository.InMemorySeatRepository;
import repository.SeatRepository;
import service.BookingService;

public class Main {
    public static void main(String[] args) {
        SeatRepository repository = new InMemorySeatRepository();
        BookingService bookingService = new BookingService(repository);

        Flight flight = new Flight("SU-1234", "Moscow", "London", 10);
        Passenger ivan = new Passenger(1, "Ivan");
        Passenger anna = new Passenger(2, "Anna");

        Booking b1 = bookingService.bookSeat(ivan, flight, 5);
        System.out.println("Успешно: " + b1);
        System.out.println("Свободных мест: "
                + bookingService.getAvailableSeatsCount(flight));

        try {
            bookingService.bookSeat(anna, flight, 5);
        } catch (IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        Booking b2 = bookingService.bookSeat(anna, flight, 7);
        System.out.println("Успешно: " + b2);
        System.out.println("Свободных мест: "
                + bookingService.getAvailableSeatsCount(flight));
    }
}