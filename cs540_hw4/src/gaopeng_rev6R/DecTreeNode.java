
package gaopeng_rev6R;
import java.util.ArrayList;
import java.util.List;


/**
 * Possible class for internal organization of a decision tree.
 * Included to show standardized output method, print().
 * 
 * You can add extra attributes in this class if you want.
 * 
 */
//for a root node, parentAttribute is null
//for a terminal node, attribute is null
public class DecTreeNode {
	String label;
	String attribute;
	String parentAttributeValue; // if is the root, set to "ROOT"
	boolean terminal;
	List<DecTreeNode> children;
	boolean explored; //gw: for dfs use
	int level; //gw: for node printing indent
	String labelIfLeaf;
	
	

	DecTreeNode(String _label, String _attribute, String _parentAttributeValue, boolean _terminal) {
		label = _label;
		attribute = _attribute;
		parentAttributeValue = _parentAttributeValue;
		terminal = _terminal;
		explored=false;
		level=0;
		if (_terminal) {
			children = null;
		} else {
			children = new ArrayList<DecTreeNode>();
		}
	}

	/**
	 * Add child to the node.
	 * 
	 * For printing to be consistent, children should be added
	 * in order of the attribute values as specified in the
	 * dataset.
	 */
	public void addChild(DecTreeNode child) {
		if (children != null) {
			children.add(child);
		}
	}
	
	/**
	 * Prints the subtree of the node
	 * with each line prefixed by k * 4 blank spaces.
	 */
	public void print(int k) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("    ");
		}
		sb.append(parentAttributeValue);
		//sb.append(": "+attribute);
		if (terminal) {
			sb.append(" (" + label + ")");
			System.out.println(sb.toString());
		} else {
			sb.append(" {" + attribute + "?}");
			System.out.println(sb.toString());
			for(DecTreeNode child: children) {
				child.print(k+1);
			}
		}
	}
	
	//get deepest level
	public static void fillHeightIfRoot(DecisionTree tr, DecTreeNode root) {
		root.traverse(0,tr);
	}	
	
	public void traverse(int k, DecisionTree tr) {
		tr.nodeCount++;
		if (k>tr.height) tr.height=k;
		if (terminal) {
			;
		} else {
			for(DecTreeNode child: children) {
				child.traverse(k+1, tr);
			}
		}
	}
	
	public static void deleteAllIfRoot(DecisionTree tr, DecTreeNode root) {
		DecTreeNode.traverseDeletion(root);
	}
	
	public static void traverseDeletion(DecTreeNode nd) {
		if(!nd.terminal)
			for(DecTreeNode child: nd.children) {
				DecTreeNode.traverseDeletion(child);
			}
		nd=null;
	}
	
	
	public DecTreeNode copy(DecTreeNode nd) {
		
		DecTreeNode newNode=null;
		//use reference to determine equivalence
		if(this!=nd){
			newNode= new DecTreeNode(this.label,this.attribute,this.parentAttributeValue,this.terminal);
			newNode.explored=this.explored;
			newNode.level=this.level;
			newNode.labelIfLeaf=this.labelIfLeaf;
		
			if(!newNode.terminal) {
				for(DecTreeNode child: this.children) {
					newNode.children.add(child.copy(nd));
				}
			}
		}
		//if this==nd, need to set its label using Plurality value
		else if(this==nd) { 
			newNode= new DecTreeNode(this.labelIfLeaf,null,this.parentAttributeValue,true);
			newNode.explored=this.explored;
			newNode.level=this.level;
			newNode.labelIfLeaf=this.labelIfLeaf;
		}
		
		return newNode;
	}
	

	public List<DecTreeNode> getInternalNodes() {
		List<DecTreeNode> internalNodes=new ArrayList<DecTreeNode>();
		this.traverseInternal(internalNodes);
		return internalNodes;
	}
	
	public void traverseInternal(List<DecTreeNode> queue) {
		if(!this.terminal){
			queue.add(this);	
			for(DecTreeNode child: children) {
				if(!this.terminal) {
					child.traverseInternal(queue);
				}
			}
		}
	}
	
	

}
