import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener{

    public class Block {
    //additional class for all objects in game

        //parameters of image shown on a screen
        int x;
        int y;
        int startX;
        int startY;
        int width;
        int height;
        Image image;

        //direction and velocity the object is moving
        //directionQueue tells what direction object wants to start moving
        //possible directions:
        //'S' - Stop, 'R' - right, 'L' - left, 'U' - up, 'D' - down
        char directionQueue = 'S';
        char direction = 'S';
        int velocityX = 0;
        int velocityY = 0;

        public Block(int x, int y, int width, int height, Image image) {
            this.y = y;
            this.x = x;
            this.startX = x;
            this.startY = y;
            this.width = width;
            this.height = height;
            this.image = image;
        }

        void addDirectionToQueue(char direction){
            directionQueue = direction;
        }

        char[] getPossibleDirections(){
        //return list of possible directions that object can move into

            List<String> directionsList = new ArrayList<>();
            for(char d: new char[]{'D','U','L','R'} ){
                if(this.checkDirection(d)){
                    directionsList.add(String.valueOf(d));
                }
            }

            char[] directions  = new char[directionsList.size()];
            int i=0;
            for (String s:directionsList){
                directions[i] = s.toCharArray()[0];
                i++;
            }

            return directions;
        }

        boolean checkDirection(char d){
            //checks if object can move in given direction
            //if it's true then true is return
            //false if otherwise
            boolean isCollision = false;
            char prevDirection = this.direction;

            this.direction = d;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            for(Block block : walls){
                if (collision(this,block)){
                    isCollision = true;
                    break;
                }
            }

            this.x -= this.velocityX;
            this.y -= this.velocityY;
            this.direction = prevDirection;
            updateVelocity();

            return !isCollision;
        }

        void updateDirection(){
            //checks if object can change direction into directionQueue value
            //if so changes velocity and direction of object.
            if (this.directionQueue != this.direction){
                if(checkDirection(this.directionQueue)){
                    this.direction = this.directionQueue;
                    updateVelocity();
                }
            }
        }

        void updateVelocity(){
            //updates velocity of object given his direction
            if (this.direction == 'U'){
                this.velocityX = 0;
                this.velocityY = -tilesize/4;
            }
            else if(this.direction == 'D'){
                this.velocityX = 0;
                this.velocityY = tilesize/4;
            }
            else if(this.direction == 'L'){
                this.velocityX = -tilesize/4;
                this.velocityY = 0;
            }
            else if(this.direction == 'R'){
                this.velocityX = tilesize/4;
                this.velocityY = 0;
            } else if (this.direction == 'S') {
                this.velocityX =0;
                this.velocityY =0;
            }
        }

        void move(){
        // moves object according to its direction.
        // checks for collision and goes back if it's detected.

            this.x += this.velocityX;
            this.y += this.velocityY;

            for (Block block:walls){
                if(collision(this,block)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    break;
                }
            }

            if(this.x >= boardWidth){
                this.x = -tilesize + (this.x - boardWidth);
            } else if (this.x<=-tilesize) {
                this.x = boardWidth - (-tilesize - this.x);
            }
            if(this.y >= boardHeight){
                this.y = -tilesize + (this.y-boardHeight);
            } else if (this.y<=-tilesize) {
                this.y = boardHeight + tilesize + this.y;
            }
        }

        void reset(){
            //resets the block to starting position
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    //board size parameters
    //must match with titleMap size
    private final int tilesize = 32;
    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int boardWidth = columnCount * tilesize;
    private final int boardHeight = rowCount * tilesize;

    private final int foodsize = 4;

    //images of all objects in game
    private Image wallImage;
    private Image blueGhostImage;
    private Image brownGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image pacManUpImage;
    private Image pacManDownImage;
    private Image pacManLeftImage;
    private Image pacManRightImage;

    //information about all objects in game
    private HashSet<Block> walls;
    private HashSet<Block> foods;
    private HashSet<Block> ghosts;
    private Block pacman;

    //testing varibles
    boolean isHighlighted=false;
    private int highlightTileX;
    private int highlightTileY;


    //Timer and random seed
    private Timer gameloop;
    private Random random = new Random();

    //Timer parameters
    private int innerTimer=0;
    private final int delay = 50; //time between actions
    private final int actionsPerSecond = 1000/delay;
    private final int scatterTime=7;
    private final int chaseTime= 20;
    private final int pacmanHitTime=3;
    private final int getReadyTime=3;

    //various game parameters
    int score = 0;
    int lives = 3;
    boolean isGameOver = false;
    boolean isGameWon = false;
    boolean isScatter = true;
    boolean isPacmanHit = false;
    boolean isGameReady = false;


    //initial loading map
    // X - walls, O - skip, P - pacman
    // E - exit (exit must be placed on edge and not in the corner. Opposite tile must also be exit).
    // r,b,p,o - ghosts, ' ' - food
    private char[][] tileMap = {
            {'X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X'},
            {'X',' ',' ',' ',' ',' ',' ',' ',' ','X',' ',' ',' ',' ',' ',' ',' ',' ','X'},
            {'X',' ','X','X',' ','X','X','X',' ','X',' ','X','X','X',' ','X','X',' ','X'},
            {'X',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','X'},
            {'X',' ','X','X',' ','X',' ','X','X','X','X','X',' ','X',' ','X','X',' ','X'},
            {'X',' ',' ',' ',' ','X',' ',' ',' ',' ',' ',' ',' ','X',' ',' ',' ',' ','X'},
            {'X','X','X','X',' ','X','X','X','X',' ','X','X','X','X',' ','X','X','X','X'},
            {'X','O','O','X',' ','X',' ',' ',' ',' ',' ',' ',' ','X',' ','X','O','O','X'},
            {'X','X','X','X',' ','X',' ','X','X','r','X','X',' ','X',' ','X','X','X','X'},
            {'E',' ',' ',' ',' ',' ',' ',' ','b','p','o',' ',' ',' ',' ',' ',' ',' ','E'},
            {'X','X','X','X',' ','X',' ','X','X','X','X','X',' ','X',' ','X','X','X','X'},
            {'X','O','O','X',' ','X',' ',' ',' ',' ',' ',' ',' ','X',' ','X','O','O','X'},
            {'X','X','X','X',' ','X',' ','X','X','X','X','X',' ','X',' ','X','X','X','X'},
            {'X',' ',' ',' ',' ',' ',' ',' ',' ','X',' ',' ',' ',' ',' ',' ',' ',' ','X'},
            {'X',' ','X','X',' ','X','X','X',' ','X',' ','X','X','X',' ','X','X',' ','X'},
            {'X',' ','X',' ',' ',' ',' ',' ',' ','P',' ',' ',' ',' ',' ',' ','X',' ','X'},
            {'X','X',' ','X',' ','X',' ','X','X','X','X','X',' ','X',' ','X',' ','X','X'},
            {'X',' ',' ',' ',' ','X',' ',' ',' ','X',' ',' ',' ','X',' ',' ',' ',' ','X'},
            {'X',' ','X','X','X','X','X','X',' ','X',' ','X','X','X','X','X','X',' ','X'},
            {'X',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','X'},
            {'X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X','X'}
    };

    PacMan(){

        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setBackground(Color.BLACK);

        //load images
        wallImage = new ImageIcon("images/wall.png").getImage();
        blueGhostImage = new ImageIcon("images/blueGhost.png").getImage();
        brownGhostImage = new ImageIcon("images/orangeGhost.png").getImage();
        pinkGhostImage = new ImageIcon("images/pinkGhost.png").getImage();
        redGhostImage = new ImageIcon("images/redGhost.png").getImage();
        pacManRightImage = new ImageIcon("images/pacmanRight.png").getImage();
        pacManLeftImage = new ImageIcon("images/pacmanLeft.png").getImage();
        pacManUpImage = new ImageIcon("images/pacmanUp.png").getImage();
        pacManDownImage = new ImageIcon("images/pacmanDown.png").getImage();

        loadMap();
        for (Block ghost: ghosts){
            ghost.direction = randomDirection();
            ghost.addDirectionToQueue(randomDirection());
            ghost.updateVelocity();
//            System.out.println(ghost.image+":"+ghost.direction+" "+ghost.directionQueue);
        }
//        System.out.println(walls.size());
//        System.out.println(foods.size());
//        System.out.println(ghosts.size());

        addKeyListener(this);
        setFocusable(true);

        gameloop = new Timer(delay,this); //20 FPS
        gameloop.start(); //to działa niezależnie i asynchronicznie z resztą programu

    }

    private void loadMap(){
    //loads map according to tileMap array
    //adds invisible walls between exits from map
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for(int r=0;r<rowCount;r++){
            for(int c=0;c<columnCount;c++){
                char tileMapChar = tileMap[r][c];
                int x = c*tilesize;
                int y = r*tilesize;
                if(tileMapChar=='X'){
                    walls.add(new Block(x,y,tilesize,tilesize,wallImage));
                }
                else if(tileMapChar=='b'){
                    ghosts.add(new Block(x,y,tilesize,tilesize,blueGhostImage));
                }
                else if(tileMapChar=='p'){
                    ghosts.add(new Block(x,y,tilesize,tilesize,pinkGhostImage));
                }
                else if(tileMapChar=='o'){
                    ghosts.add(new Block(x,y,tilesize,tilesize, brownGhostImage));
                }
                else if(tileMapChar=='r'){
                    ghosts.add(new Block(x,y,tilesize,tilesize,redGhostImage));
                }
                else if(tileMapChar=='P'){
                    pacman = new Block(x,y,tilesize,tilesize,pacManRightImage);
                }
                else if(tileMapChar==' '){
                    foods.add(new Block(x+(tilesize-foodsize)/2,y+(tilesize-foodsize)/2,foodsize,foodsize,null));
                }
                else if(tileMapChar=='E'){
                    foods.add(new Block(x+(tilesize-foodsize)/2,y+(tilesize-foodsize)/2,foodsize,foodsize,null));
                    if(c==0){
                        walls.add(new Block(x-tilesize,y-tilesize,tilesize,tilesize,wallImage));
                        walls.add(new Block(x-tilesize,y+tilesize,tilesize,tilesize,wallImage));
                    }
                    else if(c==columnCount-1){
                        walls.add(new Block(x+tilesize,y-tilesize,tilesize,tilesize,wallImage));
                        walls.add(new Block(x+tilesize,y+tilesize,tilesize,tilesize,wallImage));
                    }
                    else if (r==0) {
                        walls.add(new Block(x+tilesize,y-tilesize,tilesize,tilesize,wallImage));
                        walls.add(new Block(x-tilesize,y-tilesize,tilesize,tilesize,wallImage));
                    } else if (r==rowCount-1) {
                        walls.add(new Block(x+tilesize,y+tilesize,tilesize,tilesize,wallImage));
                        walls.add(new Block(x-tilesize,y+tilesize,tilesize,tilesize,wallImage));
                    }
                }
            }
        }

    }

    private void movePacman(){
    //checks queued direction of PacMan and changes it if possible
    //move pacmans once according to velocity
    //changes image of PacMan so it matches direction
    //handles collison with food
        pacman.updateDirection();
        pacman.move();

        Block foodeaten = null;
        for (Block food:foods){
            if(collision(pacman,food)){
                foodeaten = food;
                score += 1;
                break;
            }
        }
        foods.remove(foodeaten);

        if(pacman.direction == 'U'){
            pacman.image = pacManUpImage;
        } else if (pacman.direction == 'D') {
            pacman.image = pacManDownImage;
        } else if (pacman.direction == 'R') {
            pacman.image = pacManRightImage;
        } else if (pacman.direction == 'L') {
            pacman.image = pacManLeftImage;
        }

    }

    private void moveGhosts(){
    // decides which direction should each of the ghosts move
    // and moves them accordingly

        //changes mode between scatter and change
        boolean isModeChanged;
        isModeChanged  = changeMode();

        for(Block ghost:ghosts){

            List <String> preferedGhostMoves = new ArrayList<>();

            //testowo
//            if(ghost.image != redGhostImage ){
//                continue;
//            }

            if(isModeChanged){
                ghost.addDirectionToQueue(oppositeDirection(ghost.direction));
                ghost.updateDirection();
            }
            else {

                if (ghost.image == redGhostImage) {
                    getRedDirection(preferedGhostMoves, ghost);
                }
                if (ghost.image == pinkGhostImage) {
                    getPinkDirection(preferedGhostMoves, ghost);
                }
                if (ghost.image == blueGhostImage) {
                    getBlueDirection(preferedGhostMoves, ghost);
                }
                if (ghost.image == brownGhostImage) {
                    getBrownDirection(preferedGhostMoves, ghost);
                }
                setGhostDirection(preferedGhostMoves, ghost);
            }

            ghost.move();
        }
    }

    private void setGhostDirection(List<String> preferedGhostMoves, Block ghost){
    // sets ghost direction prioritizing directions in the list
    // direction opposite to current can never be chosen unless it's only one possible

        char[] directions = ghost.getPossibleDirections();

        if (directions.length==1){
            ghost.addDirectionToQueue(directions[0]);
        }
        else{
            int n;
            List <String> priorityList = new ArrayList<>();
            List <String> secondaryList = new ArrayList<>();

            for(char d:directions){
                if (d==oppositeDirection(ghost.direction)){
                    continue;
                }
                if( preferedGhostMoves.contains(String.valueOf(d)) ){
                    priorityList.add(String.valueOf(d));
                }
                else{
                    secondaryList.add(String.valueOf(d));
                }
            }

            if (!priorityList.isEmpty()){
                n = random.nextInt(priorityList.size());
                ghost.addDirectionToQueue(priorityList.get(n).toCharArray()[0]);
            }
            else{
                n = random.nextInt(secondaryList.size());
                ghost.addDirectionToQueue(secondaryList.get(n).toCharArray()[0]);
            }
        }
        ghost.updateDirection();

    }

    private void getChaseDirection(List<String> preferedMoves, int chaseTileX, int chaseTileY, int blockPosX, int blockPosY){
        // adds to list directions that will move object into direction of
        // a given tile
            if (blockPosX-chaseTileX>0){
                preferedMoves.add("L");
            }
            if (blockPosX-chaseTileX<0){
                preferedMoves.add("R");
            }
            if(blockPosY-chaseTileY>0){
                preferedMoves.add("U");
            }
            if(blockPosY-chaseTileY<0){
                preferedMoves.add("D");
            }
    }

    private void getRedDirection(List<String> preferedGhostMoves, Block ghost){
        //adds directions to the list that are prefered for chasing pacman
            int chaseTileX,chaseTileY;
            if(!isScatter) {
                chaseTileX = pacman.x;
                chaseTileY = pacman.y;
            }
            else{
                chaseTileX = (tileMap[0].length -1)*tilesize;
                chaseTileY = 0;
            }
            getChaseDirection(preferedGhostMoves,chaseTileX,chaseTileY, ghost.x,ghost.y);
    }
    

    private void getPinkDirection(List<String> preferedGhostMoves, Block ghost){
      //adds directions that are prefered for chasing tile 4 spaces ahead of pacman
            int chaseTileX,chaseTileY;
            if(!isScatter) {
                chaseTileX = pacman.x + tilesize * 4 * Integer.signum(pacman.velocityX);
                chaseTileY = pacman.y + tilesize * 4 * Integer.signum(pacman.velocityY);
            }
            else{
                chaseTileX = 0;
                chaseTileY = 0;
            }

            getChaseDirection(preferedGhostMoves, chaseTileX, chaseTileY, ghost.x, ghost.y);
    }

    private void getBlueDirection(List<String> preferedGhostMoves, Block ghost){

            int chaseTileX, chaseTileY;
            int redGhostX=0, redGhostY=0;
            for(Block b:ghosts){
                if(b.image == redGhostImage){
                    redGhostX = b.x;
                    redGhostY = b.y;
                }
            }
            if(!isScatter) {
                chaseTileX = redGhostX + (pacman.x - redGhostX) * 2;
                chaseTileY = redGhostY + (pacman.y - redGhostY) * 2;
            }
            else{
                chaseTileX = (tileMap[0].length -1)*tilesize;
                chaseTileY = (tileMap.length-1) * tilesize;
            }

            getChaseDirection(preferedGhostMoves, chaseTileX, chaseTileY, ghost.x, ghost.y);
    }

    private void getBrownDirection(List<String> preferedGhostMoves, Block ghost){

        int chaseTileX, chaseTileY;

        if( Math.hypot(ghost.x - pacman.x, ghost.y- pacman.y )<5*tilesize || isScatter){
            chaseTileX = 0;
            chaseTileY = (tileMap.length-1) * tilesize;
        }
        else{
            chaseTileX = pacman.x;
            chaseTileY = pacman.y;
        }

            getChaseDirection(preferedGhostMoves, chaseTileX, chaseTileY, ghost.x, ghost.y);
    }

    private boolean changeMode(){
        //changes ghost behaviour between chase and scatter based on timer
        //returns true if mode was change

        //timer intervals as class variables??
        if(isScatter && innerTimer==actionsPerSecond*scatterTime){
            isScatter = false;
            innerTimer = 0;
            System.out.println("Chase mode");
            return true;
        }
        if(!isScatter && innerTimer==actionsPerSecond*chaseTime ){
            isScatter = true;
            innerTimer = 0;
            System.out.println("Scatter mode");
            return true;
        }
        return false;
    }

    private void highlightTile(int highlightTileX,int highlightTileY){
        this.highlightTileX = highlightTileX;
        this.highlightTileY = highlightTileY;
        this.isHighlighted = true;

    }


    private void cheatCode(){
        foods.removeAll(foods);
    }

    private void waitPacmanHit(){
        //what happends during time after pacman was hit
        if(innerTimer>=pacmanHitTime*actionsPerSecond){
            isPacmanHit = false;
            isGameReady = false;
            innerTimer = 0;
            if (lives==0){
                isGameOver = true;
            }
            resetPositions();
        }

    }

    private void waitReadyScreen(){
        if(innerTimer>=getReadyTime*actionsPerSecond){
            isGameReady = true;
            isScatter = true;
            innerTimer = 0;
        }

    }

    private void checkPacmanHit(){
        //check if Pacman collides with ghosts
        // handles all consequences of a hit
        for (Block ghost:ghosts){
            if(collision(pacman,ghost)){
                lives-=1;
                innerTimer = 0;
                isPacmanHit = true;

            }
        }
    }

    private void checkFoodEaten(){

        if(foods.isEmpty()){
              isGameWon = true;
//            loadMap();
//            resetPositions();
        }
    }


    private void resetPositions(){
        pacman.reset();
        pacman.addDirectionToQueue('S');
        for (Block ghost:ghosts){
            ghost.reset();
            ghost.direction = randomDirection();
            ghost.addDirectionToQueue(randomDirection());
        }
    }

    private boolean collision(Block a,Block b) {
        //returns true if block collide with each other
        //false otherwise
        return (a.x < b.x + b.width &&
                b.x < a.x + a.width &&
                a.y < b.y + b.height &&
                b.y < a.y + a.height);
    }
    private char randomDirection(){

        switch(random.nextInt(4)){
            case 0: return 'U';
            case 1: return 'D';
            case 2: return 'R';
            case 3: return 'L';
            default: return 'S';
        }
    }

    private char oppositeDirection(char d){

        switch(d){
            case 'U': return 'D';
            case 'D': return 'U';
            case 'L': return 'R';
            case 'R': return 'L';
            default: return 'S';
        }
    }

    // this gets called automaticly somewhere
    //in constructor maybe?
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(!isPacmanHit) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }
        else{
            float alpha;
            alpha = 1 - (float) (innerTimer % (actionsPerSecond/2)) /(actionsPerSecond/2);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(pacman.image, pacman.x, pacman.y,pacman.width,pacman.height, this);
            g2d.dispose();
        }
        for (Block ghost: ghosts){
            g.drawImage(ghost.image,ghost.x,ghost.y,ghost.width, ghost.height, null);
        }
        for (Block wall:walls){
            g.drawImage(wall.image,wall.x,wall.y,wall.width,wall.height, null);

        }
        g.setColor(Color.WHITE);
        for (Block food:foods){
            g.fillRect(food.x,food.y,food.width,food.height);
        }
        g.setFont(new Font("Ariel", Font.PLAIN, tilesize/2));
        g.drawString("Score: "+String.valueOf(score), tilesize/2,tilesize/2);
        g.drawString("Lives : "+String.valueOf(lives), tilesize/2,tilesize);

        //testing: highlights a tile
        if(isHighlighted){
            g.setColor(Color.RED);
            g.fillRect(highlightTileX,highlightTileY,tilesize,tilesize);
        }

        if(!isGameReady && !isGameOver && !isGameWon){
            g.setFont(new Font("Ariel", Font.PLAIN, 40)); //stała do dodania
            int strLenght = g.getFontMetrics().stringWidth("READY");
            g.setColor(Color.WHITE);
            g.drawString("READY", (boardWidth/2)-strLenght/2,(boardHeight/2));

        }
        if(isGameOver){
            g.setFont(new Font("Ariel", Font.PLAIN, 40)); //stała do dodania
            int strLenght = g.getFontMetrics().stringWidth("Game Over");
            g.setColor(Color.RED);
            g.drawString("Game Over", (boardWidth/2)-strLenght/2,(boardHeight/2));

        }

        if(isGameWon){
            g.setFont(new Font("Ariel", Font.PLAIN, 40)); //stała do dodania
            int strLenght = g.getFontMetrics().stringWidth("Game Won");
            g.setColor(Color.GREEN);
            g.drawString("Game Won", (boardWidth/2)-strLenght/2,(boardHeight/2));
        }

    }


    @Override
    public void actionPerformed(ActionEvent e) {
    // Main game loop
    // moves each object, then checks for pacman hit and game end
        innerTimer++;

        if(isPacmanHit){
            waitPacmanHit();
        }
        else if(!isGameReady){
            waitReadyScreen();
        }
        else {
            movePacman();
            moveGhosts();
            checkPacmanHit();
            checkFoodEaten();
        }
        repaint();
        if(isGameOver || isGameWon){
            gameloop.stop();}
        }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {

//        if(gameover){
//            loadMap();
//            lives = 3;
//            score = 0;
//            resetPositions();
//            gameover = false;
//            gameloop.start();
//        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            pacman.addDirectionToQueue('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN){
            pacman.addDirectionToQueue('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT){
            pacman.addDirectionToQueue('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            pacman.addDirectionToQueue('R');
        }
        //testowanie
        else if (e.getKeyCode() == KeyEvent.VK_Q){
            cheatCode();
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
