/**
 * This is where you build your AI for the Chess game.
 */
package games.chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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
    // values to store checkRow, checkColumn, kingRow, kingColumn
    int cr, cc, kr, kc, rank = 7;
    // true if it is whites turn to move
    boolean isWhiteTurn;
    // fen string, part of fen string that has board pieces info
    String fen, pieces;
    // fen string splitted into 6 parts
    String[] parts;
    // board rows from fen string
    String[] rows;
    // game board
    char[][] board;

    // <<-- Creer-Merge: fields -->> - Code you add between this comment and the end
    // comment will be preserved between Creer re-runs.
    // you can add additional fields here for your AI to use
    // <<-- /Creer-Merge: fields -->>

    /**
     * This returns your AI's name to the game server. Just replace the string.
     * 
     * @return string of you AI's name
     */
    public String getName() {
        // <<-- Creer-Merge: get-name -->> - Code you add between this comment and the
        // end comment will be preserved between Creer re-runs.
        return "random"; // REPLACE THIS WITH YOUR TEAM NAME!
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
        // <<-- /Creer-Merge: start -->>
        super.start();
        initializeBoard();
    }

    /**
     * This is automatically called every time the game (or anything in it) updates.
     * If a function you call triggers an update this will be called before that
     * function returns.
     */
    public void gameUpdated() {
        // <<-- Creer-Merge: game-updated -->> - Code you add between this comment and
        // the end comment will be preserved between Creer re-runs.
        // <<-- /Creer-Merge: game-updated -->>
        super.gameUpdated();
        initializeBoard();
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
        // <<-- Creer-Merge: makeMove -->> - Code you add between this comment and the
        // end comment will be preserved between Creer re-runs.
        // Put your game logic here for makeMove

        List<String> moves = new ArrayList<>();
        String currentTurnMove = null;
        boolean isKingSafe = true;
        Random random = new Random();

        // If king is under check, try to move king to a safe location
        if (isKingUnderCheck()) {
            List<String> kingPieceMoves = getKingMoves(kr, kc);
            // if king has a safe location to move, return that move
            if (kingPieceMoves.size() > 0) {
                currentTurnMove = kingPieceMoves.get(random.nextInt(kingPieceMoves.size()));
                moves.addAll(kingPieceMoves);
            } else {
                isKingSafe = false;
            }
        }

        // iterate over board, check if the piece is of current color and generate all
        // it's moves
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char ch = board[i][j];
                if (Character.isLetter(ch) && (!(Character.isUpperCase(ch) ^ isWhiteTurn))) {
                    // all possible moves for piece at index i,j
                    List<String> currentMoves = getCurrentPieceMoves(i, j);
                    for (String move : currentMoves) {
                        moves.add(move);
                    }
                }
            }
        }

        // if king is under check and king cant be saved by moving king, check if king
        // can be saved by killing opponent piece that is source of check
        if (!isKingSafe) {
            for (String move : moves) {
                int column = move.charAt(2) - 97; // column coordinate
                int row = move.charAt(3) - 48 - 1; // row coordinate
                if (cr == row && cc == column) {
                    currentTurnMove = move;
                    isKingSafe = true;
                }
            }
        }
        // en passant moves
        if (!parts[3].equals("-")) {
            for (String move : getEnPassantMoves(parts[3])) {
                moves.add(move);
            }
        }

        // castling moves
        if (!isKingUnderCheck() && !parts[2].equals("-")) {
            for (String move : getCastlingMoves(parts[2])) {
                moves.add(move);
            }
        }
        // return a move by randomly selecting one
        if (currentTurnMove == null && moves.size() > 0) {
            currentTurnMove = moves.get(random.nextInt(moves.size()));
        }
        System.out.println(moves);
        System.out.println(currentTurnMove);
        return currentTurnMove;
        // <<-- /Creer-Merge: makeMove -->>
    }

    private List<String> getCastlingMoves(String castle) {
        List<String> moves = new ArrayList<>();
        for (char ch : castle.toCharArray()) {
            boolean kingSide = Character.toLowerCase(ch) == 'k';
            // check if we can castle the current turn(white/black) king
            if (!(Character.isUpperCase(ch) ^ isWhiteTurn)) {
                int tc = kingSide ? 6 : 1;// target column based on king or queen side castle
                int dir = kingSide ? 1 : -1;// direction vector based on king or queen side castle
                if (canCastle(tc, dir)) {
                    moves.add(getFormattedMove(kr, kc, kr, tc));
                }
            }
        }
        return moves;
    }

    private boolean canCastle(int tc, int dir) {
        int currColumn = kc + dir;
        // start at e8 and increment until g8 for kingside castle
        // and decrement until b8 for queen side castle
        while (!((dir == 1 && currColumn > tc) || (dir == -1 && currColumn < tc))) {
            if (!(board[kr][currColumn] == ' ' && isSafeMove(kr, currColumn))) {
                return false;
            }
            currColumn += dir;
        }
        return true;
    }

    private boolean isKingUnderCheck() {
        return !isSafeMove(kr, kc);
    }

    private List<String> getEnPassantMoves(String cell) {
        // load row, column from fen string
        int c = cell.charAt(0) - 97;
        int r = cell.charAt(1) - 48 - 1;

        List<String> moves = new ArrayList<>();
        // pawns attack vectors
        int[] pawnX = new int[] { 1, 1, -1, -1 };
        int[] pawnY = new int[] { 1, -1, 1, -1 };
        isWhiteTurn = !isWhiteTurn;
        int startIndex = isWhiteTurn ? 0 : 2;
        int endIndex = isWhiteTurn ? 2 : 4;

        for (int i = startIndex; i < endIndex; i++) {
            int nr = r + pawnX[i];
            int nc = c + pawnY[i];
            // check if move is in bounds
            if (isValidMove(nr, nc) == 1 && Character.toLowerCase(board[nr][nc]) == 'p') {
                moves.add(getFormattedMove(nr, nc, r, c));
            }

        }
        isWhiteTurn = !isWhiteTurn;
        return moves;
    }

    private List<String> getCurrentPieceMoves(int r, int c) {
        char ch = Character.toLowerCase(board[r][c]);
        switch (ch) {
            case 'p':
                return getPawnMoves(r, c);

            case 'n':
                return getKnightMoves(r, c);

            case 'k':
                return getKingMoves(r, c);

            case 'q':
            case 'r':
            case 'b':
                return getSlidingMoves(r, c);
        }
        return null;
    }

    private List<String> getPawnMoves(int r, int c) {
        List<String> moves = new ArrayList<>();

        // one step for black and white pawns
        int nr = r + (isWhiteTurn ? 1 : -1);
        if (isValidMove(nr, c) == 0 && willKingBeSafe(r, c, nr, c)) {
            moves.add(getFormattedMove(r, c, nr, c));
        }

        // two steps for white pawn
        if (r == 1 && isWhiteTurn) {
            if (isValidMove(nr, c) == 0
                    && isValidMove(nr + 1, c) == 0
                    && willKingBeSafe(r, c, nr + 1, c)) {
                moves.add(getFormattedMove(r, c, nr + 1, c));
            }
        }

        // two steps for black pawn
        if (r == 6 && !isWhiteTurn) {
            if (isValidMove(nr, c) == 0
                    && isValidMove(nr - 1, c) == 0
                    && willKingBeSafe(r, c, nr, c)) {
                moves.add(getFormattedMove(r, c, nr - 1, c));
            }
        }
        // opposite side kills
        int nc = c + 1;
        if (isValidMove(nr, nc) == 1
                && Character.isUpperCase(board[nr][nc]) != isWhiteTurn
                && willKingBeSafe(r, c, nr, nc)) {
            moves.add(getFormattedMove(r, c, nr, nc));
        }
        nc = c - 1;
        if (isValidMove(nr, nc) == 1
                && Character.isUpperCase(board[nr][nc]) != isWhiteTurn
                && willKingBeSafe(r, c, nr, nc)) {
            moves.add(getFormattedMove(r, c, nr, nc));
        }
        return moves;
    }

    private List<String> getKnightMoves(int r, int c) {
        List<String> moves = new ArrayList<>();
        // the eight steps a knight can take
        int knightX[] = { 2, 1, -1, -2, -2, -1, 1, 2 };
        int knightY[] = { 1, 2, 2, 1, -1, -2, -2, -1 };

        for (int i = 0; i < 8; i++) {
            int nr = r + knightX[i];
            int nc = c + knightY[i];
            if (isValidMove(nr, nc) != -1 && willKingBeSafe(r, c, nr, nc))
                moves.add(getFormattedMove(r, c, nr, nc));
        }
        return moves;
    }

    private List<String> getKingMoves(int r, int c) {
        List<String> moves = new ArrayList<>();
        char tmp = board[r][c];
        board[r][c] = ' ';
        // looping over the eight directions that a king can move
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // current position is not a valid move
                if (i == 0 && j == 0)
                    continue;
                // calculate new indices
                int nr = r + i;
                int nc = c + j;
                if (isValidMove(nr, nc) != -1 && isSafeMove(nr, nc)) {
                    moves.add(getFormattedMove(r, c, nr, nc));
                }
            }
        }
        board[r][c] = tmp;
        return moves;
    }

    private List<String> getSlidingMoves(int r, int c) {
        List<String> moves = new ArrayList<>();
        // eight possible directions for sliding pieces, of which the first four are for
        // rook, and last four are for bishop
        int[] rowCoordiantes = new int[] { 1, -1, 0, 0, -1, -1, 1, 1 };
        int[] columnCoordinates = new int[] { 0, 0, 1, -1, -1, 1, -1, 1 };
        char piece = Character.toLowerCase(board[r][c]);
        // setting indices based on piece
        int startIndex = piece == 'b' ? 4 : 0;
        int endIndex = piece == 'r' ? 4 : 8;

        for (int i = startIndex; i < endIndex; i++) {
            int dr = rowCoordiantes[i];
            int dc = columnCoordinates[i];

            int nr = r + dr;
            int nc = c + dc;

            int moveResult = isValidMove(nr, nc);

            while (moveResult == 0 && willKingBeSafe(r, c, nr, nc)) {
                moves.add(getFormattedMove(r, c, nr, nc));
                nr = nr + dr;
                nc = nc + dc;
                moveResult = isValidMove(nr, nc);
            }
            // if the last position has an enemy piece, it is a valid move
            if (moveResult == 1 && willKingBeSafe(r, c, nr, nc))
                moves.add(getFormattedMove(r, c, nr, nc));
        }
        return moves;
    }

    private boolean willKingBeSafe(int r, int c, int nr, int nc) {

        char king = board[r][c];
        char currentPiece = board[nr][nc];
        // move king to new location
        board[r][c] = ' ';
        board[nr][nc] = king;
        boolean checkStatus = isKingUnderCheck();
        // bring king back to his original location
        board[r][c] = king;
        board[nr][nc] = currentPiece;
        return !checkStatus;
    }

    private int isValidMove(int nr, int nc) {
        // out of index
        if (nr < 0 || nc < 0 || nr >= board.length || nc >= board[0].length)
            return -1;
        char ch = board[nr][nc];
        // empty square
        if (ch == ' ')
            return 0;
        // same color piece
        if ((!(Character.isUpperCase(ch) ^ isWhiteTurn)))
            return -1;
        // opponent piece
        return 1;
    }

    private String getFormattedMove(int c, int r, int newRow, int newCol) {
        // UCL format
        return (char) (r + 97) + "" + (c + 1) + "" + (char) (newCol + 97) + "" + (newRow + 1);
    }

    private boolean isSafeMove(int r, int c) {
        // check sliding attacks
        int[] rowCoordiantes = new int[] { 1, -1, 0, 0, -1, -1, 1, 1 };
        int[] columnCoordinates = new int[] { 0, 0, 1, -1, -1, 1, -1, 1 };
        for (int i = 0; i < 8; i++) {
            int dr = rowCoordiantes[i];
            int dc = columnCoordinates[i];
            int nr = r + dr;
            int nc = c + dc;
            int moveResult = isValidMove(nr, nc);
            while (moveResult == 0) {
                nr = nr + dr;
                nc = nc + dc;
                moveResult = isValidMove(nr, nc);
            }
            if (moveResult == 1) {
                if ((i < 4 && Character.toLowerCase(board[nr][nc]) == 'r') ||
                        (i >= 4 && Character.toLowerCase(board[nr][nc]) == 'b') ||
                        (Character.toLowerCase(board[nr][nc]) == 'q')) {
                    cr = nr;
                    cc = nc;
                    return false;
                }
            }
        }
        // check knight attacks
        int knightX[] = { 2, 1, -1, -2, -2, -1, 1, 2 };
        int knightY[] = { 1, 2, 2, 1, -1, -2, -2, -1 };
        for (int i = 0; i < 8; i++) {
            int nr = r + knightX[i];
            int nc = c + knightY[i];
            if (isValidMove(nr, nc) == 1 && Character.toLowerCase(board[nr][nc]) == 'n') {
                cr = nr;
                cc = nc;
                return false;
            }
        }

        return checkPawnAndKingAttacks(r, c);
    }

    private boolean checkPawnAndKingAttacks(int r, int c) {
        // check pawn attacks
        int[] pawnX = new int[] { 1, 1, -1, -1 };
        int[] pawnY = new int[] { 1, -1, 1, -1 };
        // only opposite color pawns can attack, hence the start and end coordinates
        int startIndex = isWhiteTurn ? 0 : 2;
        int endIndex = isWhiteTurn ? 2 : 4;
        for (int i = startIndex; i < endIndex; i++) {
            int nr = r + pawnX[i];
            int nc = c + pawnY[i];
            if (isValidMove(nr, nc) == 1 && Character.toLowerCase(board[nr][nc]) == 'p') {
                return false;
            }

        }
        // check king attacks
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0)
                    continue;
                int nr = r + i;
                int nc = c + j;
                if (isValidMove(nr, nc) == 1 && Character.toLowerCase(board[nr][nc]) == 'k') {
                    return false;
                }
            }
        }
        return true;
    }

    private void initializeBoard() {
        board = new char[8][8];
        for (char[] b : board)
            Arrays.fill(b, ' ');
        fen = game.fen; // latest fen string
        parts = fen.split(" "); // divide into 6 parts
        pieces = parts[0]; // load the loaction of pieces
        rows = pieces.split("/"); // get individual rows
        isWhiteTurn = "w".equals(parts[1].toLowerCase());

        // create game board
        for (int i = 0; i < board.length; i++) {
            int file = rank - i;
            String curr = rows[i];
            int j = 0;
            for (char ch : curr.toCharArray()) {
                if (Character.isDigit(ch)) {
                    j += ch - 48;
                } else {
                    board[file][j++] = ch;
                }
            }
        }

        // get kings location
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                char ch = board[i][j];
                if (ch == 'K' && isWhiteTurn) {
                    kr = i;
                    kc = j;
                }
                if (ch == 'k' && !isWhiteTurn) {
                    kr = i;
                    kc = j;
                }
            }
        }
    }
}
