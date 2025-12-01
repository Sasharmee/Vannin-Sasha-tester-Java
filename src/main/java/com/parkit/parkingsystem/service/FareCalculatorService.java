package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
	    calculateFare(ticket, false); // Appel à la méthode avec paramètre discount à true
	}

	public void calculateFare(Ticket ticket, boolean discount) {
	    if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
	        throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
	    }

	    double inHour = (double) ticket.getInTime().getTime();
	    double outHour = (double) ticket.getOutTime().getTime();

	    double duration = (outHour - inHour) /(1000*60*60);

	    if (duration <= 0.5) {
	        ticket.setPrice(0);
	        return;
	    }

	    double price;
	    switch (ticket.getParkingSpot().getParkingType()) {
	        case CAR:
	            price = duration * Fare.CAR_RATE_PER_HOUR;
	            break;
	        case BIKE:
	            price = duration * Fare.BIKE_RATE_PER_HOUR;
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown Parking Type");
	    }

	    if (discount) {
	        price = price * 0.95; // application de la remise de 5 %
	    }

	    ticket.setPrice(price);
	}
}