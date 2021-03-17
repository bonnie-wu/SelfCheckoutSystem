import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;

public class SoftwareMainTest {
	
	private SelfCheckoutStation station;

	@Before
	public void setup() {
		
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
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testScanAndPay() {
		ArrayList<BarcodedItem> scannedItem = new ArrayList<>(Arrays.asList(new BarcodedItem[] {
				newItem("01234", 1000),
				newItem("012345", 1500),
				newItem("0123456", 3000)
		}));
		
		ArrayList<BarcodedProduct> databaseProducts = new ArrayList<>(Arrays.asList(new BarcodedProduct[] {
				newProduct("01234", 4.50),
				newProduct("012345", 7.50),
				newProduct("0123456", 9.50),
				newProduct("01234567", 1.50),
				newProduct("012345678", 8.50)
		}));
		
		SoftwareMain main = new SoftwareMain(station, scannedItem);
		
		main.populateDatabase(databaseProducts);
		
		main.ScanMain(newItem("01234567", 1020));
		main.ScanMain(newItem("012345678", 1050));
		
		main.updateScannedProducts();
		
		assertSame(4.50 + 7.50 + 9.50 + 1.50 + 8.50, main.customerPayment.getTotal());
	}
	
	private BarcodedProduct newProduct(String barcode, double price) {
		return new BarcodedProduct(new Barcode(barcode), "Test Product", new BigDecimal(price));
	}
	
	private BarcodedItem newItem(String barcode, int weight) {
		return new BarcodedItem(new Barcode(barcode), weight);
	}

}
