/*
 * 	Class:			SoftwareMain.java
 * 	Description:	A main class that initializes the functionality of CustomerPayment.java
 * 					and CustomerScanItem.java, tying the two functionalities together. Also
 * 					implements additional initialization options for testing.
 * 	Date:			3/17/2021
 * 	Authors: 		Derek Urban, Bonnie Wu
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.listeners.AbstractDeviceListener;
import org.lsmr.selfcheckout.devices.listeners.BanknoteValidatorListener;
import org.lsmr.selfcheckout.devices.listeners.BarcodeScannerListener;
import org.lsmr.selfcheckout.devices.listeners.CoinValidatorListener;
import org.lsmr.selfcheckout.devices.listeners.ElectronicScaleListener;
import org.lsmr.selfcheckout.external.ProductDatabases;
import org.lsmr.selfcheckout.products.BarcodedProduct;

public class SoftwareMain {
	
	//Flag to indicate whether the user is paying, or scanning items
	private boolean payMode = false;
	
	//Global variables
	public ArrayList<BarcodedItem> previouslyScannedItems;
	public ProductDatabases productDatabase;
	public SelfCheckoutStation station;
	public CustomerScanItem customerScanItem;
	public CustomerPayment customerPayment;
	
	/**
	 * Default Constructor that initializes a station and default listeners, intended for testing use
	 */
	SoftwareMain(){
		payMode = false;
		initialize();
	}
	
	/**
	 * Constructor that requires a prebuilt SelfCheckotStation and list of previouslyScannedItems, intended for testing use
	 * 
	 * @param SelfCheckoutStation station
	 * 			The station used by CustomerPayment.java and CustomerScanItem.java
	 * 
	 * @param ArrayList<BarcodedItem> previouslyScannedItems
	 * 			The list of BarcodedItems that have been previously scanned, used by CustomerScanItem.java
	 * 
	 * @throws SimulationException
	 * 			If any of the parameters are null
	 */
	SoftwareMain(SelfCheckoutStation station, ArrayList<BarcodedItem> previouslyScannedItems){
		payMode = false;
		
		if(station == null)
			throw new SimulationException("Station is null");
		
		if(previouslyScannedItems == null)
			throw new SimulationException("previouslyScannedItems is null");
			
		this.station = station;
		customerScanItem = new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, previouslyScannedItems);
		customerPayment = new CustomerPayment(new ArrayList<BarcodedProduct>(), station);
	}

	/**
	 * Pay with coin, updates the list of scanned products and enables "payMode"
	 * 
	 * @param Coin coin
	 * 			The coin to pay with
	 */
	public void Pay(Coin coin) {
		payMode = true;
		
		updateScannedProducts();
		
		try {
			customerPayment.PayCoin(coin);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Pay with banknote, updates the list of scanned products and enables "payMode"
	 * 
	 * @param Banknote
	 * 			The banknote to pay with
	 */
	public void Pay(Banknote banknote) {
		payMode = true;
		
		updateScannedProducts();
		
		try {
			customerPayment.PayBanknote(banknote);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Scans an item using the main scanner if payMode isn't enabled
	 */
	public void ScanMain(BarcodedItem item) {
		if(!payMode)
			customerScanItem.scanItemMain(item);
	}
	
	/**
	 * Scans an item using the hand held scanner if payMode isn't enabled
	 */
	public void ScanHeld(BarcodedItem item) {
		if(!payMode)
			customerScanItem.scanItemHeld(item);
	}
	
	/**
	 * Places an item in the bagging area
	 */
	public void Bag(BarcodedItem item) {
		try {
			customerScanItem.placeItemInBagging(item);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Initializes a SelfCheckoutStation to be used, as well as populating the previously scanned list, and
	 * productDatabase, used by CustomerPayment.java. Also initializes default listeners to be registered by
	 * hardware components
	 */
	public void initialize() {
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
		
		initializeListeners(station.mainScanner, station.handheldScanner, station.coinValidator, station.baggingArea, station.banknoteValidator);
		
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
	
	/**
	 * Initializes default listeners used by the default SelfCheckoutStation
	 */
	private void initializeListeners(BarcodeScanner mainScanner, BarcodeScanner heldScanner, CoinValidator coinValidator, 
									 ElectronicScale baggingArea, BanknoteValidator banknoteValidator) {
		mainScanner.register(new BarcodeScannerListener() {
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
				System.out.println("Main Scanner, scanned barcode: "+barcode.toString());
			}
		});
		
		heldScanner.register(new BarcodeScannerListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
				System.out.println("Handheld Scanner, scanned barcode: "+barcode.toString());
			}
		});
		
		coinValidator.register(new CoinValidatorListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void validCoinDetected(CoinValidator validator, BigDecimal value) {
				System.out.println("Valid Coin detected with value of: "+value);
			}
			public void invalidCoinDetected(CoinValidator validator) {
				System.out.println("Invalid Coin detected");
			}
		});
		
		banknoteValidator.register(new BanknoteValidatorListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
				System.out.println("Valid Banknote detected with value of: "+value);
			}
			public void invalidBanknoteDetected(BanknoteValidator validator) {
				System.out.println("Invalid Banknote detected");
			}
		});
		
		baggingArea.register(new ElectronicScaleListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void weightChanged(ElectronicScale scale, double weightInGrams) {
				System.out.println("Weight in bagging area is now: "+weightInGrams);
			}
			public void overload(ElectronicScale scale) {
				System.out.println("Weight in bagging area is overloading");
			}
			public void outOfOverload(ElectronicScale scale) {
				System.out.println("Weight in bagging area is no longer overloading");
			}
		});
	}

	/**
	 * Populates the database of the SelfCheckoutStation based off a list of BarcodedProducts.
	 * Clears the database prior to population.
	 * 
	 * @param ArrayList<BarcodedProduct> list
	 * 			The list of products to be added into the database
	 * 
	 * @throws SimulationException
	 * 			If the list provided is null
	 */
	public void populateDatabase(ArrayList<BarcodedProduct> list) {
		if(list == null)
			throw new SimulationException("Can't populate database with null list");
		
		productDatabase.BARCODED_PRODUCT_DATABASE.clear();
		
		for(BarcodedProduct product : list) {
			productDatabase.BARCODED_PRODUCT_DATABASE.put(product.getBarcode(), product);
		}
	}
	
	/**
	 * Converts a list of BarcodedItems into a list of BarcodedProducts based off the product database
	 * 
	 * @param ArrayList<BarcodedItem> list
	 * 			The list of items to be converted to products
	 * 
	 * @return ArrayList<BarcodedProduct> productList
	 * 			The list of products converted from items based off the product database
	 * 
	 * @throws SimulationException
	 * 			If the list provided is null
	 * 			If the barcode found in an item isn't in the product database
	 * 			
	 */
	public ArrayList<BarcodedProduct> convertItemToProduct(ArrayList<BarcodedItem> list) {
		if(list == null)
			throw new SimulationException("Can't convert null list");
		
		ArrayList<BarcodedProduct> productList = new ArrayList<BarcodedProduct>();
		
		for(BarcodedItem item : list) {
			if(!productDatabase.BARCODED_PRODUCT_DATABASE.containsKey(item.getBarcode()))
				throw new SimulationException("Item not in product database");
			
			productList.add(productDatabase.BARCODED_PRODUCT_DATABASE.get(item.getBarcode()));
		}
		
		return productList;
	}
	
	/**
	 * Updates the list of scanned products in CustomerPayment.java from CustomerScanItem.java
	 */
	public void updateScannedProducts() {
		customerPayment.updateScannedProducts(convertItemToProduct(customerScanItem.getScannedItems()));
	}
	
	/**
	 * Getter for the SelfCheckoutStation used by the program
	 * 
	 * @return SelfCheckoutStation station
	 */
	public SelfCheckoutStation getStation() {
		return station;
	}
	
	/**
	 * Reset function that disabled payMode and clears the list of scanned items.
	 */
	public void reset() {
		payMode = false;
		
		customerScanItem.clearScannedItems();
		updateScannedProducts();
	}
}
