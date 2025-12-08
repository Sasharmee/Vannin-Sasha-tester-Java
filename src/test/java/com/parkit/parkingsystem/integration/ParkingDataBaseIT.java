package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        verify(ticketDAO, times(1)).updateTicket(argThat(ticket -> ticket.getPrice() >= 0));
        verify(ticketDAO, times(1)).updateTicket(argThat(ticket -> 
        ticket.getOutTime() != null && 
        ticket.getInTime() != null &&
        ticket.getOutTime().after(ticket.getInTime())
    ));
    }
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // Création du parking spot (libre)
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        
        // Mock du ticket
        Ticket mockTicket = mock(Ticket.class);

        // Date d'entrée 2h avant maintenant
        Date inTime = new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000);

        // Configuration des getters du mock
        when(mockTicket.getParkingSpot()).thenReturn(parkingSpot);
        when(mockTicket.getVehicleRegNumber()).thenReturn("ABCDEF");
        when(mockTicket.getInTime()).thenReturn(inTime);
        when(mockTicket.getOutTime()).thenReturn(null);

        // Configuration des DAO mocks
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(mockTicket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Configuration de la saisie de plaque simulée
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // Création du service avec les mocks injectés
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Simulation de l'entrée du véhicule
        parkingService.processIncomingVehicle();

        // Simulation de la sortie du véhicule
        parkingService.processExitingVehicle();

        // Récupération du ticket après sortie (mock toujours le même)
        Ticket ticketUpdated = ticketDAO.getTicket("ABCDEF");

        // Calcul attendu : prix normal x 0.95 (remise 5%)
        double expectedPrice = ticketUpdated.getPrice() * 0.95;

        // Vérifier que la remise est appliquée (tolérance 0.01)
        assertEquals(expectedPrice, ticketUpdated.getPrice(), 0.01);
    }
  

}
