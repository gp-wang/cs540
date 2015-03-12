

/*
	Spam detection using a Naive Bayes classifier.

	The program is incomplete, it only reads in messages
	and creates the dictionary together
	with the word counts for each class (spam and ham).
 */

package gaopeng_rev1;
import gaopeng.NBSpamDetectionIO;

import java.io.*;
import java.util.*;
import java.lang.*;

class WordStat {
	int counterHam =0;
	int counterSpam =0;
	double p_w_given_ham_log = 0.0;
	double p_w_given_spam_log = 0.0;		
}


public class NBSpamDetect
{
	// This a class with two counters (for ham and for spam)
	static class Multiple_Counter
	{
		int counterHam = 0;
		int counterSpam    = 0;
	}


	public static void classifyWithAllWords(String[] args) 
			throws IOException
			{
		//gw: training set dir
		// Location of the directory (the path) taken from the cmd line (first arg)
		File dir_location      = new File( args[0] ); 

		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] dir_listing     = new File[0];

		// Check if the cmd line arg is a directory and list it
		if ( dir_location.isDirectory() )
		{
			dir_listing = dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Listings of the two sub-directories (ham/ and spam/)
		File[] listing_ham = new File[0];
		File[] listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean hamFound = false; boolean spamFound = false;
		for (int i=0; i<dir_listing.length; i++) {
			if (dir_listing[i].getName().equals("ham")) { listing_ham = dir_listing[i].listFiles(); hamFound  = true;}
			else if (dir_listing[i].getName().equals("spam")) { listing_spam = dir_listing[i].listFiles(); spamFound = true;}
		}
		if (!hamFound || !spamFound) {
			System.out.println( "- Error: specified directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Print out the number of messages in ham and in spam
		//System.out.println( "\t number of ham messages is: " + listing_ham.length );
		//System.out.println( "\t number of spam messages is: "    + listing_spam.length );

		//******************************
		// Create a hash table for the vocabulary (word searching is very fast in a hash table)
		Hashtable<String,Multiple_Counter> vocab = new Hashtable<String,Multiple_Counter>();
		Multiple_Counter old_cnt   = new Multiple_Counter();

		//		gw
		Hashtable<String,WordStat> vocab_stat = new Hashtable<String,WordStat>();

		int nWordsHam = 0;
		int nWordsSpam = 0;

		// Read the e-mail messages
		// The ham mail
		for ( int i = 0; i < listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( !word.equals("") ) { // if string isn't empty
						nWordsHam++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterHam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterHam++;
							//							is this necessary?
							vocab_stat.put(word, ws);
						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 1;
							fresh_cnt.counterSpam    = 0;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 1;
							ws.counterSpam = 0;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init							
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}
		// The spam mail
		for ( int i = 0; i < listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( ! word.equals("") ) {	
						nWordsSpam ++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterSpam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterSpam++;
							//							is this necessary?
							vocab_stat.put(word, ws);

						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 0;
							fresh_cnt.counterSpam    = 1;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 0;
							ws.counterSpam = 1;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}

		// Print out the hash table
		//		for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
		//		{	
		//			String word;
		//
		//			word = e.nextElement();
		//			old_cnt  = vocab.get(word);
		//
		//			System.out.println( word + " | in ham: " + old_cnt.counterHam + 
		//					" in spam: "    + old_cnt.counterSpam);
		//		}

		// Now all students must continue from here
		// Prior probabilities must be computed from the number of ham and spam messages
		// Conditional probabilities must be computed for every unique word
		// add-1 smoothing must be implemented
		// Probabilities must be stored as log probabilities (log likelihoods).
		// Bayes rule must be applied on new messages, followed by argmax classification (using log probabilities)
		// Errors must be computed on the test set and a confusion matrix must be generated

		// prior prob
		int nMessagesHam = listing_ham.length;

		int nMessagesSpam = listing_spam.length;

		int nMessagesTotal = nMessagesHam + nMessagesSpam;

		if(nMessagesHam == 0 || nMessagesSpam ==0)
			System.out.println("Zero ham or spam messages");

		double p_ham_log =  Math.log((nMessagesHam* 1.0)/(nMessagesTotal));
		double p_spam_log =  Math.log((nMessagesSpam* 1.0)/(nMessagesTotal));

		Iterator it = vocab_stat.entrySet().iterator();
		WordStat ws = null;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.counterHam ++;
			ws.counterSpam ++;
			nWordsHam ++;
			nWordsSpam ++;
		}

		//System.out.println("nWordsHam = " +nWordsHam);
		//System.out.println("nWordsSpam = " + nWordsSpam);

		//		gw:
		it = vocab_stat.entrySet().iterator();
		ws = null;  
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.p_w_given_ham_log = Math.log(ws.counterHam *1.0 / nWordsHam);
			ws.p_w_given_spam_log = Math.log(ws.counterSpam *1.0 / nWordsSpam);
			//vocab_stat.put(is,ws);
		}
		//		TODO: further confirm arg index
		//test sets
		File test_dir_location      = new File( args[1] ); 

		//		TODO: verify below works
		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] test_dir_listing     = new File[0];


		// Check if the cmd line arg is a directory and list it
		if ( test_dir_location.isDirectory() )
		{
			test_dir_listing = test_dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		TODO: verify File[0]
		// Listings of the two sub-directories (ham/ and spam/)
		File[] test_listing_ham = new File[0];
		File[] test_listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean test_hamFound = false; boolean test_spamFound = false;
		for (int i=0; i<test_dir_listing.length; i++) {
			if (test_dir_listing[i].getName().equals("ham")) { test_listing_ham = test_dir_listing[i].listFiles(); test_hamFound = true;}
			else if (test_dir_listing[i].getName().equals("spam")) { test_listing_spam = test_dir_listing[i].listFiles(); test_spamFound = true;}
		}
		if (!test_hamFound || !test_spamFound) {
			System.out.println( "- Error: specified test directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		// Print out the number of messages in ham and in spam
		//		System.out.println( "\t number of test ham messages is: " + test_listing_ham.length );
		//		System.out.println( "\t number of test spam messages is: "    + test_listing_spam.length );


		//		metrics
		int true_positives = 0; //spam classified as spam
		int true_negatives = 0; //ham classified as ham
		int false_positives = 0; //ham ... as spam
		int false_negatives = 0; //spam... as ham

		// Test starts
		// The ham mail
		for ( int i = 0; i < test_listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( !word.equals("") ) { // if string isn't empty
						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();

			if (p_msg_spam_log > p_msg_ham_log) false_positives ++;
			else true_negatives ++;
		}


		// The spam mail
		for ( int i = 0; i < test_listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:			

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( ! word.equals("") ) {	

						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();
			if (p_msg_spam_log > p_msg_ham_log) true_positives ++;
			else false_negatives ++;
		}

		System.out.println("Result 1:");
		System.out.println("All_Words\t\ttrue spam\ttrue ham");
		System.out.println("Classified spam:\t"+true_positives+"\t\t"+false_positives);
		System.out.println("Classified ham: \t"+false_negatives+"\t\t"+ true_negatives);
		System.out.println("");


			}

	public static void classifyWithLowerCaseWords(String[] args) 
			throws IOException
			{
		//gw: training set dir
		// Location of the directory (the path) taken from the cmd line (first arg)
		File dir_location      = new File( args[0] ); 

		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] dir_listing     = new File[0];

		// Check if the cmd line arg is a directory and list it
		if ( dir_location.isDirectory() )
		{
			dir_listing = dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Listings of the two sub-directories (ham/ and spam/)
		File[] listing_ham = new File[0];
		File[] listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean hamFound = false; boolean spamFound = false;
		for (int i=0; i<dir_listing.length; i++) {
			if (dir_listing[i].getName().equals("ham")) { listing_ham = dir_listing[i].listFiles(); hamFound  = true;}
			else if (dir_listing[i].getName().equals("spam")) { listing_spam = dir_listing[i].listFiles(); spamFound = true;}
		}
		if (!hamFound || !spamFound) {
			System.out.println( "- Error: specified directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Print out the number of messages in ham and in spam
		//System.out.println( "\t number of ham messages is: " + listing_ham.length );
		//System.out.println( "\t number of spam messages is: "    + listing_spam.length );

		//******************************
		// Create a hash table for the vocabulary (word searching is very fast in a hash table)
		Hashtable<String,Multiple_Counter> vocab = new Hashtable<String,Multiple_Counter>();
		Multiple_Counter old_cnt   = new Multiple_Counter();

		//		gw
		Hashtable<String,WordStat> vocab_stat = new Hashtable<String,WordStat>();

		int nWordsHam = 0;
		int nWordsSpam = 0;

		// Read the e-mail messages
		// The ham mail
		for ( int i = 0; i < listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");
					
					word = word.toLowerCase();

					if ( !word.equals("") ) { // if string isn't empty
						nWordsHam++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterHam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterHam++;
							//							is this necessary?
							vocab_stat.put(word, ws);
						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 1;
							fresh_cnt.counterSpam    = 0;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 1;
							ws.counterSpam = 0;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init							
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}
		// The spam mail
		for ( int i = 0; i < listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					word = word.toLowerCase();
					
					if ( ! word.equals("") ) {	
						nWordsSpam ++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterSpam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterSpam++;
							//							is this necessary?
							vocab_stat.put(word, ws);

						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 0;
							fresh_cnt.counterSpam    = 1;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 0;
							ws.counterSpam = 1;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}

		// Print out the hash table
		//		for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
		//		{	
		//			String word;
		//
		//			word = e.nextElement();
		//			old_cnt  = vocab.get(word);
		//
		//			System.out.println( word + " | in ham: " + old_cnt.counterHam + 
		//					" in spam: "    + old_cnt.counterSpam);
		//		}

		// Now all students must continue from here
		// Prior probabilities must be computed from the number of ham and spam messages
		// Conditional probabilities must be computed for every unique word
		// add-1 smoothing must be implemented
		// Probabilities must be stored as log probabilities (log likelihoods).
		// Bayes rule must be applied on new messages, followed by argmax classification (using log probabilities)
		// Errors must be computed on the test set and a confusion matrix must be generated

		// prior prob
		int nMessagesHam = listing_ham.length;

		int nMessagesSpam = listing_spam.length;

		int nMessagesTotal = nMessagesHam + nMessagesSpam;

		if(nMessagesHam == 0 || nMessagesSpam ==0)
			System.out.println("Zero ham or spam messages");

		double p_ham_log =  Math.log((nMessagesHam* 1.0)/(nMessagesTotal));
		double p_spam_log =  Math.log((nMessagesSpam* 1.0)/(nMessagesTotal));

		Iterator it = vocab_stat.entrySet().iterator();
		WordStat ws = null;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.counterHam ++;
			ws.counterSpam ++;
			nWordsHam ++;
			nWordsSpam ++;
		}

		//System.out.println("nWordsHam = " +nWordsHam);
		//System.out.println("nWordsSpam = " + nWordsSpam);

		//		gw:
		it = vocab_stat.entrySet().iterator();
		ws = null;  
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.p_w_given_ham_log = Math.log(ws.counterHam *1.0 / nWordsHam);
			ws.p_w_given_spam_log = Math.log(ws.counterSpam *1.0 / nWordsSpam);
			//vocab_stat.put(is,ws);
		}
		//		TODO: further confirm arg index
		//test sets
		File test_dir_location      = new File( args[1] ); 

		//		TODO: verify below works
		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] test_dir_listing     = new File[0];


		// Check if the cmd line arg is a directory and list it
		if ( test_dir_location.isDirectory() )
		{
			test_dir_listing = test_dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		TODO: verify File[0]
		// Listings of the two sub-directories (ham/ and spam/)
		File[] test_listing_ham = new File[0];
		File[] test_listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean test_hamFound = false; boolean test_spamFound = false;
		for (int i=0; i<test_dir_listing.length; i++) {
			if (test_dir_listing[i].getName().equals("ham")) { test_listing_ham = test_dir_listing[i].listFiles(); test_hamFound = true;}
			else if (test_dir_listing[i].getName().equals("spam")) { test_listing_spam = test_dir_listing[i].listFiles(); test_spamFound = true;}
		}
		if (!test_hamFound || !test_spamFound) {
			System.out.println( "- Error: specified test directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		// Print out the number of messages in ham and in spam
		//		System.out.println( "\t number of test ham messages is: " + test_listing_ham.length );
		//		System.out.println( "\t number of test spam messages is: "    + test_listing_spam.length );


		//		metrics
		int true_positives = 0; //spam classified as spam
		int true_negatives = 0; //ham classified as ham
		int false_positives = 0; //ham ... as spam
		int false_negatives = 0; //spam... as ham

		// Test starts
		// The ham mail
		for ( int i = 0; i < test_listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");
					word = word.toLowerCase();

					if ( !word.equals("") ) { // if string isn't empty
						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();

			if (p_msg_spam_log > p_msg_ham_log) false_positives ++;
			else true_negatives ++;
		}


		// The spam mail
		for ( int i = 0; i < test_listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:			

			while ((line = in.readLine()) != null)					// read a line
			{
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");
					word = word.toLowerCase();

					if ( ! word.equals("") ) {	

						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();
			if (p_msg_spam_log > p_msg_ham_log) true_positives ++;
			else false_negatives ++;
		}
		
		System.out.println("Result 2: ");
		System.out.println("Lower-case\t\ttrue spam\ttrue ham");
		System.out.println("Classified spam:\t"+true_positives+"\t\t"+false_positives);
		System.out.println("Classified ham: \t"+false_negatives+"\t\t"+ true_negatives);
		System.out.println("");

			}

public static void classifyWithHeaders(String[] args) 
			throws IOException
			{
		//gw: training set dir
		// Location of the directory (the path) taken from the cmd line (first arg)
		File dir_location      = new File( args[0] ); 

		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] dir_listing     = new File[0];

		// Check if the cmd line arg is a directory and list it
		if ( dir_location.isDirectory() )
		{
			dir_listing = dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Listings of the two sub-directories (ham/ and spam/)
		File[] listing_ham = new File[0];
		File[] listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean hamFound = false; boolean spamFound = false;
		for (int i=0; i<dir_listing.length; i++) {
			if (dir_listing[i].getName().equals("ham")) { listing_ham = dir_listing[i].listFiles(); hamFound  = true;}
			else if (dir_listing[i].getName().equals("spam")) { listing_spam = dir_listing[i].listFiles(); spamFound = true;}
		}
		if (!hamFound || !spamFound) {
			System.out.println( "- Error: specified directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		// Print out the number of messages in ham and in spam
		//System.out.println( "\t number of ham messages is: " + listing_ham.length );
		//System.out.println( "\t number of spam messages is: "    + listing_spam.length );

		//******************************
		// Create a hash table for the vocabulary (word searching is very fast in a hash table)
		Hashtable<String,Multiple_Counter> vocab = new Hashtable<String,Multiple_Counter>();
		Multiple_Counter old_cnt   = new Multiple_Counter();

		//		gw
		Hashtable<String,WordStat> vocab_stat = new Hashtable<String,WordStat>();

		int nWordsHam = 0;
		int nWordsSpam = 0;

		// Read the e-mail messages
		// The ham mail
		for ( int i = 0; i < listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				if(!(line.startsWith("From")||line.startsWith("To")||line.startsWith("Subject")||line.startsWith("Cc"))) continue;
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				
								
				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( !word.equals("") ) { // if string isn't empty
						nWordsHam++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterHam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterHam++;
							//							is this necessary?
							vocab_stat.put(word, ws);
						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 1;
							fresh_cnt.counterSpam    = 0;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 1;
							ws.counterSpam = 0;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init							
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}
		// The spam mail
		for ( int i = 0; i < listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			while ((line = in.readLine()) != null)					// read a line
			{
				if(!(line.startsWith("From")||line.startsWith("To")||line.startsWith("Subject")||line.startsWith("Cc"))) continue;				
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				
				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( ! word.equals("") ) {	
						nWordsSpam ++;
						if ( vocab.containsKey(word) )				// check if word exists already in the vocabulary
						{
							old_cnt = vocab.get(word);	// get the counter from the hashtable
							old_cnt.counterSpam ++;			// and increment it

							vocab.put(word, old_cnt);

							//gw
							WordStat ws = vocab_stat.get(word);
							ws.counterSpam++;
							//							is this necessary?
							vocab_stat.put(word, ws);

						}
						else
						{
							Multiple_Counter fresh_cnt = new Multiple_Counter();
							fresh_cnt.counterHam = 0;
							fresh_cnt.counterSpam    = 1;

							vocab.put(word, fresh_cnt);			// put the new word with its new counter into the hashtable
							//gw
							WordStat ws = new WordStat();
							ws.counterHam = 0;
							ws.counterSpam = 1;
							ws.p_w_given_ham_log = 0.0 ; //init
							ws.p_w_given_spam_log = 0.0; //init
							vocab_stat.put(word, ws);							

						}
					}
				}
			}

			in.close();
		}

		// Print out the hash table
		//		for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
		//		{	
		//			String word;
		//
		//			word = e.nextElement();
		//			old_cnt  = vocab.get(word);
		//
		//			System.out.println( word + " | in ham: " + old_cnt.counterHam + 
		//					" in spam: "    + old_cnt.counterSpam);
		//		}

		// Now all students must continue from here
		// Prior probabilities must be computed from the number of ham and spam messages
		// Conditional probabilities must be computed for every unique word
		// add-1 smoothing must be implemented
		// Probabilities must be stored as log probabilities (log likelihoods).
		// Bayes rule must be applied on new messages, followed by argmax classification (using log probabilities)
		// Errors must be computed on the test set and a confusion matrix must be generated

		// prior prob
		int nMessagesHam = listing_ham.length;

		int nMessagesSpam = listing_spam.length;

		int nMessagesTotal = nMessagesHam + nMessagesSpam;

		if(nMessagesHam == 0 || nMessagesSpam ==0)
			System.out.println("Zero ham or spam messages");

		double p_ham_log =  Math.log((nMessagesHam* 1.0)/(nMessagesTotal));
		double p_spam_log =  Math.log((nMessagesSpam* 1.0)/(nMessagesTotal));

		Iterator it = vocab_stat.entrySet().iterator();
		WordStat ws = null;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.counterHam ++;
			ws.counterSpam ++;
			nWordsHam ++;
			nWordsSpam ++;
		}

		//System.out.println("nWordsHam = " +nWordsHam);
		//System.out.println("nWordsSpam = " + nWordsSpam);

		//		gw:
		it = vocab_stat.entrySet().iterator();
		ws = null;  
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String is = (String) pairs.getKey();
			ws = (WordStat) pairs.getValue();

			ws.p_w_given_ham_log = Math.log(ws.counterHam *1.0 / nWordsHam);
			ws.p_w_given_spam_log = Math.log(ws.counterSpam *1.0 / nWordsSpam);
			//vocab_stat.put(is,ws);
		}
		//		TODO: further confirm arg index
		//test sets
		File test_dir_location      = new File( args[1] ); 

		//		TODO: verify below works
		// Listing of the directory (should contain 2 subdirectories: ham/ and spam/)
		File[] test_dir_listing     = new File[0];


		// Check if the cmd line arg is a directory and list it
		if ( test_dir_location.isDirectory() )
		{
			test_dir_listing = test_dir_location.listFiles();
		}
		else
		{
			System.out.println( "- Error: cmd line arg not a directory.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		TODO: verify File[0]
		// Listings of the two sub-directories (ham/ and spam/)
		File[] test_listing_ham = new File[0];
		File[] test_listing_spam    = new File[0];

		// Check that there are 2 sub-directories
		boolean test_hamFound = false; boolean test_spamFound = false;
		for (int i=0; i<test_dir_listing.length; i++) {
			if (test_dir_listing[i].getName().equals("ham")) { test_listing_ham = test_dir_listing[i].listFiles(); test_hamFound = true;}
			else if (test_dir_listing[i].getName().equals("spam")) { test_listing_spam = test_dir_listing[i].listFiles(); test_spamFound = true;}
		}
		if (!test_hamFound || !test_spamFound) {
			System.out.println( "- Error: specified test directory does not contain ham and spam subdirectories.\n" );
			Runtime.getRuntime().exit(0);
		}

		//		// Print out the number of messages in ham and in spam
		//		System.out.println( "\t number of test ham messages is: " + test_listing_ham.length );
		//		System.out.println( "\t number of test spam messages is: "    + test_listing_spam.length );


		//		metrics
		int true_positives = 0; //spam classified as spam
		int true_negatives = 0; //ham classified as ham
		int false_positives = 0; //ham ... as spam
		int false_negatives = 0; //spam... as ham

		// Test starts
		// The ham mail
		for ( int i = 0; i < test_listing_ham.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_ham[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:

			while ((line = in.readLine()) != null)					// read a line
			{
				if(!(line.startsWith("From")||line.startsWith("To")||line.startsWith("Subject")||line.startsWith("Cc"))) continue;
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( !word.equals("") ) { // if string isn't empty
						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();

			if (p_msg_spam_log > p_msg_ham_log) false_positives ++;
			else true_negatives ++;
		}


		// The spam mail
		for ( int i = 0; i < test_listing_spam.length; i ++ )
		{
			FileInputStream i_s = new FileInputStream( test_listing_spam[i] );
			BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
			String line;
			String word;

			//			gw:
			double p_msg_ham_log = 0.0;
			double p_msg_spam_log = 0.0;

			p_msg_ham_log += p_ham_log;
			p_msg_spam_log += p_spam_log;
			//			gw:			

			while ((line = in.readLine()) != null)					// read a line
			{
				if(!(line.startsWith("From")||line.startsWith("To")||line.startsWith("Subject")||line.startsWith("Cc"))) continue;
				StringTokenizer st = new StringTokenizer(line);			// parse it into words

				while (st.hasMoreTokens())
				{
					word = st.nextToken().replaceAll("[^a-zA-Z]","");

					if ( ! word.equals("") ) {	

						//							gw:
						if(vocab_stat.containsKey(word)){
							WordStat iws = vocab_stat.get(word); 
							double ip_w_given_ham_log = iws.p_w_given_ham_log;
							double ip_w_given_spam_log = iws.p_w_given_spam_log;

							p_msg_ham_log += ip_w_given_ham_log ;
							p_msg_spam_log += ip_w_given_spam_log ;
						}
						//							gw:						

					}
				}
			}

			in.close();
			if (p_msg_spam_log > p_msg_ham_log) true_positives ++;
			else false_negatives ++;
		}

		System.out.println("Result 3: ");
		System.out.println("Header_Only\t\ttrue spam\ttrue ham");
		System.out.println("Classified spam:\t"+true_positives+"\t\t"+false_positives);
		System.out.println("Classified ham: \t"+false_negatives+"\t\t"+ true_negatives);
		System.out.println("");


			}

	public static void main(String[] args) throws IOException {
		NBSpamDetectionIO.classifyWithAllWords(args);
		NBSpamDetectionIO.classifyWithLowerCaseWords(args);
		NBSpamDetectionIO.classifyWithHeaders(args);
	}
}

