package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<SimpleLinesMarker> routeList;
	
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	private HashMap<Integer, Location> airports;
	private HashMap<Integer, List<Integer>> connections;
	private HashMap<Integer, List<Marker>> flightsFromAirports;
	private List<Marker> flightList;
	
	public void setup() {
		// setting up PAppler
		size(800,600, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550);
		MapUtils.createDefaultEventDispatcher(this, map);

		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// lists for markers, hashmaps for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		flightList = new ArrayList<Marker>();
		airports = new HashMap<Integer, Location>();
		connections = new HashMap<Integer, List<Integer>>();
		flightsFromAirports = new HashMap<Integer, List<Marker>>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature, Integer.parseInt(feature.getId()));
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<SimpleLinesMarker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
		     	route.addLocation(airports.get(dest));
				addConnection(source, dest);
			}
			
			FlightMarker fm = new FlightMarker(route.getLocations(), route.getProperties(),source, dest);
			fm.setHidden(true);
			System.out.println(fm.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			addFlight(source, fm);
			flightList.add(fm);
		}
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(flightList);
		
		map.addMarkers(airportList);
		
	}
	
//add a route to the connectivity map of airports	
	private void addConnection(int source, int dest){
		
		List<Integer> connectionsList;
		
		if(connections.containsKey(source)!=true) {
			connectionsList = new ArrayList<Integer>();
		}
		else {
			connectionsList = connections.get(source);
		}
		
		connectionsList.add(dest);
		connections.put(source, connectionsList);
	}
	
	private void addFlight(int airportCode, Marker flight){
		
		List<Marker> flights;
		if(flightsFromAirports.containsKey(airportCode)) {
			flights = flightsFromAirports.get(airportCode);
		}
		else {
			flights =new ArrayList<Marker>();
		}
		flights.add(flight);
		flightsFromAirports.put(airportCode, flights);
	}
	
	@Override
	public void mouseMoved() {
		if(lastSelected!=null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectAirportIfHover();
	}
	
	private void selectAirportIfHover(){

		for(Marker airport: airportList) {
			if(lastSelected != null) {
				return;
			}
			if(airport.isInside(map, mouseX, mouseY)) {
				lastSelected = (CommonMarker)airport;
				airport.setSelected(true);
			}
			
		}
		
	}
	
	@Override
	public void mouseClicked() {
		if(lastClicked!=null) {
			unhideAirports();
			lastClicked = null;
		}
		else {
			checkAirportsClicked();

		}
		
	}
	
	private void unhideAirports() {
		for(Marker airport: airportList) {
			airport.setHidden(false);
		}
		int airportCode = ((AirportMarker)lastClicked).getCode();
		if(flightsFromAirports.containsKey(airportCode)) {
			for(Marker flight: flightsFromAirports.get(airportCode)) {
				flight.setHidden(true);
			}
		}
	}
	
	private void checkAirportsClicked() {
		
		for(Marker airport: airportList) {
			if(airport.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) airport;
				System.out.println("ID="+lastClicked);
				break;
			}	
		}
	
		if(lastClicked == null) return;
		
		for(Marker airport: airportList) {
			if((CommonMarker)airport!=lastClicked) {
				if(isConnected((CommonMarker)airport, lastClicked)!=true) {
					airport.setHidden(true);
				} 
				else {
					int airportCode = ((AirportMarker)lastClicked).getCode();
					if(flightsFromAirports.containsKey(airportCode)) {
						for(Marker flight: flightsFromAirports.get(airportCode)) {
							flight.setHidden(false);
						}
					}
				}
			}
		}
	}
	
	private boolean isConnected(CommonMarker airport1, CommonMarker airport2) {
		Integer source = getAirportCode(airport1);
		Integer dest = getAirportCode(airport2);
		if(connections.containsKey(source)) {
			if(connections.get(source).contains(dest))
				return true;
			
		}
		return false;
	}
	
	private Integer getAirportCode(CommonMarker airport) {
		return ((AirportMarker)airport).getCode();
	} 
	
	
	public void draw() {
		background(0);
		map.draw();
		
	}
	

}
