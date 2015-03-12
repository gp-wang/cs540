package gaopeng;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of a digit representation. Each instance consists of a list of
 * feature values, and the corresponding digit label.
 * 
 * DO NOT MODIFY.
 *
 */
public class Instance {
	
	/**
	 * The list of feature values. Because the values in each line of the dataset are normalized,
	 * we use Double to store them.
	 */
	public List<Double> features;
	
	/**
	 * The digit label of the instance.
	 */
	public String label;
	
	
	/**
	 * Constructor.
	 */
	public Instance() {
		features = new ArrayList<Double> ();
	}
	
	/**
	 * Add a feature value to the list.
	 * @param featureValue
	 */
	public void addFeatureValue(Double featureValue) {
		features.add(featureValue);
	}
	
	/**
	 * Return the feature value list
	 * @return
	 */
	public List<Double> getFeatureValue() {
		return features;
	}
	
	/**
	 * Set the digit label of the instance
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Get the digit label of the instance
	 * @return
	 */
	public String getLabel() {
		return label;
	}
}