package ygraphs.ai.smart_fox.games;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class GameBoard {

	/* Variables
	 * 
	 * board - A 2D integer array representing a game state. Row and column 0 are padded with zeros.
	 * 0 = an empty space, 1 = white piece, 2 = black piece, 3 = square blocked by arrow;
	 * row - number of rows + 1
	 * col - number of rows + 1
	 * size -  row * col
	 * white/black - 2D arrays storing positions of the players' pieces
	 */
	private int[][] board;
	private int row, col, size;
	private int[][] white = {{4, 1}, {1, 4}, {1, 7}, {4, 10}};
	private int[][] black = {{7, 1}, {10, 4}, { 10, 7}, {7, 10}};


	// Constructors
	
	/* Constructor to build game board at start of game.
	 * Takes in an ArrayList (from the game server) with the starting board positions.
	 * (For some reason the server represents the game state in a weird way;
	 * 	basically the rows are stored in reverse order, so this constructor reverses them back)
	 */
	public GameBoard(ArrayList<Integer> state) {
		int size = state.size();
		row = (int) Math.sqrt(size);
		col = size/row;
		board = new int[row][col];
		
		int j = 0;
		int k = 0;
		for (int i = col; i < size; i++) {
			int val = state.get(i);
			board[row - i/row][i%row] = val;
			
			if(state.get(i) == 1){
				white[j][0] = row - i/row;
				white[j++][1] = i%row;
			}
			else if(state.get(i) == 2){
				black[k][0] = row - i/row;
				black[k++][1] = i%row;
			}
		}
		printBoard();
		printArray(white);
		printArray(black);
	}
	//default constructor
	public GameBoard() {
		board = new int[11][11];
		row = 11;
		col = 11;

		board[4][1] = 1;
		board[1][4] = 1;
		board[1][7] = 1;
		board[4][10] = 1;
		board[7][1] = 2;
		board[10][4] = 2;
		board[10][7] = 2;
		board[7][10] = 2;

		printBoard();
	}
	public GameBoard(GameBoard state){
		this.board = arrayCopy(state.board);
		this.white = arrayCopy(state.white);
		this.black = arrayCopy(state.black);
		this.size = state.size;
		this.row = state.row;
		this.col = state.col;
	}
	//constructor to generate result of an action
	public GameBoard(GameBoard state, int[] a) {
		this(state);
		update(a);
	}
	
	/*
	public int getRow(){ return row;}
	public int getCol(){ return col;}
	public int getSize(){ return size;}
	public int[][] getWhite(){return white;}
	public int[][] getBlack(){return black;}
	*/
	
	/* Modifies this game state according to the specified action
	 * Can take an action as an array or as individual values. In either case, order is
	 * 0. New row; 1. New column; 2. Arrow row; 3. Arrow column; 4. Old row; 5. Old column;
	 */
	public boolean update(int[] action){
		if(action.length != 6) return false;
		else return update(action[0], action[1], action[2], action[3], action[4], action[5]);
	}
	public boolean update(int qrow, int qcol, int arow, int acol, int qfr, int qfc) {
		
		int player = board[qfr][qfc];
		board[qfr][qfc] = 0;
		board[qrow][qcol] = player;
		board[arow][acol] = 3;
		
		int[][] pieces;
		if(player == 1)
			pieces = white;
		else if (player == 2)
			pieces = black;
		else return false;

		for(int i = 0; i < 4; i++){
			if(qfr == pieces[i][0] && qfc == pieces[i][1]){
				pieces[i][0] = qrow;
				pieces[i][1] = qcol;
				break;
			}
		}
		return true;
	}
	
	/*Undoes an action - The reverse of update(a)
	 */
	public boolean undo(int[] action){
		if(action.length != 6) return false;
		else return undo(action[0], action[1], action[2], action[3], action[4], action[5]);
	}
	public boolean undo(int qrow, int qcol, int arow, int acol, int qfr, int qfc) {
		
		int player = board[qrow][qcol];
		board[arow][acol] = 0;
		board[qrow][qcol] = 0;
		board[qfr][qfc] = player;
		
		int[][] pieces;
		if(player == 1)
			pieces = white;
		else if (player == 2)
			pieces = black;
		else return false;
		
		for(int i = 0; i < 4; i++){
			if(qrow == pieces[i][0] && qcol == pieces[i][1]){
				pieces[i][0] = qfr;
				pieces[i][1] = qfc;
				break;
			}
		}
		return true;
	}
	
	public boolean isTerminal(boolean isWhiteMove){
		int[][] pieces;
		if(isWhiteMove)
			pieces = white;
		else
			pieces = black;
		
		for(int i = 0; i < 4; i++) {
			int r = pieces[i][0];
			int c = pieces[i][1];
			if(	
				r+1 < row && board[r+1][c] == 0 || r+1 < row && c+1 < col && board[r+1][c+1] == 0 || 
				r-1 > 0   && board[r-1][c] == 0 || r+1 < row && c-1 > 0   && board[r+1][c-1] == 0 || 
				c+1 < col && board[r][c+1] == 0 || r-1 > 0   && c+1 < col && board[r-1][c+1] == 0 || 
				c-1 > 0   && board[r][c-1] == 0 || r-1 > 0   && c-1 > 0   && board[r-1][c-1] == 0				
					){
				return false;
			}
		}
		return true;
	}
	
	/* Action factory - Generates every possible move available to a given player 
	 * - true for white, false for black
	 */
	public List<int[]> getActions(boolean isWhiteMove){
		List<int[]> actions = new LinkedList<>();
		
		int[][] pieces;
		if(isWhiteMove)
			pieces = white;
		else 
			pieces = black;
		
		for(int i = 0; i < 4; i++) {
			actions.addAll(moveQueen(pieces[i][0], pieces[i][1]));
			}
		return actions;
	}
	/* Generates the available moves for a queen at a specified location.*/
	private List<int[]> moveQueen(int fr, int fc) {
		
		List<int[]> moves = new LinkedList<>();
		
		int qr = fr;
		int qc = fc;
		while(++qr < row && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(++qr < row && ++qc < col && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(++qc < col && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(--qr >= 1 && ++qc < col && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(--qr >= 1 && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(--qr >= 1 && --qc >= 1 && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(--qc >= 1 && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		qr = fr;
		qc = fc;
		while(++qr < row && --qc >= 1 && board[qr][qc] == 0 ){
			moves.addAll(shootArrow(qr, qc, fr, fc));
		}
		
		return moves;
	}
	/* Generates the locations an arrow can be shot at from a specified position */
	private List<int[]> shootArrow(int qr, int qc, int fr, int fc) {

		List<int[]> actions = new LinkedList<>();
		//clears the position of the moved queen
		int temp = board[fr][fc];
		board[fr][fc] = 0;
		
		int ar = qr;
		int ac = qc;
		while(++ar < row && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(++ar < row && ++ac < col && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while( ++ac < col && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(--ar >= 1 && ++ac < col && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(--ar >= 1 && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(--ar >= 1 && --ac >= 1 && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(--ac >= 1 && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		ar = qr;
		ac = qc;
		while(++ar < row && --ac >= 1 && board[ar][ac] == 0 ){
			int[] a = {qr, qc, ar, ac, fr, fc};
			actions.add(a);
		}
		
		board[fr][fc] = temp;
		
		return actions;
	}
	
	/*heuristic evaluation function for the GameBoard - uses simple minimum-distance function
	 * 
	 */
	public int eval(boolean isWhite) {
		//printBoard();
		int[][] blk = minDistance(black);
		int[][] wht = minDistance(white);
		int count = 0;
		for(int i = 1; i < row; i ++)
			for(int j = 1; j < col; j++){
				int w = wht[i][j];
				int b = blk[i][j];
				if(w>0 && (b<=0 || w<b))
					count++;
				if(b>0 && (w<=0 || w>b))
					count--;
			}
		if(isWhite)
			return count;
		else
			return -count;
	}
	private int[][] minDistance(int[][] player){
		int[][] dist = new int[row][col];
		Deque<int[]> queue = new ArrayDeque<>();
		for(int i = 0; i < 4; i++){
			int[] root = {0, 0, player[i][0], player[i][1]};
			queue.add(root);
		}
		while(!queue.isEmpty()){
			int[] a = queue.remove();
			for(int[] n: shootArrow(a[2], a[3], 0, 0)){
				if(dist[n[2]][n[3]] == 0){
					dist[n[2]][n[3]] = dist[n[0]][n[1]] + 1;
					queue.add(n);
				}
			}
		}
		//printArray(dist);
		return dist;
	}
	
	//for testing/debugging
	private int[][] arrayCopy(int[][] old){
		int[][] copy = new int[old.length][old[0].length];
		for(int i=0; i< old.length; i++)
			  for(int j=0; j< old[0].length; j++)
			    copy[i][j] = old[i][j];
		return copy;
	}
	public String toString(){
		String temp = new String();
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				temp = temp + board[i][j] + " ";
			}
			temp = temp + "\n";
		}
		return temp;
	}
	public void printBoard(){
		for(int i = board.length -1; i > 0; i--){
			for(int j = 1; j < board[0].length; j++)
				System.out.print(board[i][j] + " ");
			System.out.println();
		}
		System.out.println();
	}
	public void printArray(int[][] a){
		for(int i = 0; i < a.length; i++){
			for(int j = 0; j < a[0].length; j++)
				System.out.print(a[i][j] + " ");
			System.out.println();
		}
		System.out.println();
	}
}