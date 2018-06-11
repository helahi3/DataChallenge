/*
 * Input is of format:
 * { "id": 1, "discount_type": "product", "discount_value": 5.0, "collection": "Lifestyle" }
 * Output is of format:
 * {"total_after_discount":2904.0,"total_amount":2925.0}
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DataChallenge {

	public static void main(String[] args) {
		
		JSONParser parser = new JSONParser();
		try {
			
			//File IO
			String inputName = "/Users/Hamza/Desktop/input001.txt"; 
			
			FileReader input = new FileReader(inputName);
			
			//Parse into JSON Object and get values
			JSONObject inputObject = (JSONObject) parser.parse(input);
			Long cartID = (Long) inputObject.get("id");
			Double discountValue = (Double) inputObject.get("discount_value");
			String discountType = (String) inputObject.get("discount_type");

			BufferedReader br = null;
			JSONArray urlArr = new JSONArray();

			int pageNumber = 1;
			int totalPages;
			do {
				//Use API to loop through all pages of site
				//And save all JSON objects
				URL url = new URL("http://backend-challenge-fall-2018.herokuapp.com/carts.json?id=" + cartID + "&page=" + pageNumber);
				br = new BufferedReader(new InputStreamReader(url.openStream()));
				JSONObject urlObj = (JSONObject) parser.parse(br.readLine());
				urlArr.add(urlObj);
				
				//Get number of pages 
				JSONObject pageObj = (JSONObject) urlObj.get("pagination");
				Long perPage = (Long) pageObj.get("per_page");
				Long totalProd = (Long) pageObj.get("total");
				
				
				totalPages = (int) (totalProd/perPage +1); 
				
			} while (pageNumber++ < totalPages);
						
			//Loop through urlArray to get array of all products in JSON format
			JSONArray productsArr = new JSONArray();
			for(Object o : urlArr) {
				JSONObject pageObj = (JSONObject) o;
				productsArr.addAll((JSONArray)pageObj.get("products"));
			}
			
			//Get the discount key type and key value and assign to variables
			String discountKeyType = ""; 
			String discountKeyValue = "";
			
			if(inputObject.containsKey("collection")) {
				discountKeyType = "collection";
				discountKeyValue = (String) inputObject.get("collection");
			}
			else if (inputObject.containsKey("product_value")) {
				discountKeyType = "product_value";
				discountKeyValue = "" + 0;
			}
			else if (inputObject.containsKey("cart_value")) {
				discountKeyType = "cart_value";
				discountKeyValue = "" + inputObject.get("cart_value");
			}
			
			

						
			Double totalPrice = 0.0;
			Double discountedPrice = 0.0;
			
			//Discount type is either cart or product
			//Loop through all products and get their price and if discount eligible
			//Sum up total price and discounted price
			if (discountType.equals("product")) {
				
					for (Object product : productsArr) {
						JSONObject prod = (JSONObject) product;
						Double price = (Double) prod.get("price");

						totalPrice += price;
						
						if(prodDiscElig(prod,discountKeyType,discountKeyValue)) {
							price -= discountValue;
							if(price < 0) price = 0.0;
						}						
						discountedPrice += price;
					} 
			}
			else {
				for (Object product : productsArr) {
					JSONObject prod = (JSONObject) product;
					Double price = (Double) prod.get("price");

					totalPrice += price;
					discountedPrice = totalPrice;
					if(totalPrice >= Double.parseDouble(discountKeyValue)) discountedPrice -= discountValue;
				} 
			}
			
			//Save prices to a JSON Object and print
			JSONObject price = new JSONObject();
			price.put("total_amount", totalPrice);
			price.put("total_after_discount", discountedPrice);
			System.out.println(price);
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
	}
	
	//Return whether the given product is eligible for a discount
	//based on its discount key
	public static boolean prodDiscElig(JSONObject prod, String discElig, String input) {
		if(discElig.equals("collection")) {
			
			if(prod.containsKey("collection") && prod.get("collection").equals(input)) {
				return true;
			}
		}
		else if(discElig.equals("product_value")) {
			if((Double) prod.get("price") > Double.parseDouble(input)) {
				return true;
			}
		}
	return false;
		
	}

}
