package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        // 1. Créez un ticket avec une entrée il y a moins de 30 minutes
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes avant maintenant
        Date outTime = new Date();

        // 2. Configurez une place de parking de type CAR
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        // 3. Initialisez le ticket avec ces données
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // 4. Appelez la méthode calculateFare
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        fareCalculatorService.calculateFare(ticket);

        // 5. Vérifiez que le prix est bien 0 pour moins de 30 minutes
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        // 1. Créez un ticket avec une entrée il y a moins de 30 minutes
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes avant maintenant
        Date outTime = new Date();

        // 2. Configurez une place de parking de type BIKE
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        // 3. Initialisez le ticket avec ces données
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // 4. Appelez la méthode calculateFare
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        fareCalculatorService.calculateFare(ticket);

        // 5. Vérifiez que le prix est bien 0 pour moins de 30 minutes
        assertEquals(0, ticket.getPrice());
    }
    @Test
    public void calculateFareCarWithDiscount() {
        // 1. Créez un ticket pour une voiture avec une durée > 30 minutes
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - 60 * 60 * 1000); // 1 heure avant maintenant
        Date outTime = new Date();

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // 2. Appelez calculateFare avec discount = true
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        fareCalculatorService.calculateFare(ticket, true);

        // 3. Calculez le prix attendu (95% du tarif plein)
        double expectedPrice = 0.95 * Fare.CAR_RATE_PER_HOUR;

        // 4. Vérifiez que le tarif calculé correspond
        assertEquals(expectedPrice, ticket.getPrice(), 0.01); // tolérance pour les doubles
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        // 1. Créez un ticket pour un vélo avec une durée > 30 minutes
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - 60 * 60 * 1000); // 1 heure avant maintenant
        Date outTime = new Date();

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // 2. Appelez calculateFare avec discount = true
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        fareCalculatorService.calculateFare(ticket, true);

        // 3. Calculez le prix attendu (95% du tarif plein)
        double expectedPrice = 0.95 * Fare.BIKE_RATE_PER_HOUR;

        // 4. Vérifiez que le tarif calculé correspond
        assertEquals(expectedPrice, ticket.getPrice(), 0.01);
    }
}
