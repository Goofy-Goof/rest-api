package com.example.restservice;

import java.net.MalformedURLException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


@RestController
public class RequestController{

	static MongoCollection collection;
	static void connectDB(){
		String uri = "mongodb+srv://artem:artem2288@cluster0-i1rs3.mongodb.net/test\n\n";
		MongoClientOptions.Builder options = MongoClientOptions.builder();
		options.sslEnabled(true);
		options.socketKeepAlive(true);
		MongoClientURI clientURI = new MongoClientURI(uri, options);
		MongoClient mongoClient = new MongoClient(clientURI);

		MongoDatabase db = mongoClient.getDatabase("warehouse");
		RequestController.collection = db.getCollection("goods");
	}
	@GetMapping("/read")
	@ResponseStatus(HttpStatus.OK)
	public Document read(@RequestParam(value = "id", required = true) String str_id) {

		Document found = (Document) collection.find(new Document("id", str_id)).first();
		if(found != null) {
			found.remove("_id");
			return found;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/create")
	@ResponseStatus(HttpStatus.OK)
	public void create(@RequestParam(value = "id", required = true) String str_id,
					   @RequestParam(value = "SKU", defaultValue = "") String SKU,
					   @RequestParam(value = "description", defaultValue = "") String descrp,
					   @RequestParam(value = "photoURL", defaultValue = "https://www.google.de/") String str_url,
					   @RequestParam(value = "price", defaultValue = "0") String str_price,
					   @RequestParam(value = "amount", defaultValue = "0") String str_amount) throws MalformedURLException{
			Document document = new Product(Long.parseLong(str_id), SKU, descrp, str_url,
					Double.parseDouble(str_price), Long.parseLong(str_amount)).toDocument();
			collection.insertOne(document);
	}


	@DeleteMapping("/delete")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@RequestParam(value = "id", required = true) String str_id) {
		Document document = new Document().append( "id", str_id );
		collection.deleteOne( document );
	}


	private void updateField(Document document, String key, String value){
		Bson updatedvalue = new Document(key, value);
		Bson updateoperation = new Document("$set", updatedvalue);
		collection.updateOne(document,updateoperation);
		System.out.println("updated " + key);
	}


	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PutMapping("/update")
	@ResponseStatus(HttpStatus.OK)
	public void update(@RequestParam(value = "id", required = true) String str_id,
					   @RequestParam(value = "SKU", required = false) Optional<String> SKU,
					   @RequestParam(value = "description", required = false) Optional<String> descrp,
					   @RequestParam(value = "photoURL", required = false) Optional<String> str_url,
					   @RequestParam(value = "price", required = false) Optional<String> str_price,
					   @RequestParam(value = "amount", required = false) Optional<String> str_amount){
		Document found = (Document) collection.find( new Document("id", str_id)).first();
		if(found != null){
			SKU.ifPresent( value -> updateField( found, "SKU", value));
			descrp.ifPresent( value -> updateField(found, "description", value));
			str_url.ifPresent( value -> updateField(found, "photoURL", value));
			str_price.ifPresent( value -> updateField( found, "price", value ));
			str_amount.ifPresent( value -> updateField( found, "warehouse amount", value ) );
		}

	}
}
