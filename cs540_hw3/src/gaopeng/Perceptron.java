package gaopeng;
import java.util.*;
/**
 * You should implement your Perceptron in this class. 
 * Any methods, variables, or secondary classes could be added, but will 
 * only interact with the methods or variables in this framework.
 * 
 * You must add code for at least the 3 methods specified below. Because we
 * don't provide the weights of the Perceptron, you should create your own 
 * data structure to store the weights.
 * 
 */

//-gw, implementation:
//		weights: field, array; weight for bias
//		inputs:	field, array, bias
//		outputs: field, array
//		process(): method, sigmoid, activation function
//		link: 	embedded within process, loop through all inputs when calc'ing activation function output


public class Perceptron {

	/**
	 * The initial value for ALL weights in the Perceptron.
	 * We fix it to 0, and you CANNOT change it.
	 */
	public final double INIT_WEIGHT = 0.0;

	/**
	 * Learning rate value. You should use it in your implementation.
	 * You can set the value via command line parameter.
	 */
	public final double ALPHA;

	/**
	 * Training iterations. You should use it in your implementation.
	 * You can set the value via command line parameter.
	 */
	public final int EPOCH;

	// create weights variables, input units, and output units.
	// gw, 03302014:
	//use array for simplicity for now
	//TODO: change to data structure (class) for later
	private double[] inputs;
	private double [][] weights;
	private double [] outputs;

	private int featureNum;
	private int labelNum;


	/**
	 * Constructor. You should initialize the Perceptron weights in this
	 * method. Also, if necessary, you could do some operations on
	 * your own variables or objects.
	 * 
	 * @param alpha
	 * 		The value for initializing learning rate.
	 * 
	 * @param epoch
	 * 		The value for initializing training iterations.
	 * 
	 * @param featureNum
	 * 		This is the length of input feature vector. You might
	 * 		use this value to create the input units.
	 * 
	 * @param labelNum
	 * 		This is the size of label set. You might use this
	 * 		value to create the output units.
	 */
	public Perceptron(double alpha, int epoch, int featureNum, int labelNum) {
		this.ALPHA = alpha;
		this.EPOCH = epoch;

		this.featureNum=featureNum;
		this.labelNum=labelNum;

		//gw: init fields
		inputs=new double[featureNum+1]; //+1 for bias input;
		weights=new double[labelNum][featureNum+1];
		outputs=new double[labelNum];

		//	gw, 03302014:
		// init input, weights, output
		for (int i= 0; i < featureNum +1 ; i++) {
			if(i==0)	inputs[i]=1;
			else		inputs[i]=0;
			for (int j = 0; j < labelNum; j++) {
				weights[j][i]=INIT_WEIGHT;
				if(i==0) outputs[j]=0;
			}
		}
	}

	/**
	 * Train your Perceptron in this method.
	 * 
	 * @param trainingData
	 */
	//The network connection is implemented using formulae
	public void train(Dataset trainingData) {

		for(int e=EPOCH;e>0;e--){
			for (Iterator<Instance> iterator = trainingData.instanceList.iterator(); iterator.hasNext();) {
				Instance ins = (Instance) iterator.next();
				inputs[0]=1.0; //bias

				//init inputs
				for (int i = 1; i < featureNum+1; i++) 
					inputs[i]=ins.features.get(i-1);

				double [] T = new double[labelNum];
				double [] O = new double[labelNum];
				int label=Integer.parseInt(ins.getLabel());
				//iterate over all labels				
				for (int i = 0; i < labelNum; i++) {
					double sum=0.0;

					//get teacher value
					if (i==label) T[i]=1.0;
					else T[i]=0.0;

					sum+=inputs[0]*weights[i][0]; //bias
					for (int j = 1; j < featureNum + 1; j++) {
						sum += (inputs[j]*weights[i][j]);	
					}

					//actual output value
					O[i]=Perceptron.sigmoid(sum);
					double [] deltaWeight=new double[featureNum+1];

					//update all weight of one label
					for (int j = 0; j < featureNum + 1; j++) {
						deltaWeight[j]=ALPHA*(T[i]-O[i])*O[i]*(1-O[i])*inputs[j];
						weights[i][j]+=deltaWeight[j];
					}
				}
			}
		}


	}

	/**
	 * Test your Perceptron in this method. Refer to the homework documentation
	 * for implementation details and requirement of this method.
	 * 
	 * @param testData
	 */
	public void classify(Dataset testData) {

		int count=0;
		int correct=0;
		for (Iterator<Instance> iterator = testData.instanceList.iterator(); iterator.hasNext();count++) {
			Instance ins = (Instance) iterator.next();
			inputs[0]=1.0; //bias

			//init inputs
			for (int i = 1; i < featureNum+1; i++) 
				inputs[i]=ins.features.get(i-1);

			//actual value
			double [] O = new double[labelNum];

			int output=0;
			double prev=0.0;
			//iterate over all labels				
			for (int i = 0; i < labelNum; i++) {
				double sum=0.0;

				sum+=inputs[0]*weights[i][0]; //bias
				for (int j = 1; j < featureNum + 1; j++) {
					sum += (inputs[j]*weights[i][j]);	
				}

				//actual output value
				O[i]=Perceptron.sigmoid(sum);
				if(O[i]>prev){
					output=i;
					prev=O[i];
				}

			} 
			//label ends
			System.out.println(output);
			
			int label=Integer.parseInt(ins.getLabel());
			if (label==output) correct++;
			
		} 
		//iterator ends
		double accuracy=correct/(double) count;
		System.out.printf("%.4f",accuracy);


	}



	private static double sigmoid(double x){
		double y=0.0;
		y=1/(1+Math.exp(-x));
		return y;
	}

}