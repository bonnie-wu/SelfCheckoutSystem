
import java.util.ArrayList;

import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SimulationException;

public class CustomerScanItem {
	
	private ArrayList<Item> scannedItems;
	private BarcodeScanner scanner;
	private ElectronicScale baggingScale;
	
	CustomerScanItem(BarcodeScanner scanner, ElectronicScale baggingScale, ArrayList<Item> previouslyScannedItems){
		if(scanner == null)
			throw new SimulationException("Barcode scanner is null");
		
		if(baggingScale == null)
			throw new SimulationException("Bagging area scale is null");
		
		if(previouslyScannedItems == null)
			throw new SimulationException("List of previously scanned items is null");
		
		if(previouslyScannedItems.size() == 0)
			throw new SimulationException("List of previously scanned items is empty");
		
		this.scanner = scanner;
		this.baggingScale = baggingScale;
		scannedItems = previouslyScannedItems;
	}
	
	CustomerScanItem(BarcodeScanner scanner, ElectronicScale baggingScale){
		if(scanner == null)
			throw new SimulationException("Barcode scanner is null");
		
		if(baggingScale == null)
			throw new SimulationException("Bagging area scale is null");
		
		this.scanner = scanner;
		this.baggingScale = baggingScale;
		scannedItems = new ArrayList<Item>();
	}
	
	public void scanItem(Item item) {
		if(item == null)
			throw new SimulationException("Can't scan item, item is null.");
		
		if(scanner.isDisabled())
			throw new SimulationException("Can't scan item, Scanner is disabled.");
		
		scanner.scan(item);
		scannedItems.add(item);
	}
	
	public void placeItemInBagging(Item item) throws OverloadException {
		if(item == null)
			throw new SimulationException("Can't place null item in bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging are scale is disabled, item not added.");

		if(scannedItemWeights() < baggingScale.getCurrentWeight())
			throw new SimulationException("Unidentified object in bagging area, please remove.");
		
		baggingScale.add(item);
	}
	
	public void removeScannedItem(Item item) {
		if(item == null)
			throw new SimulationException("Can't remove item, item is null.");

		scannedItems.remove(item);
	}
	
	public void removeItemFromBagging(Item item) throws OverloadException {
		if(item == null)
			throw new SimulationException("Can't remove null item from bagging area.");
		
		if(baggingScale.isDisabled())
			throw new SimulationException("Bagging are scale is disabled, item not removed.");
		
		baggingScale.remove(item);
	}
	
	public void clearBaggedItems() throws OverloadException {
		for(Item item : scannedItems) {
			try {
				baggingScale.remove(item);
			}
			catch(SimulationException ex) {}
		}
		
		if(baggingScale.getCurrentWeight() != 0)
			throw new SimulationException("Unpaid item is in bagging area");
	}
	
	public void clearScannedItems() {
		scannedItems.clear();
	}
	
	private double scannedItemWeights() {
		double total = 0;
		
		for(Item item : scannedItems) {
			total += item.getWeight();
		}
		
		return total;
	}
}
