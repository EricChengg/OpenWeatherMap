package appit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import jdk.incubator.http.HttpHeaders;


public class OpenWeather {
	private static HttpURLConnection connection;
	
	public static void main (String[] args) {
		HttpURLConnection connection;
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		try {
			// fail url
			//URL url = new URL("https://api.openweathermap.org/data/2.5/weather?id=1819729&APPID=7b1e2e7&la");
			
			// work url
			URL url = new URL("https://api.openweathermap.org/data/2.5/weather?id=1819729&APPID=70f0c182309d85a12932105c67b1e2e7&la");
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestProperty("Authorization","Bearer "+" Actual bearer token issued by provider.");
			connection.setRequestProperty("Content-Type","application/json");
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(50000);
			
			int status = connection.getResponseCode();
			// DB IP and port.
			MongoClient mongoClient = new MongoClient( "localhost" , 27017);

			System.out.println("DB connented");
			
			// get DB
			DB db = mongoClient.getDB("WEATHER_INFO");
			
			Set<String> colls = db.getCollectionNames();
			System.out.println("Show all collection : ");
			for (String s : colls) {
			System.out.println(s);
			}
			

			
			// get collection
			DBCollection collection = db.getCollection("weather");
			
			BasicDBObject searchOldWeatherQuery = new BasicDBObject();
			searchOldWeatherQuery.put("id", 1819729);
			
			System.out.println("Status : "+ status);
			if (status > 299) {
				System.out.println("Client errors / Server errors / Redirects.");
				displayOldWeather(collection, searchOldWeatherQuery);
			} else {
				System.out.println("Successful responses.");
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line = reader.readLine()) != null) {
					BasicDBObject document = BasicDBObject.parse(line);
					saveCurrentWeather(collection,searchOldWeatherQuery, document);
					displayOldWeather(collection, searchOldWeatherQuery);
				}
				reader.close();
			}
			System.out.println(responseContent.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void displayOldWeather(DBCollection collection, BasicDBObject searchOldQuery) {
		DBCursor cursor = collection.find(searchOldQuery);
		while (cursor.hasNext()) {
			System.out.println(cursor.next());
		}
	}
	public static boolean findOldWeather(DBCollection collection, BasicDBObject searchOldQuery) {
		DBCursor cursor = collection.find(searchOldQuery);
		if (cursor.count() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void saveCurrentWeather(DBCollection collection, BasicDBObject searchOldQuery, BasicDBObject document) {
		if (findOldWeather(collection, searchOldQuery) == true) {
			System.out.println("Update.");
			collection.update(searchOldQuery, document);
		} else {
			System.out.println("Insert.");
			collection.insert(document);
		}
	}
}
