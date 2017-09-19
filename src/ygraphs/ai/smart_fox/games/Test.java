package ygraphs.ai.smart_fox.games;

import java.util.ArrayList;
import java.util.Arrays;

public class Test {
	public static void main(String[] args) {
		
		Integer[] a = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 0, 0, 2, 3, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0};
		GameBoard game = new GameBoard(new ArrayList<Integer>(Arrays.asList(a)));
		System.out.println(game.eval(true));
		
		/*
		a = new Integer[] {0, 0, 0, 0, 0,  0, 0, 2, 2, 0,  0, 2, 0, 0, 2,  0, 1, 0, 0, 1,  0, 0, 1, 1, 0};
		game = new GameBoard(new ArrayList<Integer>(Arrays.asList(a)));
		*/
		
		game = new GameBoard();
		int[] v;
		boolean white_player = true;
		for(int i = 0; i < 20; i++){
			AmazonGameSearch search = new AmazonGameSearch(game, white_player);
			v = search.getBestMove();
			game.update(v);
			game.printBoard();
			white_player = !white_player;
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			for(Thread t : threads){
				System.out.println(t);
			}
		}
		
		/*
		for(int i = 0; i < 50; i++){
			ArrayList<int[]> moves = game.getActions(true);
			int[] m = moves.get((int) (moves.size()*Math.random()));
			game = new GameBoard(game, m);
			//game.update(m);
			game.printBoard();
		}
		*/
	}
}
