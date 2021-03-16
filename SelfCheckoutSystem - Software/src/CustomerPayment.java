import java.util.ArrayList;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

public class CustomerPayment {

	private ArrayList<Product> scannedItems;
	private float total;
	private SelfCheckoutStation station;
	
	
	CustomerPayment(ArrayList<Product> scannedItems, SelfCheckoutStation station){
		if(scannedItems == null) {
			throw new SimulationException("List of scanned items is null");
		}
		if(station == null) {
			throw new SimulationException("Banknote Validator is null");
		}
		this.scannedItems = scannedItems;
		this.station = station;
	}

	// calculate the total of everything in the arrayList
	public void total() {
		int length = this.list.length;
		for(int i = 0; i < length; i++) {
			setTotal(getTotal() + this.scannedItems[i].getPrice());
		}
	}
	
	// payment of the total, will include coins and banknote
	// boolean if there are banknotes
	// boolean if there are coins
	// banknotes will be handled first
	public void Pay(boolean banknote, boolean coin, Banknote banknote, Coin coin){
		if(banknote == true) {
			if(banknote == null) {
				throw new SimulationException("Banknote is null");
			}
		boolean validNote = station.banknoteValidator.isValid(banknote);
		if (validNote == true) {
			station.banknoteValidator.accept(banknote);
			
			setTotal(getTotal() - banknote.getValue());
			}
		}
		
		if(coin == true) {
			if(coin == null) {
				throw new SimulationException("Coin is null");
			}
		boolean validCoin = station.coinValidator.isValid(coin);
		if(validCoin == true) {
			station.coinValidator.accept(coin);
			setTotal(getTotal() - coin.getValue());
			}
		}
	}
	
	// getter and setter for the price total
	public float getTotal() {
		return total;
	}
	
	public void setTotal(float newTotal) {
		this.total = newTotal;
	}
}
