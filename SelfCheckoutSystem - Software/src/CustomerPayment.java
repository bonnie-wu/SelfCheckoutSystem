import java.math.BigDecimal;
import java.util.ArrayList;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;


public class CustomerPayment {

	private ArrayList<BarcodedProduct> scannedItems;
	private float total;
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
	}

	/**
	 * Calculates the price total for all the scanned items in scannedItems
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
	 * 
	 * @param coin Type Coin of the coin that is being used to pay with
	 * @throws DisabledException occurs when the coin is null
	 */
	public void PayCoin(Coin coin) throws DisabledException{
		if(coin == null) {
			throw new SimulationException("Coin is null");
		}
		
		int capacity = station.coinStorage.getCoinCount();
		
		station.coinValidator.accept(coin);
		
		if(capacity != station.coinStorage.getCoinCount())
			total -= coin.getValue().floatValue();
	}
	
	/**
	 * Method for the customer to pay with a banknote
	 * 
	 * @param banknote Type Banknote of the banknote that is being used to pay with
	 * @throws DisabledException occurs when the banknote is null
	 */
	public void PayBanknote(Banknote banknote) throws DisabledException{
		if(banknote == null) {
			throw new SimulationException("Banknote is null");
		}
		
		int capacity = station.banknoteStorage.getBanknoteCount();
		
		station.banknoteValidator.accept(banknote);
		
		if(capacity != station.banknoteStorage.getBanknoteCount())
			total -= banknote.getValue();
	}
	
	
	/**
	 * Getter for the total
	 * @return total, type float to access the total of this class
	 */
	public float getTotal() {
		return total;
	}
	
	/**
	 * Setter for the total
	 * @param newTotal, type float of the new total
	 */
	public void setTotal(float newTotal) {
		total = newTotal;
	}
	
	/**
	 * Method to update new scanned products and calculate the new total
	 * @param scannedProducts ArrayList of barcoded products of scanned products
	 */
	public void updateScannedProducts(ArrayList<BarcodedProduct> scannedProducts) {
		if(scannedProducts == null)
			throw new SimulationException("Can't update scanned products, input is null");
		
		scannedItems = scannedProducts;
		total();
	}
}
