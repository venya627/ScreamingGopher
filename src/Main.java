import java.util.*;

public class Main {

    public static void main(String[] args) {
        startLoop();
    }

    private static void startLoop() {
        Scanner input = new Scanner(System.in);
        Scanner tokenizer;
        Bitboard board = new Bitboard();
        String command;

        while (true) {
            command = input.nextLine().trim();
            if (command.isBlank()) continue;

            switch (command) {
                case "quit" -> System.exit(0);
                case "stop" -> Engine.stopThinking();
                case "debug on" -> Engine.setDebugging(true);
                case "debug off" -> Engine.setDebugging(false);
                case "ponderhit" -> Engine.setPondering(false);
                case "isready" -> Engine.notifyWhenReady();
                case "uci" -> System.out.println("id name Screaming Gopher 0.1.0\nid author Veniamin Matviichuk\nuciok");
                case "ucinewgame" -> {
                    Engine.stopThinking();
                    Engine.resetStats();
                    Engine.clearCashe();
                    board.loadDefaultPosition();
                }
                default -> {
                    tokenizer = new Scanner(command);
                    String token = tokenizer.next();

                    if (Engine.isThinking() && (token.equals("setoption") || token.equals("go") || token.equals("position") || token.equals("perft"))) {
                        System.out.printf("Command ignored: '%s'. Engine is currently thinking.\n", command);
                        continue;
                    }

                    try {
                        switch (token) {
                            case "setoption" -> setOption(tokenizer);
                            case "go" -> go(tokenizer, board);
                            case "position" -> position(tokenizer, board);
                            case "perft" -> {
                                int depth = tokenizer.nextInt();
                                new Thread(() -> Engine.perft(board, depth));
                            }
                            default -> System.out.printf("Unknown command: '%s'. Type help for more information.\n", command);
                        }
                    } catch (Exception e) {
                        System.out.printf("Error processing command: '%s'. %s.\n", command, e.getMessage());
                    }
                }
            }
        }
    }

    private static void setOption(Scanner tokenizer) {

    }

    private static void go(Scanner tokenizer, Bitboard board) {
        Engine.Limits limits = new Engine.Limits();

        while (tokenizer.hasNext()) {
            switch (tokenizer.next()) {
                case "searchmoves" -> {
                    int[] moves = new int[Bitboard.MAX_LEGAL_MOVES];
                    int count = 0;

                    while (tokenizer.hasNext()) {
                        moves[count++] = Bitboard.parseMove(board, tokenizer.next());
                    }

                    limits.searchMoves = Arrays.copyOfRange(moves, 0, count);
                }
                case "wtime" -> limits.time[Bitboard.WHITE] = tokenizer.nextInt();
                case "btime" -> limits.time[Bitboard.BLACK] = tokenizer.nextInt();
                case "winc " -> limits.increment[Bitboard.WHITE] = tokenizer.nextInt();
                case "binc" -> limits.increment[Bitboard.BLACK] = tokenizer.nextInt();
                case "movestogo" -> limits.movesToGo = tokenizer.nextInt();
                case "depth" -> limits.depth = tokenizer.nextInt();
                case "nodes" -> limits.nodes = tokenizer.nextInt();
                case "mate" -> limits.mate = tokenizer.nextInt();
                case "movetime" -> limits.moveTime = tokenizer.nextInt();
                case "ponder" -> Engine.setPondering(true);
                case "infinite" -> limits.infinite = true;
            }
        }

        new Thread(() -> Engine.search(board, limits)).start();
    }

    private static void position(Scanner tokenizer, Bitboard board) {
        switch (tokenizer.next()) {
            case "startpos", "standard" -> {
                board.loadDefaultPosition();
                if (tokenizer.hasNext()) tokenizer.next();
                makeMovesFromTokenizer(tokenizer, board);
            }

            case "fen" -> {
                StringBuilder builder = new StringBuilder();
                String token;

                while (tokenizer.hasNext() && !(token = tokenizer.next()).equals("moves")) {
                    builder.append(token).append(" ");
                }

                String fen = builder.toString();
                board.loadPosition(fen);
                makeMovesFromTokenizer(tokenizer, board);
            }

            case "display", "show" -> System.out.println(board);

            case "eval", "evaluate" -> System.out.println(Engine.evaluate(board));
        }
    }

    private static void makeMovesFromTokenizer(Scanner tokenizer, Bitboard board) {
        while (tokenizer.hasNext()) {
            board.makeMove(Bitboard.parseMove(board, tokenizer.next()));
        }
    }
}







