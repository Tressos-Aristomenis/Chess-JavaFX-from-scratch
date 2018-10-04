package chessgame.entities;

import java.util.ArrayList;
import javafx.scene.image.ImageView;

/**
 *
 * @author Aris
 */
public class Pawn extends Piece {
    public Pawn(int color, ImageView pawnImage) {
        super(color, pawnImage);
    }
    
    @Override
    public int tilesToSearch() {
        return 1;
    }

    @Override
    public ArrayList<Integer[]> getMoves() {
        ArrayList<Integer[]> moves = new ArrayList<>();
        
        // destination tiles the piece can go. Eg. can go to : --> (row, column).
        moves.add(new Integer[]{-1 * this.getColor(), 0});
        moves.add(new Integer[]{-1 * this.getColor(), -1});
        moves.add(new Integer[]{-1 * this.getColor(), 1});
        
        return moves;
    }
}
