package cs540.checkers.gaopeng;





/* Don't forget to change this line to cs540.checkers.<username> */


import cs540.checkers.*;
import static cs540.checkers.CheckersConsts.*;

import java.util.*;

/*
 * This is a skeleton for an alpha beta checkers player. Please copy this file
 * into your own directory, i.e. src/<username>/, and change the package 
 * declaration at the top to read
 *     package cs540.checkers.<username>;
 * , where <username> is your cs department login.
 */
/** This is a skeleton for an alpha beta checkers player. */
public class GaopengPlayer extends CheckersPlayer implements GradedCheckersPlayer
{
    /** The number of pruned subtrees for the most recent deepening iteration. */
    public int pruneCount;
    //public Evaluator sbe;
    public BetterEvaluator sbe;		//Feature 1: a better evaluator considering weight of material, and position
    public int lastPrunedNodeScore;
    public int moveCount=0;

    public GaopengPlayer(String name, int side)
    { 
        super(name, side);
        // Use SimpleEvaluator to score terminal nodes
        //sbe = new SimpleEvaluator();
        sbe=new BetterEvaluator();
    }

    public void calculateMove(int[] bs)
    {
        /* Remember to stop expanding after reaching depthLimit */
        /* Also, remember to count the number of pruned subtrees. */

        // Place your code here
    	BoardState boardState=new BoardState(bs,side);
    	List<Move> successorMoves = Utils.getAllPossibleMoves(bs, side);	
    	        /* If this player has no moves, return out */
        if (successorMoves.size() == 0)
            return;

        int rootAlpha = Integer.MIN_VALUE;
        Move nextMove = null;
        Random dice=new Random();
        
        for (int depthCounter=1;depthCounter<this.depthLimit;depthCounter+=2)
        {
        	
        	pruneCount=0;
        	rootAlpha = Integer.MIN_VALUE;
        	nextMove = null;
        	
        /* Find best board state among those reachable from one move */
	        for (Move move : successorMoves)
	        {
				/* Execute the move so we can score the board state resulting from 
				 * the move */
				boardState.execute(move);

				int rootScore=0;
				
				rootScore= minValue( boardState,Integer.MIN_VALUE, Integer.MAX_VALUE, depthCounter-1);

				/* Update bestMove if score > bestScore */
				if (rootScore > rootAlpha)
				{
					nextMove = move;
					rootAlpha = rootScore;
					
				}
				else if((moveCount>50)&&(rootScore>=(rootAlpha*0.8))&&(dice.nextDouble()>0.8)) //feature 2: when deep into a game, introduce a probability to take slightly worse moves to avoid repeated moves that generates no result
				{
					nextMove = move;
					rootAlpha = rootScore;
				}

				/* Revert the move so we can score additional board states. */
				boardState.revert();

	        }
        

		if(Utils.verbose == true){
			System.out.println("Move is: " + nextMove + "\tFinal Minmax Value is: " + rootAlpha+"\nPrune Count is: " + this.pruneCount + "\t last pruned Node minmax is: " + lastPrunedNodeScore);
			
		}
        /* Set the best move as the chosen move */
        setMove(nextMove);
        moveCount++;
        }
        

    }

    public int getPruneCount()
    {
        return pruneCount;
    }
    
    public int maxValue(BoardState bs, int alpha, int beta, int depth)
    {
    	   	 	
    	List<Move> successorMoves = bs.getAllPossibleMoves();
    	//check remaining depth balance
   	 	if(depth==0||successorMoves.size()==0)
    	{
   	 		return (side!=BLK?sbe.evaluate1(bs.D):((-1)*sbe.evaluate1(bs.D)));
    	}
    	    	

    	
		for (Move move:successorMoves)
		{
			bs.execute(move); //gw: temporarily move forward
			alpha=Math.max(alpha, minValue(bs,alpha,beta,depth-1));
			bs.revert(); //gw: restore the state before the move
			
			if (alpha>=beta) //gw:beta cutoff
			{
				pruneCount++;
				lastPrunedNodeScore=beta;
				return beta;


			}
		}
	
    	return alpha;
    	
    }
    
    public int minValue(BoardState bs, int alpha, int beta, int depth) 
    {
    	    	
    	
       	
   	 	
    	List<Move> successorMoves = bs.getAllPossibleMoves();
    	//check remaining depth balance
   	 	if(depth==0||successorMoves.size()==0)
    	{
   	 		return (side!=BLK?sbe.evaluate1(bs.D):((-1)*sbe.evaluate1(bs.D)));
    	}
    	    	
    	    	
    	
		for (Move move:successorMoves)
		{
			bs.execute(move);
			beta=Math.min(beta, maxValue(bs,alpha,beta,depth-1));
			bs.revert();
			
			if (alpha>=beta) //gw:alpha cutoff 
			{
				pruneCount++;
				lastPrunedNodeScore=alpha;				
				return alpha;
			}
		}
	
    	return beta;	

    }

	@Override
	public int getLastPrunedNodeScore() {
		// TODO Auto-generated method stub
		return lastPrunedNodeScore;
	}
    
    
    
}
