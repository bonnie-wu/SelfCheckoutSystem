import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.Product;

public class CustomerPaymentTest {

	private SelfCheckoutStation station;
	private int index = 0;

	@Before
	public void setup() {
		index = 0;
		
		Currency currency = Currency.getInstance(Locale.CANADA);
		int[] banknoteDenominations = {5, 10, 20, 50, 100};
		BigDecimal[] coinDenominations = {	BigDecimal.valueOf(0.05), 
											BigDecimal.valueOf(0.10),
											BigDecimal.valueOf(0.25),
											BigDecimal.valueOf(0.50), 
											BigDecimal.valueOf(1.00), 
											BigDecimal.valueOf(2.00)};
		int scaleMaximumWeight = (25*1000);	//Scale maximum in grams
		int scaleSensitivity = (15);		//Scale sensitivity in grams
		
		station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
	}
	
	@Test
	public void testOnErrorIfNull() {
		try {
			new CustomerPayment(null, station);
			fail();
		} catch (SimulationException e) {
			// expected
		}
		
		try {
			new CustomerPayment(new ArrayList<BarcodedProduct>(), null);
			fail();
		} catch (SimulationException e) {
			// expected
		}
	}
	
	@Test
	public void testIfTotalIsCorrect() {
		ArrayList<Product> scannedProducts = new ArrayList<>(Arrays.asList(new Product[] {
				newProduct(15.2),
				newProduct(5.0),
				newProduct(1.5),
				newProduct(100)
		}));
		
		//CustomerPayment payment = new CustomerPayment(scannedProducts)
	}
	
	private BarcodedProduct newProduct(double price) {
		index++;
		return new BarcodedProduct(new Barcode(String.valueOf(index)), "Desc: " + index, new BigDecimal(price));
	}

}
