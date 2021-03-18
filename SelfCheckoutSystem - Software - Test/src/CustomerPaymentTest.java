/*
 * 	Class:			CustomerPaymentTest.java
 * 	Description:	JUnit testing class for CustomerPayment.java
 * 	Date:			3/17/2021
 * 	Authors: 		Vianney, Nguyen
 */

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
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.Acceptor;
import org.lsmr.selfcheckout.devices.BanknoteDispenser;
import org.lsmr.selfcheckout.devices.BanknoteSlot;
import org.lsmr.selfcheckout.devices.BanknoteStorageUnit;
import org.lsmr.selfcheckout.devices.CoinStorageUnit;
import org.lsmr.selfcheckout.devices.CoinTray;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.UnidirectionalChannel;
import org.lsmr.selfcheckout.devices.listeners.AbstractDeviceListener;
import org.lsmr.selfcheckout.devices.listeners.BanknoteDispenserListener;
import org.lsmr.selfcheckout.devices.listeners.BanknoteSlotListener;
import org.lsmr.selfcheckout.devices.listeners.BanknoteStorageUnitListener;
import org.lsmr.selfcheckout.devices.listeners.CoinSlotListener;
import org.lsmr.selfcheckout.devices.listeners.CoinStorageUnitListener;
import org.lsmr.selfcheckout.devices.listeners.CoinTrayListener;
import org.lsmr.selfcheckout.devices.listeners.CoinValidatorListener;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.Product;

public class CustomerPaymentTest {

	private static String BASEDIR = "\\some-path\\";
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
	 * Verifies that the coin storage can become full - this implies that the coin listener is
	 * working as intended
	 * @throws DisabledException
	 */
	@Test
	public void testMaxCoinStorage() throws DisabledException{
		int maxLoop = 10000;
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 1100.0)
		}));
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// repeatedly insert coins until it becomes full
		for(int i = 0; i < maxLoop; i++) {
			try {
				payment.PayCoin(new Coin(BigDecimal.valueOf(1.00), getCurrency()));
			}
			catch(SimulationException e) { return; }
		}
		
		fail("Coin storage should be full and throw SimulationException");
	}
	
	/**
	 * Checks to see if the banknote storage has proper code set up so that it can let the software
	 * know if it becomes full.
	 * @throws DisabledException
	 */
	@Test
	public void testMaxBanknoteStorage() throws DisabledException{
		int maxLoop = 10000;
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 5500.0)
		}));
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// repeatedly insert banknotes until full
		for(int i = 0; i < maxLoop; i++) {
			try {
				payment.PayBanknote(new Banknote(5, getCurrency()));
				station.banknoteInput.removeDanglingBanknote();
			}
			catch(SimulationException e) { return; }
		}
		
		fail("Banknote storage should be full and throw SimulationException");
	}
	
	/**
	 * Test to see if an error arises from attempting to pay with coins without scanning any items
	 * @throws DisabledException
	 */
	@Test
	public void testPayWhenNothingScanned1() throws DisabledException{
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(); // nothing scanned
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// pay without scanning
		try {
			payment.PayCoin(new Coin(BigDecimal.valueOf(1.00), getCurrency()));
		}
		catch(SimulationException ex) { return; }
		
		// no exception -> machine accepted payment => fail
		fail("Expected SimulationException");
	}
	
	/**
	 * Test to see if an error arises from attempting to pay with banknotes without scanning any items
	 * @throws DisabledException
	 */
	@Test
	public void testPayWhenNothingScanned2() throws DisabledException{
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>();
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		
		// pay without scanning
		try {
			payment.PayBanknote(new Banknote(5, getCurrency())); // pay with banknote
		}
		catch(SimulationException ex) { return; }
		
		fail("Expected SimulationException");
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
	 * Verifies that paying over the amount with banknotes doesn't bring the total below 0
	 * @throws DisabledException 
	 */
	@Test
	public void testOverPayment1() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 1.0)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(5, getCurrency())); // pay with banknote
		
		assertEquals(payment.getTotal() >= 0, true);
	}
	
	/**
	 * Verifies that paying over the amount with coins doesn't bring the total below 0
	 * @throws DisabledException 
	 */
	@Test
	public void testOverPayment2() throws DisabledException {
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 1.0)
		}));
		
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayCoin(new Coin(BigDecimal.valueOf(2.00), getCurrency())); //pay with coin
		
		assertEquals(payment.getTotal() >= 0, true);
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
		try {
			payment.updateScannedProducts(scannedProducts);
		} catch (Exception e) {}
		
		assertEquals(15.2-10, payment.getTotal(), 0.0001);
		
	}
	
	
	/**
	 * Verify that a coin is delivered to its correct sink
	 * @throws DisabledException
	 */
	@Test
	public void testCorrectCoinSink() throws DisabledException {
		CoinReceiverListenerStub storageListener = new CoinReceiverListenerStub();
		CoinReceiverListenerStub rejectListener = new CoinReceiverListenerStub();
		
		station.coinStorage.register(storageListener);
		station.coinTray.register(rejectListener);
		
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		// pay
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayCoin(new Coin(BigDecimal.valueOf(2.00), getCurrency()));
		
		// verify that the machine received it
		assertEquals(1, storageListener.count);
		assertEquals(0, rejectListener.count);
		
		// try it out with invalid coin
		storageListener.count = 0;
		rejectListener.count = 0;
		
		payment.PayCoin(new Coin(BigDecimal.valueOf(3), getCurrency()));
		// verify that the machine received it
		assertEquals(0, storageListener.count);
		assertEquals(1, rejectListener.count);
	}

	/**
	 * Verify that a banknote is either accepted or rejected properly
	 * @throws DisabledException
	 */
	@Test
	public void testCorrectBanknoteResponse() throws DisabledException {
		BanknoteReceiverListenerStub storageListener = new BanknoteReceiverListenerStub();
		BanknoteReceiverListenerStub rejectListener = new BanknoteReceiverListenerStub();
		
		station.banknoteStorage.register(storageListener);
		station.banknoteInput.register(rejectListener);
		
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		
		// pay
		CustomerPayment payment = new CustomerPayment(scannedProducts, station);
		payment.PayBanknote(new Banknote(10, getCurrency()));
		
		// verify that the machine received it
		assertEquals(1, storageListener.count);
		assertEquals(0, rejectListener.count);
		
		// try it out with invalid coin
		storageListener.count = 0;
		rejectListener.count = 0;
		
		payment.PayBanknote(new Banknote(19, getCurrency()));
		// verify that the machine received it
		assertEquals(0, storageListener.count);
		assertEquals(1, rejectListener.count);
	}
	
	/**
	 * Test other branches of execution to see that each branch has been called
	 */
	@Test
	public void testOnErrorIfNull() throws DisabledException{
		try {
			new CustomerPayment(null, station);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/ }
		
		try {
			new CustomerPayment(new ArrayList<BarcodedProduct>(), null);
			fail("Should throw SimulationException if constructor parameter is null");
		} catch (SimulationException e) {/*expected*/}
		
		ArrayList<BarcodedProduct> scannedProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 15.2)
		}));
		CustomerPayment customerPayment = new CustomerPayment(scannedProducts, station);
		try {
			customerPayment.PayBanknote(null);
			fail("Should throw SimulationException if PayBanknote parameter is null");
		} catch (SimulationException e) {/*expected*/}
		
		try {
			customerPayment.PayCoin(null);;
			fail("Should throw SimulationException if PayCoin parameter is null");
		} catch (SimulationException e) {/*expected*/}
		
		try {
			customerPayment.updateScannedProducts(null);
			fail("Should throw SimulationException if updateScannedProducts parameter is null");
		} catch (SimulationException e) {/*expected*/}
	}
	
	/**
	 * Helper method that generates a BarcodedProduct given the barcode and the price
	 * @param barcode the barcode string
	 * @param price   the price of the item
	 * @return		  a BarcodedProduct instance
	 */
	private BarcodedProduct newProduct(String barcode, double price) {
		index++;
		return new BarcodedProduct(new Barcode(barcode), "Test Product" + index, new BigDecimal(price));
	}
	
	/**
	 * Returns the currency used.
	 * 
	 * This exists so that the test cases know what currency the software expects.
	 * @return
	 */
	private Currency getCurrency() {
		return Currency.getInstance(Locale.CANADA);
	}
	
	private static class CoinReceiverListenerStub implements CoinStorageUnitListener, CoinTrayListener {
		public int count = 0;

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void coinAdded(CoinTray tray) {
			count++;
		}

		@Override
		public void coinsFull(CoinStorageUnit unit) {
		}

		@Override
		public void coinAdded(CoinStorageUnit unit) {
			count++;
		}

		@Override
		public void coinsLoaded(CoinStorageUnit unit) {
		}

		@Override
		public void coinsUnloaded(CoinStorageUnit unit) {
		}
	}
	
	private static class BanknoteReceiverListenerStub implements BanknoteStorageUnitListener, BanknoteSlotListener {
		
		public int count = 0;

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceListener> device) {
		}

		@Override
		public void banknotesFull(BanknoteStorageUnit unit) {
		}

		@Override
		public void banknoteAdded(BanknoteStorageUnit unit) {
			count++;
		}

		@Override
		public void banknotesLoaded(BanknoteStorageUnit unit) {
		}

		@Override
		public void banknotesUnloaded(BanknoteStorageUnit unit) {
		}

		@Override
		public void banknoteInserted(BanknoteSlot slot) {
		}

		@Override
		public void banknoteEjected(BanknoteSlot slot) {
			count++;
		}

		@Override
		public void banknoteRemoved(BanknoteSlot slot) {
		}
		
	}

}
