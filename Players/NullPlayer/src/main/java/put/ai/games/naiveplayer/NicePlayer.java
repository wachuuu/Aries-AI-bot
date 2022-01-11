/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;
import put.ai.games.game.moves.MoveMove;

import java.util.*;
import java.util.List;

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
    private long timeStart;
    private long timeStop;
    private int depthLimit = 3;

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

    Integer getMoveValue(Board b, Color color) {
        int value = 0;
        try {
            if (b.getWinner(color) == color) return Integer.MAX_VALUE;
        } catch (Exception e) {
            System.out.println("two winners");
        }

        for (int i = 1; i <= boardSize; i++) {
            for (int j = 1; j <= boardSize; j++) {
               if (b.getState(i-1, j-1) == color) {
                   value += 1000;
                   if (color == Color.PLAYER1) {
                       value += i*i * j*j;
                   }
                   if (color == Color.PLAYER2) {
                       value += (this.boardSize + 1 - i) * (this.boardSize + 1 - i) *
                               (this.boardSize + 1 - j) * (this.boardSize + 1 - j);
                   }
               }
               if (b.getState(i-1, j-1) == getOpponent(color)) {
                   value -= 1000;
                   if (color == Color.PLAYER1) {
                       value -= (this.boardSize + 1 - i) * (this.boardSize + 1 - i) *
                               (this.boardSize + 1 - j) * (this.boardSize + 1 - j);
                   }
                   if (color == Color.PLAYER2) {
                       value -= i*i * j*j;
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

        if (depth == 0 || nextMoves.isEmpty() || hasTimeEnded())
        return new MyMove(null, getMoveValue(b, color));

        Color winner = b.getWinner(getOpponent(color));

        if (winner == color) {
            return new MyMove(null, Integer.MAX_VALUE);
        } else if (winner == getOpponent(color)) {
            return new MyMove(null, Integer.MIN_VALUE);
        }

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
            if (alpha >= beta) {
                bestMove.value = beta;
                return  bestMove;
            }
        }
        assert bestMove != null;
        bestMove.value = alpha;
        return bestMove;
    }

    MyMove getBestMove(Board b, Color color) {
        List<Move> moves = b.getMovesFor(color);
        System.out.println(moves);
        moves.sort(Comparator.comparingInt((Move move) -> estimateGoalDistance(b, move, color)));
        MyMove bestMove = new MyMove(moves.get(0), getMoveValue(b, color));
        for (int depth = 1; depth <= this.depthLimit && !hasTimeEnded(); depth++) {
            MyMove candidateMove = negamax(b, color, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
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

    private void assignTime() {
        this.timeStart = System.currentTimeMillis();
        this.timeStop = getTime();
    }

    @Override
    public Move nextMove(Board b) {
        this.assignBoardSize(b);
        this.assignColors();
        this.assignTime();

        MyMove nextMove = getBestMove(b, this.myColor);
        b.doMove(nextMove.move);
        return nextMove.move;
    }
}
