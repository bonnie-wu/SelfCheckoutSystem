import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

public class SoftwareMain {
	
	SelfCheckoutStation station;
	
	public void main(String[] args) {
		initialize();
		
		
	}
	
	private void initialize() {
		Currency currency = Currency.getInstance(Locale.CANADA);
		int[] banknoteDenominations = {5, 10, 20, 50, 100};
		BigDecimal[] coinDenominations = {	BigDecimal.valueOf(0.05), 
											BigDecimal.valueOf(0.10),
											BigDecimal.valueOf(0.25),
											BigDecimal.valueOf(0.50), 
											BigDecimal.valueOf(1.00), 
											BigDecimal.valueOf(2.00)};
		int scaleMaximumWeight = (25*1000);
		int scaleSensitivity = (15);
		
		station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
	}
}
