/*
 * 	Class:			CustomerScanItem.java
 * 	Description:	Handles the functionality of a customer scanning an item, removing a scanned item,
 * 					placing an item in the bagging area, and removing an item from bagging area.
 * 	Date:			3/17/2021
 * 	Authors: 		Derek Urban, Bonnie Wu
 */

import java.util.ArrayList;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.listeners.AbstractDeviceListener;
import org.lsmr.selfcheckout.devices.listeners.BarcodeScannerListener;
import org.lsmr.selfcheckout.devices.listeners.ElectronicScaleListener;

public class CustomerScanItem {
	
	//Local flags to indicate listeners responses
	private boolean itemScanned = false;
	private boolean scaleOverload = false;
	
	//Global variables used by the system to scan and bag items
	private ArrayList<BarcodedItem> scannedItems;
	private BarcodeScanner scannerMain;
	private BarcodeScanner scannerHeld;
	private ElectronicScale baggingScale;
	
	/**
	 * 	Constructor that initializes listeners and hardware, while ensuring hardware is initialized correctly.Also recieves
	 *  a list of previously scanned items, to simulate the scanning of an item mid-process.
	 *  
	 * 	@param BarcodeScanner scannerMain
	 * 			BarcodeScanner object used by the SelfCheckoutStation, main scanner
	 * 
	 *  @param BarcodeScanner scannerHeld
	 *  		BarcodeScanenr object used by the SelfCheckoutStaion, hand held scanner
	 *  
	 *  @param ElectronicScale baggingScale
	 *  		ElectronicScale object used by the SelfCheckoutStation, the electronic weight scale in the bagging area
	 *  
	 *  @param ArrayList<BarcodedItem> previouslyScannedItems
	 *  		A list of previously scanned BarcodedItems, to help simulate previously scanned items prior to scan
	 *  
	 *  @throws SimulationException
	 *  		If any of the given parameters are null.
	 *  		If the list of previouslyScannedItems is empty
	 */
	CustomerScanItem(BarcodeScanner scannerMain, BarcodeScanner scannerHeld, ElectronicScale baggingScale, ArrayList<BarcodedItem> previouslyScannedItems){
		if(scannerMain == null)
			throw new SimulationException("Main barcode scanner is null");
		
		if(scannerHeld == null)
			throw new SimulationException("Held barcode scanner is null");
		
		if(baggingScale == null)
			throw new SimulationException("Bagging area scale is null");
		
		if(previouslyScannedItems == null)
			throw new SimulationException("List of previously scanned items is null");
		
		if(previouslyScannedItems.size() == 0)
			throw new SimulationException("List of previously scanned items is empty");
		
		this.scannerMain = scannerMain;
		this.scannerHeld = scannerHeld;
		this.baggingScale = baggingScale;
		scannedItems = previouslyScannedItems;
		
		initListeners();
	}
	
	
	/**
	 * 	Constructor that initializes listeners and hardware, while ensuring hardware is initialized correctly.
	 *  Does not receive a list of previously scanned items.
	 *  
	 * 	@param BarcodeScanner scannerMain
	 * 			BarcodeScanner object used by the SelfCheckoutStation, main scanner
	 * 
	 *  @param BarcodeScanner scannerHeld
	 *  		BarcodeScanenr object used by the SelfCheckoutStaion, hand held scanner
	 *  
	 *  @param ElectronicScale baggingScale
	 *  		ElectronicScale object used by the SelfCheckoutStation, the electronic weight scale in the bagging area
	 *  
	 *  @throws SimulationException
	 *  		If any of the given parameters are null.
	 */
	CustomerScanItem(BarcodeScanner scannerMain, BarcodeScanner scannerHeld, ElectronicScale baggingScale){
		if(scannerMain == null)
			throw new SimulationException("Main barcode scanner is null");
		
		if(scannerHeld == null)
			throw new SimulationException("Held barcode scanner is null");
		
		if(baggingScale == null)
			throw new SimulationException("Bagging area scale is null");
		
		this.scannerMain = scannerMain;
		this.scannerHeld = scannerHeld;
		this.baggingScale = baggingScale;
		scannedItems = new ArrayList<BarcodedItem>();
		
		initListeners();
	}
	
	/**
	 * Initializes the listeners used by this class to listen to hardware and ensure use cases are properly
	 * commenced, such as scanning items and placing items in bagging area. Will set flags according to hardware responses
	 */
	private void initListeners() {
		scannerMain.register(new BarcodeScannerListener() {
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
				if(barcodeScanner.equals(scannerMain))
					itemScanned = true;
			}
		});
		
		scannerHeld.register(new BarcodeScannerListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
				if(barcodeScanner.equals(scannerHeld))
					itemScanned = true;
			}
		});
		
		baggingScale.register(new ElectronicScaleListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void weightChanged(ElectronicScale scale, double weightInGrams) {}
			public void overload(ElectronicScale scale) {
				if(scale.equals(baggingScale))
					scaleOverload = true;
			}
			public void outOfOverload(ElectronicScale scale) {
				if(scale.equals(baggingScale))
					scaleOverload = false;
			}
		});
	}
	
	/**
	 * Scans an item using the main scanner from SelfCheckoutSystem
	 * 
	 * @param BarcodedItem item
	 * 			The item to be scanned
	 * 
	 * @throws SimulationException
	 * 			If the item is null
	 * 			If the scanner is disabled
	 * 			If the bagging area is overloading weight and needs to be cleared before proceeding
	 */
	public void scanItemMain(BarcodedItem item) {
		
		itemScanned = false;
		
		if(item == null)
			throw new SimulationException("Can't scan item, item is null.");
		
		if(scannerMain.isDisabled())
			throw new SimulationException("Can't scan item, Scanner is disabled.");
		
		if(scaleOverload)
			throw new SimulationException("Can't scan item, bagging area exceeds weight limit.");
		
		scannerMain.scan(item);
		
		if(itemScanned)
			scannedItems.add(item);
	}
	
	/**
	 * Scans an item using the hand held scanner from SelfCheckoutSystem
	 * 
	 * @param BarcodedItem item
	 * 			The item to be scanned
	 * 
	 * @throws SimulationException
	 * 			If the item is null
	 * 			If the scanner is disabled
	 * 			If the bagging area is overloading weight and needs to be cleared before proceeding
	 */
	public void scanItemHeld(BarcodedItem item) {
		itemScanned = false;
		
		if(item == null)
			throw new SimulationException("Can't scan item, item is null.");
		
		if(scannerHeld.isDisabled())
			throw new SimulationException("Can't scan item, Scanner is disabled.");
		
		if(scaleOverload)
			throw new SimulationException("Can't scan item, bagging area exceeds weight limit.");
		
		scannerHeld.scan(item);
		
		if(itemScanned)
			scannedItems.add(item);
	}
	
	/**
	 * Places the given item into the bagging area of the SelfCheckoutStation
	 * 
	 * @param BarcodedItem item
	 * 			The item to be placed in bagging area
	 * 
	 * @throws OverloadException
	 * 			If the weight scale is overloaded when trying to add an item (shouldn't be called)
	 * 
	 * @throws SimulationException
	 * 			If the item is null
	 * 			If the bagging area scale is disabled
	 * 			If the item being added will exceed the weight limit of the scale
	 * 			If the item being added will exceed the combined weight of the scanned products
	 * 			(Implying an item on the scale wasn't scanned)
	 */
	public void placeItemInBagging(BarcodedItem item) throws OverloadException{
		if(item == null)
			throw new SimulationException("Can't place null item in bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging scale is disabled, item not added.");

		if(baggingScale.getCurrentWeight() + item.getWeight() > baggingScale.getWeightLimit())
			throw new SimulationException("Cannot place item in bagging area, weight limit will be exceeded.");
		
		if(scannedItemWeights() < baggingScale.getCurrentWeight() + item.getWeight())
			throw new SimulationException("Unidentified object in bagging area, please remove.");
		
		baggingScale.add(item);
	}
	
	/**
	 * Removes a scanned item from the list of scanned items
	 * 
	 * @param BarcodedItem item
	 * 			The item to be removed
	 * 
	 * @throws SimulationException
	 * 			If item is null
	 * 			If item isn't in the list of scannedItems
	 */
	public void removeScannedItem(BarcodedItem item) {
		if(item == null)
			throw new SimulationException("Can't remove item, item is null.");
		
		if(!scannedItems.contains(item))
			throw new SimulationException("Can't remove item, item never scanned");
		
		scannedItems.remove(item);
	}
	
	/**
	 * Removes a scanned item from the bagging area
	 * 
	 * @param BarcodedItem item
	 * 			The item to be removed
	 * 
	 * @throws OverloadException
	 * 			If the bagging area scale is currently exceeding its max weight (Shouldn't be thrown)
	 * 
	 * @throws SimulationException
	 * 			If item is null
	 * 			If the baggingScale is disabled
	 */
	public void removeItemFromBagging(BarcodedItem item) throws OverloadException {
		if(item == null)
			throw new SimulationException("Can't remove null item from bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging are scale is disabled, item not removed.");
		
		baggingScale.remove(item);
	}
	
	/**
	 * Simply removes all items from the bagging area, intended for testing purposes
	 * 
	 * @throws OverloadException
	 * 			If the bagging area scale is currently exceeding its max weight (Shouldn't be thrown)
	 * 
	 * @throws SimulationException
	 * 			If an unscanned item is on the bagging scale
	 */
	public void clearBaggedItems() throws OverloadException {
		for(BarcodedItem item : scannedItems) {
			try {
				baggingScale.remove(item);
			}
			catch(SimulationException ex) {}
		}
		
		if(baggingScale.getCurrentWeight() != 0)
			throw new SimulationException("Unpaid item is in bagging area");
	}
	
	/**
	 *  Clears the scanned items
	 */
	public void clearScannedItems() {
		scannedItems.clear();
	}
	
	/**
	 * Getter for the total combined weight of all scanned items
	 * 
	 * @return double total
	 * 			The total combined weight of all scanned items
	 */
	private double scannedItemWeights() {
		double total = 0;
		
		for(BarcodedItem item : scannedItems) {
			total += item.getWeight();
		}
		
		return total;
	}
	
	/**
	 * Getter for the list of scanned items
	 */
	public ArrayList<BarcodedItem> getScannedItems(){
		return scannedItems;
	}
}
