package gaopeng;
import java.util.ArrayList;
import java.util.List;

/**
 * This class organizes the information of an input file into a list structure.
 * 
 * DO NOT MODIFY.
 *
 */
public class Dataset {
	
	/**
	 * A list of instance.
	 */
	public final List<Instance> instanceList;
	
	public final String SEPARATOR = ",";
	
	/**
	 * The length of feature vector (the number of different features).
	 * This might be useful for creating input units or weights.
	 */
	public int featureNum;
	
	
	/**
	 * Constructor
	 */
	public Dataset() {
		instanceList = new ArrayList<Instance> ();
	}
	
	/**
	 * Given a line of raw data, we format it into an object of Instance class,
	 * and add it to the instance list.
	 * 
	 * @param line
	 */
	public void addInstance(String line) {
		String[] split = line.split(SEPARATOR);
		if(split.length <= 1) {
			System.err.println("Error: bad line format");
			System.exit(0);
		}
		
		Instance ins = new Instance();
		for(int i = 1; i < split.length; i ++)
			ins.addFeatureValue(Double.valueOf(split[i]));
		ins.setLabel(split[0]);
		instanceList.add(ins);
		
		int feaNum = split.length - 1;
		if(featureNum == 0) {
			featureNum = feaNum;
		}
		else {
			if(featureNum != feaNum) {
				System.err.println("Error: different feature number");
				System.exit(0);
			}
		}
	}
}