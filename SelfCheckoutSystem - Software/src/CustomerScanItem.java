
import java.util.ArrayList;

import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SimulationException;

public class CustomerScanItem {
	
	//Initializing global variables used to scan items or place items in bagging area.
	private ArrayList<BarcodedItem> scannedItems;
	private BarcodeScanner scannerMain;
	private BarcodeScanner scannerHeld;
	private ElectronicScale baggingScale;
	
	/*
	 * 	This constructor should throw a simulation exception if any invalid parameter is given.
	 * 	It should also initialize previously scanned items.
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
	}
	
	/*
	 * 	This constructor should throw a simulation exception if any invalid parameter is given.
	 * 	It shouldn't initialize previously scanned items.
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
	}
	
	/*
	 *  This function should scan a valid item and add it to the scannedItems list
	 *  It completes this task by calling the main scanner in SelfCheckoutStation, which will notify it's listeners upon successful scan
	 */
	public void scanItemMain(BarcodedItem item) {
		if(item == null)
			throw new SimulationException("Can't scan item, item is null.");
		
		if(scannerMain.isDisabled())
			throw new SimulationException("Can't scan item, Scanner is disabled.");
		
		scannerMain.scan(item);
		scannedItems.add(item);
	}
	
	/*
	 *  This function should scan a valid item and add it to the scannedItems list
	 *  It completes this task by calling the hand held scanner in SelfCheckoutStation, which will notify it's listeners upon successful scan
	 */
	public void scanItemHeld(BarcodedItem item) {
		if(item == null)
			throw new SimulationException("Can't scan item, item is null.");
		
		if(scannerHeld.isDisabled())
			throw new SimulationException("Can't scan item, Scanner is disabled.");
		
		scannerHeld.scan(item);
		scannedItems.add(item);
	}
	
	/*
	 *  This function should place a valid item into the bagging area.
	 *  It should add the item to the bagging area scale which should update the total weight on the bagging area scale.
	 *  It should notify the bagging scale listeners upon successful placement
	 */
	public void placeItemInBagging(BarcodedItem item) throws OverloadException{
		if(item == null)
			throw new SimulationException("Can't place null item in bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging are scale is disabled, item not added.");

		if(baggingScale.getCurrentWeight() + item.getWeight() > baggingScale.getWeightLimit())
			throw new SimulationException("Cannot place item in bagging area, weight limit will be exceeded.");
		
		if(scannedItemWeights() < baggingScale.getCurrentWeight() + item.getWeight())
			throw new SimulationException("Unidentified object in bagging area, please remove.");
		
		baggingScale.add(item);
	}
	
	/*
	 *  This function should remove a valid item from the scanned list.
	 */
	public void removeScannedItem(BarcodedItem item) {
		if(item == null)
			throw new SimulationException("Can't remove item, item is null.");

		scannedItems.remove(item);
	}
	
	/*
	 *  This function should remove a valid item from bagging area scale.
	 */
	public void removeItemFromBagging(BarcodedItem item) throws OverloadException {
		if(item == null)
			throw new SimulationException("Can't remove null item from bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging are scale is disabled, item not removed.");
		
		baggingScale.remove(item);
	}
	
	/*
	 * 	Clears all items from the bagging area
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
	
	/*
	 * 	Clears all items that have been scanned
	 */
	public void clearScannedItems() {
		scannedItems.clear();
	}
	
	/*
	 * 	Returns the total weight of all the scanned items
	 */
	private double scannedItemWeights() {
		double total = 0;
		
		for(BarcodedItem item : scannedItems) {
			total += item.getWeight();
		}
		
		return total;
	}
	
	/*
	 * 	Returns the list of scanned items
	 */
	public ArrayList<BarcodedItem> getScannedItems(){
		return scannedItems;
	}
}
