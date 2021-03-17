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

	// calculate the total of everything in the arrayList
	public void total() {
		total = 0;
		int length = this.scannedItems.size();
		for(int i = 0; i < length; i++) {
			total += this.scannedItems.get(i).getPrice().floatValue();
		}
	}
	
	// payment of the total, will include coins and banknote
	// boolean if there are banknotes
	// boolean if there are coins
	// banknotes will be handled first
	
	public void PayCoin(Coin coin) throws DisabledException{
		if(coin == null) {
			throw new SimulationException("Coin is null");
		}
		
		int capacity = station.coinStorage.getCoinCount();
		
		station.coinValidator.accept(coin);
		
		if(capacity != station.coinStorage.getCoinCount())
			total -= coin.getValue().floatValue();
	}
	
	public void PayBanknote(Banknote banknote) throws DisabledException{
		if(banknote == null) {
			throw new SimulationException("Banknote is null");
		}
		
		int capacity = station.banknoteStorage.getBanknoteCount();
		
		station.banknoteValidator.accept(banknote);
		
		if(capacity != station.banknoteStorage.getBanknoteCount())
			total -= banknote.getValue();
	}
	
	// getter and setter for the price total
	public float getTotal() {
		return total;
	}
	
	public void setTotal(float newTotal) {
		total = newTotal;
	}
	
	public void updateScannedProducts(ArrayList<BarcodedProduct> scannedProducts) {
		if(scannedProducts == null)
			throw new SimulationException("Can't update scanned products, input is null");
		
		scannedItems = scannedProducts;
		total();
	}
}
