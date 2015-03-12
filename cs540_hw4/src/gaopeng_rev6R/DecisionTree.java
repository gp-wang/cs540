
package gaopeng_rev6R;
import java.util.*;
/**
 * This class provides a framework for accessing a decision tree.
 * Put your code in constructor, printInfoGain(), buildTree and buildPrunedTree()
 * You can add your own help functions or variables in this class 
 */
public class DecisionTree {
	/**
	 * training data set, pruning data set and testing data set
	 */
	private DataSet train = null;		// Training Data Set
	private DataSet tune = null;		// Tuning Data Set
	private DataSet test = null;		// Testing Data Set
	
	private DecTreeNode root=null;
	private double accuracy=0.0; //IMPORTANT: this accuracy is calc using TUNE set
	private static final double SIGMA=1E-8;
	public int height;
	public int nodeCount;

	/**
	 * Constructor
	 * 
	 * @param train  
	 * @param tune
	 * @param test
	 */
	DecisionTree(DataSet train, DataSet tune, DataSet test) {
		this.train = train;
		this.tune = tune; //gw: for mode!=2, the tune set passed into this method is null.
		this.test = test;
		this.height=0;
		this.nodeCount=0;
	}
	
	/**
	 * print information gain of each possible question at root node.
	 * 
	 */
	public void printInfoGain()
	{
		List<Instance> instances=this.train.instances;
			
		boolean [] attr_remain=new boolean [train.attr_name.length];
		for(int i=0;i<attr_remain.length;i++)
			attr_remain[i]=true;
		
		//calc original entropy
		double entropy_original = 0.0;
		int count_0 = 0;
		int [] count_1=new int[train.labels.length];
		double [] prob_original = new double[train.labels.length];
		for(int k=0;k<train.labels.length;k++) {
			count_0=0;
			for (Instance in: instances) {
						count_0++;
						if (in.label.equals(train.labels[k]))
							count_1[k]++;
			}
			if (count_0==0)
				prob_original[k]=0.0;
			else
				prob_original[k]=(1.0*count_1[k])/count_0;
			//TODO: verify below calculation in theory
			if((prob_original[k]-0.0)<SIGMA||(1.0-prob_original[k])<SIGMA)
				entropy_original+= 0;
			else
				entropy_original+= (-1.0)*prob_original[k]*DecisionTree.log2(prob_original[k]);			
		}
		//System.out.println("entropy_original = "+entropy_original);
		
		
		// loop over each attr_name, i		
		//assume initialized to 0 by default
		int []total_0=new int[train.attr_name.length]; 
		int [][]total_1=new int[train.attr_name.length][];
		int [][][]total_2=new int[train.attr_name.length][][];
		
		double [] entropy_X=new double [train.attr_name.length];
		for(int i=0;i<train.attr_name.length;i++) {
			if (!attr_remain[i]) continue;
			
			double[] prob_Xv=new double[train.attr_val[i].length]; //array length is # of avail values of that attr
			
			total_1[i]=new int[train.attr_val[i].length];
			total_2[i]=new int[train.attr_val[i].length][];
			
			//loop over each attr value,j
			double [] entropy_Xv =new double[train.attr_val[i].length];
			for(int j=0;j<train.attr_val[i].length;j++) {

				total_2[i][j]=new int[train.labels.length];
				//loop over each classification,k
				double [] prob_Yk_Xv=new double[train.labels.length];
				for(int k=0;k<train.labels.length;k++) {
					
					total_0[i]=0;//gw:necessary to re-init for each label value
					total_1[i][j]=0;//gw:necessary to re-init for each label value
					//loop over each instance
					for (Instance in: instances) {
						total_0[i]++;//total number of instances 
						if (in.attributes.get(i).equals(train.attr_val[i][j])) 
							total_1[i][j]++;//total number of instances of a specific: attr_value // the increment is not streamed into k
						if (in.label.equals(train.labels[k])&&in.attributes.get(i).equals(train.attr_val[i][j])) 
							total_2[i][j][k]++;//total number of instances of a specific: attr_value, label // the increment is streamed into different [k] elements
						
					}
					
					if (total_1[i][j]==0)
						prob_Yk_Xv[k]=0.0; //just an default value, anyway the contribution to H(Y|X) will be zero because P(X=j) is zero
					else
						prob_Yk_Xv[k]=(1.0*total_2[i][j][k])/total_1[i][j];
					//TODO: verify below calculation in theory
					if((prob_Yk_Xv[k]-0.0)<SIGMA||(1.0-prob_Yk_Xv[k])<SIGMA)
						entropy_Xv[j]+= 0;
					else
						entropy_Xv[j]+= (-1.0)*prob_Yk_Xv[k]*DecisionTree.log2(prob_Yk_Xv[k]);
				}
				
				//infoGain[j] is ready now;
				prob_Xv[j]=(1.0*total_1[i][j])/total_0[i];
				entropy_X[i]+= prob_Xv[j]*entropy_Xv[j];
			}
		}
		
		//infoGain_X[] contains the infoGains of each attr_name
		double temp=0.0;
		String str=null;
		for(int i=0;i<train.attr_name.length;i++) {
				temp=entropy_original-entropy_X[i];
				str=train.attr_name[i];
				//System.out.println(str + ": info gain = "+temp);			
				System.out.printf("\n%s: info gain = %.3f",str,temp);			
		}
		
	}
	
	
	//given a set of instances, and an attr_name to find PluralityValue upon. return the majority vote of attr_value of that attr_name
	public String PluralityValue(List<Instance> instances) {
		
		int [] count=new int[this.train.labels.length];
		
		for(int i=0;i<this.train.labels.length;i++) {
			for (Instance in : instances) {
				if (in.label.equals(train.labels[i])) 
					count[i]++;
			}	
		}
		
		String majorLabel=this.train.labels[0];
		int maxCount=0;
		for(int i=0;i<this.train.labels.length;i++) {
			if (count[i]>maxCount) {	
				maxCount=count[i];
				majorLabel=this.train.labels[i];
			}
		}
		
		return majorLabel;
	}
			
	// a tree is nothing but a node referencing the root of tree
	//attributes are the attr_names 
	public DecTreeNode DecisionTreeLearning(List<Instance> instances,  boolean [] attr_remain, List<Instance> parentInstances, String parentAttrValue) {
		DecTreeNode node=null;
		//check whether same label for instances
		int cnt=0;
		String label_0=null;
		boolean sameLabel=false;
		if (instances.size()>0){
			for (Iterator<Instance> iterator = instances.iterator(); iterator.hasNext();) {
				Instance instance = (Instance) iterator.next();
				if (cnt==0) {
					label_0=instance.label;
					sameLabel=true;
				}
				else {
					if(instance.label!=label_0) {
						sameLabel=false;
						break;
					}
				}
				cnt++;
			}
		}
		
		String label=null;
		//put codes here
		//term node, self attribute is null
		if (instances.size()==0) {
			label=this.PluralityValue(parentInstances);
			node=new DecTreeNode(label,null,parentAttrValue, true);
			node.labelIfLeaf=label;
		}
		else if (sameLabel) {
			label=label_0;
			node=new DecTreeNode(label,null,parentAttrValue, true);
			node.labelIfLeaf=label_0;
		}
		else if(this.getRemainAttrCount(attr_remain)==0) {
			label=this.PluralityValue(instances);
			node=new DecTreeNode(label,null,parentAttrValue, true);
			node.labelIfLeaf=label;			
		}
		else {
			String A=this.argmaxImportance(instances, attr_remain);
			//for non-terminal node, assign node's label to null
			node=new DecTreeNode(null,A,parentAttrValue, false);
			//node's label if this node is to be pruned (delete subtree of this node, use major vote)
			node.labelIfLeaf=this.PluralityValue(instances);
			int index=this.getIndexOfAttrName(A);
			//for each value of A
			for(int i=0;i<this.train.attr_val[index].length;i++) {
				String attrValue=train.attr_val[index][i];
				List<Instance> exs=this.extract(instances, A, attrValue);
				

				//attributes-A
				boolean [] attr_remain_new=new boolean[attr_remain.length];
				for(int j=0;j<attr_remain.length;j++) {
					attr_remain_new[j]=attr_remain[j];
					if (j==this.getIndexOfAttrName(A)) attr_remain_new[j]=false;
				}
				
				//build subtree
				String valueA=this.train.attr_val[this.getIndexOfAttrName(A)][i];
				DecTreeNode subtree=this.DecisionTreeLearning(exs, attr_remain_new, instances, valueA);
				//add subtree as child of node 
				node.children.add(subtree);
			}
			
		}
		
		return node;
	}
	
	/**
	 * Build a decision tree given only a training set.
	 * 
	 */
	public void buildTree() {
		
		//gw: implement figure 18.5 algorithm here
		//gw: there is a helpful data structure in DectreeNode.java
		
		String rootAttribute=new String("ROOT");

		ArrayList<String> attributes=new ArrayList<String>();		
		
		for (String string : train.attr_name) {
			attributes.add(string);
		}
		
		boolean [] attr_remain=new boolean [train.attr_name.length];
		for(int i=0;i<attr_remain.length;i++)
			attr_remain[i]=true;
				
		root=DecisionTreeLearning(train.instances, attr_remain, null,rootAttribute); //double confirm about parentExample set value
		//get height of tree
		DecTreeNode.fillHeightIfRoot(this, this.root);
		
	}
	

	/**
	 * Build a decision tree given a training set then prune it using a tuning set.
	 * 
	 */
	public void buildPrunedTree() {


		//gw: implement the prune meta-algorithm in hw4 description here (very detail)
		this.buildTree();
		DecisionTree prunedTree=null;
		prunedTree=this.DecisionTreeTuning();
		
		//update this's internal content to the prunedTree's
		this.root=prunedTree.root;
		this.accuracy=prunedTree.accuracy;
	
	}
	
	
	public DecisionTree DecisionTreeTuning() {

		//done: add gc helper before running mushroom eg
		//init candidate tree, and its accuracy
		DecisionTree candTree=this;
		//calculate the original tree's accuracy on tune set
		candTree.accuracy=DecisionTree.calcTestAccuracy(candTree.tune, candTree.tuneClassify());
		
		//set the flag 
		boolean proceed;

		// tune using TUNE set
		do {
			List<DecTreeNode> internalNodes=candTree.root.getInternalNodes(); //root is included
			DecisionTree prunedTree=null;
			DecisionTree tempTree=null;
			
			//init
			proceed=false;



			for (DecTreeNode nd: internalNodes) {

				prunedTree=candTree.copyExceptNode(nd);
				prunedTree.accuracy=DecisionTree.calcTestAccuracy(prunedTree.tune, prunedTree.tuneClassify());
				DecTreeNode.fillHeightIfRoot(prunedTree, prunedTree.root);//populate info about height and nodecount

				//get the most accurate tree out of the candidates, using Tune set
				if (tempTree==null) {
					tempTree=prunedTree;
				}
				else if(prunedTree.accuracy>tempTree.accuracy) {
					tempTree=prunedTree;
				}
				else if(Math.abs(prunedTree.accuracy-tempTree.accuracy)<SIGMA) {
					if(prunedTree.height<tempTree.height) {
						tempTree=prunedTree;
					}
				}
				else {
					;
				}
			}//end for loop

			//update candTree
			if(tempTree.accuracy>candTree.accuracy){
				candTree=tempTree;
				proceed=true;
			}
			else if(Math.abs(tempTree.accuracy-candTree.accuracy)<SIGMA){
					if (tempTree.height<=candTree.height) {
						candTree=tempTree;
						proceed=true;										
					}
			}
			
	
		} while(proceed);

		return candTree; 
	}

public static double  calcTestAccuracy(DataSet test, String[] results) {
		
		if(results == null) {
			 System.out.println("Error in calculating accuracy: " +
			 		"You must implement the classify method");
			 System.exit(-1);
		}
		
		List<Instance> testInsList = test.instances;
		if(testInsList.size() == 0) {
			System.out.println("Error: Size of test set is 0");
			System.exit(-1);
		}
		if(testInsList.size() > results.length) {
			System.out.println("Error: The number of predictions is inconsistant " +
					"with the number of instances in test set, please check it");
			System.exit(-1);
		}
		
		int correct = 0, total = testInsList.size();
		for(int i = 0; i < testInsList.size(); i ++)
			if(testInsList.get(i).label.equals(results[i]))
				correct ++;
		
		return correct * 1.0 / total;
	}
	
	public DecisionTree copyExceptNode(DecTreeNode nd) {
		DecisionTree treeNew= new DecisionTree(train, tune, test);
		DecTreeNode newRoot=this.root.copy(nd); //new Root is now a root node connects to a group of new nodes forming a tree except the subtree of node nd

		treeNew.root=newRoot;

		return treeNew;
		
	}
	
	
	//only for tuning purpose
	public String[] tuneClassify() {
		
		// gw: output a column of results as output of test dataset, (similar as output format of hw3 programming problem)
		// gw: the accuracy is calculated in HW4.java so no need to worry here
		String [] results=new String[tune.instances.size()];
		int i=0;
		for (Instance in : this.tune.instances) {
			DecTreeNode node=root;
					
			//while(!node.children.isEmpty()) {
			while(!node.terminal) {

				String attrName=node.attribute;
				int attrNameIndex=this.getIndexOfAttrName(attrName);
				int attrValueIndex=this.getIndexOfAttrValue(attrName, in.attributes.get(attrNameIndex));
				
				node=node.children.get(attrValueIndex);
			}
			
			//System.out.println(node.label);
			results[i]=node.label;
			i++;
		}
		
		return results;
	}

	
  /**
   * Evaluates the learned decision tree on a test set.
   * @return the label predictions for each test instance 
   * 	according to the order in data set list
   */
	public String[] classify() {
		
		// gw: output a column of results as output of test dataset, (similar as output format of hw3 programming problem)
		// gw: the accuracy is calculated in HW4.java so no need to worry here
		String [] results=new String[test.instances.size()];
		int i=0;
		for (Instance in : this.test.instances) {
			DecTreeNode node=root;
					
			//while(!node.children.isEmpty()) {
			while(!node.terminal) {

				String attrName=node.attribute;
				int attrNameIndex=this.getIndexOfAttrName(attrName);
				int attrValueIndex=this.getIndexOfAttrValue(attrName, in.attributes.get(attrNameIndex));
				
				node=node.children.get(attrValueIndex);
			}
			
			//System.out.println(node.label);
			results[i]=node.label;
			i++;
		}
		
		return results;
	}

	/**
	 * Prints the tree in specified format. It is recommended, but not
	 * necessary, that you use the print method of DecTreeNode.
	 * 
	 * Example:
	 * Root {odor?}
     *     a (e)
     *     m (e)
   	 *	   n {habitat?}
     *         g (e)
     *  	   l (e)
     *	   p (p)
   	 *	   s (e)
	 *         
	 */
	public void print() {
		// gw: there is a helper print funtion which works on a given node
		//		here I might need only to implement dfs() traversal sequence. (recall that dfs is just a stack ordered node visiting sequence)

			DecTreeNode node=root;
			node.print(0);
			//this.dfsPrint(node);
			//return;
	}
	

	//helper functions
	//caluclate base of log 2
	public static double log2(double a) {
		return Math.log10(a)/Math.log10(2.0);
	}

	//find argmax using this.train and arguments passed in 
	public String argmaxImportance(List<Instance> instances, boolean [] attr_remain) {
		
		

		
		// loop over each attr_name, i		
		//assume initialized to 0 by default
		int []total_0=new int[train.attr_name.length]; 
		int [][]total_1=new int[train.attr_name.length][];
		int [][][]total_2=new int[train.attr_name.length][][];
		
		double [] entropy_X=new double [train.attr_name.length];
		for(int i=0;i<train.attr_name.length;i++) {
			if (!attr_remain[i]) continue;
			
			double[] prob_Xv=new double[train.attr_val[i].length]; //array length is # of avail values of that attr
			
			total_1[i]=new int[train.attr_val[i].length];
			total_2[i]=new int[train.attr_val[i].length][];
			
			//loop over each attr value,j
			double [] entropy_Xv =new double[train.attr_val[i].length];
			for(int j=0;j<train.attr_val[i].length;j++) {

				total_2[i][j]=new int[train.labels.length];
				//loop over each classification,k
				double [] prob_Yk_Xv=new double[train.labels.length];
				for(int k=0;k<train.labels.length;k++) {
					
					total_0[i]=0;//gw:necessary to re-init for each label value
					total_1[i][j]=0;//gw:necessary to re-init for each label value
					//loop over each instance
					for (Instance in: instances) {
						total_0[i]++;//total number of instances 
						if (in.attributes.get(i).equals(train.attr_val[i][j])) 
							total_1[i][j]++;//total number of instances of a specific: attr_value // the increment is not streamed into k
						if (in.label.equals(train.labels[k])&&in.attributes.get(i).equals(train.attr_val[i][j])) 
							total_2[i][j][k]++;//total number of instances of a specific: attr_value, label // the increment is streamed into different [k] elements
						
					}
					
					if (total_1[i][j]==0)
						prob_Yk_Xv[k]=0.0; //just an default value, anyway the contribution to H(Y|X) will be zero because P(X=j) is zero
					else
						prob_Yk_Xv[k]=(1.0*total_2[i][j][k])/total_1[i][j];
					//TODO: verify below calculation in theory
					if((prob_Yk_Xv[k]-0.0)<SIGMA||(1.0-prob_Yk_Xv[k])<SIGMA)
						entropy_Xv[j]+= 0;
					else
						entropy_Xv[j]+= (-1.0)*prob_Yk_Xv[k]*DecisionTree.log2(prob_Yk_Xv[k]);
				}
				
				//entropy_Xv[j] is ready now;
				prob_Xv[j]=(1.0*total_1[i][j])/total_0[i];
				entropy_X[i]+= prob_Xv[j]*entropy_Xv[j];
			}
		}
		
		//entropy_X[] contains the infoGains of each attr_name
		double temp=1.0;
		String str=null;
		for(int i=0;i<train.attr_name.length;i++) {
			if(entropy_X[i]<temp&&attr_remain[i]) {
				temp=entropy_X[i];
				str=train.attr_name[i];
			}
		}
		
		return str;
	}
	public int getRemainAttrCount(boolean[] attr_remain) {
		int count=0;
		for (int i = 0; i < train.attr_name.length; i++)
			if (attr_remain[i])
				count++;
		return count;
	}

	public int getIndexOfAttrValue(String attrName, String attrValue) {

		for(int i=0;i<train.attr_name.length;i++) {
			if(!attrName.equals(train.attr_name[i])) continue;
			for (int j = 0; j < train.attr_val[i].length; j++) {
				if(attrValue.equals(train.attr_val[i][j])) return j;
			}
		}
		System.out.println("error: invalid attr_value" + attrValue);//TODO: make this an exception
		return -1;
	}
	
	public int getIndexOfAttrName(String attrName) {
		for(int i=0;i<train.attr_name.length;i++) {
			if(!attrName.equals(train.attr_name[i])) continue;
			return i;
		}
		System.out.println("error: invalid attr_name" + attrName);//TODO: make this an exception
		return -1;
		
	}
	
	public List<Instance> extract(List<Instance> instances, String attrName, String attrValue) {
		List<Instance> extracted=new ArrayList<Instance>();
		int attrNameIndex=this.getIndexOfAttrName(attrName);
				
		for (Instance instance : instances) {
			if(instance.attributes.get(attrNameIndex).equals(attrValue))
					extracted.add(instance);
		}
				
		return extracted;
	}
	public static void main(String[] args) {
		System.out.println(DecisionTree.log2(4.0));
	}
}
