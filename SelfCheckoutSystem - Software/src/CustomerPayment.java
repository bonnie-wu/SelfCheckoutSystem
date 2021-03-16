import java.util.ArrayList;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

public class CustomerPayment {

	private ArrayList<Product> scannedItems;
	private float total;
	private CoinValidator coinValidator;
	private BanknoteValidator banknoteValidator;
	
	
	CustomerPayment(ArrayList<Product> scannedItems, CoinValidator coinValidator, BanknoteValidator banknoteValidator){
		if(scannedItems == null) {
			throw new SimulationException("List of scanned items is null");
		}
		if(banknoteValidator == null) {
			throw new SimulationException("Banknote Validator is null");
		}
		if(coinValidtor == null) {
			throw new SimulationException("Coin Validator is null");
		}
		this.coinValidator = coinValidator;
		this.banknoteValidator = banknoteValidator;
		this.scannedItems = scannedItems;
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
		boolean valid = banknoteValidator.isValid(banknote);
		if (valid == true) {
			banknoteValidator.accept(banknote);
			setTotal(getTotal() - banknote.getValue());
			}
		}
		if(coin == true) {
			if(coin == null) {
				throw new SimulationException("Coin is null");
			}
		boolean valid = coinValidator.isValid(coin);
		if(valid == true) {
			coinValidator.accept(coin);
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
