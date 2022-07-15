/**
 * This is where you build your AI for the Chess game.
 */
package games.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import joueur.BaseAI;

// <<-- Creer-Merge: imports -->> - Code you add between this comment and the end comment will be preserved between Creer re-runs.
// you can add additional import(s) here
// <<-- /Creer-Merge: imports -->>

/**
 * This is where you build your AI for the Chess game.
 */
public class AI extends BaseAI {
    /**
     * This is the Game object itself, it contains all the information about the
     * current game
     */
    public Game game;

    /**
     * This is your AI's player. This AI class is not a player, but it should
     * command this Player.
     */
    public Player player;

    public GameState gameState;

    public int hScoreBeforeMove, opponentScoreBeforeMove;

    Map<Character, Integer> points = new HashMap<>();

    /**
     * This returns your AI's name to the game server. Just replace the string.
     * 
     * @return string of you AI's name
     */
    public String getName() {
        // <<-- Creer-Merge: get-name -->> - Code you add between this comment and the
        // end comment will be preserved between Creer re-runs.
        return "depth 3 minimax"; // REPLACE THIS WITH YOUR TEAM NAME!
        // <<-- /Creer-Merge: get-name -->>
    }

    /**
     * This is automatically called when the game first starts, once the Game object
     * and all GameObjects have been initialized, but before any players do
     * anything.
     * This is a good place to initialize any variables you add to your AI, or start
     * tracking game objects.
     */
    public void start() {
        // <<-- Creer-Merge: start -->> - Code you add between this comment and the end
        // comment will be preserved between Creer re-runs.
        super.start();
        gameState = new GameState();
        gameState.initializeBoard(game.fen);
        // heuristic values for pieces
        points.put('K', 900);
        points.put('Q', 90);
        points.put('R', 50);
        points.put('B', 30);
        points.put('N', 30);
        points.put('P', 10);
        // <<-- /Creer-Merge: start -->>
    }

    /**
     * This is automatically called every time the game (or anything in it) updates.
     * If a function you call triggers an update this will be called before that
     * function returns.
     */
    public void gameUpdated() {
        // <<-- Creer-Merge: game-updated -->> - Code you add between this comment and
        // the end comment will be preserved between Creer re-runs.
        super.gameUpdated();
        gameState.initializeBoard(game.fen);
        // <<-- /Creer-Merge: game-updated -->>
    }

    /**
     * This is automatically called when the game ends.
     * You can do any cleanup of you AI here, or do custom logging. After this
     * function returns the application will close.
     * 
     * @param won    true if your player won, false otherwise
     * @param reason a string explaining why you won or lost
     */
    public void ended(boolean won, String reason) {
        // <<-- Creer-Merge: ended -->> - Code you add between this comment and the end
        // comment will be preserved between Creer re-runs.
        super.ended(won, reason);
        // <<-- /Creer-Merge: ended -->>
    }

    /**
     * This is called every time it is this AI.player's turn to make a move.
     *
     * @return A string in Universal Chess Interface (UCI) or Standard Algebraic
     *         Notation (SAN) formatting for the move you want to make. If the move
     *         is invalid or not properly formatted you will lose the game.
     */
    public String makeMove() {

        List<String> moves = gameState.getAllMoves(0);// Get a list of all possible moves for current turn
        int max = Integer.MIN_VALUE; // variable to stire max score
        String bestMove = "";
        // when multiple moves have same heuristic score we store all of them
        List<String> movesWithSameScore = new ArrayList<>();
        int depth = 4;
        for (String move : moves) {
            // pre compute heuristic scores before making the move
            hScoreBeforeMove = heuristic(gameState.board, gameState.isWhiteTurn);
            opponentScoreBeforeMove = heuristic(gameState.board, !gameState.isWhiteTurn);

            // apply move on board
            int[] arr = getMoveCordiantes(move); // it converts move in string format to array indices
            char src = gameState.board[arr[0]][arr[1]];
            char target = gameState.board[arr[2]][arr[3]];
            gameState.board[arr[2]][arr[3]] = src;// move piece to destination
            gameState.board[arr[0]][arr[1]] = ' ';// make source of piece moved an empty square

            int score = minimax(gameState.board, false, depth, gameState.isWhiteTurn, gameState, Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            if (score > max) {
                max = score;
                bestMove = move;
                movesWithSameScore = new ArrayList<>(); // found a new best move so empty the list
                movesWithSameScore.add(move);
            } else if (score == max) {
                movesWithSameScore.add(move); // store moves with same heuristic value
            }
            // change the board back to initial state
            gameState.board[arr[2]][arr[3]] = target;
            gameState.board[arr[0]][arr[1]] = src;

        }
        if (movesWithSameScore.size() > 1) {
            // if there are multiple bestmoves select one at random
            bestMove = movesWithSameScore.get(new Random().nextInt(movesWithSameScore.size()));
        }
        return bestMove;
        // <<-- /Creer-Merge: makeMove -->>
    }

    private int numberOfPieces(GameState gameState) {
        char[][] board = gameState.board;
        boolean isWhiteTurn = gameState.isWhiteTurn;
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char ch = board[i][j];
                if (Character.isLetter(ch) && (!(Character.isUpperCase(ch) ^ isWhiteTurn))) {
                    count++;
                }
            }
        }
        return count;
    }

    private int minimax(char[][] board, boolean maximizingTurn, int depth, boolean originalTurn, GameState gb,
            int alpha, int beta) {
        // return heuristic evaluation of board when depth is reached
        if (depth == 0) {
            int playerScore = heuristic(board, originalTurn);
            int opponentScore = heuristic(board, !originalTurn);
            int s = (opponentScoreBeforeMove - opponentScore) - (hScoreBeforeMove - playerScore);
            return s;
        }
        // get available moves based on whose turn it is
        List<String> currentMoves = gb.getAllMoves(maximizingTurn ? 0 : 1);

        if (currentMoves.size() == 0) {
            int score = maximizingTurn ? -900 : 900;
            score = score + (depth * (maximizingTurn ? -1 : 1));
            System.out.println("possible checkmate position");
            return score;
        }
        if (maximizingTurn) {
            int bestScore = Integer.MIN_VALUE;
            for (String move : currentMoves) {
                int[] arr = getMoveCordiantes(move);
                char src = board[arr[0]][arr[1]];
                char target = board[arr[2]][arr[3]];
                board[arr[2]][arr[3]] = src;
                board[arr[0]][arr[1]] = ' ';
                int score = minimax(board, false, depth - 1, originalTurn, gb, alpha, beta);
                bestScore = Math.max(score, bestScore); // select max value if it is maximizing player
                alpha = Math.max(alpha, score);
                board[arr[2]][arr[3]] = target;
                board[arr[0]][arr[1]] = src;
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (String move : currentMoves) {
                int[] arr = getMoveCordiantes(move);
                char src = board[arr[0]][arr[1]];
                char target = board[arr[2]][arr[3]];
                board[arr[2]][arr[3]] = src;
                board[arr[0]][arr[1]] = ' ';
                int score = minimax(board, true, depth - 1, originalTurn, gb, alpha, beta);
                bestScore = Math.min(score, bestScore); // select min value if it is minimizing player
                beta = Math.min(beta, score);
                board[arr[2]][arr[3]] = target;
                board[arr[0]][arr[1]] = src;
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }

    private int heuristic(char[][] board, boolean isWhiteTurn) {
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                char ch = board[i][j];
                // if the piece is of required color, add it's value to count
                if (Character.isLetter(ch) && (!(Character.isUpperCase(ch) ^ isWhiteTurn))) {
                    count += points.get(Character.toUpperCase(ch));
                }
            }
        }
        return count;
    }

    private int[] getMoveCordiantes(String move) {
        // convert move from string format to integer coordinates
        int sc = move.charAt(0) - 97;
        int sr = move.charAt(1) - 48 - 1;
        int tc = move.charAt(2) - 97;
        int tr = move.charAt(3) - 48 - 1;
        return new int[] { sr, sc, tr, tc };
    }
}
