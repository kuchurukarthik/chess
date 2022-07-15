/**
 * This is where you build your AI for the Chess game.
 */
package games.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

    public ChessObject chessObject;

    public long divFactor = 1000000000;
    Map<Character, Integer> points = new HashMap<>();

    int currentDepth = 0;
    private int maxDepthLimit = 5;

    /**
     * This returns your AI's name to the game server. Just replace the string.
     *
     * @return string of you AI's name
     */
    public String getName() {
        // <<-- Creer-Merge: get-name -->> - Code you add between this comment and the
        // end comment will be preserved between Creer re-runs.
        return "Sravan"; // REPLACE THIS WITH YOUR TEAM NAME!
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
        chessObject = new ChessObject();
        chessObject.initializeBoard(game.fen);
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
        chessObject.initializeBoard(game.fen);
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
        String bestMove = "";
        long t0 = (System.nanoTime() / divFactor);
        long secsRemaining = (long) (player.timeRemaining / divFactor);
        long timeFactor = (long) (t0 + (secsRemaining * 0.022));
        int depth = 2;
        do {
            currentDepth = depth;
            bestMove = searchMoves(depth);
            depth++;
            t0 = (System.nanoTime() / divFactor);
        } while (checkDepth(t0, timeFactor, depth));
        bestMove = searchMoves(depth);
        return bestMove;
        // <<-- /Creer-Merge: makeMove -->>
    }

    /**
     * @param depth
     * @return bestMove possible for current turn after searching through all
     *         possible moves
     */
    private String searchMoves(int depth) {
        // Get a list of all possible moves for current turn
        List<String> moves = chessObject.getAllMoves(0);
        // variable to stire max score
        int max = Integer.MIN_VALUE;
        String bestMove = "";
        // when multiple moves have same heuristic score we store all of them
        List<String> movesWithSameScore = new ArrayList<>();
        for (String move : moves) {
            // apply move on board
            int[] arr = getMoveCordiantes(move); // it converts move in string format to array indices
            char src = chessObject.board[arr[0]][arr[1]];
            char target = chessObject.board[arr[2]][arr[3]];
            chessObject.board[arr[2]][arr[3]] = src;// move piece to destination
            chessObject.board[arr[0]][arr[1]] = ' ';// make source of piece moved an empty square
            int score = minimax(chessObject, false, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score > max) {
                max = score;
                bestMove = move;
                movesWithSameScore = new ArrayList<>(); // found a new best move so empty the list
                movesWithSameScore.add(move);
            } else if (score == max) {
                movesWithSameScore.add(move); // store moves with same heuristic value
            }
            // change the board back to initial state
            chessObject.board[arr[2]][arr[3]] = target;
            chessObject.board[arr[0]][arr[1]] = src;
        }
        if (movesWithSameScore.size() > 1) {
            // if there are multiple bestmoves select one at random
            bestMove = movesWithSameScore.get(new Random().nextInt(movesWithSameScore.size()));
        }

        return bestMove;
    }

    /**
     * @param moves
     * @param chessObject
     * @return List of moves which kills the opponent pieces
     */
    private List<String> getCaptureMoves(List<String> moves, ChessObject chessObject) {
        List<String> captureMoves = new ArrayList<>();
        for (String move : moves) {
            int[] moveVector = getMoveCordiantes(move);
            char target = chessObject.board[moveVector[2]][moveVector[3]];
            char src = chessObject.board[moveVector[0]][moveVector[1]];
            if (target != ' ' && Character.isUpperCase(src) != Character.isUpperCase(target)) {
                captureMoves.add(move);
            }
        }

        return captureMoves;
    }

    /**
     * @param gs
     * @param maximizingTurn
     * @param depth
     * @param alpha
     * @param beta
     * @return The core minimax algorithm which returns hEval value of a move for a
     *         dynamic depth
     */
    private int minimax(ChessObject gs, boolean maximizingTurn, int depth, int alpha, int beta) {

        List<String> currentMoves = gs.getAllMoves(maximizingTurn ? 0 : 1);

        // return heuristic evaluation of board when depth is reached
        if (depth <= 0 || currentMoves.size() == 0) {
            int playerScore = heuristic(gs.board, gs.isWhiteTurn);
            int opponentScore = heuristic(gs.board, !gs.isWhiteTurn);
            int score = playerScore - opponentScore;
            if (score != 0 && isQuiescentState(maximizingTurn, gs) || isDepthLimitHit(depth)) {
                if (currentMoves.size() == 0) {
                    score += maximizingTurn ? -900 : 900;
                    // include depth in score when checkmate is possible in multiple ways
                    score += (depth * (maximizingTurn ? -1 : 1));
                }
                return score;
            }
        }

        if (maximizingTurn) {
            // maximizing player selcts the max possible score from its children
            int bestScore = Integer.MIN_VALUE;
            for (String move : currentMoves) {
                // apply current move on board
                int[] arr = getMoveCordiantes(move);
                char src = gs.board[arr[0]][arr[1]];
                char target = gs.board[arr[2]][arr[3]];
                gs.board[arr[2]][arr[3]] = src;
                gs.board[arr[0]][arr[1]] = ' ';
                // calculate score for current move
                int score = minimax(gs, false, depth - 1, alpha, beta);
                // update bestScore and alpha
                bestScore = Math.max(score, bestScore);
                alpha = Math.max(alpha, score);
                // revert the move from board
                gs.board[arr[2]][arr[3]] = target;
                gs.board[arr[0]][arr[1]] = src;
                // prune the state if a better move was found previously
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        } else {
            // minimizing player selcts the min possible score from its children
            int bestScore = Integer.MAX_VALUE;
            for (String move : currentMoves) {
                // apply current move on board
                int[] arr = getMoveCordiantes(move);
                char src = gs.board[arr[0]][arr[1]];
                char target = gs.board[arr[2]][arr[3]];
                gs.board[arr[2]][arr[3]] = src;
                gs.board[arr[0]][arr[1]] = ' ';
                // calculate score for current move
                int score = minimax(gs, true, depth - 1, alpha, beta);
                // update bestScore and alpha
                bestScore = Math.min(score, bestScore);
                beta = Math.min(beta, score);
                // revert the move from board
                gs.board[arr[2]][arr[3]] = target;
                gs.board[arr[0]][arr[1]] = src;
                // prune the state if a better move was found previously
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }

    /**
     * @param depth
     * @return true if max depth search is hit
     */
    private boolean isDepthLimitHit(int depth) {
        return ((depth * -1) + currentDepth) >= 2;
    }

    /**
     * @param maximizingTurn
     * @param gs
     * @return true if the board state is Quiet(no kills possible) or false if the
     *         state is not Quiet
     */
    private boolean isQuiescentState(boolean maximizingTurn, ChessObject gs) {
        List<String> currentMoves = gs.getAllMoves(maximizingTurn ? 0 : 1);
        List<String> captureMoves = getCaptureMoves(currentMoves, gs);
        return captureMoves.size() == 0;
    }

    /**
     * @param board
     * @param isWhiteTurn
     * @return returns hEval value of board
     */
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

    /**
     * @param move
     * @return takes a move and converts it into integer coordinates for row and
     *         columns
     */
    private int[] getMoveCordiantes(String move) {
        // convert move from string format to integer coordinates
        int sc = move.charAt(0) - 97;
        int sr = move.charAt(1) - 48 - 1;
        int tc = move.charAt(2) - 97;
        int tr = move.charAt(3) - 48 - 1;
        return new int[] { sr, sc, tr, tc };
    }

    private boolean checkDepth(long currentTime, long timeFactor, int depth) {
        boolean timeLimit = currentTime <= timeFactor; // timeout condition check
        boolean depthLimit = depth <= 3;// limiting maxDepth to 5 based on performance of algorithm
        boolean gameTimeLimit = ((player.timeRemaining / divFactor) / 60) > 10;
        return timeLimit && depthLimit && gameTimeLimit; // avoiding deeper levels in second half of game
    }

}
