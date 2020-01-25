package com.example.restservice;

import org.springframework.data.annotation.Id;
import org.bson.Document;
import java.net.MalformedURLException;
import java.net.URL;


public class Product{

	@Id private long id;	//i suppose id is unique
	private String SKU;
	private String description;
	private URL photoUrl;
	private double price;
	private long warehouseAmount;


	public Product(long id, String SKU, String description, String photoUrl, double price, long warehouseAmount) throws MalformedURLException{
		this.id = id;
		this.SKU = SKU;
		this.description = description;
		this.photoUrl = new URL(photoUrl);
		this.price = price;
		this.warehouseAmount = warehouseAmount;
	}
	public Product(Document doc) throws MalformedURLException{
		this(	Long.parseLong(doc.getString("id")), doc.getString("SKU"),
				doc.getString("description"), doc.getString("photoUrl"), Double.parseDouble(doc.getString("price")),
				Long.parseLong(doc.getString("warehouseAmount")) );
	}
	public Document toDocument(){
		return new Document()
				.append("id", Long.toString(this.id))
				.append("SKU", this.SKU)
				.append("description", this.description)
				.append("photoUrl", this.photoUrl.toString())
				.append("price",  Double.toString(this.price))
				.append("warehouse amount", Long.toString(this.warehouseAmount));
	}

}
