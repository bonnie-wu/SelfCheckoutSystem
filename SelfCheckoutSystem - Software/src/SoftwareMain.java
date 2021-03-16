import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.external.ProductDatabases;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.Product;

public class SoftwareMain {
	
	public static ArrayList<BarcodedItem> previouslyScannedItems;
	public static ProductDatabases productDatabase;
	public static SelfCheckoutStation station;
	public static CustomerScanItem customerScanItem;
	public static CustomerPayment customerPayment;
	
	public static void main(String[] args) {
		initialize();
		
		
	}
	
	public static void Pay(Coin coin) {
		try {
			customerPayment.PayCoin(coin);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void Pay(Banknote banknote) {
		try {
			customerPayment.PayBanknote(banknote);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void ScanMain(BarcodedItem item) {
		customerScanItem.scanItemMain(item);
	}
	
	public static void ScanHeld(BarcodedItem item) {
		customerScanItem.scanItemHeld(item);
	}
	
	public static void Bag(BarcodedItem item) {
		try {
			customerScanItem.placeItemInBagging(item);
		}
		catch(OverloadException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void initialize() {
		Currency currency = Currency.getInstance(Locale.CANADA);
		int[] banknoteDenominations = {5, 10, 20, 50, 100};
		BigDecimal[] coinDenominations = {	BigDecimal.valueOf(0.05), 
											BigDecimal.valueOf(0.10),
											BigDecimal.valueOf(0.25),
											BigDecimal.valueOf(0.50), 
											BigDecimal.valueOf(1.00), 
											BigDecimal.valueOf(2.00)};
		int scaleMaximumWeight = (25*1000);	//Scale maximum in grams
		int scaleSensitivity = (15);		//Scale sensitivity in grams
		
		station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
		
		BarcodedItem itemList[] = {		new BarcodedItem(new Barcode("012345"), 500),	
										new BarcodedItem(new Barcode("012346"), 2000)
								  };				//Only fill for testing purposes (alternative to populateItems() within initialize)
		BarcodedProduct productList[] = {	new BarcodedProduct(new Barcode("012345"), "Cheese sticks", BigDecimal.valueOf(2.95)),
											new BarcodedProduct(new Barcode("012346"), "Chicken nuggets", BigDecimal.valueOf(10.99)),
										};	//Only fill for testing purposes (alternative to populateDatabase() within initialize)
		
		previouslyScannedItems = new ArrayList<BarcodedItem>(Arrays.asList(itemList));
		populateDatabase(new ArrayList<BarcodedProduct>(Arrays.asList(productList)));
		
		if(previouslyScannedItems.size() == 0)
			customerScanItem = new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea);
		else
			customerScanItem = new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, previouslyScannedItems);
		
		customerPayment = new CustomerPayment(convertItemToProduct(previouslyScannedItems), station);
	}
	
	public static void populateItems(ArrayList<BarcodedItem> list) {
		previouslyScannedItems.clear();
		
		for(BarcodedItem item : list) {
			previouslyScannedItems.add(item);
		}
	}
	
	public static void populateDatabase(ArrayList<BarcodedProduct> list) {
		productDatabase.BARCODED_PRODUCT_DATABASE.clear();
		
		for(BarcodedProduct product : list) {
			productDatabase.BARCODED_PRODUCT_DATABASE.put(product.getBarcode(), product);
		}
	}
	
	public static ArrayList<Product> convertItemToProduct(ArrayList<BarcodedItem> list) {
		ArrayList<Product> productList = new ArrayList<Product>();
		
		for(BarcodedItem item : list) {
			if(!productDatabase.BARCODED_PRODUCT_DATABASE.containsKey(item.getBarcode()))
				throw new SimulationException("Item not in product database");
			
			productList.add(productDatabase.BARCODED_PRODUCT_DATABASE.get(item.getBarcode()));
		}
		
		return productList;
	}
	
}
