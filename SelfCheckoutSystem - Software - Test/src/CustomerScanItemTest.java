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
	
	@Test
	public void testOnErrorIfNull() throws DisabledException{
		ArrayList<BarcodedItem> scannedItems = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				newBarcodedItem("01234", 1.0)
		}));
		
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
	 * Verifies that the customer cannot scan multiple items without placing it in the bag
	 */
	@Test
	public void testCannotScanMultipleWithoutBagging() {
		customerScan.scanItemHeld(newBarcodedItem("12345", 1.0)); // scan first item
		
		try {
			// let's scan more items without bagging it
			customerScan.scanItemHeld(newBarcodedItem("12345", 1.0));
			customerScan.scanItemMain(newBarcodedItem("12345", 1.0));
			
			fail("Customer was able to scan multiple items without bagging it.");
		} catch (Exception e) {
		}
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
	
	private BarcodedItem newBarcodedItem(String barcode, double weight) {
		return new BarcodedItem(new Barcode(barcode), 1.0);
	}

	private Currency getCurrency() {
		return Currency.getInstance(Locale.CANADA);
	}
}
