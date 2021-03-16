import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;

public class Main {
	public static void main(String[] args) {
		SoftwareMain software = new SoftwareMain();
		System.out.println("Starting application...\n");
		
		
		software.initialize();
		
		BarcodedItem cheeseSticks = new BarcodedItem(new Barcode("012345"), 500);
		BarcodedItem chickenNuggets = new BarcodedItem(new Barcode("012346"), 2000);
		
		software.ScanMain(cheeseSticks);
		software.ScanHeld(chickenNuggets);
		
		software.Pay(new Banknote(5, Currency.getInstance(Locale.CANADA)));
		software.Pay(new Coin(BigDecimal.valueOf(0.05), Currency.getInstance(Locale.CANADA)));
		
		software.Bag(cheeseSticks);
		software.Bag(chickenNuggets);
		
		System.out.println("\nEnding application...");
	}
}
