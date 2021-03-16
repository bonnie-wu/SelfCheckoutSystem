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
import org.lsmr.selfcheckout.products.Product;

public class SoftwareMain {
	
	public ArrayList<BarcodedItem> previouslyScannedItems;
	public ProductDatabases productDatabase;
	public SelfCheckoutStation station;
	public CustomerScanItem customerScanItem;
	public CustomerPayment customerPayment;

	public void Pay(Coin coin) {
		try {
			customerPayment.PayCoin(coin);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	public void Pay(Banknote banknote) {
		try {
			customerPayment.PayBanknote(banknote);
		}
		catch(DisabledException ex) {
			ex.printStackTrace();
		}
	}
	
	public void ScanMain(BarcodedItem item) {
		customerScanItem.scanItemMain(item);
	}
	
	public void ScanHeld(BarcodedItem item) {
		customerScanItem.scanItemHeld(item);
	}
	
	public void Bag(BarcodedItem item) {
		try {
			customerScanItem.placeItemInBagging(item);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
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
		
		BarcodedItem itemList[] = {		/*new BarcodedItem(new Barcode("012345"), 500),	
										new BarcodedItem(new Barcode("012346"), 2000)*/
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
	
	public void populateItems(ArrayList<BarcodedItem> list) {
		previouslyScannedItems.clear();
		
		for(BarcodedItem item : list) {
			previouslyScannedItems.add(item);
		}
	}
	
	public void populateDatabase(ArrayList<BarcodedProduct> list) {
		productDatabase.BARCODED_PRODUCT_DATABASE.clear();
		
		for(BarcodedProduct product : list) {
			productDatabase.BARCODED_PRODUCT_DATABASE.put(product.getBarcode(), product);
		}
	}
	
	public ArrayList<Product> convertItemToProduct(ArrayList<BarcodedItem> list) {
		ArrayList<Product> productList = new ArrayList<Product>();
		
		for(BarcodedItem item : list) {
			if(!productDatabase.BARCODED_PRODUCT_DATABASE.containsKey(item.getBarcode()))
				throw new SimulationException("Item not in product database");
			
			productList.add(productDatabase.BARCODED_PRODUCT_DATABASE.get(item.getBarcode()));
		}
		
		return productList;
	}
	
}
