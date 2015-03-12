

package gaopeng_rev6R;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class HW4 {
	
	/**
	 * Runs the tests for HW4.
	 */
	public static void main(String[] args) {
		 if (args.length != 4 && args.length != 3) {
			 System.out.println("usage: java HW4 <modeFlag> <trainFilename> " +
				 		"<testFilename>");
			 System.exit(-1);
		 }
		 
		 /*
		  * mode 0 : print info gain for each possible question at root node
		  * 	 1 : create a decision tree using the training set, then print 
		  *      	   the tree and the prediction accuracy on the test set
		  *      2 : create a decision tree using the training set, prune using 
		  *        	   the tuning set, then print the tree and prediction accuracy 
		  *        	   on the test set [Extra Point]
		  */
		 int mode = Integer.parseInt(args[0]);
		 if (mode < 0 || mode > 2) {
			 System.out.println("Error: modeFlag must be an integer 0, 1 or 2");
			 System.exit(-1);
		 }
		 
		 // Turn text into array
		 // Only create the sets that we intend to use
		 DataSet trainSet = null, tuneSet = null, testSet = null; //gw: read this => done!

		 trainSet = createDataSet(args[1], mode);
		 testSet = createDataSet(args[2], mode);
		 if(mode == 2 && args.length == 4)
			 tuneSet = createDataSet(args[3], mode);
		 
		 // Create decision tree
		 DecisionTree tree = new DecisionTree(trainSet, tuneSet, testSet); //gw: read this
		 if (mode == 0)
		 {
			 // print
			 tree.printInfoGain();
		 } else if (mode == 1) {
			 tree.buildTree();
		 } else {
			 if(tuneSet == null) {
				 System.out.println("Empty tuning set");
				 System.exit(-1);
			 }
			 tree.buildPrunedTree();
		 }
		 
		 if (mode == 1 || mode == 2)
		 {
			 // print the tree and calculate accuracy
			 tree.print();
			 calcTestAccuracy(testSet, tree.classify());		 //gw: read this => done!
		 }
	}

	/**
	 * Converts from text file format to DataSet format.
	 * 
	 */
	private static DataSet createDataSet(String file, int modeFlag) {
		DataSet set = new DataSet();
		BufferedReader in;
		final String DELIMITER = ",";
		try {
			in = new BufferedReader(new FileReader(file));
			String line = in.readLine().substring(2); // %% class Label
			String[] splitline = line.split(DELIMITER);
			
			set.labels = splitline.clone(); //gw: available labels, e.g. label[0]=p,label[1]=e
			
			line = in.readLine(); // attribute number
			int num = Integer.parseInt(line);
			set.attr_name = new String [num];
			set.attr_val = new String [num][];
			int count = 0;
			
			while (in.ready()) {
				line = in.readLine(); 
				if (line.length() <2)
					continue;
				else
				{
					if (line.substring(0, 2).equals("##"))
					{
						line = line.substring(2);
						set.addAttribute(line, count);
						count++;
					}
					else 
						set.addInstance(line);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} 
		
		return set;
	}
	
	/**
	 * Calculate predication accuracy on the test set.
	 * Note that you should implement classify() method firstly.
	 * DO NOT MODIFY
	 */
	//gw: assert that the result output from classify() is a string sequence of predicted labels of the test set in order (one result per line) 
	private static void calcTestAccuracy(DataSet test, String[] results) {
		
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
		
		System.out.println("Prediction accuracy on the test set is: " 
				+ String.format("%.3f", correct * 1.0 / total));
		return;
	}
}
