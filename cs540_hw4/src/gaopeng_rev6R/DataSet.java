package gaopeng_rev6R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class organizes the information of a data set into simple structures.
 *
 * Do not modify.
 * 
 */
public class DataSet {

	public List<Instance> instances = null; // ordered list of instances

	public String [] labels = null;
	public String [] attr_name = null;		// name of attribute
	public String [][] attr_val = null;     // candidate values of each attribute
	
	/**
	 * Add instance to collection.
	 */
	public void addInstance(String line) {
		if (instances == null) {
			instances = new ArrayList<Instance>();
		}
		Instance instance = new Instance();
		
		String[] splitline = line.split(",");
		for(int i = 1; i < splitline.length; i ++)
			instance.addAttribute(splitline[i]);
		instance.setLabel(splitline[0]);
		
		instances.add(instance);
	}
	
	public void addAttribute(String line, int idx) {
		String[] splitline = line.split(" ");
		attr_name[idx] = splitline[0]; //gw: read this
		attr_val[idx] = splitline[1].split(",").clone();
	}
}
