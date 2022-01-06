/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;
import put.ai.games.game.moves.MoveMove;

import java.util.List;
import java.util.stream.Collectors;
import java.awt.Point;

public class NicePlayer extends Player {

    private Color myColor;
    private Color enemyColor;
    private int boardSize;

    @Override
    public String getName() {
        return "Gabriel Wachowski 145275 Mateusz Świercz 145225";
    }

    private void assignColors() {
        // dostepne kolory: PLAYER1 or PLAYER2
        this.myColor = getColor();                  // kolor naszego gracza (gracz "max")
        this.enemyColor = getOpponent(getColor());  // kolor przeciwnika (gracz "min")
    }

    private void assignBoardSize(Board board) {
        this.boardSize = board.getSize();           // rozmiar planszy
    }

    private List<Point> getEnemyPosition(Board b) {
        // pobieram ruchy przeciwnika, biore z nich pozycje startowa i zapisuje w liście punktów
        return b.getMovesFor(enemyColor)
                .stream()
                .map(move -> (MoveMove) move )
                .map(moveMove -> new Point(moveMove.getSrcX(), moveMove.getSrcY()))
                .collect(Collectors.toList());
    }

    @Override
    public Move nextMove(Board b) {
        // dla planszy 8x8:
        // player 1, pomaranczowy, lewy gorny rog (0,0) - cel prawy dolny (7,7)
        // player 2, niebieski, prawy dolny rog (7,7) - cel lewy gorny (0,0)

        this.assignBoardSize(b);    // odczytujemy i zapisujemy rozmiar planszy
        this.assignColors();        // odczytujemy kolor naszego gracza

        // odczytujemy dostene dla naszego gracza ruchy,
        // interfejs Move (getMovesFor()) jest niewystarczajacy,
        // rzutujemy go na inny posiadający wiecej danych (MoveMove)
        List<MoveMove> myMoveMoves = b.getMovesFor(myColor).stream().map(move -> (MoveMove) move).collect(Collectors.toList());

        // pobieramy liste z pozycjami przeciwnika
        List<Point> enemyPosition = getEnemyPosition(b);

        // w razie nie znalezienia odpowiedniego ruchu bierzemy pierwszy z brzegu
        Move nextMove = myMoveMoves.get(0);

        // sprawdzamy pokolei ruchy, jezeli koncowa pozycja jest pozycja przeciwnika
        // (nastepuje przesuniecie) to wykonujemy ten ruch
        for (MoveMove move: myMoveMoves) {
            if (enemyPosition.stream().anyMatch(point -> point.x == move.getDstX() && point.y == move.getDstY())) {
                System.out.println("Found move "+move.getDstX()+" "+move.getDstY());
                nextMove = move;
                break;
            }
        }

        return nextMove;
    }
}
