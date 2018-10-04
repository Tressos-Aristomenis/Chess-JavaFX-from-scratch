package chessgame.entities;

/**
 *
 * @author Aris
 */
public class Tile {
    private Piece pieceOnTile;
    private String defaultTileColor;
    
    public Tile(Piece piece) {
        this.pieceOnTile = piece;
    }
    
    public void setDefaultTileColor(String defaultColor) {
        defaultTileColor = defaultColor;
    }
    
    public String getDefaultTileColor() {
        return defaultTileColor;
    }
    
    public Piece getPiece() {
        return this.pieceOnTile;
    }
    
    public Piece removePiece() {
        Piece released = this.getPiece();
        this.setPiece(null);
        
        return released;
    }
    
    public void setPiece(Piece pieceToBeSet) {
        this.pieceOnTile = pieceToBeSet;
    }
    
    public boolean isEmpty() {
        return pieceOnTile == null;
    }
    
    @Override
    public String toString() {
        if (this.pieceOnTile != null)
            return "Piece on tile: " + this.pieceOnTile + ". Player: " + this.pieceOnTile.getColor();
        else
            return "Piece on tile: " + this.pieceOnTile;
    }
}
