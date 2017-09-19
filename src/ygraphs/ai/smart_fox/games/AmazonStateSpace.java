package ygraphs.ai.smart_fox.games;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

public class AmazonStateSpace implements Runnable {
	
	private static int depth;
	private static int[] bestMove = null;
	private static int[][] moveOrder = new int[20][6];
	
	private static Deque<int[]> moveOrdering;
	private static Deque<int[]> bestOrdering;
	
	private GameBoard state;
	private boolean player;
	private ExecutorService e = Executors.newSingleThreadExecutor();
	
	public AmazonStateSpace(GameBoard state, boolean whitePlayer){
		this.state = new GameBoard(state);
		this.player = whitePlayer;
	}
	

	@Override
	public void run() {
		
		absearch(state, player);
		
	}
	
	public int[] getBestMove(){

		e.execute(this);
		try{
		Thread.sleep(5000);
		}
		catch(InterruptedException ex){System.out.println("Something happened");}
		e.shutdown();
		
		return bestMove;
	}
	
	/*	Iterative-deepening alpha-beta search
	 * 	
	 */
	public static int[] absearch(GameBoard state, boolean player) {
    	//function ALPHA-BETA-SEARCH(state) returns an action
		
		long time0,time1;
		long timeLimit = 30000;
	    time0 = System.currentTimeMillis();
	     
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
		  		
	        time1=System.currentTimeMillis();
	       // System.out.printf("action: (%d, %d) to (%d, %d), fire at (%d, %d)\n value: %d\n\n",bestMove[4], bestMove[5], bestMove[0], bestMove[1], bestMove[2], bestMove[3], v);
	        
	    }
	    while (time1-time0<timeLimit && depth < 10);
		    	 
		//return the action in ACTIONS(state) with value v
	    System.out.printf("action: (%d, %d) to (%d, %d), fire at (%d, %d)\n value: %d\n\n",bestMove[4], bestMove[5], bestMove[0], bestMove[1], bestMove[2], bestMove[3], v);
		//return v;
	    
	    return bestMove;
	}
	
	/* 
	 * Alpha-Beta pruning algorithm (+ killer move ordering)
	 * Takes in a game board state and a search depth limit.
	 * Returns an int array with the following values:
	 * 0:	utility of best move
	 * 1-6: coordinates of best move (qrow, qcol, arow, acol, qfr, qfc)
	 */
	private static int alphabeta(GameBoard state, int d, boolean player){
		return alphabeta(state, d, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
	}
	private static int alphabeta(GameBoard state, int d, int alpha, int beta, boolean maxPlayer, boolean player){
		if(d >= depth){
			int v = state.eval(player);
			return v;
		}
		List<int[]> actions = state.getActions(player);
		if(actions.size() <= 0){
			int v = state.eval(player);
			return v;
		}
		int v;
		if(maxPlayer){
			v = Integer.MIN_VALUE;
			
			for(int[] a: actions){
				state.update(a);
				//v = Math.max(v, alphabeta(state, d+ 1, alpha, beta, false, player));
				
				int u = alphabeta(state, d+ 1, alpha, beta, false, player);
				if(u > v){
					v = u;
					if(d == 0)
						bestMove = a;
				}
				state.undo(a);
				
				alpha = Math.max(alpha, v);
				if(beta <= alpha){
					break;
				}
			}

			return v;
		}
		else
			v = Integer.MAX_VALUE;
			
			for(int[] a: actions){
				state.update(a);
				//v = Math.min(v, alphabeta(state, d+ 1, alpha, beta, true, player));	
				
				int u = alphabeta(state, d+ 1, alpha, beta, true, player);
				if(u < v){
					v = u;
					if(d == 0)
						bestMove = a;
				}
				state.undo(a);
				
				beta = Math.min(beta, v);
				if(beta <= alpha){
					break;
				}
			}
			return v;
	}
	
	/* Negamax game tree search with alpha-beta pruning
	 * 
	 */
	private static int negamax(GameBoard node, int d, int a, int b, boolean player){
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
	private static List<int[]> OrderMoves(List<int[]> childNodes){
		int[] best = bestOrdering.poll();
		if(best != null){
			childNodes.add(0, best);
		}
		return childNodes;
	}
	
	/* Alpha-Beta implementation from the textbook. Basically the same as the one above, but a bit uglier.
	 * Ignore for now, I will probably delete this as soon as I'm sure the algorithm above is correct.
	 */
	private static int[] maxValue(GameBoard state, int depth, int alpha, int beta, boolean player){
		//function MAX-VALUE(state,alpha, beta) returns a utility value
				//if CUTOFF-TEST(state, depth) then return EVAL(state)
		if(depth <= 0){
			int[] v = {state.eval(player)};
			return v;
		}
		List<int[]> actions = state.getActions(player);
		if(actions.size() <= 0){
			int[] v = {state.eval(player)};
			return v;
		}
		int v[] = {Integer.MIN_VALUE, 0, 0, 0, 0, 0, 0};
		for(int[] a: actions){
			
			v[0] = Math.max(v[0], minValue(new GameBoard(state, a), depth-1, alpha, beta, !player)[0]);
			v[1] = a[0];
			v[2] = a[1];
			v[3] = a[2];
			v[4] = a[3];
			v[5] = a[4];
			v[6] = a[5];
			
			//if(v[0] >= beta)
			//	return v;
			alpha = Math.max(alpha, v[0]);
			if(beta <= alpha)
				return v;
		}
		return v;
	}
	private static int[] minValue(GameBoard state, int depth, int alpha, int beta, boolean opponent){
		//function MIN-VALUE(state,alpha, beta) returns a utility value
				//if CUTOFF-TEST(state, depth) then return EVAL(state)
		if(depth <= 0){
			int[] v = {state.eval(opponent)};
			return v;
		}
		List<int[]> actions = state.getActions(opponent);
		if(actions.size() <= 0){
			int[] v = {state.eval(opponent)};
			return v;
		}
		int v[] = {Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0};
		for(int[] a: actions){
			
			v[0] = Math.min(v[0], maxValue(new GameBoard(state, a), depth-1, alpha, beta, !opponent)[0]);
			v[1] = a[0];
			v[2] = a[1];
			v[3] = a[2];
			v[4] = a[3];
			v[5] = a[4];
			v[6] = a[5];
			
			//if(v[0] <= alpha)
			//	return v;
			beta = Math.min(beta, v[0]);
			if(beta <= alpha)
				return v;
		}
		return v;
	}

}

