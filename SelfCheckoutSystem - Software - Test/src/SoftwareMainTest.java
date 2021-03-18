/*
 * 	Class:			SoftwareMainTest.java
 * 	Description:	JUnit testing class for SoftwareMain.java
 * 	Date:			3/17/2021
 * 	Authors: 		Vianney, Nguyen
 */

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.listeners.AbstractDeviceListener;
import org.lsmr.selfcheckout.devices.listeners.BarcodeScannerListener;
import org.lsmr.selfcheckout.products.BarcodedProduct;

/**
 * Tests the overall functionality of the self-checkout software. This class
 * focuses more on integration testing rather than unit testing.
 *
 */
public class SoftwareMainTest {
	
	private SelfCheckoutStation station;

	@Before
	public void setup() {
		
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
	}
	
	/**
	 * Verifies that the total cost reflects on the items that have been scanned
	 */
	@Test
	public void testTotalCostAfterScanning() {
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				newItem("01234", 1000),
				newItem("012345", 1500),
				newItem("0123456", 3000)
		}));
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50),
				newProduct("012345", 7.50),
				newProduct("0123456", 9.50),
				newProduct("01234567", 1.50),
				newProduct("012345678", 8.50)
		}));
		
		SoftwareMain main = new SoftwareMain(station, scannedItems);
		
		main.populateDatabase(databaseProducts);
		
		// Scan extra items
		main.ScanMain(newItem("01234567", 1020));
		main.ScanMain(newItem("012345678", 1050));
		
		main.updateScannedProducts();
		
		assertEquals(4.50 + 7.50 + 9.50 + 1.50 + 8.50, main.customerPayment.getTotal(), 0.000001);
	}
	
	/**
	 * Verifies that an unknown item cannot be scanned
	 */
	@Test
	public void testScanningItemNotInDatabase() {
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50)
		}));
		
		SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
		main.populateDatabase(databaseProducts);
		
		try {
			// Scan with unknown barcode
			main.ScanMain(newItem("01234567", 1020));
			main.updateScannedProducts();
			
			fail("Invalid item scanned successfully");
		} catch (Exception e) {
			// expected
		}
			
		assertEquals(0.0, main.customerPayment.getTotal(), 0.000001);
	}
	
	/**
	 * Verifies that an unknown item cannot be scanned
	 */
	@Test
	public void testPaymentAfterScan() {
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50),
				newProduct("1337", 100)
		}));
		
		SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
		main.populateDatabase(databaseProducts);
		
		BarcodedItem item1 = newItem("01234", 5);
		BarcodedItem item2 = newItem("1337", 2);
		
		// Test scanning multiple items and see if payment works as expected
		main.ScanMain(item1); // valid
		main.Bag(item1);
		
		main.ScanMain(item2); // valid
		main.Bag(item2);
		try {
			main.ScanMain(newItem("011", 2)); // invalid
		} catch (Exception e){}

		// Pay $2
		main.Pay(new Coin(BigDecimal.valueOf(2.0), Currency.getInstance(Locale.CANADA)));
		
		// verify that it's correct
		assertEquals((4.50+100)-2.0, main.customerPayment.getTotal(), 0.000001);
	}
	
	/**
	 * Verifies that the scanner works when an item is scanned
	 */
	@Test
	public void testIfScannerWorks() {
		BarcodeScannerListenerStub listener = new BarcodeScannerListenerStub();
		
		station.mainScanner.register(listener);
		station.handheldScanner.register(listener);
		
		
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50),
				newProduct("1337", 100)
		}));
		
		SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
		main.populateDatabase(databaseProducts);
		BarcodedItem item1 = newItem("01234", 5);
		BarcodedItem item2 = newItem("1337", 5);
		
		// Check main scanner
		main.ScanMain(item1);
		assertEquals(1, listener.scanned);
		main.Bag(item1);
		
		// Check hand-held scanner
		main.ScanHeld(item2);
		assertEquals(2, listener.scanned);
		main.Bag(item2);
		
		// Check main scan on invalid item. scanner should still notify
		main.ScanHeld(newItem("010101", 5));
		assertEquals(3, listener.scanned);
		
		// Check scan on invalid item. scanner should still notify
		try {
			main.ScanMain(newItem("010101", 5));
		} catch (Exception e){}
		assertEquals(4, listener.scanned);
		
		// Check hand-held scan on invalid item
		try {
			main.ScanHeld(newItem("010101", 5));
		} catch (Exception e){}
		assertEquals(5, listener.scanned);
	}
	
	/**
	 * Verify that the scanning an item after paying with banknotes doesn't change anything
	 */
	@Test
	public void testCannotScanAfterPayingWithBanknote() {
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50)
		}));
		
		SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
		main.populateDatabase(databaseProducts);
		
		// Scan, then pay
		main.ScanMain(newItem("01234", 1));
		main.updateScannedProducts();
		main.Pay(new Banknote(5, Currency.getInstance(Locale.CANADA)));
		
		// Attempt to scan again
		main.ScanHeld(newItem("01234", 1));
		try {
			main.updateScannedProducts();
		} catch (Exception e){ /* expected */ }
		
		// Verify nothing changed
		assertEquals(0, main.customerPayment.getTotal(), 0.0001);
	}
	
	/**
	 * Verify that the scanning an item after paying with coins doesn't change anything
	 */
	@Test
	public void testCannotScanAfterPayingWithCoin() {
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50)
		}));
		
		SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
		main.populateDatabase(databaseProducts);
		
		// Scan, then pay
		main.ScanMain(newItem("01234", 1));
		main.updateScannedProducts();
		main.Pay(new Coin(BigDecimal.valueOf(2.0), Currency.getInstance(Locale.CANADA)));
		
		// Attempt to scan again
		main.ScanHeld(newItem("01234", 1));
		try {
			main.updateScannedProducts();
		} catch (Exception e){ /* expected */ }
		
		// Verify nothing changed
		assertEquals(0, main.customerPayment.getTotal(), 0.0001);
	}
	
	/**
	 * Verify that all branch of execution has been went through
	 */
	@Test
	public void testOnNullParameters() {
		try {
			SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
			main.populateDatabase(null);
		} catch (SimulationException e) { /* expected */ }
		
		try {
			SoftwareMain main = new SoftwareMain(station, new ArrayList<>());
			main.convertItemToProduct(null);
		} catch (SimulationException e) { /* expected */ }
	}
	
	private BarcodedProduct newProduct(String barcode, double price) {
		return new BarcodedProduct(new Barcode(barcode), "Test Product", new BigDecimal(price));
	}
	
	private BarcodedItem newItem(String barcode, int weight) {
		return new BarcodedItem(new Barcode(barcode), weight);
	}
	
	private static class BarcodeScannerListenerStub implements BarcodeScannerListener {
		public int scanned = 0;

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
			scanned++;
		}
	}

}
