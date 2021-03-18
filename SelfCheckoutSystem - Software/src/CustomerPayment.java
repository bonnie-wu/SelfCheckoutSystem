/*
 * 	Class:			CustomerPayment.java
 * 	Description:	Handles the functionality of a customer paying with coin or banknote.
 * 	Date:			3/17/2021
 * 	Authors: 		Derek Urban, Bonnie Wu
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
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
import org.lsmr.selfcheckout.products.BarcodedProduct;


public class CustomerPayment {
	
	//Listener flags, used by the system to record listener responses
	private boolean coinPaid = false;
	private boolean banknotePaid = false;
	
	// used to indicate if the user attempted to pay. if so then they cannot scan more items
	private boolean canScan = true;
	
	//Global variables
	private ArrayList<BarcodedProduct> scannedItems;
	private float total;
	private float change;
	private SelfCheckoutStation station;
	
	
	/**
	 * Constructor for class and initializes variables
	 * 
	 * @param scannedItems An ArrayList of Barcoded Products of items that have been scanned
	 * @param station The SelfCheckoutStation on which this software is operating on
	 */
	CustomerPayment(ArrayList<BarcodedProduct> scannedItems, SelfCheckoutStation station){
		if(scannedItems == null) {
			throw new SimulationException("List of scanned items is null");
		}
		if(station == null) {
			throw new SimulationException("Banknote Validator is null");
		}
		this.scannedItems = scannedItems;
		this.station = station;
		total();
		
		initListeners();
	}
	
	/**
	 * Initializes the listeners used by this class to listen to hardware and ensure use cases are properly
	 * commenced, such as paying with coin or banknote. Will set flags according to hardware responses
	 */
	private void initListeners() {
		station.coinValidator.register(new CoinValidatorListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void validCoinDetected(CoinValidator validator, BigDecimal value) {
				if(validator.equals(station.coinValidator))
					coinPaid = true;
			}
			public void invalidCoinDetected(CoinValidator validator) {
				if(validator.equals(station.coinValidator))
					coinPaid = false;
			}
		});
		
		station.banknoteValidator.register(new BanknoteValidatorListener(){
			public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {}
			public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
				if(validator.equals(station.banknoteValidator))
					banknotePaid = true;
			}
			public void invalidBanknoteDetected(BanknoteValidator validator) {
				if(validator.equals(station.banknoteValidator))
					banknotePaid = false;
			}
		});
	}

	/**
	 * Calculates the price total for all the scanned items in scannedItems by 
	 * iterating through the whole list
	 */
	public void total() {
		total = 0;
		int length = this.scannedItems.size();
		for(int i = 0; i < length; i++) {
			total += this.scannedItems.get(i).getPrice().floatValue();
		}
	}
	
	/**
	 * Method for the customer to pay with a coin
	 * Checks the capacity of the coin storage and attempt to accept coin
	 * Only when there is space in the storage, update the total
	 * 
	 * @param coin Type Coin of the coin that is being used to pay with
	 * @throws DisabledException occurs when the coin is null
	 */
	public void PayCoin(Coin coin) throws DisabledException{
		canScan = false;
		
		coinPaid = false;
		
		if(total <= 0)
			throw new SimulationException("Total amount owed is less than or equal to 0");
		
		if(coin == null) {
			throw new SimulationException("Coin is null");
		}
		
		int coinCount = station.coinStorage.getCoinCount();
		
		if(station.coinStorage.getCapacity() == coinCount)
			throw new SimulationException("Cannot deliver coin, coin storage is full!");
		
		station.coinValidator.accept(coin);
		
		if(coinPaid)
			total -= coin.getValue().floatValue();
		
		if(total <= 0) {
			change = Math.abs(total);
			total = 0;
		}
		
	}
	
	/**
	 * Method for the customer to pay with a banknote
	 * checks the capacity of the banknote storage and attempt to accept banknote
	 * Only when there is space in the storage, update the total
	 * 
	 * @param banknote Type Banknote of the banknote that is being used to pay with
	 * @throws DisabledException occurs when the banknote is null
	 */
	public void PayBanknote(Banknote banknote) throws DisabledException{
		canScan = false;
		
		banknotePaid = false;
		
		if(total <= 0)
			throw new SimulationException("Total amount owed is less than or equal to 0");
		
		if(banknote == null) {
			throw new SimulationException("Banknote is null");
		}
		
		int banknoteCount = station.banknoteStorage.getBanknoteCount();
		
		if(station.banknoteStorage.getCapacity() == banknoteCount)
			throw new SimulationException("Cannot deliver banknote, banknote storage is full!");
		
		station.banknoteValidator.accept(banknote);
		
		if(banknotePaid)
			total -= banknote.getValue();
		
		if(total <= 0) {
			change = Math.abs(total);
			total = 0;
		}
	}
	
	
	/**
	 * Getter for the total
	 * @return total, type float to access the total of this class
	 */
	public float getTotal() {
		return total;
	}
	
	/**
	 * Method to update new scanned products and calculate the new total
	 * @param scannedProducts ArrayList of barcoded products of scanned products
	 */
	public void updateScannedProducts(ArrayList<BarcodedProduct> scannedProducts) {
		if(scannedProducts == null)
			throw new SimulationException("Can't update scanned products, input is null");
		if (!canScan) throw new SimulationException("Attempted to scan while paying");
		
		scannedItems = scannedProducts;
		total();
		change = 0;
	}
}
