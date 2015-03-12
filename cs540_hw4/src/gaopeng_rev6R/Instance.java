package gaopeng_rev6R;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds data for particular instance.
 */
public class Instance {
	
	public String label;
	public List<String> attributes = null;

	/**
	 * Add attribute values in the order of
	 * attributes as specified by the dataset
	 */
	public void addAttribute(String i) {
		if (attributes == null) {
			attributes = new ArrayList<String>();
		}
		attributes.add(i);//gw: this should be adding the reference
		//TODO: verify this
	}
	
	
	/**
	 * Add label value to the instance
	 */
	public void setLabel(String _label) {
		label = _label;
	}
}
