package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO); //refactorer car souci pour test de processIncomingVehiclee
    }

  
    @Test
    public void processExitingVehicleTest() throws Exception {
        // Simuler la lecture du numéro de plaque, ici "ABCDEF" correspond à votre verify
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Créer un ticket et son parking spot (avec disponibilité false au départ)
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date());
        ticket.setOutTime(null);

        // Configurer les mocks pour que la méthode processExitingVehicle fonctionne correctement
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Appeler la méthode à tester
        parkingService.processExitingVehicle();

        // Vérifications
        verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, times(1)).getTicket("ABCDEF");
        verify(ticketDAO, times(1)).getNbTicket("ABCDEF");
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }
    @Test
    public void processIncomingVehicleTest() throws Exception{
        {
        	when(inputReaderUtil.readSelection()).thenReturn(1);
        	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
            
        }
    }
    
  
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
    	//mise en place des mocks
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        //comportement de nos mocks
        when(ticketDAO.getTicket(eq("ABCDEF"))).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getNbTicket(eq("ABCDEF"))).thenReturn(0);
        //appel de la méthode processExitingVehicle
        parkingService.processExitingVehicle();
        
        //vérification que les appels ont été effectués une seule fois chacun
        verify(ticketDAO, times(1)).getTicket(eq("ABCDEF"));
        verify(ticketDAO, times(1)).getNbTicket(eq("ABCDEF"));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));

        //vérification que l'espace n'est pas libéré
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }
    
    @Test //on test la réalisation si un parking est dispo
    public void testGetNextParkingNumberIfAvailable() {
   
	when(inputReaderUtil.readSelection()).thenReturn(1); //voiture
	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); //place disponible

	ParkingSpot result = parkingService.getNextParkingNumberIfAvailable(); //appel de la méthode pour récupérer la place de parking

	assertNotNull(result); //vérification que le résultat n'est pas supposé être nul car place dispo
	assertEquals(1, result.getId()); //vérification de l'ID
	assertEquals(ParkingType.CAR, result.getParkingType()); //vérification type de véhicule
	assertTrue(result.isAvailable()); //vérification place dispo
     }

    @Test //test lorsqu'aucune place parking dispo
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1); //type de véhicule = CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1); //return -1 = pas de place 

        assertNull(parkingService.getNextParkingNumberIfAvailable()); //vérification que la méthode renvoie bien null
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3); //mauvais type de véhicule

        assertNull(parkingService.getNextParkingNumberIfAvailable()); //vérification que la valeur renvoyé est nulle car pas bon type de véhicule

        // vérifie que l'exécution s'arrête car le véhicule n'est pas bon
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }

}
