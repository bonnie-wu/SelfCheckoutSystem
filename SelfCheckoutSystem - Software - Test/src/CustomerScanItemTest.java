/*
 * 	Class:			CustomerScanItemTest.java
 * 	Description:	JUnit testing class for CustomerScanItem.java
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
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;

public class CustomerScanItemTest {
	
	private static String BASEDIR = "\\some-path\\";
	CustomerScanItem customerScan;
	SelfCheckoutStation station;
	
	@Before
	public void setup() {
		Currency currency = getCurrency();
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
		customerScan = new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea);
	}
	
	/**
	 * Checks if the customer can scan using the main scanner
	 */
	@Test
	public void testCanScanWithMainScanner() {
		customerScan.scanItemMain(new BarcodedItem(new Barcode("12345"), 1.0));
		assertTrue(customerScan.getScannedItems().size() == 1);
	}
	
	/**
	 * Checks if the customer can scan using the handheld scanner
	 */
	@Test
	public void testCanScanWithHandheldScanner() {
		customerScan.scanItemHeld(newBarcodedItem("12345", 1.0));
		assertTrue(customerScan.getScannedItems().size() == 1);
	}
	
	/**
	 * Checks if the scanner can scan using multiple scanners
	 * @throws OverloadException 
	 */
	@Test
	public void testCanScanWithMultipleScanners() throws OverloadException {
		customerScan.clearBaggedItems();
		customerScan.clearScannedItems();
		
		BarcodedItem item1 = newBarcodedItem("12345", 1.0);
		BarcodedItem item2 = newBarcodedItem("22345", 2.0);
		BarcodedItem item3 = newBarcodedItem("33345", 3.0);
		
		customerScan.scanItemHeld(item1);
		customerScan.placeItemInBagging(item1);
		
		customerScan.scanItemHeld(item2);
		customerScan.placeItemInBagging(item2);
		
		customerScan.scanItemMain(item3);
		customerScan.placeItemInBagging(item3);
		
		assertEquals(3, customerScan.getScannedItems().size());
	}
	
	/**
	 * Test if an unknown item placed on the bagging area is rejected
	 */
	@Test
	public void testBaggingAreaRejectsUnknownItem() {
		try {
			customerScan.placeItemInBagging(newBarcodedItem("12345", 1.0));
		} catch (OverloadException e) {
			fail();
		} catch (SimulationException e) {
			// expected
		}
	}
	
	/**
	 * Check if the machine will complain if an item from the bagging area is removed
	 * @throws OverloadException 
	 */
	public void testCannotRemoveBaggedItem() throws OverloadException {
		BarcodedItem item1 = newBarcodedItem("12345", 1.0);
		customerScan.scanItemMain(item1);
		customerScan.placeItemInBagging(item1);
		
		// Remove the item from the bagging area and attempt to scan a new item
		try {
			customerScan.removeItemFromBagging(item1);
			customerScan.scanItemMain(newBarcodedItem("55245", 1.0));
			
			fail("Machine didn't react when an item from the bagging area is removed");
		} catch (Exception e) {
			// expected
		}
		
	}
	@Test
	public void testFunctionParameters() throws DisabledException, OverloadException{
		ArrayList<BarcodedItem> scannedItems = new ArrayList<BarcodedItem>();
		
		try {
			new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, null);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(station.mainScanner, station.handheldScanner, null, scannedItems);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(station.mainScanner, null, station.baggingArea, scannedItems);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(null, station.handheldScanner, station.baggingArea, scannedItems);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
			fail("Should throw SimulationException if previously scanned items is an empty list");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(station.mainScanner, station.handheldScanner, null);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(station.mainScanner, null, station.baggingArea);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerScanItem(null, station.handheldScanner, station.baggingArea);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			customerScan.placeItemInBagging(null);
			fail("Should throw SimulationException if placeItemInBagging parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			customerScan.removeItemFromBagging(null);
			fail("Should throw SimulationException if removeItemFromBagging parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			customerScan.removeScannedItem(null);
			fail("Should throw SimulationException if removeScannedItem parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			customerScan.scanItemHeld(null);
			fail("Should throw SimulationException if scanItemHeld parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			customerScan.scanItemMain(null);
			fail("Should throw SimulationException if scanItemMain parameter is null");
		} catch (SimulationException e) {/*expected*/ }
	}

	@Test
	public void testScanClear() {
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				newBarcodedItem("01234", 1.0),
				newBarcodedItem("012524", 3.0),
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
		
		customerScanItem.clearScannedItems();
		
		assertEquals(customerScanItem.getScannedItems().size(), 0);
	}
	
	@Test
	public void testBaggingClear() throws OverloadException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		BarcodedItem item2 = newBarcodedItem("012524", 3.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1,
				item2
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
		
		customerScanItem.placeItemInBagging(item1);
		customerScanItem.placeItemInBagging(item2);
		
		customerScanItem.clearBaggedItems();
		
		assertEquals(station.baggingArea.getCurrentWeight(), 0.0, 0.01);
	}
	
	@Test
	public void testBaggingClear2() throws OverloadException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		BarcodedItem item2 = newBarcodedItem("012524", 3.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1,
				item2
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
		
		station.baggingArea.add(newBarcodedItem("00000", 1.0));
		
		customerScanItem.placeItemInBagging(item1);
		
		try {
			customerScanItem.clearBaggedItems();
		}
		catch(SimulationException ex) { return; };
		fail("Expected SimulationExceptin when clearing bagging area, but unscanned item is found.");
	}
	
	@Test
	public void testWeightExceeded() throws OverloadException{
		BarcodedItem item1 = newBarcodedItem("01234", 24000.0);
		BarcodedItem item2 = newBarcodedItem("012345", 1500.0);
		
		try {
			customerScan.clearBaggedItems();
			customerScan.clearScannedItems();
			customerScan.scanItemHeld(item1);
			customerScan.scanItemMain(item2);
			customerScan.placeItemInBagging(item1);
			customerScan.placeItemInBagging(item2);
		}
		catch(SimulationException ex) {return;}
		fail("Expected OverloadException or SimulationException when bagging area scale exceeds wieght limit.");
	}
	
	@Test
	public void testOverload() throws OverloadException{
		station.baggingArea.add(newBarcodedItem("214124", 25001));
		
		try {
			BarcodedItem item1 = newBarcodedItem("01234", 10.0);
			customerScan.scanItemHeld(item1);
			fail("Expected SimulationException when trying to scan with an overloaded scale");
		}
		catch(SimulationException ex) { /* expected */}
		
		try {
			BarcodedItem item1 = newBarcodedItem("01234", 10.0);
			customerScan.scanItemMain(item1);
			fail("Expected SimulationException when trying to scan with an overloaded scale");
		}
		catch(SimulationException ex) { /* expected */}
	}
	
	@Test
	public void testScannerDisabled() throws OverloadException{
		try {
			station.mainScanner.disable();
			customerScan.scanItemMain(newBarcodedItem("01234", 1.0));
			fail("Should throw SimulationException if trying to scan when main scanner is disabled");
		} catch (SimulationException e) { station.mainScanner.enable(); }
		
		try {
			station.handheldScanner.disable();
			customerScan.scanItemHeld(newBarcodedItem("01234", 1.0));
			fail("Should throw SimulationException if trying to scan when hand held scanner is disabled");
		} catch (SimulationException e) { station.handheldScanner.enable(); }
		
		try {
			BarcodedItem item1 = newBarcodedItem("01234", 1.0);
			station.baggingArea.disable();
			customerScan.scanItemHeld(item1);
			customerScan.placeItemInBagging(item1);
			fail("Should throw SimulationException if trying to place item in bagging when bagging scale is disabled");
		} catch (SimulationException e) { station.baggingArea.enable(); }
	}
	
	/*
	 * Test to remove an item that was never scanned
	 */
	@Test
	public void testRemoveUnscannedItem() throws SimulationException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		BarcodedItem item2 = newBarcodedItem("01244", 1.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);

		try {
		customerScanItem.removeScannedItem(item2);
		fail("Should throw SimulationException if trying to remove an item that was never scanned");
		}
		catch(SimulationException e) {/* Expected */};
	}
	
	/*
	 * Test to remove an item that was scanned
	 */
	@Test
	public void testRemoveScannedItem() throws SimulationException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);

		try {
		customerScanItem.removeScannedItem(item1);
		}
		catch(SimulationException e) {/* Expected */};
		assertEquals(0, customerScanItem.getScannedItems().size());
	}
	
	/*
	 * Test to remove an item that was placed in the bagging area after the baggingArea is disabled
	 */
	@Test
	public void testRemoveBaggedItemDisabled() throws OverloadException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
		
		customerScanItem.placeItemInBagging(item1);
		station.baggingArea.disable();
		
		try {
		customerScanItem.removeItemFromBagging(item1);
		fail("Should throw SimulationException if bagging area is disabled");
		}
		catch(SimulationException e) {/* Expected */};
		
	}
	
	/*
	 * Test to remove an item from bagging area
	 */
	@Test
	public void testRemoveBaggedItem() throws OverloadException{
		BarcodedItem item1 = newBarcodedItem("01234", 1.0);
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				item1
		}));
		
		CustomerScanItem customerScanItem = 
		new CustomerScanItem(station.mainScanner, station.handheldScanner, station.baggingArea, scannedItems);
		
		customerScanItem.placeItemInBagging(item1);
		
		try {
		customerScanItem.removeItemFromBagging(item1);
		}
		catch(SimulationException e) {/* Expected */};
		assertEquals(0.0, station.baggingArea.getCurrentWeight(), 0.001);
	}
	
	private BarcodedItem newBarcodedItem(String barcode, double weight) {
		return new BarcodedItem(new Barcode(barcode), weight);
	}

	private Currency getCurrency() {
		return Currency.getInstance(Locale.CANADA);
	}
}
