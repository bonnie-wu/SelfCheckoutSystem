import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.DisabledException;
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
		
		Currency currency = getCurrency();
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
	
	/**
	 * Paying with an invalid coin / banknote should not affect total
	 */
	@Test
	public void testInvalidCoinOrBanknote() throws DisabledException {
		
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// Test out invalid coin
		payment.PayCoin(new Coin(BigDecimal.valueOf(10), Currency.getInstance(Locale.FRANCE))); // not CAD
		assertEquals(15.2, payment.getTotal(), 0.001);
		
		// Test out banknote
		payment.PayBanknote(new Banknote(2, Currency.getInstance(Locale.FRANCE)));
		assertEquals(15.2, payment.getTotal(), 0.001);
	}
	
	/**
	 * Paying with a different currency should be rejected by the machine
	 * @throws DisabledException
	 */
	@Test
	public void testWrongCurrency() throws DisabledException {
		
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// Test out coin
		payment.PayCoin(new Coin(BigDecimal.valueOf(1), Currency.getInstance(Locale.FRANCE))); // not CAD
		assertEquals(15.2, payment.getTotal(), 0.001);
		
		// Test out banknote
		payment.PayBanknote(new Banknote(10, Currency.getInstance(Locale.FRANCE)));
		assertEquals(15.2, payment.getTotal(), 0.001);
	}
	
	/**
	 * Checks whether the class can properly sum up the totals
	 */
	@Test
	public void testIfTotalIsCorrect() {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2),
				newProduct("01234", 5.0),
				newProduct("01234", 1.5),
				newProduct("01234", 100)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		assertEquals(15.2+5.0+1.5+100, payment.getTotal(), 0.001);
	}
	
	/**
	 * Tests if a coin payment properly deducts the total cost
	 * @throws DisabledException
	 */
	@Test
	public void testIfCoinPaymentIsCorrect() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayCoin(new Coin(BigDecimal.valueOf(1.00), getCurrency()));
		
		assertEquals(15.2-1.0, payment.getTotal(), 0.0001);
	}

	/**
	 * Tests if a banknote payment properly deducts the total cost
	 * @throws DisabledException
	 */
	@Test
	public void testIfBanknotePaymentIsCorrect() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(10, getCurrency()));
		
		assertEquals(15.2-10.0, payment.getTotal(), 0.0001);
	}
	
	/**
	 * Checks if the user can pay multiple times with multiple different methods
	 * @throws DisabledException
	 */
	@Test
	public void testIfMultiplePaymentSupported() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(5, getCurrency())); // pay with banknote
		payment.PayCoin(new Coin(BigDecimal.valueOf(1.0), getCurrency()));
		payment.PayBanknote(new Banknote(5, getCurrency())); // pay with banknote
		
		assertEquals(15.2-5-1-5, payment.getTotal(), 0.001);
	}
	
	/**
	 * Verifies that paying over the amount doesn't bring the total below 0
	 * @throws DisabledException 
	 */
	@Test
	public void testOverPayment() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 1.0)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(5, getCurrency())); // pay with banknote
		
		assertTrue(payment.getTotal() >= 0);
	}
	
	/**
	 * Ensures that the customer cannot scan more items once they've paid
	 * @throws DisabledException
	 */
	@Test
	public void testCannotScanAfterPaying() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		// pay
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(10, getCurrency()));
		
		// scan more items
		payment.updateScannedProducts(scannedProducts);
		
		assertEquals(15.2-10, payment.getTotal(), 0.0001);
		
	}
	

	/**
	 * For full coverage
	 */
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
	
	
	private BarcodedProduct newProduct(String barcode, double price) {
		index++;
		return new BarcodedProduct(new Barcode(barcode), "Test Product" + index, new BigDecimal(price));
	}
	
	private Currency getCurrency() {
		return Currency.getInstance(Locale.CANADA);
	}

}
