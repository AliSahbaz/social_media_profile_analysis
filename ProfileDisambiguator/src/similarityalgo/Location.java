/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package similarityalgo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Location has the ability to keep geographical location for any given location
 * with string representation or longitude and latitude. It has functionalities
 * to calculate distances between two locations. Uses Google Map API to
 * calculate the actual distance. MAP_API_KEY: this can be retrieved from
 * developer.google.com. Create empty project there and enable Geocoding API in
 * the google dash board. This key can be get from the Dash board under
 * Credentials. *
 */
public class Location {

    private String location;
    private double LONGITUDE;
    private double LATITUDE;

    private static String MAP_API_KEY;

    /**
     * This is a constructor that gets the string representation of a location
     * and loads the geographical coordinates.
     * 
     * @param addrss
     *            : string representation of a location. The addrss shouldn't
     *            contain comma or space. It should be replaced with '+' Eg:
     *            "Colombo, SriLanka" => "Colombo++Srilanka"
     */
    public Location(String api_key, String addrss) {
	this.location = addrss;
	MAP_API_KEY = api_key;
	loadCoordinatesJSON(addrss);
    }

    /**
     * This is a constructor that takes latitude and longitude of a location.
     * 
     * @param latitude
     *            : valid latitude of a location
     * @param longitude
     *            : valid longitude of a location It assigns them to the class
     *            attributes.
     */
    public Location(String api_key, double latitude, double longitude) {
	MAP_API_KEY = api_key;
	this.LATITUDE = latitude;
	this.LONGITUDE = longitude;
    }

    /**
     * @param location
     *            : object of a Location that represents a geographical location
     * @return the direct distance between this object and location
     */
    public double getEuclideanDistance(Location location) {
	return (sqrt(pow(this.LONGITUDE - location.LONGITUDE, 2)
		+ pow(this.LATITUDE - location.LATITUDE, 2)));
    }

    /**
     * @param addrss
     *            : represents the location by string
     * @return the direct distance between this object and location
     */
    public double getEuclideanDistance(String addrss) {
	Location location = new Location(MAP_API_KEY, addrss);
	return this.getEuclideanDistance(location);
    }

    /**
     * This function calculates actual geographical distance between two
     * locations using GoogleMap API.
     * 
     * @param location1
     *            : string representation of a location
     * @param location
     *            : string representation of a location
     * @return the actual geographical distance between two locations.
     */
    public double getActualDistance(String location) {
	double distance = -1.0;
	try {
	    InputStream is = new URL(
		    "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
			    + this.location + "&destinations=" + location
			    + "&mode=driving&language=en-EN&key=" + MAP_API_KEY)
		    .openStream();

	    System.out
		    .println("--------------------------------------------------------------");

	    JsonReader rdr = Json.createReader(is);

	    JsonObject obj = rdr.readObject();
	    String status = obj.getJsonString("status").getString();
	    if (!status.equals("OK")) {
		System.err.println("Distance can't be calculated...");
	    }
	    JsonArray results = obj.getJsonArray("rows");
	    for (JsonObject result : results.getValuesAs(JsonObject.class)) {
		JsonArray results2 = result.getJsonArray("elements");

		for (JsonObject result2 : results2
			.getValuesAs(JsonObject.class)) {
		    if (!result2.getJsonString("status").getString()
			    .equals("OK")) {
			System.err.println("Distance NOT_FOUND");
			return distance;
		    }

		    JsonObject disObj = result2.getJsonObject("distance");
		    String disStr = disObj.getString("text");
		    System.out.println("Distance between " + this.location
			    + " and " + location + ": " + disStr);

		    String temp = null;
		    if (disStr.contains(" km")) {
			temp = disStr.replace(" km", "");
			distance = Double.parseDouble(temp.replace(",", ""));
		    } else if (disStr.contains(" m")) {
			temp = disStr.replace(" m", "");
			distance = Double.parseDouble(temp.replace(",", ""));
			distance = distance / 1000.0;
		    } else {
			System.err.println("Distance Unit UNKNOWN");
		    }

		    System.out
			    .println("Estimated Duration for Driving between "
				    + this.location
				    + " and "
				    + location
				    + ": "
				    + result2.getJsonObject("duration")
					    .getString("text"));
		    System.out
			    .println("--------------------------------------------------------------");
		}
	    }

	} catch (MalformedURLException ex) {
	    System.out.println(ex);
	} catch (IOException ex) {
	    System.out.print(ex);
	}

	return distance;
    }

    /**
     * This function gets the string representation of a location and finds the
     * geographical coordinates for that.
     * 
     * @param addrss
     *            : string representation of a location. The addrss consists of
     *            comma and space should be replaced with '+' Eg:
     *            "Colombo, SriLanka" => "Colombo++Srilanka"
     */
    private void loadCoordinatesJSON(String addrss) {
	try {
	    URL url = new URL(
		    "https://maps.googleapis.com/maps/api/geocode/json?address="
			    + addrss + "&sensor=true_or_false&key="
			    + MAP_API_KEY);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Accept", "application/json");

	    if (conn.getResponseCode() != 200) {
		throw new RuntimeException("Failed : HTTP error code : "
			+ conn.getResponseCode());
	    }
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    (conn.getInputStream())));

	    String output;
	    String latitude, longitude;

	    while ((output = br.readLine()) != null) {
		if (output.equals("            \"location\" : {")) {

		    String[] str1 = br.readLine().split("\\s+");
		    latitude = (str1[3].split(","))[0];
		    // System.out.println("Latitude: " + latitude);
		    this.LATITUDE = Double.parseDouble(latitude);

		    String[] str2 = br.readLine().split("\\s+");
		    longitude = str2[3];
		    // System.out.println("Longitude: " + longitude);
		    this.LONGITUDE = Double.parseDouble(longitude);
		    break;
		}
	    }

	} catch (MalformedURLException ex) {
	    System.out.println("URL Error: " + ex);
	} catch (IOException ex) {
	    System.out.println(ex);
	}
    }

    /**
     * This function writes location details such as state coordinates and all
     * to a file called location.xml
     */
    public void writeCoordinatesXML() {

	try {

	    String thisLine;
	    String addrs = this.location;
	    // addrs = addrs.replace(' ', '+');

	    URL u = new URL(
		    "https://maps.googleapis.com/maps/api/geocode/xml?address="
			    + addrs + "&sensor=true_or_false&key="
			    + MAP_API_KEY);

	    BufferedReader theHTML = new BufferedReader(new InputStreamReader(
		    u.openStream()));

	    FileWriter fstream = new FileWriter("location.xml");

	    BufferedWriter out = new BufferedWriter(fstream);

	    while ((thisLine = theHTML.readLine()) != null) {
		out.write(thisLine);
	    }

	    out.close();

	} catch (MalformedURLException ex) {

	    System.out.println("MalformedURL Error: " + ex);

	} catch (IOException ex) {

	    System.out.println(ex);

	}

    }

    public static void main(String args[]) {
	String api_key = "AIzaSyBztOQ-dXgY6v5fBzcNRlBw_AOSbwlknxg";

	Location locationStr1 = new Location(api_key, "+38.813646+-94.908161");// Chavakachcheri,
	Location locationStr2 = new Location(api_key, "Kansas+City++KS");

	System.out.println("Distance: "
		+ locationStr1.getEuclideanDistance(locationStr2));

	Location location = new Location(api_key, "KL++Malaysia");
	double dist = location.getActualDistance("Singapore");
	System.out.println("Distance (km): " + dist);

	location = new Location(api_key, "Jaffna");
	dist = location.getActualDistance("Chavakchcheri");
	System.out.println("Distance (km): " + dist);
	location.writeCoordinatesXML();

    }

}
