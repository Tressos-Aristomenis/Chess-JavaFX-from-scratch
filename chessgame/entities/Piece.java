package chessgame.entities;

import java.util.ArrayList;
import javafx.scene.image.ImageView;

/**
 *
 * @author Aris
 */
public abstract class Piece {
    private int color;
    private boolean hasMoved;
    private ImageView pieceImage;
    
    public Piece(int color, ImageView pieceImage) {
        this.color = color;
        this.pieceImage = pieceImage;
        this.hasMoved = false;
    }
    
    public void isMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    
    public boolean hasMoved() {
        return this.hasMoved;
    }
    
    public ImageView getImage() {
        return this.pieceImage;
    }
    
    public int getColor() {
        return this.color;
    }
    
    @Override
    public String toString() {
        if (this instanceof Pawn) {
            return "Pawn";
        }
        else if (this instanceof Bishop) {
            return "Bishop";
        }
        else if (this instanceof Knight) {
            return "Knight";
        }
        else if (this instanceof Rook) {
            return "Rook";
        }
        else if (this instanceof Queen) {
            return "Queen";
        }
        else {
            return "King";
        }
    }
    
    public abstract int tilesToSearch();
    public abstract ArrayList<Integer[]> getMoves();
}
