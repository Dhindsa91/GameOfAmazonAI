package ygraphs.ai.smart_fox.games;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmazonGameSearch implements Runnable
{
	private int depth;
	private int[] bestMove = null;
	private int[][] moveOrder = new int[20][6];
	
	private Deque<int[]> moveOrdering;
	private Deque<int[]> bestOrdering;
	
	private GameBoard state;
	private boolean player;
	
	public AmazonGameSearch(GameBoard state, boolean whitePlayer){
		this.state = new GameBoard(state);
		this.player = whitePlayer;
	}
	

	@Override
	public void run() {
		
		try {
			absearch(state, player);
		} 
		catch (InterruptedException e) {}
		
	}
	
	public int[] getBestMove(){

		ExecutorService e = Executors.newSingleThreadExecutor();
		
		e.execute(this);
		try{
		Thread.sleep(5000);
		}
		catch(InterruptedException ex){System.out.println("Something happened");}
		e.shutdownNow();
		System.out.printf("action: (%d, %d) to (%d, %d), fire at (%d, %d)\n\n",bestMove[4], bestMove[5], bestMove[0], bestMove[1], bestMove[2], bestMove[3]);
		return bestMove;
	}
	
	/*	Iterative-deepening alpha-beta search
	 * 	
	 */
	public int[] absearch(GameBoard state, boolean player) throws InterruptedException {
    	//function ALPHA-BETA-SEARCH(state) returns an action

	     int v;
	     
	     depth = 1;
	     bestMove = new int[6];
	     bestOrdering = new ArrayDeque<>();
	     
	    do{
	    	System.out.println("searching depth " + depth);
	    	int d = 0;
		    moveOrdering = new ArrayDeque<>();
	  		
	    	//v = alphabeta(state, d, player);
	    	v = negamax(state, d, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
    		depth++;
	  		bestOrdering = new ArrayDeque<>(moveOrdering);
		    
		  	for(int[] a: bestOrdering){
		  		for(int i: a){
		  			System.out.print(i);
		  		}
				System.out.println();
		  	}     
	    }
	    while (depth < 20);
		    	 
		//return the action in ACTIONS(state) with value v
	    
	    return bestMove;
	}
	
	/* Negamax game tree search with alpha-beta pruning
	 * 
	 */
	private int negamax(GameBoard node, int d, int a, int b, boolean player) throws InterruptedException{
		if(Thread.interrupted()){
			throw new InterruptedException();
		}
		
		if( d >= depth || node.isTerminal(player)){
			return node.eval(player);
		}

		List<int[]> childNodes = node.getActions(player);
		childNodes = OrderMoves(childNodes);
		int bestValue = Integer.MIN_VALUE;
		Deque<int[]> bestMoves = new ArrayDeque<>();
		for(int[] child : childNodes){
			node.update(child);
			int v = -negamax(node, d+1, -b, -a, !player);
			node.undo(child);
			//bestValue = Math.max( bestValue, v );
			if(v > bestValue){
				bestValue = v;
				bestMoves = new ArrayDeque<>(moveOrdering);
				bestMoves.addFirst(child);
				
				if(d == 0){
					bestMove = child;
				}
			}
			a = Math.max( a, v );
			if (a >= b)
				break;
		}
		moveOrdering = bestMoves;
		return bestValue;
	}
	private List<int[]> OrderMoves(List<int[]> childNodes){
		int[] best = bestOrdering.poll();
		if(best != null){
			childNodes.add(0, best);
		}
		return childNodes;
	}
}