/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;
import put.ai.games.game.moves.MoveMove;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.awt.Point;

class MyMove {
    Move move;
    Integer value;

    MyMove(Move move, Integer value) {
        this.move = move;
        this.value = value;
    }
}

public class NicePlayer extends Player {

    private Color myColor;
    private Color enemyColor;
    private int boardSize;
    private long timeStart = 0;
    private long timeStop;
    static Random random = new Random(0xCAFFE);
    private int depthLimit = 20;

    @Override
    public String getName() {
        return "Gabriel Wachowski 145275 Mateusz Świercz 145225";
    }

    boolean hasTimeEnded() {
        return System.currentTimeMillis() - timeStart >= timeStop - 500;
    }

    MyMove getRandomMove(Board b, Color color) {
        List<Move> moves = b.getMovesFor(color);
        Move move = moves.get(random.nextInt(moves.size()));
        return new MyMove(move, getMoveValue(b, color));
    }

    Integer getMoveValue(Board b, Color color) {
        Integer value = 0;
        if (b.getWinner(color) == color) return 999999;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
               if (b.getState(i, j) == color) value++;
               if (b.getState(i, j) == getOpponent(color)) value--;
            }
        }
        return value;
    }

    MyMove negamax(Board b, Color color, int depth, int alpha, int beta) {
        Color nextColor = getOpponent(color);
        List<Move> nextMoves = b.getMovesFor(color);
        Collections.shuffle(nextMoves);
        if (depth == 0 || nextMoves.isEmpty() || hasTimeEnded())
            return new MyMove(null, getMoveValue(b, color));

        MyMove bestMove = null;
        for (Move move : nextMoves) {
            b.doMove(move);
            MyMove nextMove = negamax(b, nextColor, depth - 1, -beta, -alpha);
            b.undoMove(move);
            if (nextMove.value == null) continue;
            nextMove.value *= -1;
            nextMove.move = move;
            if (bestMove == null || nextMove.value > bestMove.value) bestMove = nextMove;
            alpha = Math.max(bestMove.value, alpha);
            if (bestMove.value >= beta) return bestMove;
        }

        return bestMove;
    }

    MyMove getBestMove(Board b, Color color) {
        MyMove bestMove = getRandomMove(b, color);
        for (int depth = 1; depth <= this.depthLimit && !hasTimeEnded(); depth++) {
            MyMove candidateMove = negamax(b, color, depth, 0, 999999);
            if (candidateMove.value > bestMove.value) bestMove = candidateMove;
        }

        System.out.println("("+bestMove.value+")\t"+bestMove.move);
        return bestMove;
    }

    private void assignColors() {
        this.myColor = getColor();
        this.enemyColor = getOpponent(getColor());
    }

    private void assignBoardSize(Board board) {
        this.boardSize = board.getSize();
    }

    private void assignTimeStop() {
        this.timeStop = getTime();
    }

    @Override
    public Move nextMove(Board b) {
        this.assignBoardSize(b);
        this.assignColors();
        this.assignTimeStop();

        List<MoveMove> myMoveMoves = b.getMovesFor(myColor).stream().map(move -> (MoveMove) move).collect(Collectors.toList());

        MyMove nextMove = getBestMove(b, this.myColor);

        return nextMove.move;
    }
}
