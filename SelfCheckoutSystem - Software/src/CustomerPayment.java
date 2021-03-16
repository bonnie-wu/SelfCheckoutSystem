import java.math.BigDecimal;
import java.util.ArrayList;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.Product;

public class CustomerPayment {

	private ArrayList<Product> scannedItems;
	private BigDecimal total;
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
		int length = this.scannedItems.size();
		for(int i = 0; i < length; i++) {
			setTotal(getTotal().add(this.scannedItems.get(i).getPrice()));
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
			setTotal(getTotal().subtract(coin.getValue()));
	}
	
	public void PayBanknote(Banknote banknote) throws DisabledException{
		if(banknote == null) {
			throw new SimulationException("Banknote is null");
		}
		
		int capacity = station.banknoteStorage.getBanknoteCount();
		
		station.banknoteValidator.accept(banknote);
		
		if(capacity != station.banknoteStorage.getBanknoteCount())
			setTotal(getTotal().subtract(BigDecimal.valueOf(banknote.getValue())));
	}
	
	// getter and setter for the price total
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal newTotal) {
		this.total = newTotal;
	}
}
