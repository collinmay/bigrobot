package whs.botdriver;

public interface Motor {
	public double getCurrent(); // amps
	public double getTemperature(); // degrees celsius
	public double getSpeed(); // -1 to 1
}
