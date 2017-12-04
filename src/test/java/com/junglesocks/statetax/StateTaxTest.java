package com.junglesocks.statetax;

import com.junglesocks.Constants;
import com.junglesocks.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StateTaxTest {

    private WebDriver driver;
    private static final String ITEM_KEY = "line_item_quantity_";

    /**
     * initalize web driver
     * @throws Exception
     */
    @BeforeClass
    public void setUp() throws Exception
    {
        JSONObject chromeDriverPath = Utility.readFile(Constants.SETTINGS_FILE);
        // read the path to webdriver from settings.json
        System.setProperty("webdriver.chrome.driver", chromeDriverPath.get("pathToChromeDriver").toString());
        driver = new ChromeDriver();

    }

    @DataProvider(name = "stateTaxInput")
    public Object[][] salesTaxInput()  throws Exception
    {
        JSONObject inputObj = Utility.readFile(Constants.INPUT_FILE);
        Set<String> fields = inputObj.keySet();
        int numberOfElements = inputObj.keySet().size();
        Object[][] result = new Object[numberOfElements][1];
        int count = 0;
        for(String key : fields){
            JSONArray input = (JSONArray) inputObj.get(key);
            Iterator<JSONObject> elements = input.iterator();
            List<String[]> inputList = new ArrayList<String[]>();
            while(elements.hasNext())
            {
                JSONObject obj = elements.next();
                inputList.add(new String[]{obj.get("itemtype").toString(), obj.get("quantity").toString(), obj.get("state").toString()});
            }
            result[count][0] = inputList;
            count++;
        }

        return result;
    }

    @Test(dataProvider = "stateTaxInput")
    public void testSalesTax(List<String[]> list) throws Exception
    {
        driver.get(Constants.JUNGLE_SOCKS_URL);
        Iterator<String[]> iterator = list.iterator();
        float expectedTaxes = 0;
        float expectedSubtotal = 0;

        while (iterator.hasNext()){
            String[] inputValues = iterator.next();
            String itemtype = inputValues[0];
            String quantity = inputValues[1];
            String state = inputValues[2];

            driver.findElement(By.id(ITEM_KEY+itemtype)).sendKeys(quantity);
            Select dropdown = new Select(driver.findElement(By.name("state")));
            dropdown.selectByValue(state.toUpperCase());

            float salesTax = Utility.calculateSalesTax(Utility.findItemPrice(itemtype), Integer.parseInt(quantity), state);
            expectedTaxes = BigDecimal.valueOf(salesTax + expectedTaxes).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

            expectedSubtotal = expectedSubtotal + Integer.parseInt(quantity) * Utility.findItemPrice(itemtype);
        }
        driver.findElement(By.name("commit")).click();

        float subtotal = Float.parseFloat(driver.findElement(By.id("subtotal")).getText().substring(1));
        float taxes = Float.parseFloat(driver.findElement(By.id("taxes")).getText().substring(1));
        float total = Float.parseFloat(driver.findElement(By.id("total")).getText().substring(1));

        Assert.assertEquals(taxes, expectedTaxes, "Taxes should be "+expectedTaxes);
        Assert.assertEquals(subtotal, expectedSubtotal, "Subtotal should be "+expectedSubtotal);
        Assert.assertEquals(total, expectedTaxes+expectedSubtotal, "Total should be "+expectedSubtotal+expectedTaxes);
    }

    /**
     * close the resources
     */
    @AfterClass
    public void closeResources()
    {
        driver.close();
    }

}

