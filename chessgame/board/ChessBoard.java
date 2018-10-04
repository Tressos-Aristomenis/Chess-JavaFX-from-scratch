package chessgame.board;

import chessgame.entities.Bishop;
import chessgame.entities.King;
import chessgame.entities.Knight;
import chessgame.entities.Pawn;
import chessgame.entities.Piece;
import chessgame.entities.Queen;
import chessgame.entities.Rook;
import chessgame.entities.Tile;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Aris
 */
public class ChessBoard implements Initializable {
    final int ROWS = 8;
    final int COLUMNS = 8;
    final Map<Tile, Node> TILE_NODE = new HashMap<>();
	
    public GridPane mainGrid, topGrid;
    protected final String LIGHT = "#DBDACE";
    protected final String DARK  = "#CC6600";
    protected final String CLICKED_COLOR = "#FFDF00";
    protected final String AVAILABLE_TILE_COLOR = "#6FFFD9";
    protected final String ENEMY_IN_AVAILABLE_TILE_COLOR = "#FF6666";
    protected final String KING_IN_CHECK_COLOR = "#8B0000";
    private Tile[][] tiles = new Tile[ROWS][COLUMNS];
    private Tile startTile;
    private int currentPlayer;
    private ArrayList<Tile> legalMoves;
    private Tile kingPosition;
    private int WINNER = 0;
    private boolean gameOver = false;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createNewGame();
    }
    
    
    
    
    
        // --------------------   PAWN RELATED FUNCTIONS   -------------------- \\
    
    private ArrayList<Tile> validatePawn(final int startingRow, final int startingColumn, final int ruleRow, final int ruleColumn) {
        ArrayList<Tile> moves = new ArrayList<>();
        
        Tile pawnTile;
        final Tile startingTile = tiles[startingRow][startingColumn];
        
        try {
            if (startingColumn - ruleColumn == startingColumn) {                    // straight rule.
                pawnTile = tiles[startingRow + ruleRow][startingColumn];           // single straight move.
                
                if (pawnTile.isEmpty()) {
                    moves.add(pawnTile);
                    
                    if (!startingTile.getPiece().hasMoved()) {
                        pawnTile = tiles[startingRow + ruleRow * 2][startingColumn];        // double move in front.
                        if (pawnTile.isEmpty()) {
                            moves.add(pawnTile);
                        }
                    }
                }
            }
            else {                                      // diagonal rule.
                pawnTile = tiles[startingRow + ruleRow][startingColumn + ruleColumn];
                if (collisionOccured(pawnTile)) {
                    if (pawnTile.getPiece().getColor() != startingTile.getPiece().getColor()) {
                        moves.add(pawnTile);
                    }
                }
            }
        }
        catch(Exception ee) {
        }
        
        return moves;
    }
    
    private void pawnPromotion(Tile pawnTile) {
        if (!(pawnTile.getPiece() instanceof Pawn)) {
            return;
        }
        
        if (getRow(pawnTile) == 0 && currentPlayer == 1 || getRow(pawnTile) == 7 && currentPlayer == -1) {
            mainGrid.getChildren().remove(pawnTile.getPiece().getImage());
            pawnTile.removePiece();
            String ch = printMenuToChoosePromotionPiece();
            createPieceToPromoteTo(pawnTile, ch);
        }
    }
    
    
            // --------------------   MOVE FUNCTIONS   -------------------- \\
    
    private void move(final Tile newTile, final int rowClicked, final int colClicked) {
        Piece destination = newTile.removePiece();
        Piece start = startTile.removePiece();
        final int startRow = getRow(startTile);
        final int startColumn = getColumn(startTile);
        
        removePieceImagesAfterMove(start, destination, rowClicked, colClicked);
        
        newTile.setPiece(start);
        startTile.setPiece(null);
        start.isMoved(true);
        
        
        if (newTile.getPiece() instanceof King && Math.abs(getColumn(newTile) - startColumn) == 2) {
            applyCastlingMove(startRow, startColumn, newTile);
        }
        
        pawnPromotion(newTile);
        setTileColor(kingPosition, kingPosition.getDefaultTileColor());         // when the move is completed, we are sure that king is not in check.
        
        resetPieceColors();
        legalMoves.clear();
        currentPlayer *= -1;
        
        ImageView img = new ImageView(".resources/Chess_rdt60.png");
        img.setFitHeight(40);
        img.setFitWidth(40);
        topGrid.add(img, 0, 0);
    }
    
    private void applyCastlingMove(final int startKingRow, final int startKingColumn, Tile kingTile) {
        final int destinationKingColumn = getColumn(kingTile);
        Tile rookStart = null, rookEnd = null;

        if (destinationKingColumn - startKingColumn == 2) {                         // right rook.
            rookStart = tiles[startKingRow][startKingColumn +3];
            rookEnd = tiles[startKingRow][startKingColumn +1];
        }
        else {                                                                 // left rook.
            rookStart = tiles[startKingRow][startKingColumn -4];
            rookEnd = tiles[startKingRow][startKingColumn -1];
        }

        removePieceImagesAfterMove(rookStart.getPiece(), rookEnd.getPiece(), getRow(rookEnd), getColumn(rookEnd));

        Piece rook = rookStart.removePiece();
        rookEnd.setPiece(rook);
    }
    
    private boolean hasLegalMoves() {
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLUMNS; y++) {
                Tile curr = tiles[x][y];
                if (curr.isEmpty()) {
                    continue;
                }
                
                if (curr.getPiece().getColor() == currentPlayer) {
                    ArrayList<Tile> pseudolegal = getAllPieceMoves(x, y);
                    if (!excludeMoves(tiles[x][y], pseudolegal).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private ArrayList<Tile> getAllPieceMoves(final int startRow, final int startColumn) {
        final Tile startingTile = tiles[startRow][startColumn];               // gets tile that the method is called on.
        
        ArrayList<Integer[]> moves = startingTile.getPiece().getMoves();
        ArrayList<Tile> LEGAL_MOVES = new ArrayList<>();
        
        int multiMove = startingTile.getPiece().tilesToSearch();
        
        for (Integer[] rule : moves) {
            int numberOfMoves = 1;
            
            final int x = rule[0];
            final int y = rule[1];
            
            int movingRow = startRow;            // x coordinates of movingTile.
            int movingColumn = startColumn;     // y coordinates of movingTile.
            Tile movingTile = null;
            // each time holds the tile that the starting tile is moving on. eg If rook is moving down (movingTile = [1, 0] -> = [2, 0] -> = [3, 0].
            
            if (startingTile.getPiece() instanceof Pawn) {
                LEGAL_MOVES.addAll(validatePawn(startRow, startColumn, x, y));
                continue;
            }
            
            
            while (numberOfMoves <= multiMove) {
                movingRow += x;
                movingColumn += y;
                
                try {
                    movingTile = tiles[movingRow][movingColumn];
                }
                catch(Exception ee) { break; }
                
                if (collisionOccured(movingTile)) {                                                                     // checks for collision with other piece.
                    if (startingTile.getPiece().getColor() != movingTile.getPiece().getColor()) {
                        LEGAL_MOVES.add(movingTile);
                    }
                    break;
                }
                
                if (canBeCastled(startingTile, y)) {
                    if (checkCastlingMove(y) == false) {
                        continue;
                    }
                }
                
                
                LEGAL_MOVES.add(movingTile);
                numberOfMoves++;
            }
        }
        
        return LEGAL_MOVES;
    }
    
    private boolean checkCastlingMove(final int destCol) {
        Tile kingtile = getKingtile();
        
        
        // if king is in check don't allow castling.
        if (kingInCheck(kingtile)) {
            return false;
        }
        
        final int kingRow = getRow(kingtile);
        final int kingCol = getColumn(kingtile);
        
        
        // checks if rook has moved.
        try {
            Piece rook = null;
            
            // right rook. destCol > 0 cause it's 2 columns after.
            if (destCol > 0) {
                if (tiles[kingRow][kingCol +3].getPiece() instanceof Rook) {
                    rook = (Rook) tiles[kingRow][kingCol +3].getPiece();
                }
            }
            else {
                if (tiles[kingRow][kingCol -4].getPiece() instanceof Rook) {
                    rook = (Rook) tiles[kingRow][kingCol -4].getPiece();
                }
            }
            
            if (rook.hasMoved()) {
                return false;
            }
        }
        catch(Exception ee) { return false; }
        
        
        // left rook.
        for (int y = 1; y <= 2; y++) {
            if (!isValidMove(tiles[kingRow][kingCol - y], tiles[kingRow][kingCol])) {          // checks if tiles that king moves through are not in check.
                return false;
            }
        }
        
        
        // right rook.
        for (int y = 1; y <= 2; y++) {
            if (!isValidMove(tiles[kingRow][kingCol + y], tiles[kingRow][kingCol])) {          // checks if tiles that king moves through are not in check.
                return false;
            }
        }
        
        
        // checks if tiles between king tile and rook tile are empty.
        if (destCol > 0) {
            for (int y = 1; y <= 2; y++) {
                if (!tiles[kingRow][kingCol + y].isEmpty()) {
                    return false;
                }
            }
        }
        else {
            for (int y = 1; y <= 3; y++) {
                if (!tiles[kingRow][kingCol - y].isEmpty()) {
                    return false;
                }
            }
        }
        
        
        
        return true;
    }
    
    private ArrayList<Tile> excludeMoves(Tile start, ArrayList<Tile> probableMoves) {
        ArrayList<Tile> validMoves = new ArrayList<>();
        
        for (Tile move : probableMoves) {
            if (isValidMove(move, start)) {
                validMoves.add(move);
            }
        }
        
        return validMoves;
    }
    
    private boolean isValidMove(Tile destination, Tile start) {
        Piece swap = start.removePiece();       // temporarily move piece to next available tile.
        Piece occupy = destination.removePiece();
        destination.setPiece(swap);
        
        Tile kingtile = getKingtile();
        boolean inCheck = kingInCheck(kingtile);
        
        start.setPiece(swap);                   // reset pieces to their start position.
        destination.setPiece(occupy);
        
        return !inCheck;
    }
    
    private void removePieceImagesAfterMove(Piece clicked, Piece occupied, final int rowClicked, final int colClicked) {
        mainGrid.getChildren().remove(clicked.getImage());
        
        if (occupied != null) {
            mainGrid.getChildren().remove(occupied.getImage());
        }
        
        mainGrid.add(clicked.getImage(), colClicked, rowClicked);
    }
    
    
        // --------------------   ON CLICK FUNCTIONS   -------------------- \\
    
    @FXML
    @SuppressWarnings("unused")
    public void onTileClick(MouseEvent e) {
        Node current = (Node) e.getSource();
        final int rowClicked = GridPane.getRowIndex(current);
        final int colClicked = GridPane.getColumnIndex(current);
        final Tile tileClicked = tiles[rowClicked][colClicked];
        
        
        if (legalMoves.contains(tileClicked)) {
            move(tileClicked, rowClicked, colClicked);
            kingPosition = getKingtile();
            
            if (kingInCheck(kingPosition)) {
                setTileColor(kingPosition, KING_IN_CHECK_COLOR);           // sets king tile to red because he is under check.
            }
            
            
            if (isCheckmate(kingPosition)) {
                WINNER = currentPlayer * -1;
                gameOver = true;
            }
            else if (isStalemate(kingPosition)) {
                gameOver = true;
            }
            
            
            if (gameOver) {
                menuAfterGameEnds();
            }
        }
        else if (tileClicked.isEmpty()) {
            return;
        }
        else if (tileClicked.getPiece().getColor() == currentPlayer) {
            resetPieceColors();
            startTile = tileClicked;
            
            kingPosition = getKingtile();
            if (kingInCheck(kingPosition)) {
                setTileColor(kingPosition, KING_IN_CHECK_COLOR);              // remain red color in king after other piece clicks of same team.
            }
            
            ArrayList<Tile> probableMoves = getAllPieceMoves(rowClicked, colClicked);
            legalMoves = excludeMoves(startTile, probableMoves);
            
            setColorsRelatedToPiece();
        }
        
    }
    
    @FXML
    public void onClickNewGame() {
        createNewGame();
    }
    
    @FXML
    public void onClickExit() {
        System.exit(-1);
    }
    
    
        // --------------------   KING RELATED FUNCTIONS   -------------------- \\
    
    private Tile getKingtile() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (tiles[row][col].isEmpty()) {
                    continue;
                }
                if (tiles[row][col].getPiece().getColor() == currentPlayer) {
                    Piece ally = tiles[row][col].getPiece();
                    if (ally instanceof King) {
                        return tiles[row][col];
                    }
                }
            }
        }
        
        return null;
    }
    
    private boolean kingInCheck(Tile kingtile) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (tiles[row][col].isEmpty()) {
                    continue;
                }
                if (tiles[row][col].getPiece().getColor() != kingtile.getPiece().getColor()) {
                    ArrayList<Tile> enemyMoves = getAllPieceMoves(row, col);
                    
                    if (tiles[row][col].getPiece() instanceof Pawn) {
                        try {
                           enemyMoves.remove(tiles[row + currentPlayer][col]);
                           enemyMoves.remove(tiles[row + currentPlayer * 2][col]);
                        }
                        catch(Exception ee) {}
                    }
                    if (enemyMoves.contains(kingtile)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean isCheckmate(Tile kingtile) {
        return kingInCheck(kingtile) && !hasLegalMoves();
    }
    
    private boolean isStalemate(Tile kingtile) {
        return !kingInCheck(kingtile) && !hasLegalMoves();
    }
    
    
        // --------------------   INITIALIZE & RESET BOARD FUNCTIONS   -------------------- \\
    
    private void resetMainGrid() {
        for(Node node : mainGrid.getChildren()) {
            if (node instanceof ImageView) {
                ((ImageView) node).setImage(null);
            }
        }
    }
    
    private void createNewGame() {
        legalMoves = new ArrayList<>();
        resetMainGrid();
        initializeBoardTiles();
        initializeTileColors();
        currentPlayer = 1;
        kingPosition = null;
        startTile = null;
        WINNER = 0;
        gameOver = false;
    }
    
    private void initializeBoardTiles() {
        // --------------------   BLACK PLAYER   -------------------- \\
        
        tiles[0][0] = new Tile(new Rook(-1, new ImageView(".resources/Chess_rdt60.png")));
        tiles[0][1] = new Tile(new Knight(-1, new ImageView(".resources/Chess_ndt60.png")));
        tiles[0][2] = new Tile(new Bishop(-1, new ImageView(".resources/Chess_bdt60.png")));
        tiles[0][3] = new Tile(new Queen(-1, new ImageView(".resources/Chess_qdt60.png")));
        tiles[0][4] = new Tile(new King(-1, new ImageView(".resources/Chess_kdt60.png")));
        tiles[0][5] = new Tile(new Bishop(-1, new ImageView(".resources/Chess_bdt60.png")));
        tiles[0][6] = new Tile(new Knight(-1, new ImageView(".resources/Chess_ndt60.png")));
        tiles[0][7] = new Tile(new Rook(-1, new ImageView(".resources/Chess_rdt60.png")));
        
        for (int pawnCol = 0; pawnCol < 8; pawnCol++) {
            tiles[1][pawnCol] = new Tile(new Pawn(-1, new ImageView(".resources/Chess_pdt60.png")));
        }
        
        
        
        // --------------------   WHITE PLAYER   -------------------- \\
        
        tiles[7][0] = new Tile(new Rook(1, new ImageView(".resources/Chess_rlt60.png")));
        tiles[7][1] = new Tile(new Knight(1, new ImageView(".resources/Chess_nlt60.png")));
        tiles[7][2] = new Tile(new Bishop(1, new ImageView(".resources/Chess_blt60.png")));
        tiles[7][3] = new Tile(new Queen(1, new ImageView(".resources/Chess_qlt60.png")));
        tiles[7][4] = new Tile(new King(1, new ImageView(".resources/Chess_klt60.png")));
        tiles[7][5] = new Tile(new Bishop(1, new ImageView(".resources/Chess_blt60.png")));
        tiles[7][6] = new Tile(new Knight(1, new ImageView(".resources/Chess_nlt60.png")));
        tiles[7][7] = new Tile(new Rook(1, new ImageView(".resources/Chess_rlt60.png")));
        for (int pawnColumn = 0; pawnColumn < 8; pawnColumn++) {
            tiles[6][pawnColumn] = new Tile(new Pawn(1, new ImageView(".resources/Chess_plt60.png")));
        }
        
        
        initializeEmptyTiles();
        setPieceImagesMouseTransparent();
        printPieceImagesToBoard();
    }
    
    private void setPieceImagesMouseTransparent() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (!tiles[i][j].isEmpty()) {
                    tiles[i][j].getPiece().getImage().setMouseTransparent(true);
                }
            }
        }
    }
    
    private void initializeEmptyTiles() {
        for (int emptyRow = 2; emptyRow < 6; emptyRow++) {
            for (int emptyCol = 0; emptyCol < 8; emptyCol++) {
                tiles[emptyRow][emptyCol] = new Tile(null);
            }
        }
    }
    
    private void printPieceImagesToBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (!tiles[i][j].isEmpty()) {
                    mainGrid.add(tiles[i][j].getPiece().getImage(), j, i);
                }
            }
        }
    }
    
    
        // --------------------   USEFUL IN GAME FUNCTIONS   -------------------- \\
    
    private void menuAfterGameEnds() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over !");
        alert.setHeaderText(null);
        
        if (WINNER != 0) {
            if (WINNER == -1) {
                alert.setContentText("CHECK MATE! Black player wins.");
            }
            else{
                alert.setContentText("CHECK MATE! White player wins.");
            }
        }
        else {
           alert.setContentText("STALE MATE! The player to move has no legal move, but is not in check! "); 
        }
        ButtonType buttonTypeOne = new ButtonType("New Game");
        ButtonType buttonTypeTwo = new ButtonType("Exit");
        
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == buttonTypeOne) {
            createNewGame();
        }
        else {
            System.exit(0);
        }
    }
    
    private void createPieceToPromoteTo(Tile desti, String ch) {
        ImageView image = null;
        Piece promotePiece = null;
        
        
        switch(ch) {
            case "Queen" :
                if (currentPlayer == -1) {
                    image = new ImageView(".resources/Chess_qdt60.png");
                }
                else {
                    image = new ImageView(".resources/Chess_qlt60.png"); 
                }
                promotePiece = new Queen(currentPlayer, image);
                break;
            case "Rook" :
                if (currentPlayer == -1) {
                    image = new ImageView(".resources/Chess_rdt60.png");
                }
                else {
                    image = new ImageView(".resources/Chess_rlt60.png"); 
                }
                promotePiece = new Rook(currentPlayer, image);
                break;
            case "Knight" :
                if (currentPlayer == -1) {
                    image = new ImageView(".resources/Chess_ndt60.png");
                }
                else {
                    image = new ImageView(".resources/Chess_nlt60.png"); 
                }
                promotePiece = new Knight(currentPlayer, image);
                break;
            case "Bishop" :
                if (currentPlayer == -1) {
                    image = new ImageView(".resources/Chess_bdt60.png");
                }
                else {
                    image = new ImageView(".resources/Chess_blt60.png"); 
                }
                promotePiece = new Bishop(currentPlayer, image);
                break;
        }
        
        image.setMouseTransparent(true);
        mainGrid.add(promotePiece.getImage(), getColumn(desti), getRow(desti));
        tiles[getRow(desti)][getColumn(desti)].setPiece(promotePiece);
    }
    
    private String printMenuToChoosePromotionPiece() {
        String choice;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Choose your piece !");
        alert.setHeaderText(null);
        alert.setContentText("Select the piece you want to switch with: ");
        ButtonType buttonTypeOne = new ButtonType("Queen");
        ButtonType buttonTypeTwo = new ButtonType("Rook");
        ButtonType buttonTypeThree = new ButtonType("Knight");
        ButtonType buttonTypeFour = new ButtonType("Bishop");
        
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeFour);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.get() == buttonTypeOne){
            choice = "Queen";
        }
        else if (result.get() == buttonTypeTwo) {
            choice = "Rook";
        }
        else if (result.get() == buttonTypeThree) {
            choice = "Knight";
        }
        else {
            choice = "Bishop";
        }
       
        return choice;
    }
    
    private Node getNodeByRowColumnIndex(final int row, final int col) {
        for(Node curr: mainGrid.getChildren())
            if(GridPane.getRowIndex(curr) == row && GridPane.getColumnIndex(curr) == col)
                return curr;
        return null;
    }
    
    private int getRow(Tile tile) {
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLUMNS; y++) {
                if (tiles[x][y] == tile) {
                    return x;
                }
            }
        }
        
        return -1;
    }
    
    private int getColumn(Tile tile) {
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLUMNS; y++) {
                if (tiles[x][y] == tile) {
                    return y;
                }
            }
        }
        
        return -1;
    }
    
    private boolean collisionOccured(Tile tile) {
        return !tile.isEmpty();
    }
    
    private boolean canBeCastled(Tile start, final int destinationColumn) {
        return start.getPiece() instanceof King && (destinationColumn == 2 || destinationColumn == -2) && start.getPiece().getColor() == currentPlayer;
    }
    
                // ----------  COLOR RELATED FUNCTIONS  ---------- \\
    
    private void initializeTileColors() {
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < COLUMNS; col++){
                Node curr = getNodeByRowColumnIndex(row, col);
                assignStartingColorOnTile(row, col, curr);
                TILE_NODE.put(tiles[row][col], curr);
            }
        }
    }
   
    private void resetPieceColors() {
        if (startTile == null) {        // if no piece clicked, don't reset.
            return;
        }
        setTileColor(startTile, startTile.getDefaultTileColor());
        
        for (Tile tile : legalMoves) {
            setTileColor(tile, tile.getDefaultTileColor());
        }
    }
    
    private void setTileColor(Tile tile, final String color) {
        final Node CURRENT_NODE = TILE_NODE.get(tile);
        CURRENT_NODE.setStyle("-fx-background-color: " +color);
    }
    
    private void assignStartingColorOnTile(int row, int col, Node cur) {
        String COLOR = "";

        if (row % 2 == 0 && col % 2 == 0)
            COLOR = LIGHT;
        else if (row % 2 == 0 && col % 2 == 1)
            COLOR = DARK;
        else if (row % 2 == 1 && col % 2 == 0)
            COLOR = DARK;
        else
            COLOR = LIGHT;

        cur.setStyle("-fx-background-color: " +COLOR);
        tiles[row][col].setDefaultTileColor(COLOR);
    }
    
    private void setColorsRelatedToPiece() {
        setTileColor(startTile, CLICKED_COLOR);
        
        for (Tile tile : legalMoves) {
            if (!tile.isEmpty())
                setTileColor(tile, ENEMY_IN_AVAILABLE_TILE_COLOR);
            else
                setTileColor(tile, AVAILABLE_TILE_COLOR);
        }
    }
}