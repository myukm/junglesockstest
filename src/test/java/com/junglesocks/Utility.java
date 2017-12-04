package com.junglesocks;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class Utility {

    private static JSONParser jsonParser = new JSONParser();

    /**
     * Read the json file from the specified path
     * @param path
     * @return
     */
    public static JSONObject readFile(String path)
    {
        try {
            //get the root directory path
            String dir = System.getProperty("user.dir");
            return (JSONObject) jsonParser.parse(new FileReader(dir+path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * calculate the sales tax
     * @param price
     * @param quantity
     * @param state
     * @return
     * @throws Exception
     */
    public static float calculateSalesTax(float price, int quantity, String state) throws Exception
    {
        float salestax = 0;
        float percentST = 0;
        JSONObject inputObj = readFile(Constants.SALESTAX_FILE);
        JSONArray taxes = (JSONArray) inputObj.get("taxes");
        Iterator<JSONObject> elements = taxes.iterator();

        while (elements.hasNext()){
            JSONObject obj = elements.next();
            String state1 = obj.get("state").toString();
            if(state1.equalsIgnoreCase(state))
            {
                percentST = Float.parseFloat(obj.get("value").toString());
                break;
            }
        }
        salestax = (price * percentST) * quantity;
        return salestax;
    }

    /**
     * find the price of item
     * @param itemtype
     * @return
     * @throws Exception
     */
    public  static float findItemPrice(String itemtype) throws Exception
    {
        float price = 0;

        JSONObject inputObj = Utility.readFile(Constants.PRICE_FILE);
        JSONArray taxes = (JSONArray) inputObj.get("prices");
        Iterator<JSONObject> elements = taxes.iterator();

        while (elements.hasNext()){
            JSONObject obj = elements.next();
            String item = obj.get("itemtype").toString();
            if(item.equalsIgnoreCase(itemtype))
            {
                price = Float.parseFloat(obj.get("price").toString());
                break;
            }
        }
        return price;

    }
}
