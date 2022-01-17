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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class MyMove {
    Move move;
    Integer value;

    MyMove(Move move, Integer value) {
        this.move = move;
        this.value = value;
    }
}

public class NicePlayer extends Player {

    private long timeStart;
    private long timeStop;
    static Random random = new Random(0xCAFFE);
    private int depthLimit = 10;

    @Override
    public String getName() {
        return "Gabriel Wachowski 145275 Mateusz Świercz 145225";
    }

    boolean hasTimeEnded() {
        return System.currentTimeMillis() - timeStart >= timeStop - 500;
    }

    Integer estimateGoalDistance(Board b, Move move, Color color) {
        int size = b.getSize();
        if (color == Color.PLAYER2) {
            return Math.round((float) Math.sqrt((((MoveMove) move).getDstX())*(((MoveMove) move).getDstX()) + (((MoveMove) move).getDstY())*(((MoveMove) move).getDstY())));
        }
        else {
            return Math.round((float) Math.sqrt((size - ((MoveMove) move).getDstX())*(size - ((MoveMove) move).getDstX()) + (size - ((MoveMove) move).getDstY())*(size - ((MoveMove) move).getDstY())));
        }
    }

    MyMove getRandomMove(Board b, Color color) {
        List<Move> moves = b.getMovesFor(color);
        Move move = moves.get(random.nextInt(moves.size()));
        return new MyMove(move, getMoveValue(b, color));
    }

    Integer getMoveValue(Board b, Color color) {
        int boardSize = b.getSize();
        int value = 0;
        if (b.getWinner(color) == color) return Integer.MAX_VALUE;

        for (int i = 1; i <= boardSize; i++) {
            for (int j = 1; j <= boardSize; j++) {
               if (b.getState(i-1, j-1) == color) {
                   value += 1000;
                   if (color == Color.PLAYER1) {
                       value += i + j;
                   }
                   if (color == Color.PLAYER2) {
                       value += (boardSize + 1 - i) + (boardSize + 1 - j);
                   }
               }
               if (b.getState(i-1, j-1) == getOpponent(color)) {
                   value -= 1000;
                   if (color == Color.PLAYER1) {
                       value -= (boardSize + 1 - i) + (boardSize + 1 - j);
                       if ((boardSize + 1 - i) >= 5 && (boardSize + 1 - j) >= 5) {
                           value -= 1000;
                       }
                   }
                   if (color == Color.PLAYER2) {
                       value -= i + j;
                       if (i >= 5 && j >= 5) {
                           value -= 1000;
                       }
                   }
               }
            }
        }
        return value;
    }

    MyMove negamax(Board b, Color color, int depth, int alpha, int beta) {
        Color nextColor = getOpponent(color);
        List<Move> nextMoves = b.getMovesFor(color);
        nextMoves.sort(Comparator.comparingInt((Move move) -> estimateGoalDistance(b, move, color)));

        if (depth == 0) {
//            System.out.println("end of depth");
            return new MyMove(null, getMoveValue(b, color));
        }
        if (nextMoves.isEmpty()) {
            System.out.println("end of moves");
            return new MyMove(null, getMoveValue(b, color));
        }
        if (hasTimeEnded()) {
//            System.out.println("end of time");
            return new MyMove(null, getMoveValue(b, color));
        }
        if (b.getWinner(color) == color) {
            System.out.println("i am winning "+color);
            return new MyMove(null, Integer.MAX_VALUE);
        }
        if (b.getWinner(color) == nextColor) {
            System.out.println("opponent winning "+nextColor);
            return new MyMove(null, Integer.MIN_VALUE);
        }

        MyMove bestMove = new MyMove(null, Integer.MIN_VALUE);
        for (Move move : nextMoves) {
            b.doMove(move);
            MyMove nextMove = negamax(b, nextColor,depth-1, -beta, -alpha);
            if (nextMove.value == null) continue;
            nextMove.value *= -1;
            nextMove.move = move;
            b.undoMove(move);

            if (nextMove.value > bestMove.value) {
                bestMove.move = nextMove.move;
                bestMove.value = nextMove.value;
            }
            if (bestMove.value > alpha) alpha = bestMove.value;
            if (alpha >= beta) {
                bestMove.value = beta;
                return  bestMove;
            }
        }
        bestMove.value = alpha;
        return bestMove;
    }


    MyMove getBestMove(Board b, Color color) {
        List<Move> moves = b.getMovesFor(color);
        moves.sort(Comparator.comparingInt((Move move) -> estimateGoalDistance(b, move, color)));
        MyMove bestMove = new MyMove(moves.get(0), getMoveValue(b, color));
        for (int depth = 1; depth <= this.depthLimit && !hasTimeEnded(); depth++) {
            MyMove candidateMove = negamax(b, color, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (candidateMove.value > bestMove.value) bestMove = candidateMove;
        }

        System.out.println("("+bestMove.value+")\t"+bestMove.move);
        return bestMove;
    }


    private void assignTime() {
        this.timeStart = System.currentTimeMillis();
        this.timeStop = getTime();
    }

    @Override
    public Move nextMove(Board b) {
        assignTime();
        MyMove nextMove = getBestMove(b, getColor());
        b.doMove(nextMove.move);
        return nextMove.move;
    }
}
