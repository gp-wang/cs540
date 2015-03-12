package gaopeng;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This is the main method that will load the perceptron.
 * 
 * DO NOT MODIFY.
 *
 */
public class HW3 {
	
	/**
	 * @param args
	 * 		Input parameter. Please refer to the homework documentation for the meaning.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 5 ) {
			System.err.println("Usage:\t<training-file-path>\n\t<test-file-path>\n\t"
					+ "<alpha-value>\n\t<epoch-value>\n\t<instance-number>");
			System.exit(0);
		}
		
		// load training dataset. N is the number of instances.
		int N = Integer.valueOf(args[4]);
		if(N <= 0) {
			System.out.println("The value of instance number should be a positive integer");
			System.exit(0);
		}
		Dataset trainingData = readData(args[0], N);
		
		// load test dataset
		Dataset testData = readData(args[1]);
		
		// value of learning rate
		double alpha = Double.valueOf(args[2]);
		if(alpha <= 0) {
			System.err.println("The value of ALPHA should be greater than 0");
			System.exit(0);
		}
				
		// value of epoch (iterations)
		int epoch = Integer.valueOf(args[3]);
		if(epoch <= 0) {
			System.err.println("The value of EPOCH should be greater than 0");
			System.exit(0);
		}
		
		// use the parameters to initialize perceptron
		Perceptron perceptron = new Perceptron(alpha, epoch, trainingData.featureNum, Label.DIGITS.length);
		
		// train the perceptron
		perceptron.train(trainingData);
		
		// use the trained model to classfy the instances in the testset
		perceptron.classify(testData);
	}
	
	
	/**
	 * Build the dataset given the file name. When parsing the line, the 
	 * first token is the DIGIT label of the instance, and the rest are 
	 * feature values.
	 * 
	 * @param fileName
	 * 		Name of the input file
	 * @return
	 * 		A Dataset oject.
	 * @throws IOException
	 */
	public static Dataset readData(String fileName) throws IOException {
		Dataset data = new Dataset();
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while((line = br.readLine()) != null) {
			data.addInstance(line);
		}
		br.close();
		fr.close();
		
		return data;
	}	
	
	/**
	 * Another version of building data. Will read the FIRST number of instances
	 * in the dataset.
	 * @param fileName
	 * @param instanceNum
	 * @return
	 * 		A datset obejct
	 * @throws IOException
	 */
	public static Dataset readData(String fileName, int instanceNum) throws IOException {
		Dataset data = new Dataset();
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		int lineCount = 0;
		while((line = br.readLine()) != null) {
			data.addInstance(line);
			
			lineCount ++;
			if(lineCount == instanceNum)
				break;
		}
		br.close();
		fr.close();
				
		return data;
	}
}