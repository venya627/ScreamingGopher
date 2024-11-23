import java.util.Arrays;

public class Engine {

    public static final int MAX_DEPTH = 100;

    private static final int[] MATERIAL_VALUE = { 100, 300, 350, 500, 900, 10000, -100, -300, -350, -500, -900, -10000 };

    private static final int[][] MVV_LVA = {
        { 0, 0, 0, 0, 0, 0, 105, 205, 305, 405, 505 },
        { 0, 0, 0, 0, 0, 0, 104, 204, 304, 404, 504 },
        { 0, 0, 0, 0, 0, 0, 103, 203, 303, 403, 503 },
        { 0, 0, 0, 0, 0, 0, 102, 202, 302, 402, 502 },
        { 0, 0, 0, 0, 0, 0, 101, 201, 301, 401, 501 },
        { 0, 0, 0, 0, 0, 0, 100, 200, 300, 400, 500 },

        { 105, 205, 305, 405, 505 },
        { 104, 204, 304, 404, 504 },
        { 103, 203, 303, 403, 503 },
        { 102, 202, 302, 402, 502 },
        { 101, 201, 301, 401, 501 },
        { 100, 200, 300, 400, 500 }
    };

    private static final int[][] PIECE_SQUARE_TABLE = {

        // White
        {
            0, 0, 0, 0, 0, 0, 0, 0,
            100, 100, 100, 100, 100, 100, 100, 100,
            20, 20, 40, 60, 60, 40, 20, 20,
            10, 10, 20, 50, 50, 20, 10, 10,
            0, 0, 0, 40, 40, 0, 0, 0,
            10, -10, -20, 0, 0, -20, -10, 10,
            10, 20, 20, -40, -40, 20, 20, 10,
            0, 0, 0, 0, 0, 0, 0, 0
        },

        {
            -100, -80, -60, -60, -60, -60, -80, -100,
            -80, -40, 0, 0, 0, 0, -40, -80,
            -60, 0, 20, 30, 30, 20, 0, -60,
            -60, 10, 30, 40, 40, 30, 10, -60,
            -60, 0, 30, 40, 40, 30, 0, -60,
            -60, 10, 20, 30, 30, 20, 10, -60,
            -80, -40, 0, 10, 10, 0, -40, -80,
            -100, -80, -60, -60, -60, -60, -80, -100
        },

        {
            -40, -20, -20, -20, -20, -20, -20, -40,
            -20, 0, 0, 0, 0, 0, 0, -20,
            -20, 0, 10, 20, 20, 10, 0, -20,
            -20, 10, 10, 20, 20, 10, 10, -20,
            -20, 0, 20, 20, 20, 20, 0, -20,
            -20, 20, 20, 20, 20, 20, 20, -20,
            -20, 10, 0, 0, 0, 0, 10, -20,
            -40, -20, -20, -20, -20, -20, -20, -40
        },

        {
            0, 0, 0, 0, 0, 0, 0, 0,
            10, 20, 20, 20, 20, 20, 20, 10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            0, 0, 0, 10, 10, 0, 0, 0
        },

        {
            -40, -20, -20, -10, -10, -20, -20, -40,
            -20, 0, 0, 0, 0, 0, 0, -20,
            -20, 0, 10, 10, 10, 10, 0, -20,
            -10, 0, 10, 10, 10, 10, 0, -10,
            0, 0, 10, 10, 10, 10, 0, -10,
            -20, 10, 10, 10, 10, 10, 0, -20,
            -20, 0, 10, 0, 0, 0, 0, -20,
            -40, -20, -20, -10, -10, -20, -20, -40
        },

        {
            -60, -80, -80, -100, -100, -80, -80, -60,
            -60, -80, -80, -100, -100, -80, -80, -60,
            -60, -80, -80, -100, -100, -80, -80, -60,
            -60, -80, -80, -100, -100, -80, -80, -60,
            -40, -60, -60, -80, -80, -60, -60, -40,
            -20, -40, -40, -40, -40, -40, -40, -20,
            40, 40, 0, 0, 0, 0, 40, 40,
            40, 60, 20, 0, 0, 20, 60, 40
        },

        // Black
        {
            0, 0, 0, 0, 0, 0, 0, 0,
            -10, -20, -20, 40, 40, -20, -20, -10,
            -10, 10, 20, 0, 0, 20, 10, -10,
            0, 0, 0, -40, -40, 0, 0, 0,
            -10, -10, -20, -50, -50, -20, -10, -10,
            -20, -20, -40, -60, -60, -40, -20, -20,
            -100, -100, -100, -100, -100, -100, -100, -100,
            0, 0, 0, 0, 0, 0, 0, 0,
        },

        {
            100, 80, 60, 60, 60, 60, 80, 100,
            80, 40, 0, -10, -10, 0, 40, 80,
            60, -10, -20, -30, -30, -20, -10, 60,
            60, 0, -30, -40, -40, -30, 0, 60,
            60, -10, -30, -40, -40, -30, -10, 60,
            60, 0, -20, -30, -30, -20, 0, 60,
            80, 40, 0, 0, 0, 0, 40, 80,
            100, 80, 60, 60, 60, 60, 80, 100,
        },

        {
            40, 20, 20, 20, 20, 20, 20, 40,
            20, -10, 0, 0, 0, 0, -10, 20,
            20, -20, -20, -20, -20, -20, -20, 20,
            20, 0, -20, -20, -20, -20, 0, 20,
            20, -10, -10, -20, -20, -10, -10, 20,
            20, 0, -10, -20, -20, -10, 0, 20,
            20, 0, 0, 0, 0, 0, 0, 20,
            40, 20, 20, 20, 20, 20, 20, 40,
        },

        {
            0, 0, 0, -10, -10, 0, 0, 0,
            10, 0, 0, 0, 0, 0, 0, 10,
            10, 0, 0, 0, 0, 0, 0, 10,
            10, 0, 0, 0, 0, 0, 0, 10,
            10, 0, 0, 0, 0, 0, 0, 10,
            10, 0, 0, 0, 0, 0, 0, 10,
            -10, -20, -20, -20, -20, -20, -20, -10,
            0, 0, 0, 0, 0, 0, 0, 0,
        },

        {
            40, 20, 20, 10, 10, 20, 20, 40,
            20, 0, 0, 0, 0, -10, 0, 20,
            20, 0, -10, -10, -10, -10, -10, 20,
            10, 0, -10, -10, -10, -10, 0, 0,
            10, 0, -10, -10, -10, -10, 0, 10,
            20, 0, -10, -10, -10, -10, 0, 20,
            20, 0, 0, 0, 0, 0, 0, 20,
            40, 20, 20, 10, 10, 20, 20, 40,
        },

        {
            -40, -60, -20, 0, 0, -20, -60, -40,
            -40, -40, 0, 0, 0, 0, -40, -40,
            20, 40, 40, 40, 40, 40, 40, 20,
            40, 60, 60, 80, 80, 60, 60, 40,
            60, 80, 80, 100, 100, 80, 80, 60,
            60, 80, 80, 100, 100, 80, 80, 60,
            60, 80, 80, 100, 100, 80, 80, 60,
            60, 80, 80, 100, 100, 80, 80, 60,
        }
    };

    private static final int[][] KING_TABLE_EG = {
        { -100, -100 }, { -80, -60 }, { -60, -60 }, { -40, -60 }, { -40, -60 }, { -60, -60 }, { -80, -60 }, { -100, -100 },
        { -60, -60 }, { -40, -60 }, { -20, 0 }, { 0, 0 }, { 0, 0 }, { -20, 0 }, { -40, -60 }, { -60, -60 },
        { -60, -60 }, { -20, -20 }, { 40, 40 }, { 60, 60 }, { 60, 60 }, { 40, 40 }, { -20, -20 }, { -60, -60 },
        { -60, -60 }, { -20, -20 }, { 60, 60 }, { 80, 80 }, { 80, 80 }, { 60, 60 }, { -20, -20 }, { -60, -60 },
        { -60, -60 }, { -20, -20 }, { 60, 60 }, { 80, 80 }, { 80, 80 }, { 60, 60 }, { -20, -20 }, { -60, -60 },
        { -60, -60 }, { -20, -20 }, { 40, 40 }, { 60, 60 }, { 60, 60 }, { 40, 40 }, { -20, -20 }, { -60, -60 },
        { -60, -60 }, { -60, -40 }, { 0, -20 }, { 0, 0 }, { 0, 0 }, { 0, -20 }, { -60, -40 }, { -60, -60 },
        { -100, -100 }, { -60, -80 }, { -60, -60 }, { -60, -40 }, { -60, -40 }, { -60, -60 }, { -60, -80 }, { -100, -100 }
    };

    public static class Limits {

        public static final int DEFAULT_MOVES_TO_GO = 30;
        public static final int NO_TIME_LIMIT = Integer.MAX_VALUE;

        public int[] searchMoves = null;
        public final int[] time = new int[2];
        public final int[] increment = new int[2];
        public int movesToGo = DEFAULT_MOVES_TO_GO;
        public int depth = MAX_DEPTH;
        public int nodes = Integer.MAX_VALUE;
        public int mate = MAX_DEPTH / 2;
        public int moveTime = 0;
        public boolean infinite = false;

        public long calculateSearchTime(int turn) {
            if (moveTime > 0) return moveTime;
            if (time[turn] > 0) return time[turn] / movesToGo + increment[turn];
            return NO_TIME_LIMIT;
        }
    }

    private static volatile boolean isThinking = false;
    private static volatile boolean shouldNotify = false;
    private static volatile boolean isDebugMode = false;
    private static volatile boolean isPonderMode = false;

    private static boolean depthIncompleteOrTerminatedEarly = false;

    private static long maxTime = 0;
    private static long timeStart = 0;
    private static long nodes = 0;
    private static int ply = 0;

    private static int[][] killerMoves = new int[2][MAX_DEPTH];
    private static int[][][] historyMoves = new int[2][64][64];
    private static int[][] pvTable = new int[MAX_DEPTH][MAX_DEPTH];
    private static int[] pvLength = new int[MAX_DEPTH];

    private static void sortMoves(Bitboard board, int[] moves) {
        int length = moves.length;

        int[][] pairs = new int[length][2];

        for (int i = 0; i < length; i++) {
            pairs[i][0] = moves[i];
            pairs[i][1] = scoreMove(board, moves[i]);
        }

        Arrays.sort(pairs, (a, b) -> b[1] - a[1]);

        for (int i = 0; i < length; i++) {
            moves[i] = pairs[i][0];
        }
    }

    private static int quiescence(Bitboard board, Limits limits, int sideFactor, int alpha, int beta) {
        int evaluation = sideFactor * materialAdvantage(board); // Max advantage 10750
        nodes++;

        if (evaluation >= beta) {
            return beta;
        }

        if (evaluation > alpha) {
            alpha = evaluation;
        }

        int[] captures = board.generateCaptures();
        sortMoves(board, captures);

        if (!isThinking || !isPonderMode && (nodes > limits.nodes || System.nanoTime() - timeStart > maxTime)) {
            depthIncompleteOrTerminatedEarly = true;
        }

        for (int i = 0; i < captures.length && !depthIncompleteOrTerminatedEarly; i++) {
            int score = -quiescence(Bitboard.makeCopyAndMove(board, captures[i]), limits, -sideFactor, -beta, -alpha);

            if (score >= beta) {
                return beta;
            }

            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    private static int negamax(Bitboard board, Limits limits, int depth, int sideFactor, int alpha, int beta) {
        int[] moves;
        pvLength[ply] = ply;
        nodes++;

        if (ply == 0 && limits.searchMoves != null) {
            moves = limits.searchMoves;
        } else {
            moves = board.generateAllMoves();

            if (moves.length == 0) {
                if (board.isKingInCheck()) {
                    return ply - 10850;
                }

                return 0;
            }
        }

        if (depth < 1) {
            return quiescence(board, limits, sideFactor, alpha, beta);
        }

        boolean foundPV = false;

        sortMoves(board, moves);

        if (!isThinking || !isPonderMode && (nodes > limits.nodes || System.nanoTime() - timeStart > maxTime)) {
            depthIncompleteOrTerminatedEarly = true;
        }

        for (int i = 0; i < moves.length && !depthIncompleteOrTerminatedEarly; i++) {
            int move = moves[i];
            int score;

            ply++;

            if (foundPV) {
                Bitboard copy = new Bitboard(board);
                copy.makeMove(move);

                score = -negamax(new Bitboard(copy), limits, depth - 1, -sideFactor, -alpha - 1, -alpha);

                if (score > alpha && score < beta) {
                    score = -negamax(new Bitboard(copy), limits, depth - 1, -sideFactor, -beta, -alpha);
                }
            } else {
                score = -negamax(Bitboard.makeCopyAndMove(board, move), limits, depth - 1, -sideFactor, -beta, -alpha);
            }

            ply--;

            if (score >= beta) {
                if (!Bitboard.isCapture(Bitboard.moveKind(move))) {
                    killerMoves[1][ply] = killerMoves[0][ply];
                    killerMoves[0][ply] = move;
                }

                return beta;
            }

            if (score > alpha) {
                foundPV = true;
                alpha = score;

                if (!Bitboard.isCapture(Bitboard.moveKind(move)))
                    historyMoves[board.getTurn()][Bitboard.sourceSquare(move)][Bitboard.targetSquare(move)] += depth;

                pvTable[ply][ply] = move;

                for (int nextPly = ply + 1; nextPly < pvLength[ply + 1]; nextPly++) {
                    pvTable[ply][nextPly] = pvTable[ply + 1][nextPly];
                }

                pvLength[ply] = pvLength[ply + 1];
            }
        }

        return alpha;
    }

    private static long perftDriver(Bitboard board, int depth) {
        if (depth < 1) {
            return 1;
        }

        int[] moves = board.generateAllMoves();

        if (depth == 1) {
            return moves.length;
        }

        long nodes = 0;

        for (int i = 0; i < moves.length && isThinking; i++) {
            nodes += perftDriver(Bitboard.makeCopyAndMove(board, moves[i]), depth - 1);
        }

        return nodes;
    }

    private static int materialAdvantage(Bitboard board) {
        int score = 0;

        for (int piece = Bitboard.WHITE_PAWN; piece <= Bitboard.BLACK_KING; piece++) {
            long bitboard = board.bitboards[piece];

            int pieceValue = MATERIAL_VALUE[piece];
            int[] bonusTable = PIECE_SQUARE_TABLE[piece];

            while (bitboard != 0) {
                int square = Bitboard.lowestOneBitIndex(bitboard);
                score += pieceValue + bonusTable[square];
                bitboard &= bitboard - 1;
            }
        }

        return score;
    }

    private static int scoreMove(Bitboard board, int move) {
        if (move == pvTable[0][0]) {
            return 600;
        }

        if (Bitboard.isCapture(Bitboard.moveKind(move))) {
            int capturedPiece = board.getPiece(Bitboard.targetSquare(move));

            return MVV_LVA[board.getPiece(Bitboard.sourceSquare(move))][capturedPiece == -1 ?
                Bitboard.makePiece(board.getTurn() ^ 1, Bitboard.PAWN) :
                capturedPiece];
        }

        if (move == killerMoves[0][ply]) {
            return 90;
        }

        if (move == killerMoves[1][ply]) {
            return 80;
        }

        return historyMoves[board.getTurn()][Bitboard.sourceSquare(move)][Bitboard.targetSquare(move)];
    }

    public static void stopThinking() {
        isThinking = false;
    }

    public static void notifyWhenReady() {
        if (isThinking) shouldNotify = true;
        else System.out.println("readyok");
    }

    public static void setDebugging(boolean isSet) {
        isDebugMode = isSet;
    }

    public static void setPondering(boolean isSet) {
        isPonderMode = isSet;
    }

    public static void resetStats() {
        depthIncompleteOrTerminatedEarly = false;
        isPonderMode = false;
        maxTime = 0;
        timeStart = 0;
        nodes = 0;
        ply = 0;
    }
    
    public static void clearCashe() {
        killerMoves = new int[2][MAX_DEPTH];
        historyMoves = new int[2][64][64];
        pvTable = new int[MAX_DEPTH][MAX_DEPTH];
        pvLength = new int[MAX_DEPTH];
    }

    public static boolean isThinking() {
        return isThinking;
    }

    public static int evaluate(Bitboard board) {
        return 0;
    }

    public static void search(Bitboard board, Limits limits) {
        isThinking = true;

        int turn = board.getTurn();
        int sideFactor = turn == Bitboard.WHITE ? 1 : -1;
        int alpha = -10850;
        int beta = 10850;

        maxTime = (limits.calculateSearchTime(turn) - 5) * 1000000L;
        timeStart = System.nanoTime();

        for (int depth = 1; depth <= limits.depth; depth++) {
            int[] pvOld = pvTable[0].clone();

            int advantage = Engine.negamax(board, limits, depth, sideFactor, alpha, beta);

            if (depthIncompleteOrTerminatedEarly) {
                pvTable[0] = pvOld;
                break;
            }

            long elapsedTimeMs = (System.nanoTime() - timeStart) / 1000000 + 1;
            long nps = (1000L * nodes) / elapsedTimeMs;

            boolean isMate = Math.abs(advantage) >= beta - 100;
            int score = isMate ?
                (advantage > 0 ? (beta - advantage + 1) / 2 : (alpha - advantage) / 2) :
                advantage;

            System.out.printf("info depth %d multipv %d score %s %d nodes %d time %d nps %d pv", depth, 1, isMate ? "mate" : "cp", score, nodes, elapsedTimeMs, nps);

            for (int pv = 0; pv < pvLength[0]; pv++) {
                System.out.print(" " + Bitboard.moveToString(pvTable[0][pv]));
            }

            System.out.println();

            if (isMate && score <= limits.mate) {
                break;
            }
        }

        while (isThinking && (isPonderMode || limits.infinite)) ;

        int best = pvTable[0][0];
        int ponder = pvTable[0][1];

        System.out.printf("bestmove %s", Bitboard.moveToString(best));
        if (ponder != 0) System.out.printf(" ponder %s", Bitboard.moveToString(ponder));
        System.out.println();
        resetStats();

        isThinking = false;
    }

    public static void perft(Bitboard board, int depth) {
        isThinking = true;

        long totalNodes = 0;
        depth--;

        long start = System.nanoTime();
        int[] moves = board.generateAllMoves();

        for (int current = 0; current < moves.length && isThinking; current++) {
            int move = moves[current];
            long nodes = perftDriver(Bitboard.makeCopyAndMove(board, move), depth);
            totalNodes += nodes;

            System.out.printf("info currmove %s currmovenumber %d nodes %d\n", Bitboard.moveToString(move), current + 1, nodes);
        }

        long timeMs = (System.nanoTime() - start) / 1000000 + 1;

        System.out.printf("perft %d nodes %d time %d nps %d\n", depth + 1, totalNodes, timeMs, 1000 * totalNodes / timeMs);

        isThinking = false;
    }
}
