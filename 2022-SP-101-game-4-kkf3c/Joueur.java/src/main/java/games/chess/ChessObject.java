package games.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ChessObject {
    int kr, kc, rank = 7;
    // true if it is whites turn to move
    boolean isWhiteTurn;
    // fen string, part of fen string that has board pieces info
    String fen;
    String pieces;
    // fen string splitted into 6 parts
    String[] parts;
    // board rows from fen string
    String[] rows;
    // game board
    char[][] board;
    // array to store both kings locations
    int[] kings;

    public List<String> getAllMoves(int changeTurn) {
        // update GameState variables when turn is changed by minimax function
        if (changeTurn != 0) {
            isWhiteTurn = !isWhiteTurn;
        }
        List<String> moves = new ArrayList<>();
        updateKingsLocation();
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

        // revert changes done by minimax function
        if (changeTurn != 0) {
            isWhiteTurn = !isWhiteTurn;
        }
        updateKingsLocation();
        // System.out.println("random move: " + currentTurnMove);
        return moves;
        // <<-- /Creer-Merge: makeMove -->>
    }

    public List<String> getCastlingMoves(String castle) {
        List<String> moves = new ArrayList<>();
        for (char ch : castle.toCharArray()) {
            boolean kingSide = Character.toLowerCase(ch) == 'k';
            // check if we can castle the current turn(white/black) king
            if (!(Character.isUpperCase(ch) ^ isWhiteTurn)) {
                int tc = kingSide ? 6 : 1;// target column based on king or queen side castle
                int dir = kingSide ? 1 : -1;// direction vector based on king or queen side castle
                if (canCastle(tc, dir)) {
                    tc = kingSide ? tc : 2;
                    moves.add(getFormattedMove(kr, kc, kr, tc));
                }
            }
        }
        return moves;
    }

    public boolean canCastle(int tc, int dir) {
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

    public boolean isKingUnderCheck() {
        return !isSafeMove(kr, kc);
    }

    public List<String> getEnPassantMoves(String cell) {
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

    public List<String> getCurrentPieceMoves(int r, int c) {
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

    public List<String> getPawnMoves(int r, int c) {
        List<String> moves = new ArrayList<>();

        // one step for black and white pawns
        int nr = r + (isWhiteTurn ? 1 : -1);
        if (isValidMove(nr, c) == 0 && willKingBeSafe(r, c, nr, c)) {
            String moveToMake = getFormattedMove(r, c, nr, c);
            // check for pawn promotion
            if (nr == 0 || nr == 7) {
                addPawnPromotion(moveToMake, moves);
            } else {
                moves.add(moveToMake);
            }
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
            String moveToMake = getFormattedMove(r, c, nr, nc);
            // check for pawn promotion
            if (nr == 0 || nr == 7) {
                addPawnPromotion(moveToMake, moves);
            } else {
                moves.add(moveToMake);
            }
        }
        nc = c - 1;
        if (isValidMove(nr, nc) == 1
                && Character.isUpperCase(board[nr][nc]) != isWhiteTurn
                && willKingBeSafe(r, c, nr, nc)) {
            String moveToMake = getFormattedMove(r, c, nr, nc);
            // check for pawn promotion
            if (nr == 0 || nr == 7) {
                addPawnPromotion(moveToMake, moves);
            } else {
                moves.add(moveToMake);
            }
        }
        return moves;
    }

    private void addPawnPromotion(String moveToMake, List<String> moves) {
        for (char ch : Constants.promotionPieces) {
            moves.add(moveToMake + ch);
        }
    }

    public List<String> getKnightMoves(int r, int c) {
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

    public List<String> getKingMoves(int r, int c) {
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

    public List<String> getSlidingMoves(int r, int c) {
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

            while (moveResult == 0) {
                if (willKingBeSafe(r, c, nr, nc)) {
                    moves.add(getFormattedMove(r, c, nr, nc));
                }
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

    public boolean willKingBeSafe(int r, int c, int nr, int nc) {

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

    public int isValidMove(int nr, int nc) {
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

    public String getFormattedMove(int c, int r, int newRow, int newCol) {
        // UCL format
        return (char) (r + 97) + "" + (c + 1) + "" + (char) (newCol + 97) + "" + (newRow + 1);
    }

    public boolean isSafeMove(int r, int c) {
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
                return false;
            }
        }

        return checkPawnAndKingAttacks(r, c);
    }

    public boolean checkPawnAndKingAttacks(int r, int c) {
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

    public void initializeBoard(String fen) {
        board = new char[8][8];
        for (char[] b : board)
            Arrays.fill(b, ' ');
        // fen = game.fen; // latest fen string
        parts = fen.split(" "); // divide into 6 parts
        pieces = parts[0]; // load the loaction of pieces
        rows = pieces.split("/"); // get individual rows
        isWhiteTurn = "w".equals(parts[1].toLowerCase());
        kings = new int[4];
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
        updateKingsLocation();
    }

    private void updateKingsLocation() {
        // get kings location
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                char ch = board[i][j];
                if (ch == 'K') {
                    kings[0] = i;
                    kings[1] = j;
                }
                if (ch == 'k' && !isWhiteTurn) {
                    kings[2] = i;
                    kings[3] = j;
                }
            }
        }

        kr = isWhiteTurn ? kings[0] : kings[2];
        kc = isWhiteTurn ? kings[1] : kings[3];
    }
}
