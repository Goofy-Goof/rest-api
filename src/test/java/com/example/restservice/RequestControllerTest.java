package com.example.restservice;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.runner.RunWith;
import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.lang3.*;
import org.springframework.test.web.servlet.MvcResult;



@RunWith(SpringRunner.class)
@WebMvcTest(RequestController.class)
public class RequestControllerTest {

	@Autowired
	private MockMvc mockMvc;
	private final boolean sleep = true;
	private final boolean drop = true;
	private RequestController controller;

	@Ignore
	public String generateRandomString(boolean numerical, int len){
		String generatedString = RandomStringUtils.random(len, true, numerical);
		//System.out.println("Generated String " + generatedString);
		return generatedString;
	}

	@Ignore
	public LinkedList<Product> createProducts(int max) throws Exception{
		//create list if random objects, to test requestcontroller later with
		LinkedList<Product> productList = new LinkedList<>();
		Random random = new Random();
		for(int i = 0; i != max; i++) {
			long id = Math.abs( random.nextLong() ) + 1;
			String sku = generateRandomString( true, 15 );
			String description = generateRandomString( false, 20 );
			String url = "https://www.google.de/"; //url is always google because of exception thrown by constructor
			double price = Math.abs( random.nextDouble() );
			long amount = Math.abs( random.nextLong() );
			productList.add( new Product( id, sku, description, url, price, amount ) );
		}
		return productList;
	}
	@Ignore
	public String parseDocument(String string){
		//parse slightly different represetations of json from Document and MvcResult,
		//so they would be two string that can be compared nomally
		String[] splited_string = string.split( "," );
		String result = "";
		for(int i = 0; i != splited_string.length; i++){
			splited_string[i] = splited_string[i].replaceAll("[^a-zA-Z0-9:]+","");
			result += splited_string[i] + " ";

		}
		return result;
	}
	@Ignore
	public void fillDB(LinkedList<Product> products) throws Exception{
		for(Product product : products){
			//send request to save document representing product
			this.mockMvc.perform( post( "/create" )
					.param( "id", Long.toString( product.getId() ) )
					.param( "SKU", product.getSKU() )
					.param( "description", product.getDescription())
					.param( "photoURL", product.getPhotoURL().toString() )
					.param( "price", Double.toString( product.getPrice()) )
					.param( "warehouse amount", Long.toString( product.getWarehouseAmount()) ) )
					.andExpect( status().isOk() );

		}
	}
	@BeforeTestMethod
	public void initiatecontroller(){
		this.controller = RequestController.RequestControler();
	}
	@AfterTestMethod
	public void dropCollection() throws Exception{
		if(sleep && drop){
			System.out.println("Collection will be dropped soon");
			Thread.sleep( 10000 );
			this.controller.getCollection().drop();
		}
	}
	@Test
	public void fillAndReadDocumentsFromDBTest() throws Exception {
		//firstly check if all saved Douments can be retrieved back
		//RequestController.connectDB();
		LinkedList<Product> productList = createProducts(10);
		fillDB( productList );

		for(Product it : productList) {
			MvcResult result = this.mockMvc.perform( get( "/read" ).param( "id", Long.toString( it.getId() ) ) )
					.andExpect( status().isOk() ).andReturn();

			String found = parseDocument(result.getResponse().getContentAsString());
			String expected = parseDocument(it.toDocument().toJson());
			assertEquals( found, expected );

		}

	}
	@Test
	public void testDelete() throws Exception{
		//then check if delete really removes every document from DB
		//RequestController.connectDB();
		LinkedList<Product> products = createProducts(  10 );
		fillDB( products );
		System.out.println("DB filled");
		if(sleep)Thread.sleep( 10000 );
		for(Product pr : products){
			this.mockMvc.perform( delete( "/delete" )
					.param( "id", Long.toString( pr.getId() )))
					.andExpect( status().isOk());

			MvcResult result = this.mockMvc.perform( get( "/read" )
					.param( "id", Long.toString( pr.getId())))
					.andExpect( status().isOk() ).andReturn();
			assertEquals( "", result.getResponse().getContentAsString() );
			//System.out.println();
		}


	}
	@Ignore
	public String generateNewKey(String key) throws Exception{
		//help function to generate random new key, to be used to test replace
		switch (key){
			case ("SKU"):
				return generateRandomString( true, 15 );
			case ("description"):
				return generateRandomString( false, 20 );
			case ("price"):
				return Double.toString( Math.abs( new Random().nextDouble() ));
			case("warehouse amount"):
				return Long.toString( Math.abs( new Random().nextLong() ) + 1 );
			default:
				throw new Exception("invalid key");
		}
	}
	@Ignore
	public boolean documentWasUpdated(String old_doc, String new_doc, String key){
		/*split sligtly different string representations of document
		* then remove everythinf from each field except for letters, numbers and :
		* then split it on : and check if current string is a key, than schould have been changed,
		* and if its different than before
		* */
		key = key.replaceAll( "\\s+", "" ); //for the case of warehouse amount
		String[] old_splited = old_doc.split( "," );
		String[] new_splited = new_doc.split( "," );
		for(int i = 1; i != old_splited.length; i++){
			old_splited[i] = old_splited[i].replaceAll("[^a-zA-Z0-9:]+","");
			new_splited[i] = new_splited[i].replaceAll("[^a-zA-Z0-9:]+","");
			String[] old_key_value = old_splited[i].split( ":" );
			String[] new_key_value = new_splited[i].split( ":" );
			if(old_key_value[0].equals( key ) && new_key_value[0].equals( key )){
				return ! old_key_value[1].equals( new_key_value[1]);
			}
		}
		return false;
	}
	@Test
	public void testUpdatingValues() throws Exception{
		//for each object, try changing each field, and see if it really changed it
	//RequestController.connectDB();
	LinkedList<Product> products = createProducts( 2 );
		fillDB( products );
		System.out.println("DB filled");
		if(sleep)Thread.sleep( 10000 );
		String[] keysToChange = {"SKU", "description", "price", "warehouse amount"};
		for(Product pr : products){

			for(String key : keysToChange){
				String to_update = generateNewKey( key );
				this.mockMvc.perform( put( "/update" )
						.param( "id", Long.toString( pr.getId() ))
						.param( key, to_update ))
						.andExpect( status().isOk());

				MvcResult result = this.mockMvc.perform( get( "/read" )
						.param( "id", Long.toString( pr.getId() )))
						.andExpect( status().isOk()).andReturn();
					assertTrue(
							documentWasUpdated( pr.toDocument().toJson() , result.getResponse().getContentAsString(), key ));




			}
		}
	}

}
