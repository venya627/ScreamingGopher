import java.util.Arrays;

public class Bitboard {

    public static final long SQ1 = -9223372036854775808L;

    public static final long ALL_BITS_SET = -1L;

    public static final int MAX_LEGAL_MOVES = 218;
    public static final int MAX_LEGAL_CAPTURES = 74;
    public static final int MAX_GAME_LENGTH_PLIES = 538;

    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int BOTH = 2;

    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;

    public static final int WHITE_PAWN = 0;
    public static final int WHITE_KNIGHT = 1;
    public static final int WHITE_BISHOP = 2;
    public static final int WHITE_ROOK = 3;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_KING = 5;
    public static final int BLACK_PAWN = 6;
    public static final int BLACK_KNIGHT = 7;
    public static final int BLACK_BISHOP = 8;
    public static final int BLACK_ROOK = 9;
    public static final int BLACK_QUEEN = 10;
    public static final int BLACK_KING = 11;

    public static final int WHITE_CAN_CASTLE_KING_SIDE = 1;
    public static final int WHITE_CAN_CASTLE_QUEEN_SIDE = 2;
    public static final int BLACK_CAN_CASTLE_KING_SIDE = 4;
    public static final int BLACK_CAN_CASTLE_QUEEN_SIDE = 8;

    public static final int QUIET_MOVE = 0;
    public static final int DOUBLE_PAWN_PUSH = 1;
    public static final int KING_SIDE_CASTLE = 2;
    public static final int QUEEN_SIDE_CASTLE = 3;
    public static final int CAPTURE = 4;
    public static final int ENPASSANT_CAPTURE = 5;
    public static final int KNIGHT_PROMOTION = 8;
    public static final int BISHOP_PROMOTION = 9;
    public static final int ROOK_PROMOTION = 10;
    public static final int QUEEN_PROMOTION = 11;
    public static final int KNIGHT_PROMO_CAPTURE = 12;
    public static final int BISHOP_PROMO_CAPTURE = 13;
    public static final int ROOK_PROMO_CAPTURE = 14;
    public static final int QUEEN_PROMO_CAPTURE = 15;

    public static final int NORTH = -8;
    public static final int SOUTH = 8;
    public static final int WEST = -1;
    public static final int EAST = 1;
    public static final int NORTH_WEST = -9;
    public static final int NORTH_EAST = -7;
    public static final int SOUTH_WEST = 7;
    public static final int SOUTH_EAST = 9;

    public static final String PIECES = "PNBRQKpnbrqk";

    private static class StateInfo {

        private int turn;
        private int castlingAvailability;
        private int enPassantSquare;
        private int rule50;
        private int gamePly;
        private int pliesFromNull;
        private int lastCaptured;
        private int repetition;
        private int latestMove;
        private boolean kingInCheck;
        private StateInfo previousInfo;

        private StateInfo copy() {
            StateInfo copy = new StateInfo();

            copy.turn = turn;
            copy.castlingAvailability = castlingAvailability;
            copy.enPassantSquare = enPassantSquare;
            copy.rule50 = rule50;
            copy.gamePly = gamePly;
            copy.pliesFromNull = pliesFromNull;
            copy.lastCaptured = lastCaptured;
            copy.repetition = repetition;
            copy.kingInCheck = kingInCheck;
            copy.previousInfo = previousInfo;

            return copy;
        }
    }

    private static final long[] VERTICAL = {
        -9187201950435737472L, 4629771061636907072L, 2314885530818453536L, 1157442765409226768L, 578721382704613384L, 289360691352306692L, 144680345676153346L, 72340172838076673L
    };

    private static final long[] HORIZONTAL = {
        -72057594037927936L, 71776119061217280L, 280375465082880L, 1095216660480L, 4278190080L, 16711680L, 65280L, 255L
    };

    private static final long[] DIAGONAL = {
        72057594037927936L, 144396663052566528L, 288794425616760832L, 577588855528488960L, 1155177711073755136L, 2310355422147575808L, 4620710844295151872L, -9205322385119247871L, 36099303471055874L, 141012904183812L, 550831656968L, 2151686160L, 8405024L, 32832L, 128L
    };

    private static final long[] ANTI_DIAGONAL = {
        -9223372036854775808L, 4647714815446351872L, 2323998145211531264L, 1161999622361579520L, 580999813328273408L, 290499906672525312L, 145249953336295424L, 72624976668147840L, 283691315109952L, 1108169199648L, 4328785936L, 16909320L, 66052L, 258L, 1L
    };

    private static final int[] CASTLING_RIGHTS_UPDATE = {
        7, 15, 15, 15, 3, 15, 15, 11,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        13, 15, 15, 15, 12, 15, 15, 14
    };

    private static final int[] BIT_POSITION = {
        63, 62, 15, 61, 6, 14, 35, 60,
        2, 5, 13, 21, 25, 34, 46, 59,
        1, 8, 4, 27, 10, 12, 20, 41,
        18, 24, 30, 33, 39, 45, 51, 58,
        0, 16, 7, 36, 3, 22, 26, 47,
        9, 28, 11, 42, 19, 31, 40, 52,
        17, 37, 23, 48, 29, 43, 32, 53,
        38, 49, 44, 54, 50, 55, 56, 57
    };

    private static final int[] BISHOP_RELEVANT_BITS = {
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };

    private static final int[] ROOK_RELEVANT_BITS = {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };

    private static final long[] BISHOP_MASK = {
        18049651735527936L, 9024825867763712L, 22526811443298304L, 11333774449049600L, 5667157807464448L, 2832480465846272L, 1134765260406784L, 567382630219776L,
        70506452091904L, 35253226045952L, 87995357200384L, 44272556441600L, 22137335185408L, 11064376819712L, 4432676798464L, 2216338399232L,
        18014673925310464L, 9007336962655232L, 22518341868716544L, 11259172008099840L, 5629586008178688L, 2814792987328512L, 1125917221986304L, 562958610993152L,
        9077569074761728L, 4538784537380864L, 2339762086609920L, 19184279556981248L, 9592139778506752L, 4514594912477184L, 2256197927833600L, 1128098963916800L,
        4539058881568768L, 2269529440784384L, 1135039602493440L, 637888545440768L, 18051867805491712L, 9024834391117824L, 4512412900526080L, 2256206450263040L,
        2269530512441344L, 1134765256220672L, 567383701868544L, 2491752130560L, 70515108615168L, 18049651768822272L, 9024825867633664L, 4512412933816832L,
        1134765260406784L, 567382630203392L, 2216342585344L, 9733406720L, 275449643008L, 70506452221952L, 18049651735527424L, 9024825867763712L,
        567382630219776L, 2216338399232L, 8657588224L, 38021120L, 1075975168L, 275415828992L, 70506452091904L, 18049651735527936L
    };

    private static final long[] ROOK_MASK = {
        9115426935197958144L, 4485655873561051136L, 6782456361169985536L, 7930856604974452736L, 8505056726876686336L, 8792156787827803136L, 8935706818303361536L, 9079539427579068672L,
        35607136465616896L, 17522093256097792L, 26493970160820224L, 30979908613181440L, 33222877839362048L, 34344362452452352L, 34905104758997504L, 35466950888980736L,
        36167887395782656L, 18082844186263552L, 9110691325681664L, 4624614895390720L, 2381576680245248L, 1260057572672512L, 699298018886144L, 420017753620736L,
        36170077829103616L, 18085034619584512L, 9042787892731904L, 4521664529305600L, 2261102847592448L, 1130822006735872L, 565681586307584L, 283115671060736L,
        36170086385483776L, 18085043175964672L, 9042522644946944L, 4521262379438080L, 2260632246683648L, 1130317180306432L, 565159647117824L, 282580897300736L,
        36170086418907136L, 18085043209388032L, 9042521608822784L, 4521260808540160L, 2260630408398848L, 1130315208328192L, 565157608292864L, 282578808340736L,
        36170086419037696L, 18085043209518592L, 9042521604775424L, 4521260802403840L, 2260630401218048L, 1130315200625152L, 565157600328704L, 282578800180736L,
        36170086419038334L, 18085043209519166L, 9042521604759646L, 4521260802379886L, 2260630401190006L, 1130315200595066L, 565157600297596L, 282578800148862L
    };

    private static final long[] BISHOP_MAGIC = {
        4629709285037850944L, -9223366504628088312L, 5120592795723629060L, 141132759761920L, 2809531631617L, 2323857426010866752L, -9115265845842669039L, -8070375748268440573L,
        729591945394932208L, 289361241109169185L, 576674624898105346L, 814043259463794760L, 4611686031451234304L, -4604648042216029560L, 285875238076552L, 180429926880019456L,
        1200284894408737280L, 9020774446793216L, 1171503285509588000L, 1607786175106434080L, 72767062639902848L, 227714359982309504L, 145242204961653248L, 217581843845750784L,
        77687645478069248L, 1160055277781910272L, 9016168220229708L, 2253999105640704L, -9079045707632868864L, 18581952667975808L, 2305993229993264129L, 1157427477214136704L,
        4630844192499893312L, 2269941790016896L, 4645438808408640L, -9223301392091774904L, 5305526234070122624L, 577025901297665024L, 335555555702620672L, -9178318447857819360L,
        4688328352334464L, 72198383100019200L, 72092933297801216L, 145243313063067780L, 181270023015268362L, 4504291184353348L, 1154047415589012480L, 23094161629512708L,
        578853857170436096L, 1127072567279620L, 142941075542032L, -9151313273503481823L, 295296215052336L, 2310351024211235200L, 90081342825464064L, 5188181990817464384L,
        19550727835712L, -8891776217657637888L, 1815523701698068480L, 20345638046687496L, 1130317352288256L, -9218867672718753787L, 11261369894764544L, -9222799912716894168L
    };

    private static final long[] ROOK_MAGIC = {
        577622971529822346L, -9177773056250871614L, 9289044001554457L, 108649377788087474L, 144222137057542281L, 281509873324865L, 4616471658893414435L, 2918332662156394753L,
        237601719821861376L, -9214356032644381696L, -6915136473159040896L, 2306124554117842176L, 4755810004744798336L, 306807811055059456L, 2305913378494775424L, 289391737490311680L,
        292738650868154372L, 369442386345992L, 1130297986941056L, -8934578641954013152L, 4507997808115776L, 36592022529900560L, 87961467174912L, 9148211623198721L,
        4616196782093764691L, 10141964041191682L, 4652359161160401921L, -9221111438792195072L, 166650780554627073L, 18296010941927488L, 351912452956161L, 108086528500056064L,
        1026828420211839212L, -2015079190767074816L, -8060878181868765056L, 4620697617876648064L, 577041313770766464L, 297237854581428480L, 72092800958877700L, 432629246819582080L,
        72059793065443476L, 2306427950205506049L, 141287311278592L, 2252349636640776L, -9207608888267898750L, 145685559123976L, 2886816433189167104L, 2305984296462057508L,
        -8933452804333108157L, 324540665360810240L, 2306124518583959808L, 11822086628574224L, 42221281958957568L, 563044451238432L, 422762757767168L, 1153062311359954944L,
        36048038474664192L, 216173624212587008L, 4683744712111293440L, -8214548110420475392L, 5368303984333303810L, 1801449747694755840L, 54043401688973380L, 4683761490266956032L
    };

    private static final long[][] PAWN_ATTACKS = {
        {
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            4611686018427387904L, -6917529027641081856L, 5764607523034234880L, 2882303761517117440L, 1441151880758558720L, 720575940379279360L, 360287970189639680L, 144115188075855872L,
            18014398509481984L, 45035996273704960L, 22517998136852480L, 11258999068426240L, 5629499534213120L, 2814749767106560L, 1407374883553280L, 562949953421312L,
            70368744177664L, 175921860444160L, 87960930222080L, 43980465111040L, 21990232555520L, 10995116277760L, 5497558138880L, 2199023255552L,
            274877906944L, 687194767360L, 343597383680L, 171798691840L, 85899345920L, 42949672960L, 21474836480L, 8589934592L,
            1073741824L, 2684354560L, 1342177280L, 671088640L, 335544320L, 167772160L, 83886080L, 33554432L,
            4194304L, 10485760L, 5242880L, 2621440L, 1310720L, 655360L, 327680L, 131072L,
            16384L, 40960L, 20480L, 10240L, 5120L, 2560L, 1280L, 512L
        },

        {
            18014398509481984L, 45035996273704960L, 22517998136852480L, 11258999068426240L, 5629499534213120L, 2814749767106560L, 1407374883553280L, 562949953421312L,
            70368744177664L, 175921860444160L, 87960930222080L, 43980465111040L, 21990232555520L, 10995116277760L, 5497558138880L, 2199023255552L,
            274877906944L, 687194767360L, 343597383680L, 171798691840L, 85899345920L, 42949672960L, 21474836480L, 8589934592L,
            1073741824L, 2684354560L, 1342177280L, 671088640L, 335544320L, 167772160L, 83886080L, 33554432L,
            4194304L, 10485760L, 5242880L, 2621440L, 1310720L, 655360L, 327680L, 131072L,
            16384L, 40960L, 20480L, 10240L, 5120L, 2560L, 1280L, 512L,
            64L, 160L, 80L, 40L, 20L, 10L, 5L, 2L,
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L
        }
    };

    private static final long[] KNIGHT_ATTACKS = {
        9077567998918656L, 4679521487814656L, 38368557762871296L, 19184278881435648L, 9592139440717824L, 4796069720358912L, 2257297371824128L, 1128098930098176L,
        2305878468463689728L, 1152939783987658752L, -8646761407372591104L, 4899991333168480256L, 2449995666584240128L, 1224997833292120064L, 576469569871282176L, 288234782788157440L,
        4620693356194824192L, -6913025356609880064L, 5802888705324613632L, 2901444352662306816L, 1450722176331153408L, 725361088165576704L, 362539804446949376L, 145241105196122112L,
        18049583422636032L, 45053588738670592L, 22667534005174272L, 11333767002587136L, 5666883501293568L, 2833441750646784L, 1416171111120896L, 567348067172352L,
        70506185244672L, 175990581010432L, 88545054707712L, 44272527353856L, 22136263676928L, 11068131838464L, 5531918402816L, 2216203387392L,
        275414786112L, 687463207072L, 345879119952L, 172939559976L, 86469779988L, 43234889994L, 21609056261L, 8657044482L,
        1075839008L, 2685403152L, 1351090312L, 675545156L, 337772578L, 168886289L, 84410376L, 33816580L,
        4202496L, 10489856L, 5277696L, 2638848L, 1319424L, 659712L, 329728L, 132096L
    };

    private static final long[][] BISHOP_ATTACKS = new long[64][512];

    private static final long[][] ROOK_ATTACKS = new long[64][4096];

    private static final long[] KING_ATTACKS = {
        4665729213955833856L, -6854478632857894912L, 5796132720425828352L, 2898066360212914176L, 1449033180106457088L, 724516590053228544L, 362258295026614272L, 144959613005987840L,
        -4593460513685372928L, -2260560722335367168L, 8093091675687092224L, 4046545837843546112L, 2023272918921773056L, 1011636459460886528L, 505818229730443264L, 216739030602088448L,
        54114388906344448L, 63227278716305408L, 31613639358152704L, 15806819679076352L, 7903409839538176L, 3951704919769088L, 1975852459884544L, 846636838289408L,
        211384331665408L, 246981557485568L, 123490778742784L, 61745389371392L, 30872694685696L, 15436347342848L, 7718173671424L, 3307175149568L,
        825720045568L, 964771708928L, 482385854464L, 241192927232L, 120596463616L, 60298231808L, 30149115904L, 12918652928L,
        3225468928L, 3768639488L, 1884319744L, 942159872L, 471079936L, 235539968L, 117769984L, 50463488L,
        12599488L, 14721248L, 7360624L, 3680312L, 1840156L, 920078L, 460039L, 197123L,
        49216L, 57504L, 28752L, 14376L, 7188L, 3594L, 1797L, 770L
    };

    private static final long[] DEFAULT_BITBOARDS = new long[] { 65280L, 66L, 36L, 129L, 16L, 8L, 71776119061217280L, 4755801206503243776L, 2594073385365405696L, -9151314442816847872L, 1152921504606846976L, 576460752303423488L };
    private static final long[] DEFAULT_OCCUPANCIES = new long[] { 65535L, -281474976710656L, -281474976645121L };

    private static final long[][] LINE = new long[64][64];
    private static final long[][] BETWEEN = new long[64][64];
    private static final long[][] CHECK_MASK = new long[64][64];

    public final long[] bitboards = new long[12];
    public final long[] occupancies = new long[3];

    private StateInfo info = new StateInfo();

    static {
        for (int s1 = 0; s1 < 64; s1++) {
            //BISHOP_MAGIC[s1] = Magic.findMagicNumber(s1, true);
            //ROOK_MAGIC[s1] = Magic.findMagicNumber(s1, false);

            initializeSlidingAttacks(s1, BISHOP_RELEVANT_BITS[s1], BISHOP_MASK[s1], BISHOP_MAGIC[s1], BISHOP_ATTACKS, true);
            initializeSlidingAttacks(s1, ROOK_RELEVANT_BITS[s1], ROOK_MASK[s1], ROOK_MAGIC[s1], ROOK_ATTACKS, false);

            long knightAttacks = KNIGHT_ATTACKS[s1];

            for (int s2 = 0; s2 < 64; s2++) {
                LINE[s1][s2] = setLineThrough(s1, s2);
                BETWEEN[s1][s2] = setBitsBetween(s1, s2);
                CHECK_MASK[s1][s2] = BETWEEN[s1][s2] | knightAttacks & SQ1 >>> s2;
            }
        }
    }

    public Bitboard() {
        loadDefaultPosition();
    }

    public Bitboard(Bitboard position) {
        loadPosition(position);
    }

    public Bitboard(String fen) {
        loadPosition(fen);
    }

    private void updateOccupancies() {
        occupancies[WHITE] = bitboards[WHITE_PAWN] | bitboards[WHITE_KNIGHT] | bitboards[WHITE_BISHOP] | bitboards[WHITE_ROOK] | bitboards[WHITE_QUEEN] | bitboards[WHITE_KING];
        occupancies[BLACK] = bitboards[BLACK_PAWN] | bitboards[BLACK_KNIGHT] | bitboards[BLACK_BISHOP] | bitboards[BLACK_ROOK] | bitboards[BLACK_QUEEN] | bitboards[BLACK_KING];
        occupancies[BOTH] = occupancies[WHITE] | occupancies[BLACK];
    }

    public void loadDefaultPosition() {
        System.arraycopy(DEFAULT_BITBOARDS, 0, bitboards, 0, 12);
        System.arraycopy(DEFAULT_OCCUPANCIES, 0, occupancies, 0, 3);

        info.turn = WHITE;
        info.castlingAvailability = 15;
        info.enPassantSquare = -1;
        info.rule50 = 0;
        info.gamePly = 0;
        info.pliesFromNull = 0;
        info.lastCaptured = -1;
        info.repetition = 0;
        info.kingInCheck = false;
        info.previousInfo = null;
    }

    public void loadPosition(Bitboard position) {
        System.arraycopy(position.bitboards, 0, bitboards, 0, 12);
        System.arraycopy(position.occupancies, 0, occupancies, 0, 3);

        info = position.info.copy();
    }

    public void loadPosition(String fen) {
        String[] notation = fen.split(" ");

        String[] ranks = notation[0].split("/");

        Arrays.fill(bitboards, 0L);
        Arrays.fill(occupancies, 0L);

        for (int rank = 0; rank < 8; rank++) {
            int file = 0;

            for (int i = 0; i < ranks[rank].length(); i++) {
                char ch = ranks[rank].charAt(i);

                if (Character.isDigit(ch)) {
                    file += ch - '0';
                } else {
                    int piece = PIECES.indexOf(ch);
                    bitboards[piece] = setBit(bitboards[piece], rank * 8 + file);
                    file++;
                }
            }
        }

        updateOccupancies();

        info.turn = notation[1].equals("w") ? WHITE : BLACK;
        info.castlingAvailability = parseCastling(notation[2]);
        info.enPassantSquare = parseSquare(notation[3]);
        info.rule50 = Integer.parseInt(notation[4]);
        info.gamePly = Integer.parseInt(notation[5]) * 2 - info.turn;
        info.pliesFromNull = 0;
        info.lastCaptured = -1;
        info.repetition = 0;
        info.kingInCheck = getAttackers(lowestOneBitIndex(bitboards[WHITE_KING]), BLACK) != 0L ||
            getAttackers(lowestOneBitIndex(bitboards[BLACK_KING]), WHITE) != 0L;;
        info.previousInfo = null;
    }

    public void makeMove(int move) {
        int sourceSquare = sourceSquare(move);
        int targetSquare = targetSquare(move);
        int moveKind = moveKind(move);

        long sourceBit = SQ1 >>> sourceSquare;
        long targetBit = SQ1 >>> targetSquare;

        int movingPiece = getPiece(sourceSquare);
        int capturedPiece = getPiece(targetSquare);

        info.previousInfo = info.copy();
        info.turn ^= 1;
        info.castlingAvailability &= CASTLING_RIGHTS_UPDATE[sourceSquare] & CASTLING_RIGHTS_UPDATE[targetSquare];
        info.enPassantSquare = -1;
        info.rule50 = (movingPiece == WHITE_PAWN || movingPiece == BLACK_PAWN) ? 0 : info.rule50 + 1;
        info.gamePly++;
        info.pliesFromNull++;
        info.lastCaptured = capturedPiece;
        //info.repetition
        info.latestMove = move;
        info.kingInCheck = false;

        bitboards[movingPiece] ^= sourceBit | targetBit;

        switch (moveKind) {
            case DOUBLE_PAWN_PUSH -> info.enPassantSquare = (targetSquare + sourceSquare) >> 1;

            case KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE -> bitboards[movingPiece - 2] ^= targetBit >> 1 | targetBit << moveKind - 1;

            case ENPASSANT_CAPTURE -> bitboards[info.lastCaptured = movingPiece == WHITE_PAWN ? BLACK_PAWN : WHITE_PAWN] ^= SQ1 >>> (sourceSquare & 56 | targetSquare & 7);

            default -> {
                if (isCapture(moveKind)) {
                    info.rule50 = 0;
                    bitboards[capturedPiece] ^= targetBit;
                }

                if (isPromotion(moveKind)) {
                    bitboards[movingPiece] ^= targetBit;
                    bitboards[(moveKind & 3) + 7 - 6 * info.turn] |= targetBit;
                }
            }
        }

        updateOccupancies();
    }

    public void undoMove() {
        int move = info.latestMove;

        int sourceSquare = sourceSquare(move);
        int targetSquare = targetSquare(move);
        int moveKind = moveKind(move);

        int movedPiece = getPiece(targetSquare);

        long sourceBit = SQ1 >>> sourceSquare;
        long targetBit = SQ1 >>> targetSquare;

        bitboards[movedPiece] ^= sourceBit | targetBit;

        switch (moveKind) {
            case KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE -> bitboards[movedPiece - 2] ^= targetBit >> 1 | targetBit << moveKind - 1;

            case ENPASSANT_CAPTURE -> bitboards[info.lastCaptured] |= SQ1 >>> (sourceSquare & 56 | targetSquare & 7);

            default -> {
                if (isCapture(moveKind)) {
                    bitboards[info.lastCaptured] |= targetBit;
                }

                if (isPromotion(moveKind)) {
                    bitboards[movedPiece] ^= sourceBit;
                    bitboards[info.turn == BLACK ? WHITE_PAWN : BLACK_PAWN] |= sourceBit;
                }
            }
        }

        info = info.previousInfo;
    }

    public int[] generateCaptures() {
        int[] moves = new int[MAX_LEGAL_CAPTURES];
        int moveIndex = 0;

        int us = info.turn;
        int opponent = us ^ 1;

        int bitboardIndex = 6 * us;
        int opponentBitboardIndex = 6 * opponent;

        long occupied = occupancies[BOTH];
        long enemies = occupancies[opponent];

        int kingSquare = lowestOneBitIndex(bitboards[bitboardIndex + KING]);

        long checkers = getAttackers(kingSquare, opponent);

        boolean isInCheck = checkers != 0L;

        if (sparseBitCount(checkers) < 2) {
            long allies = occupancies[us];

            long enemyQueens = bitboards[opponentBitboardIndex + QUEEN];
            long enemyBishopsQueens = bitboards[opponentBitboardIndex + BISHOP] | enemyQueens;
            long enemyRooksQueens = bitboards[opponentBitboardIndex + ROOK] | enemyQueens;

            long movable = isInCheck ? checkers : enemies;

            long candidates = (bishopAttacks(kingSquare, enemies) & enemyBishopsQueens |
                rookAttacks(kingSquare, enemies) & enemyRooksQueens) ^ checkers;

            long pins = 0;

            while (candidates != 0) {
                int square = lowestOneBitIndex(candidates);
                candidates &= candidates - 1;

                long between = BETWEEN[kingSquare][square];
                long mask = between & allies;

                if ((mask & mask - 1) == 0) {
                    pins |= between;
                }
            }

            long pinsD1 = pins & lineThrough(kingSquare, NORTH_WEST);
            long pinsD2 = pins & lineThrough(kingSquare, NORTH_EAST);

            long pinsHV = pins & (lineThrough(kingSquare, WEST) | lineThrough(kingSquare, NORTH));
            long pinsD12 = pinsD1 | pinsD2;
            long notPinned = ~pins;

            long pawns = bitboards[bitboardIndex++];
            long knights = bitboards[bitboardIndex++] & notPinned;
            long bishops = bitboards[bitboardIndex++];
            long rooks = bitboards[bitboardIndex++];
            long queens = bitboards[bitboardIndex];

            long freePawns = pawns & notPinned;

            long pawnsCanTakeLeft = freePawns | pinsD1 & pawns;
            long pawnsCanTakeRight = freePawns | pinsD2 & pawns;

            long bishopsQueens = bishops | queens;
            long rooksQueens = rooks | queens;

            long freeBishopsQueens = bishopsQueens & notPinned;
            long freeRooksQueens = rooksQueens & notPinned;
            long pinnedD12BishopsQueens = bishopsQueens & pinsD12;
            long pinnedHVRooksQueens = rooksQueens & pinsHV;

            int left = us == WHITE ? NORTH_WEST : SOUTH_EAST;
            int right = us == WHITE ? NORTH_EAST : SOUTH_WEST;

            long lastRank = us == WHITE ? HORIZONTAL[0] : HORIZONTAL[7];

            long leftAttacks = shift(pawnsCanTakeLeft, left) & movable;
            long rightAttacks = shift(pawnsCanTakeRight, right) & movable;

            long attackLeftPromotions = leftAttacks & lastRank;
            long attackRightPromotions = rightAttacks & lastRank;

            leftAttacks &= ~lastRank;
            rightAttacks &= ~lastRank;

            int epSquare = info.enPassantSquare;

            if (epSquare != -1) {
                int forward = us == WHITE ? NORTH : SOUTH;

                long enPassant = SQ1 >>> epSquare;
                long pawnsCanEnPassant = pawns & PAWN_ATTACKS[opponent][epSquare];

                while (pawnsCanEnPassant != 0) {
                    int sourceSquare = lowestOneBitIndex(pawnsCanEnPassant);

                    long movingPawn = SQ1 >>> sourceSquare;
                    long noEnPassantPawns = occupied ^ movingPawn ^ shift(enPassant, -forward) ^ enPassant;
                    pawnsCanEnPassant ^= movingPawn;

                    if ((rookAttacks(kingSquare, noEnPassantPawns) & enemyRooksQueens) == 0L && (bishopAttacks(kingSquare, noEnPassantPawns) & enemyBishopsQueens) == 0L)
                        moves[moveIndex++] = encodeMove(sourceSquare, epSquare, ENPASSANT_CAPTURE);
                }
            }

            while (attackLeftPromotions != 0) {
                int targetSquare = lowestOneBitIndex(attackLeftPromotions);
                int sourceSquare = targetSquare - left;

                attackLeftPromotions &= attackLeftPromotions - 1;

                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUEEN_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, ROOK_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, BISHOP_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, KNIGHT_PROMO_CAPTURE);
            }

            while (attackRightPromotions != 0) {
                int targetSquare = lowestOneBitIndex(attackRightPromotions);
                int sourceSquare = targetSquare - right;

                attackRightPromotions &= attackRightPromotions - 1;

                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUEEN_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, ROOK_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, BISHOP_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, KNIGHT_PROMO_CAPTURE);
            }

            while (leftAttacks != 0) {
                int targetSquare = lowestOneBitIndex(leftAttacks);
                int sourceSquare = targetSquare - left;

                leftAttacks &= leftAttacks - 1;

                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
            }

            while (rightAttacks != 0) {
                int targetSquare = lowestOneBitIndex(rightAttacks);
                int sourceSquare = targetSquare - right;

                rightAttacks &= rightAttacks - 1;

                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
            }

            while (knights != 0) {
                int sourceSquare = lowestOneBitIndex(knights);
                long captures = KNIGHT_ATTACKS[sourceSquare] & movable;

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                knights &= knights - 1;
            }

            while (freeBishopsQueens != 0) {
                int sourceSquare = lowestOneBitIndex(freeBishopsQueens);
                long captures = bishopAttacks(sourceSquare, occupied) & movable;

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                freeBishopsQueens &= freeBishopsQueens - 1;
            }

            while (freeRooksQueens != 0) {
                int sourceSquare = lowestOneBitIndex(freeRooksQueens);
                long captures = rookAttacks(sourceSquare, occupied) & movable;

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                freeRooksQueens &= freeRooksQueens - 1;
            }

            while (pinnedD12BishopsQueens != 0) {
                int sourceSquare = lowestOneBitIndex(pinnedD12BishopsQueens);
                long captures = bishopAttacks(sourceSquare, occupied) & movable & pinsD12;

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                pinnedD12BishopsQueens &= pinnedD12BishopsQueens - 1;
            }

            while (pinnedHVRooksQueens != 0) {
                int sourceSquare = lowestOneBitIndex(pinnedHVRooksQueens);
                long captures = rookAttacks(sourceSquare, occupied) & movable & pinsHV;

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                pinnedHVRooksQueens &= pinnedHVRooksQueens - 1;
            }
        }

        long kingCaptures = KING_ATTACKS[kingSquare] & enemies;

        if (kingCaptures != 0) {
            long safeSquares = 0L;

            long occupancyNoKing = popBit(occupied, kingSquare);

            for (int pieceType = PAWN; pieceType <= KING; pieceType++, opponentBitboardIndex++) {
                long bitboard = bitboards[opponentBitboardIndex];

                while (bitboard != 0) {
                    int square = lowestOneBitIndex(bitboard);

                    switch (pieceType) {
                        case PAWN -> safeSquares |= PAWN_ATTACKS[opponent][square];
                        case KNIGHT -> safeSquares |= KNIGHT_ATTACKS[square];
                        case BISHOP -> safeSquares |= bishopAttacks(square, occupancyNoKing);
                        case ROOK -> safeSquares |= rookAttacks(square, occupancyNoKing);
                        case QUEEN -> safeSquares |= queenAttacks(square, occupancyNoKing);
                        case KING -> safeSquares |= KING_ATTACKS[square];
                    }

                    bitboard &= bitboard - 1;
                }
            }

            safeSquares = ~safeSquares;

            kingCaptures &= safeSquares;

            while (kingCaptures != 0) {
                int targetSquare = lowestOneBitIndex(kingCaptures);
                moves[moveIndex++] = encodeMove(kingSquare, targetSquare, CAPTURE);
                kingCaptures &= kingCaptures - 1;
            }
        }

        info.kingInCheck = isInCheck;

        return Arrays.copyOfRange(moves, 0, moveIndex);
    }

    public int[] generateAllMoves() {
        int[] moves = new int[MAX_LEGAL_MOVES];
        int moveIndex = 0;

        int us = info.turn;
        int opponent = us ^ 1;

        int bitboardIndex = 6 * us;
        int opponentBitboardIndex = 6 * opponent;

        long occupied = occupancies[BOTH];
        long enemies = occupancies[opponent];
        long empty = ~occupied;
        long emptyOrEnemies = empty | enemies;

        int kingSquare = lowestOneBitIndex(bitboards[bitboardIndex + KING]);

        long checkers = getAttackers(kingSquare, opponent);

        boolean isInCheck = checkers != 0L;

        if (sparseBitCount(checkers) < 2) {
            long allies = occupancies[us];

            long enemyQueens = bitboards[opponentBitboardIndex + QUEEN];
            long enemyBishopsQueens = bitboards[opponentBitboardIndex + BISHOP] | enemyQueens;
            long enemyRooksQueens = bitboards[opponentBitboardIndex + ROOK] | enemyQueens;

            long checkMask = isInCheck ? CHECK_MASK[kingSquare][lowestOneBitIndex(checkers)] : ALL_BITS_SET;

            long candidates = (bishopAttacks(kingSquare, enemies) & enemyBishopsQueens |
                rookAttacks(kingSquare, enemies) & enemyRooksQueens) ^ checkers;

            long pins = 0;

            while (candidates != 0) {
                int square = lowestOneBitIndex(candidates);
                candidates &= candidates - 1;

                long between = BETWEEN[kingSquare][square];
                long mask = between & allies;

                if ((mask & mask - 1) == 0) {
                    pins |= between;
                }
            }

            long pinsV = pins & lineThrough(kingSquare, NORTH);
            long pinsD1 = pins & lineThrough(kingSquare, NORTH_WEST);
            long pinsD2 = pins & lineThrough(kingSquare, NORTH_EAST);

            long pinsHV = pins & lineThrough(kingSquare, WEST) | pinsV;
            long pinsD12 = pinsD1 | pinsD2;
            long notPinned = ~pins;

            long pawns = bitboards[bitboardIndex++];
            long knights = bitboards[bitboardIndex++] & notPinned;
            long bishops = bitboards[bitboardIndex++];
            long rooks = bitboards[bitboardIndex++];
            long queens = bitboards[bitboardIndex];

            long freePawns = pawns & notPinned;

            long pawnsCanPush = freePawns | pinsV & pawns;
            long pawnsCanTakeLeft = freePawns | pinsD1 & pawns;
            long pawnsCanTakeRight = freePawns | pinsD2 & pawns;

            long bishopsQueens = bishops | queens;
            long rooksQueens = rooks | queens;

            long freeBishopsQueens = bishopsQueens & notPinned;
            long freeRooksQueens = rooksQueens & notPinned;
            long pinnedD12BishopsQueens = bishopsQueens & pinsD12;
            long pinnedHVRooksQueens = rooksQueens & pinsHV;

            int forward = us == WHITE ? NORTH : SOUTH;
            int left = us == WHITE ? NORTH_WEST : SOUTH_EAST;
            int right = us == WHITE ? NORTH_EAST : SOUTH_WEST;

            long lastRank = us == WHITE ? HORIZONTAL[0] : HORIZONTAL[7];

            long singlePushes = shift(pawnsCanPush, forward) & empty;
            long doublePushes = shift(singlePushes & (us == WHITE ? HORIZONTAL[5] : HORIZONTAL[2]), forward) & empty & checkMask;
            long leftAttacks = shift(pawnsCanTakeLeft, left) & enemies & checkMask;
            long rightAttacks = shift(pawnsCanTakeRight, right) & enemies & checkMask;

            singlePushes &= checkMask;

            long pushPromotions = singlePushes & lastRank;
            long attackLeftPromotions = leftAttacks & lastRank;
            long attackRightPromotions = rightAttacks & lastRank;

            singlePushes &= ~lastRank;
            leftAttacks &= ~lastRank;
            rightAttacks &= ~lastRank;

            int epSquare = info.enPassantSquare;

            if (epSquare != -1) {
                long enPassant = SQ1 >>> epSquare;
                long pawnsCanEnPassant = pawns & PAWN_ATTACKS[opponent][epSquare];

                while (pawnsCanEnPassant != 0) {
                    int sourceSquare = lowestOneBitIndex(pawnsCanEnPassant);

                    long movingPawn = SQ1 >>> sourceSquare;
                    long noEnPassantPawns = occupied ^ movingPawn ^ shift(enPassant, -forward) ^ enPassant;
                    pawnsCanEnPassant ^= movingPawn;

                    if ((rookAttacks(kingSquare, noEnPassantPawns) & enemyRooksQueens) == 0L && (bishopAttacks(kingSquare, noEnPassantPawns) & enemyBishopsQueens) == 0L)
                        moves[moveIndex++] = encodeMove(sourceSquare, epSquare, ENPASSANT_CAPTURE);
                }
            }

            while (pushPromotions != 0) {
                int targetSquare = lowestOneBitIndex(pushPromotions);
                int sourceSquare = targetSquare - forward;
                pushPromotions &= pushPromotions - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, KNIGHT_PROMOTION);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, BISHOP_PROMOTION);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, ROOK_PROMOTION);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUEEN_PROMOTION);
            }

            while (attackLeftPromotions != 0) {
                int targetSquare = lowestOneBitIndex(attackLeftPromotions);
                int sourceSquare = targetSquare - left;
                attackLeftPromotions &= attackLeftPromotions - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, KNIGHT_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, BISHOP_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, ROOK_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUEEN_PROMO_CAPTURE);
            }

            while (attackRightPromotions != 0) {
                int targetSquare = lowestOneBitIndex(attackRightPromotions);
                int sourceSquare = targetSquare - right;
                attackRightPromotions &= attackRightPromotions - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, KNIGHT_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, BISHOP_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, ROOK_PROMO_CAPTURE);
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUEEN_PROMO_CAPTURE);
            }

            while (singlePushes != 0) {
                int targetSquare = lowestOneBitIndex(singlePushes);
                int sourceSquare = targetSquare - forward;
                singlePushes &= singlePushes - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
            }

            while (doublePushes != 0) {
                int targetSquare = lowestOneBitIndex(doublePushes);
                int sourceSquare = targetSquare - forward - forward;
                doublePushes &= doublePushes - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, DOUBLE_PAWN_PUSH);
            }

            while (leftAttacks != 0) {
                int targetSquare = lowestOneBitIndex(leftAttacks);
                int sourceSquare = targetSquare - left;
                leftAttacks &= leftAttacks - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
            }

            while (rightAttacks != 0) {
                int targetSquare = lowestOneBitIndex(rightAttacks);
                int sourceSquare = targetSquare - right;
                rightAttacks &= rightAttacks - 1;
                moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
            }

            while (knights != 0) {
                int sourceSquare = lowestOneBitIndex(knights);

                long attacks = KNIGHT_ATTACKS[sourceSquare] & emptyOrEnemies & checkMask;

                long quietMoves = attacks & empty;
                long captures = attacks & enemies;

                while (quietMoves != 0) {
                    int targetSquare = lowestOneBitIndex(quietMoves);
                    quietMoves &= quietMoves - 1;
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
                }

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    captures &= captures - 1;
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                }

                knights &= knights - 1;
            }

            while (freeBishopsQueens != 0) {
                int sourceSquare = lowestOneBitIndex(freeBishopsQueens);
                long attacks = bishopAttacks(sourceSquare, occupied) & emptyOrEnemies & checkMask;

                long quietMoves = attacks & empty;
                long captures = attacks & enemies;

                while (quietMoves != 0) {
                    int targetSquare = lowestOneBitIndex(quietMoves);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
                    quietMoves &= quietMoves - 1;
                }

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                freeBishopsQueens &= freeBishopsQueens - 1;
            }

            while (freeRooksQueens != 0) {
                int sourceSquare = lowestOneBitIndex(freeRooksQueens);
                long attacks = rookAttacks(sourceSquare, occupied) & emptyOrEnemies & checkMask;

                long quietMoves = attacks & empty;
                long captures = attacks & enemies;

                while (quietMoves != 0) {
                    int targetSquare = lowestOneBitIndex(quietMoves);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
                    quietMoves &= quietMoves - 1;
                }

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                freeRooksQueens &= freeRooksQueens - 1;
            }

            while (pinnedD12BishopsQueens != 0) {
                int sourceSquare = lowestOneBitIndex(pinnedD12BishopsQueens);
                long attacks = bishopAttacks(sourceSquare, occupied) & emptyOrEnemies & checkMask & pinsD12;

                long quietMoves = attacks & empty;
                long captures = attacks & enemies;

                while (quietMoves != 0) {
                    int targetSquare = lowestOneBitIndex(quietMoves);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
                    quietMoves &= quietMoves - 1;
                }

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                pinnedD12BishopsQueens &= pinnedD12BishopsQueens - 1;
            }

            while (pinnedHVRooksQueens != 0) {
                int sourceSquare = lowestOneBitIndex(pinnedHVRooksQueens);
                long attacks = rookAttacks(sourceSquare, occupied) & emptyOrEnemies & checkMask & pinsHV;

                long quietMoves = attacks & empty;
                long captures = attacks & enemies;

                while (quietMoves != 0) {
                    int targetSquare = lowestOneBitIndex(quietMoves);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, QUIET_MOVE);
                    quietMoves &= quietMoves - 1;
                }

                while (captures != 0) {
                    int targetSquare = lowestOneBitIndex(captures);
                    moves[moveIndex++] = encodeMove(sourceSquare, targetSquare, CAPTURE);
                    captures &= captures - 1;
                }

                pinnedHVRooksQueens &= pinnedHVRooksQueens - 1;
            }
        }

        long kingAttacks = KING_ATTACKS[kingSquare] & emptyOrEnemies;

        if (kingAttacks != 0) {
            long safeSquares = 0L;

            long occupancyNoKing = popBit(occupied, kingSquare);

            for (int pieceType = PAWN; pieceType <= KING; pieceType++, opponentBitboardIndex++) {
                long bitboard = bitboards[opponentBitboardIndex];

                while (bitboard != 0) {
                    int square = lowestOneBitIndex(bitboard);

                    switch (pieceType) {
                        case PAWN -> safeSquares |= PAWN_ATTACKS[opponent][square];
                        case KNIGHT -> safeSquares |= KNIGHT_ATTACKS[square];
                        case BISHOP -> safeSquares |= bishopAttacks(square, occupancyNoKing);
                        case ROOK -> safeSquares |= rookAttacks(square, occupancyNoKing);
                        case QUEEN -> safeSquares |= queenAttacks(square, occupancyNoKing);
                        case KING -> safeSquares |= KING_ATTACKS[square];
                    }

                    bitboard &= bitboard - 1;
                }
            }

            safeSquares = ~safeSquares;

            kingAttacks &= safeSquares;

            long kingQuietMoves = kingAttacks & empty;
            long kingCaptures = kingAttacks & enemies;

            while (kingQuietMoves != 0) {
                int targetSquare = lowestOneBitIndex(kingQuietMoves);
                kingQuietMoves &= kingQuietMoves - 1;
                moves[moveIndex++] = encodeMove(kingSquare, targetSquare, QUIET_MOVE);
            }

            while (kingCaptures != 0) {
                int targetSquare = lowestOneBitIndex(kingCaptures);
                kingCaptures &= kingCaptures - 1;
                moves[moveIndex++] = encodeMove(kingSquare, targetSquare, CAPTURE);
            }

            if (!isInCheck) {
                int kingSideRights = 1 + 3 * us;
                int queenSideRights = kingSideRights * 2;

                long kingSideEmptyAndSafe = us == WHITE ? 6L : 432345564227567616L;
                long queenSideEmpty = us == WHITE ? 112L : 8070450532247928832L;
                long queenSideSafe = us == WHITE ? 48L : 3458764513820540928L;

                boolean canCastleKingSide = (info.castlingAvailability & kingSideRights) != 0 && (empty & safeSquares & kingSideEmptyAndSafe) == kingSideEmptyAndSafe;
                boolean canCastleQueenSide = (info.castlingAvailability & queenSideRights) != 0 && (empty & queenSideEmpty) == queenSideEmpty && (safeSquares & queenSideSafe) == queenSideSafe;

                if (canCastleKingSide) {
                    moves[moveIndex++] = encodeMove(kingSquare, kingSquare + 2, KING_SIDE_CASTLE);
                }

                if (canCastleQueenSide) {
                    moves[moveIndex++] = encodeMove(kingSquare, kingSquare - 2, QUEEN_SIDE_CASTLE);
                }
            }
        }

        info.kingInCheck = isInCheck;

        return Arrays.copyOfRange(moves, 0, moveIndex);
    }

    public int getPiece(int square) {
        long bit = SQ1 >>> square;

        for (int i = 0; i < 12; i++) {
            if ((bitboards[i] & bit) != 0) {
                return i;
            }
        }

        return -1;
    }

    public long getAttackers(int square, int attackerColor) {
        int pawnIndex = 6 * attackerColor;

        return PAWN_ATTACKS[attackerColor ^ 1][square] & bitboards[pawnIndex] |
            KNIGHT_ATTACKS[square] & bitboards[KNIGHT + pawnIndex] |
            bishopAttacks(square, occupancies[BOTH]) & (bitboards[BISHOP + pawnIndex] | bitboards[QUEEN + pawnIndex]) |
            rookAttacks(square, occupancies[BOTH]) & (bitboards[ROOK + pawnIndex] | bitboards[QUEEN + pawnIndex]) |
            KING_ATTACKS[square] & bitboards[KING + pawnIndex];
    }

    public int getTurn() {
        return info.turn;
    }

    public int getCastlingAvailability() {
        return info.castlingAvailability;
    }

    public int getEnPassantTargetSquare() {
        return info.enPassantSquare;
    }

    public int getHalfmoves() {
        return info.rule50;
    }

    public int getFullmoves() {
        return info.gamePly / 2 + info.turn;
    }

    public boolean isKingInCheck() {
        return info.kingInCheck;
    }

    public String getFEN() {
        StringBuilder fen = new StringBuilder();
        int spaces = 0;

        for (int square = 0; square < 64; square++) {
            if (getBit(occupancies[BOTH], square) == 0) {
                spaces++;
            } else {
                for (int bitboard = 0; bitboard < 12; bitboard++) {
                    if (getBit(bitboards[bitboard], square) != 0) {
                        if (spaces > 0) {
                            fen.append(spaces);
                            spaces = 0;
                        }
                        fen.append(PIECES.charAt(bitboard));
                        break;
                    }
                }
            }

            if ((square & 7) == 7) {
                if (spaces > 0) {
                    fen.append(spaces);
                    spaces = 0;
                }
                if (square != 63) {
                    fen.append('/');
                }
            }
        }

        fen.append(info.turn == WHITE ? " w" : " b");

        fen.append(" ").append(formatCastling(info.castlingAvailability));

        fen.append(" ").append(squareName(info.enPassantSquare));

        fen.append(" ").append(info.rule50).append(" ").append(getFullmoves());

        return fen.toString();
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();

        for (int square = 0; square < 64; square++) {
            boolean empty = true;

            for (int bitboard = 0; bitboard < 12 && empty; bitboard++) {
                if (getBit(bitboards[bitboard], square) != 0) {
                    toReturn.append(PIECES.charAt(bitboard)).append("  ");
                    empty = false;
                }
            }

            if (empty) toReturn.append(".  ");

            if ((square & 7) == 7 && square != 63) toReturn.append("\n");
        }

        return toReturn.toString();
    }

    private static void initializeSlidingAttacks(int square, int relevantBits, long attackMask, long magic, long[][] attackTable, boolean isBishop) {
        int occupancyVariations = 1 << relevantBits;

        for (int index = 0; index < occupancyVariations; index++) {
            long occupancy = setOccupancy(index, relevantBits, attackMask);
            int magicIndex = (int) ((occupancy * magic) >>> (64 - relevantBits));

            attackTable[square][magicIndex] = isBishop ? calculateBishopAttacks(square, occupancy) : calculateRookAttacks(square, occupancy);
        }
    }

    public static Bitboard makeCopyAndMove(Bitboard position, int move) {
        Bitboard board = new Bitboard(position);
        board.makeMove(move);

        return board;
    }

    public static long shift(long bitboard, int direction) {
        return switch (direction) {
            case NORTH -> bitboard << 8;
            case SOUTH -> bitboard >>> 8;
            case WEST -> (bitboard & ~VERTICAL[0]) << 1;
            case EAST -> (bitboard & ~VERTICAL[7]) >>> 1;
            case NORTH_WEST -> (bitboard & ~VERTICAL[0]) << 9;
            case NORTH_EAST -> (bitboard & ~VERTICAL[7]) << 7;
            case SOUTH_WEST -> (bitboard & ~VERTICAL[0]) >>> 7;
            case SOUTH_EAST -> (bitboard & ~VERTICAL[7]) >>> 9;
            default -> bitboard;
        };
    }

    public static long lineThrough(int square, int direction) {
        return switch (direction) {
            case NORTH, SOUTH -> VERTICAL[square & 7];
            case WEST, EAST -> HORIZONTAL[square >>> 3];
            case NORTH_WEST, SOUTH_EAST -> DIAGONAL[(square >>> 3) - (square & 7) + 7];
            case NORTH_EAST, SOUTH_WEST -> ANTI_DIAGONAL[(square >>> 3) + (square & 7)];
            default -> 0L;
        };
    }

    public static long bishopAttacks(int square, long occupancy) {
        occupancy &= BISHOP_MASK[square];
        occupancy *= BISHOP_MAGIC[square];
        occupancy >>>= 64 - BISHOP_RELEVANT_BITS[square];

        return BISHOP_ATTACKS[square][(int) (occupancy)];
    }

    public static long rookAttacks(int square, long occupancy) {
        occupancy &= ROOK_MASK[square];
        occupancy *= ROOK_MAGIC[square];
        occupancy >>>= 64 - ROOK_RELEVANT_BITS[square];

        return ROOK_ATTACKS[square][(int) (occupancy)];
    }

    public static long queenAttacks(int square, long occupancy) {
        return bishopAttacks(square, occupancy) | rookAttacks(square, occupancy);
    }

    public static long xrayBishopAttacks(int square, long occupancy) {
        return bishopAttacks(square, occupancy & ~bishopAttacks(square, occupancy));
    }

    public static long xrayRookAttacks(int square, long occupancy) {
        return rookAttacks(square, occupancy & ~rookAttacks(square, occupancy));
    }

    public static long xrayQueenAttacks(int square, long occupancy) {
        return queenAttacks(square, occupancy & ~queenAttacks(square, occupancy));
    }

    public static long calculateBishopAttacks(int square, long occupancy) {
        long attacks = 0L;
        int[] directions = {-9, -7, 7, 9};

        int squareRow = square >>> 3;
        int squareCol = square & 7;

        for (int direction : directions) {
            for (int target = square + direction; target >= 0 && target < 64 && Math.abs(squareRow - (target >>> 3)) == Math.abs(squareCol - (target & 7)); target += direction) {
                attacks = setBit(attacks, target);
                if (getBit(occupancy, target) != 0) break;
            }
        }

        return attacks;
    }

    public static long calculateRookAttacks(int square, long occupancy) {
        long attacks = 0L;
        int[] directions = {-8, -1, 1, 8};

        for (int direction : directions) {
            for (int target = square + direction; target >= 0 && target < 64 && (Math.abs(direction) != 1 || target / 8 == square / 8); target += direction) {
                attacks = setBit(attacks, target);
                if (getBit(occupancy, target) != 0) break;
            }
        }

        return attacks;
    }

    public static long setLineThrough(int squareOne, int squareTwo) {
        int squareOneRank = squareOne >>> 3;
        int squareOneFile = squareOne & 7;

        int squareTwoRank = squareTwo >>> 3;
        int squareTwoFile = squareTwo & 7;

        if (squareOneRank == squareTwoRank) return lineThrough(squareOne, WEST);
        if (squareOneFile == squareTwoFile) return lineThrough(squareOne, NORTH);
        if (squareOneRank - squareTwoRank == squareOneFile - squareTwoFile) return lineThrough(squareOne, NORTH_WEST);
        if (squareOneRank - squareTwoRank == -(squareOneFile - squareTwoFile))
            return lineThrough(squareOne, NORTH_EAST);

        return 0L;
    }

    public static long setBitsBetween(int squareOne, int squareTwo) {
        long squareOneBit = SQ1 >>> squareOne;
        long squareTwoBit = SQ1 >>> squareTwo;

        return (squareOne < squareTwo ? squareOneBit - squareTwoBit : (squareTwoBit - squareOneBit) << 1) & LINE[squareOne][squareTwo];
    }

    public static long setOccupancy(int index, int bits, long mask) {
        long occupancy = 0L;

        for (int count = 0; count < bits; count++) {
            int square = lowestOneBitIndex(mask);

            mask = popBit(mask, square);

            if ((index & (1 << count)) != 0) occupancy |= (SQ1 >>> square);
        }

        return occupancy;
    }

    public static long setBit(long bitboard, int square) {
        return bitboard | SQ1 >>> square;
    }

    public static long popBit(long bitboard, int square) {
        return bitboard & ~(SQ1 >>> square);
    }

    public static long getBit(long bitboard, int square) {
        return bitboard & SQ1 >>> square;
    }

    public static int makePiece(int color, int type) {
        return type + 6 * color;
    }

    public static int sparseBitCount(long bitboard) {
        int count = 0;

        while (bitboard != 0) {
            bitboard &= bitboard - 1;
            count++;
        }

        return count;
    }

    public static int lowestOneBitIndex(long bitboard) {
        return BIT_POSITION[(int) (((bitboard & -bitboard) * 285870213051386505L) >>> 58)];
    }

    public static int encodeMove(int sourceSquare, int targetSquare, int moveKind) {
        return sourceSquare | targetSquare << 6 | moveKind << 12;
    }

    public static int sourceSquare(int move) {
        return move & 63;
    }

    public static int targetSquare(int move) {
        return move >>> 6 & 63;
    }

    public static int moveKind(int move) {
        return move >>> 12 & 15;
    }

    public static boolean isCapture(int moveKind) {
        return (moveKind & 4) != 0;
    }

    public static boolean isPromotion(int moveKind) {
        return (moveKind & 8) != 0;
    }

    public static int parseMove(Bitboard board, String str) {
        for (int move : board.generateAllMoves()) {
            if (str.equals(moveToString(move))) {
                return move;
            }
        }

        return 0;
    }

    public static int parseCastling(String availability) {
        int castling = 0;

        int[] castlingMap = new int[128];
        castlingMap['K'] = WHITE_CAN_CASTLE_KING_SIDE;
        castlingMap['Q'] = WHITE_CAN_CASTLE_QUEEN_SIDE;
        castlingMap['k'] = BLACK_CAN_CASTLE_KING_SIDE;
        castlingMap['q'] = BLACK_CAN_CASTLE_QUEEN_SIDE;

        for (char c : availability.toCharArray()) {
            castling |= castlingMap[c];
        }

        return castling;
    }

    public static int parseSquare(String square) {
        return square.length() == 2 && Character.isLetter(square.charAt(0)) && Character.isDigit(square.charAt(1)) ? Math.abs(square.charAt(1) - '0' - 8) * 8 + Character.toLowerCase(square.charAt(0)) - 97 : -1;
    }

    public static String formatMoveKind(int moveKind) {
        return switch (moveKind) {
            case QUIET_MOVE -> "Quiet";
            case DOUBLE_PAWN_PUSH -> "Double Pawn Push";
            case KING_SIDE_CASTLE -> "King Side Castle";
            case QUEEN_SIDE_CASTLE -> "Queen Side Castle";
            case CAPTURE -> "Capture";
            case ENPASSANT_CAPTURE -> "En Passant";
            case KNIGHT_PROMOTION -> "Knight Promotion";
            case BISHOP_PROMOTION -> "Bishop Promotion";
            case ROOK_PROMOTION -> "Rook Promotion";
            case QUEEN_PROMOTION -> "Queen Promotion";
            case KNIGHT_PROMO_CAPTURE -> "Knight Promotion Capture";
            case BISHOP_PROMO_CAPTURE -> "Bishop Promotion Capture";
            case ROOK_PROMO_CAPTURE -> "Rook Promotion Capture";
            case QUEEN_PROMO_CAPTURE -> "Queen Promotion Capture";
            default -> "Error";
        };
    }

    public static String formatCastling(int availability) {
        if (availability == 0) return "-";

        char[] castling = new char[4];
        int index = 0;

        if ((availability & WHITE_CAN_CASTLE_KING_SIDE) != 0) castling[index++] = 'K';
        if ((availability & WHITE_CAN_CASTLE_QUEEN_SIDE) != 0) castling[index++] = 'Q';
        if ((availability & BLACK_CAN_CASTLE_KING_SIDE) != 0) castling[index++] = 'k';
        if ((availability & BLACK_CAN_CASTLE_QUEEN_SIDE) != 0) castling[index++] = 'q';

        return new String(castling, 0, index);
    }

    public static String squareName(int square) {
        return square >= 0 && square < 64 ? (char) ((square & 7) + 97) + "" + (8 - (square >>> 3)) : "-";
    }

    public static String moveToString(int move) {
        int moveKind = moveKind(move);

        return squareName(sourceSquare(move)) + squareName(targetSquare(move)) + (isPromotion(moveKind) ? PIECES.charAt((moveKind & 3) + 7) : "");
    }

    public static String movesToString(int[] moves) {
        StringBuilder toReturn = new StringBuilder();

        toReturn.append("Move   Encoded\n");

        for (int move : moves) {
            toReturn.append(String.format("%s    %d\n", moveToString(move), move));
        }

        return toReturn.toString();
    }

    public static String bitboardToString(long bitboard) {
        StringBuilder toReturn = new StringBuilder();

        for (int square = 0; square < 64; square++) {
            toReturn.append((getBit(bitboard, square) != 0 ? 1 : 0)).append("  ");
            if ((square & 7) == 7) toReturn.append("\n");
        }

        return toReturn.toString();
    }
}