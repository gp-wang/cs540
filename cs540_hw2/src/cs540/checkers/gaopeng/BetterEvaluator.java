package cs540.checkers.gaopeng;
import static cs540.checkers.CheckersConsts.*;

import java.util.ArrayList;

import cs540.checkers.Evaluator;

public class BetterEvaluator {
	public int evaluate1(int[] bs)
	{	

		int redPawn = 0;
		int blackPawn = 0;
		int redKing = 0;
		int blackKing = 0;
		
		
		int remainingPieces = 0;
		
		
		int redPromotionChance=0;
		int blackPromotionChance=0;
		int promotionChanceDifference=0;
		int redKingCenter=0;
		int blackKingCenter=0;
		int kingCenterDifference=0;
		int proximity=0;
		int sumDistance=0;
		ArrayList<Integer> rk=new ArrayList<Integer>();
		ArrayList<Integer> bk=new ArrayList<Integer>();
		
		
		
		//count number of pieces
		for (int i = 0; i < H * W; i++)
		{
			int piece = bs[i];
			switch(piece)
			{
			case RED_PAWN:
				redPawn++;
				redPromotionChance+=(10/((i+1)/W));
		

				break;
			case BLK_PAWN:
				blackPawn++;
		
				blackPromotionChance+=(10/((64-i)/W));
				break;
			case RED_KING:
				redKing++;
				rk.add(piece);
				redKingCenter+=(16/Math.abs(i-27));

				break;
			case BLK_KING:
				blackKing++;
				bk.add(piece);
				blackKingCenter+=(16/Math.abs(i-27));
				break;
			}
			remainingPieces++;
		}
		


		

			promotionChanceDifference=redPromotionChance-blackPromotionChance;
			promotionChanceDifference*=0.7;
			
			kingCenterDifference=redKingCenter-blackKingCenter;
			
			

		int materialDifference=0;

		materialDifference=((redPawn + 2*redKing) - (blackPawn + 2*blackKing));
		materialDifference*=10;

		
		if(remainingPieces>=13)
			{
			return materialDifference+promotionChanceDifference+kingCenterDifference;
			}
		else //if((rp.size()>0)&&(bp.size()>0))
		{
			for (Integer i : rk) {
				for (Integer j : bk) {
					sumDistance+=Math.sqrt(Math.pow(((i+1)/W)-((j+1)/W),2)+Math.pow(((i+1)/H)-((j+1)/H), 2)); //Euclidian dist, from 0 to 8sqrt2
					proximity+=8/(sumDistance+1);
					
				}
			}
			return ((int)1.4*materialDifference)+((int)1.5*promotionChanceDifference)+((int)0.5*kingCenterDifference)+proximity;
		}
		
		
			
		
	}
}
