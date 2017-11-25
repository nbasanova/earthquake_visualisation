package module6;

import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;

public class FlightMarker extends SimpleLinesMarker {
	private int airportFrom;
	private int airportTo;

	public FlightMarker(List<Location> arg0, int airportFrom, int airportTo) {
		super(arg0);
		this.airportFrom = airportFrom;
		this.airportTo = airportTo;
	}

	public FlightMarker(List<Location> arg0, HashMap<String, Object> arg1, int airportFrom, int airportTo) {
		super(arg0, arg1);
		this.airportFrom = airportFrom;
		this.airportTo = airportTo;
	}

	public FlightMarker(Location arg0, Location arg1, int airportFrom, int airportTo) {
		super(arg0, arg1);
		this.airportFrom = airportFrom;
		this.airportTo = airportTo;
	}
	
	public int getAirportFrom() {
		return this.airportFrom;
	}
	
	public int getAirportTo() {
		return this.airportTo;
	}
}
