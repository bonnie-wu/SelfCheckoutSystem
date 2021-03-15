
public class CustomerPayment {
//HEY

	private SelfCheckOutStation station;
	private float total;
	private float[] list;
	
	CustomerPayment(SelfCheckoutStation station, float[] list;){
		this.station = station;
		this.list = list;
	}

	// calculate the total of everything in the array
	public float total() {
		int length = this.list.length;
		for(int i; i < length; i++) {
			setTotal(getTotal() + list[i]);
		}	
	}
	
	// payment of the total, will include coins and banknote
	// boolean if there are banknotes
	// boolean if there are coins
	public void Pay(boolean banknote, boolean coin){
		
	}
	
	// getter and setter for the price total
	public float getTotal() {
		return total;
	}
	
	public void setTotal(float newTotal) {
		this.total = newTotal;
	}
}
