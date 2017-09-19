package ygraphs.ai.smart_fox.games;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ygraphs.ai.smart_fox.GameMessage;

public class AImazon extends GamePlayer {

	private GameClient gameClient;
	private JFrame guiFrame = null;
    private GameBoardUI boardUI = null; 
	private GameBoard board = null;
//	private AmazonStateSpace stateSpace = null;
	private boolean gameStarted = false;
	public String usrName = null;
	private boolean isWhite = false;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param passwd
	 */
	public AImazon(String name, String passwd) {

		this.usrName = name;
		setupGUI();

		connectToServer(name, passwd);
	}

	@Override
	public void onLogin() {
		// once logged in, the gameClient will have the names of available game
		// rooms
		ArrayList<String> rooms = gameClient.getRoomList();
		this.gameClient.joinRoom(rooms.get(5));

	}

	private void connectToServer(String name, String passwd) {
		// create a client and use "this" class (a GamePlayer) as the delegate.
		// the client will take care of the communication with the server.
		gameClient = new GameClient(name, passwd, this);
	}

	@Override
	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
		
		if (messageType.equals(GameMessage.GAME_ACTION_START)) {
			board = new GameBoard((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
//			stateSpace = new AmazonStateSpace(board);
			if (((String) msgDetails.get("player-black")).equals(this.userName())) {
				System.out.println("Game State: " + msgDetails.get("player-white"));
			}
			else{
				isWhite = true;
				//playerMove(9, 4, 9, 5, 1, 4);
				AmazonGameSearch search = new AmazonGameSearch(board, isWhite);
				int[] move = search.getBestMove();
				playerMove(move[0],move[1], move[2], move[3], move[4], move[5]);
			}
		} else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
			handleOpponentMove(msgDetails);
		}
		return true;
	}

	// handle the event that the opponent makes a move.
	private void handleOpponentMove(Map<String, Object> msgDetails) {
		System.out.println("OpponentMove(): " + msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
		ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT);
		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
		System.out.println("QCurr: " + qcurr);
		System.out.println("QNew: " + qnew);
		System.out.println("Arrow: " + arrow);

		//board.markPosition(qnew.get(0), qnew.get(1), arrow.get(0), arrow.get(1), qcurr.get(0), qcurr.get(1), true);
		board.update(qnew.get(0), qnew.get(1), arrow.get(0), arrow.get(1), qcurr.get(0), qcurr.get(1));
		board.printBoard();
		AmazonGameSearch search = new AmazonGameSearch(board, isWhite);
		int[] move = search.getBestMove();
		playerMove(move[0], move[1], move[2], move[3], move[4], move[5]);
		board.printBoard();
	}

	public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc) {

		int[] qf = new int[2];
		qf[0] = qfr;
		qf[1] = qfc;

		int[] qn = new int[2];
		qn[0] = x;
		qn[1] = y;

		int[] ar = new int[2];
		ar[0] = arow;
		ar[1] = acol;

		// To send a move message, call this method with the required data
		this.gameClient.sendMoveMessage(qf, qn, ar);

		board.update(x, y, arow, acol, qfr, qfc);

	}

	public boolean handleMessage(String msg) {
		System.out.println("Time Out ------ " + msg);
		return true;
	}

	@Override
	public String userName() {
		return usrName;
	}
    
	
	//set up the game board
	private void setupGUI(){
	    guiFrame = new JFrame();
		   
		guiFrame.setSize(800, 600);
		guiFrame.setTitle("Game of the Amazons (COSC 322, UBCO)");	
		
		guiFrame.setLocation(200, 200);
		guiFrame.setVisible(true);
	    guiFrame.repaint();		
		guiFrame.setLayout(null);
		
		Container contentPane = guiFrame.getContentPane();
		contentPane.setLayout(new  BorderLayout());
		 
		contentPane.add(Box.createVerticalGlue()); 
		
		boardUI = createGameBoard();		
		contentPane.add(boardUI,  BorderLayout.CENTER);
	}
    
	private GameBoardUI createGameBoard(){
		return new GameBoardUI(this);
	}	
	/**
	 * The game board
	 * 
	 * @author yongg
	 *
	 */
	public class GameBoardUI extends JPanel{
		
		private static final long serialVersionUID = 1L;
		private  int rows = 10;
		private  int cols = 10; 
		
		int width = 500;
		int height = 500;
		int cellDim = width / 10; 
		int offset = width / 20;
		
		int posX = -1;
		int posY = -1;
	
		int r = 0;
		int c = 0;
		  
		
		AImazon game = null; 
	    private BoardGameModel gameModel = null;
		
		boolean playerAMove;
		
		public GameBoardUI(AImazon game){
	        this.game = game;	       
	        gameModel = new BoardGameModel(this.rows + 1, this.cols + 1);
	      	        
	        init(true);	
		}
		
		
		public void init(boolean isPlayerA){
	        String tagB = null;
	        String tagW = null;
	        
	        tagB = BoardGameModel.POS_MARKED_BLACK;
	        tagW = BoardGameModel.POS_MARKED_WHITE;
 
	        gameModel.gameBoard[1][4] = tagW;
	        gameModel.gameBoard[1][7] = tagW;
	        gameModel.gameBoard[4][1] = tagW;
	        gameModel.gameBoard[4][10] = tagW;
	        	
	        gameModel.gameBoard[7][1] = tagB;
	        gameModel.gameBoard[7][10] = tagB;
	        gameModel.gameBoard[10][4] = tagB;
	        gameModel.gameBoard[10][7] = tagB;		
		}
		
		
		/**
		 * repaint the part of the board
		 * @param qrow queen row index
		 * @param qcol queen col index 
		 * @param arow arrow row index
         * @param acol arrow col index
         * @param qfr queen original row
         * @param qfc queen original col
		 */
		public boolean markPosition(int qrow, int qcol, int arow, int acol, 
				  int qfr, int qfc, boolean  opponentMove){						
			
			System.out.println(qrow + ", " + qcol + ", " + arow + ", " + acol 
					+ ", " + qfr + ", " + qfc);
			
			boolean valid = gameModel.positionMarked(qrow, qcol, arow, acol, qfr, qfc, opponentMove);
			repaint();						
			return valid;
		}
		
		// JCmoponent method
		protected void paintComponent(Graphics gg){
			Graphics g = (Graphics2D) gg;
 			
			for(int i = 0; i < rows + 1; i++){
				g.drawLine(i * cellDim + offset, offset, i * cellDim + offset, rows * cellDim + offset);
				g.drawLine(offset, i*cellDim + offset, cols * cellDim + offset, i*cellDim + offset);					 
			}
			
			for(int r = 0; r < rows; r++){
			  for(int c = 0; c < cols; c++){
				
					posX = c * cellDim + offset;
					posY = r * cellDim + offset;
					
					posY = (9 - r) * cellDim + offset;
					
				if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(BoardGameModel.POS_AVAILABLE)){
					g.clearRect(posX + 1, posY + 1, 49, 49);					
				}
 
				if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(
						  BoardGameModel.POS_MARKED_BLACK)){
					g.fillOval(posX, posY, 50, 50);
				} 
				else if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(
					  BoardGameModel.POS_MARKED_ARROW)) {
					g.clearRect(posX + 1, posY + 1, 49, 49);
					g.drawLine(posX, posY, posX + 50, posY + 50);
					g.drawLine(posX, posY + 50, posX + 50, posY);
				}
				else if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(BoardGameModel.POS_MARKED_WHITE)){
					g.drawOval(posX, posY, 50, 50);
				}
			  }
			}
			
		}//method
		
		//JComponent method
		public Dimension getPreferredSize() {
		        return new Dimension(500,500);
		 }	
	
	}//end of GameBoard  
	public static void main(String[] args) {
		// AImazon game = new AImazon("daniel", "cosc322");
		AImazon game = new AImazon("anthony", "cosc322");
	}
}
