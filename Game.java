/*
github.jasoncodes.ca
Rooster Rush


This is my side-scroller Rooster Rush.
The objective is to survive the "egg invasion" for 2 minutes, whilst also avoiding incoming foxes.
The foxes do 2 damage.
The player starts with 3 health and 3 bombs.
The bombs regenerate over time.
There are other power-ups to collect that will spawn throughout the game.

**CHEAT CODES**
   BACKSPACE: Go to victory screen.
   8: Increase HP.
   9: Increase bombs.
   0: Activate ghost pepper.
*/

import javax.sound.sampled.Clip;

public class Game {
    // Grid
    private Grid grid;
    private final int rowBounds = 10;
    private final int colBounds = rowBounds + rowBounds / 2;

    // Player
    private int playerRow  = 1;
    private int playerCol  = 0;
    private int playerHealth = 3;
    private int score = 0;
    private String playerSprite = "./sprites/chicken.png";
    private String runningPlayer = "./sprites/runningChicken.png";

    // Controls
    private int moveUp;
    private int moveDown;
    private int moveLeft;
    private int moveRight;

    // Obstructions / Items

    // The itemIDs are the indexes for the itemTable. Each index represents an item spawn rate (Out of 100).
    // ItemID list: 0:Egg, 1:Fox, 2:Worm, 3:GoldenWorm, 4:Heart, 5:Bomb, 6:Ghost pepper
    private double[] itemTable = new double[]{70, 7, 10, 1.5, 0.5, 0.5, 0.5};
    private final String eggSprite = "./sprites/egg.png";
    private final String crackedEggSprite = "./sprites/crackedEgg.png";
    private final String wormSprite = "./sprites/worm.png";
    private final String goldWormSprite = "./sprites/goldWorm.png";
    private final String heartSprite = "./sprites/heart.png";
    private final String foxStillSprite = "./sprites/foxStill.png";
    private final String foxRunSprite = "./sprites/foxRun.png";
    private final String foxSprite = foxStillSprite;
    private final String bombSprite = "./sprites/eggBomb.png";
    private final String explosionSprite = "/sprites/explosion.png";
    private final String ghostPepperSprite = "./sprites/ghostPepper.png";
    private final String smFireSprite = "./sprites/smFire.png";
    private final String mdFireSprite = "./sprites/mdFire.png";
    private final String fireSound = "./tunes/fire.wav";
    private final String clockSprite = "./sprites/clock.png";
    private int bombs = 2;
    private boolean levelTwo;
    private boolean ghostPepperActive;
    private int ghostPepperTimer;

    // Game Engine
    private int msElapsed = 0;
    private final int gameSpeed = 100;
    private long timeAtGameStart;
    private long timeAlive;
    private boolean gameOver;
    private Clip OST;

    public Game() {
        // Grid (Game Size)
        grid = new Grid(rowBounds, colBounds);
        updateTitle();
    }

    public void play() {
        setupGame();
        while (!gameOver) {
            // Critical
            Grid.pause(gameSpeed);
            timeAlive = (System.currentTimeMillis() - timeAtGameStart) / 1000;

            // Movement of Things
            handleKeyPress();
            if (msElapsed % 200 == 0) {
                scroll();
            }
            // Game Updates
            handleDifficulty();
            drawPlayer();
            updateTitle();
            msElapsed += gameSpeed;

            if (playerHealth <= 0){
                gameOver = true;
            }
        }
        if (playerHealth <= 0) {
            OST.close();
            playSound("./tunes/death.wav");
            grid.showMessageDialog("Game Over! :(\nYou survived for " + timeAlive + " seconds!");
        } else {
            gameVictory();
        }
    }

    private void setupGame() {
        playSound("./tunes/menuOpen.wav");
        grid.showMessageDialog("You win if you can survive for 2 minutes\nGet hit too many times, and you're dead.");
        playSound("./tunes/menuTick.wav");
        grid.showMessageDialog("Press (space) to use a bomb.\nYour bombs regenerate over time.");
        playSound("./tunes/menuTick.wav");
        setGameControls();
        boolean muted = false;
        if (!muted) {
            String gameTheme = "./tunes/Slide (8-Bit Cover) - Super Mario 64.wav";
            OST = mySound.playSound(gameTheme);
        }
        timeAtGameStart = System.currentTimeMillis();
    }

    private void playSound(String url){
        GameStuff.playSound(url);
    }

    private void setGameControls() {
        String[] controlOptions = {"Modern (WASD)", "Classic (Arrow Keys)"};
        int chosenControl = grid.showConfirmDialog("What control scheme would you like to use?", "Control Setup", controlOptions);
        playSound("./tunes/menuClose.wav");
        if (chosenControl == 0) {
            moveUp = 87;
            moveDown = 83;
            moveLeft = 65;
            moveRight = 68;
        } else {
            moveUp = 38;
            moveDown = 40;
            moveLeft = 37;
            moveRight = 39;
        }
    }

    private void handleKeyPress() {
        drawNull(playerRow, playerCol);
        int key = grid.checkLastKeyPressed();
        switch (key){
            // cheats
            case 8: gameOver = true; break;
            case 56: playerHealth++; break;
            case 57: bombs++; break;
            case 48: useGhostPepper(); break;
        }
        // W and S key handlers
        int playerSpeed = 1;
        if (playerRow - 1 >= 1 && key == moveUp) {
            playerRow -= playerSpeed;
        } else if (playerRow + 1 < rowBounds - 1 && key == moveDown) {
            playerRow += playerSpeed;
        }
        // A and D key handlers
        if (playerCol - 1 >= 0 && key == moveLeft) {
            playerCol -= playerSpeed;
        } else if (playerCol + 1 < colBounds && key == moveRight) {
            playerCol += playerSpeed;
        }
        // Space key handler
        if (key == 32) {
            if (bombs != 0) {
                useBomb();
            }
        }
    }

    private void drawPlayer(){
        if (grid.getImage(new Location(playerRow, playerCol)) != null){
            handleCollision(new Location(playerRow, playerCol));
        }
        // simulate running animation
        if (msElapsed % 200 == 0) {
            drawSprite(playerRow, playerCol, runningPlayer);
        } else {
            drawSprite(playerRow, playerCol, playerSprite);
        }
        drawBottomBar();
        if (msElapsed % 20000 == 0) {
            bombs++;
        }
        if (ghostPepperActive) {
            drawFire();
        }
        if (ghostPepperActive && msElapsed - ghostPepperTimer >= 4000){
            ghostPepperActive = false;
            ghostPepperTimer = 0;
        }
    }



    private void drawNull(int row, int col){
        grid.setImage(new Location(row, col), null);
    }

    private void drawSprite(int row, int col, String sprite) {
        grid.setImage(new Location(row, col), sprite);
    }

    private void drawSprite(Location loc, String sprite) {
        grid.setImage(loc, sprite);
    }

    private void drawBottomBar() {

        int bottomRow = rowBounds - 1;
        int heartTens = playerHealth / 10;
        int heartOnes = playerHealth % 10;
        int bombTens = bombs / 10;
        int bombOnes = bombs % 10;
        int scoreHundreds = score / 100;
        int scoreTens = (score % 100) / 10;
        int scoreOnes = score % 10;
        int timeHundreds = (int)(timeAlive / 100);
        int timeTens = (int)((timeAlive % 100) / 10);
        int timeOnes = (int)(timeAlive % 10);


        drawSprite(bottomRow, 0, heartSprite);
        drawSprite(bottomRow, 1, "./sprites/" + heartTens + ".png");
        drawSprite(bottomRow, 2, "./sprites/" + heartOnes + ".png");
        drawSprite(bottomRow, 3, bombSprite);
        drawSprite(bottomRow, 4, "./sprites/" + bombTens + ".png");
        drawSprite(bottomRow, 5, "./sprites/" + bombOnes + ".png");
        drawSprite(bottomRow, 6, wormSprite);
        drawSprite(bottomRow, 7, "./sprites/" + scoreHundreds + ".png");
        drawSprite(bottomRow, 8, "./sprites/" + scoreTens + ".png");
        drawSprite(bottomRow, 9, "./sprites/" + scoreOnes + ".png");
        drawSprite(bottomRow, 10, clockSprite);
        drawSprite(bottomRow, 11, "./sprites/" + timeHundreds + ".png");
        drawSprite(bottomRow, 12, "./sprites/" + timeTens + ".png");
        drawSprite(bottomRow, 13, "./sprites/" + timeOnes + ".png");
    }

    private void useBomb() {
        // Using a bomb will draw circle of nulls around player until covering whole screen, use grid.pause to simulate wave effect
        bombs--;
        ghostPepperActive = false;
        playSound("./tunes/bomb.wav");
        int bombSize = 12;
        for (int circleSize = 0; circleSize <= bombSize; circleSize++) {
            for (int row = 0; row < rowBounds; row++) {
                for (int col = 0; col < colBounds; col++) {
                    // Check if the current tile is on the circle.
                    if (Math.pow((row - playerRow), 2) + Math.pow((col - playerCol), 2) < Math.pow(circleSize / 2.0, 2)){
                        String currentSprite = grid.getImage(new Location(row, col));
                        if (!playerSprite.equals(currentSprite) && (eggSprite.equals(currentSprite)
                                                                || crackedEggSprite.equals(currentSprite)
                                                                || foxSprite.equals(currentSprite)
                                                                || wormSprite.equals(currentSprite)
                                                                || currentSprite == null)) {
                            drawSprite(row, col, explosionSprite);
                        }
                    }
                }
            }
            Grid.pause(10); // Simulate a "wave" effect when using a bomb.
            for (int row = 0; row < rowBounds; row++) {
                for (int col = 0; col < colBounds; col++) {
                    // Check if the current tile is on the circle.
                    if (Math.pow((row - playerRow), 2) + Math.pow((col - playerCol), 2) < Math.pow(circleSize / 2.0, 2)) {
                        String currentSprite = grid.getImage(new Location(row, col));
                        if (explosionSprite.equals(currentSprite)) {
                            drawNull(row, col);
                        }
                    }
                }
            }
        }

    }

    private void useGhostPepper() {
        ghostPepperActive = true;
        ghostPepperTimer = msElapsed;
        drawFire();
    }

    private void drawAroundPlayer(String sprite) {
        int[][] offsets = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};
        for (int[] offset : offsets) {
            checkAndDraw(offset[0], offset[1], sprite);
        }
    }

    private void checkAndDraw(int rowOffset, int colOffset, String sprite) {
        int checkRow = playerRow + rowOffset;
        int checkCol = playerCol + colOffset;
        Location checkLoc = new Location(checkRow, checkCol);

        if (checkRow >= 1 && checkRow < rowBounds - 1 && checkCol >= 0 && checkCol < colBounds - 1) {
            String checkSprite = grid.getImage(checkLoc);
            if (checkSprite == null || eggSprite.equals(checkSprite) || foxSprite.equals(checkSprite) || smFireSprite.equals(checkSprite) || mdFireSprite.equals(checkSprite)) {
                drawSprite(checkRow, checkCol, sprite);
            }
        }
    }

    private void drawFire() {
        String fireSprite;
        if (msElapsed % 200 == 0) {
            // draw small size flame
            playSound(fireSound);
            fireSprite = smFireSprite;
        } else {
            // draw mid size flame
            fireSprite = mdFireSprite;
        }
        drawAroundPlayer(fireSprite);
    }

    private void handleDifficulty() {
        if (timeAlive == 120) {
            gameOver = true;
        }
        if (timeAlive >= 110) { // frame at 5281920
            /*
             * Level 5 (IMPOSSIBLE) 100s:**
             * Fox Invasion: only foxes spawn.
             */
            spawnItem(0); // Spawn egg
        }
        if (timeAlive == 99) {
            // Deal with music
            OST.close();
            OST = mySound.playSound("./tunes/fastSlide.wav");
        }
        if (timeAlive >= 80) {
            /*
             * Level 4 (Super Hard) 100s:**
             * Fox Invasion: only foxes spawn.
             */
            itemTable[1] = 13; // Update spawn-rate of foxes.

        }
        if (timeAlive >= 60) {
            /*
             * **Level 3 (Hard) 60s:**
             *   Foxes now spawn which do 3 damage
             *   Hearts no longer spawn.
             */
            itemTable[4] = 0.25; // Update spawn-rate of hearts
            spawnItem(1); // Spawn fox
        }
        if (timeAlive >= 30) {
            /*
             * **Level 2 (Medium) 30s:**
             *   Obstacles spawn more often.
             *   Eggs spawn on the same line as the player now.
             */
            spawnItem(3); // Spawn gold worm
            levelTwo = true;
        }
        spawnItem(0); // Spawn egg
        spawnItem(2); // Spawn worm
        spawnItem(5); // Spawn bomb
        spawnItem(4); // Spawn heart
        spawnItem(6); // Spawn ghostpepper
    }

    private void spawnItem(int itemID) {
        // This method is used to calculate probability of things spawning.
        if (rollItem(itemTable[itemID])) {
            Location randLocation;
            if (itemID == 0) { // Egg Spawning
                randLocation = randomRowLocation(); // Only eggs will be spawned at the end of a row.
                if (levelTwo && msElapsed % 300 == 0) { // If the difficulty is levelTwo then spawn an egg on the player row.
                    drawSprite(playerRow, colBounds - 1, eggSprite);
                }
            } else if (itemID == 1) { // Fox Spawning
                randLocation = randomColLocation();
            } else {
                randLocation = randomLocation(); // Spawn collectables anywhere on the map
            }
            switch (itemID) {
                case 0:
                    drawSprite(randLocation, eggSprite);
                    break;
                case 1:
                    drawSprite(randLocation, foxSprite);
                    break;
                case 2:
                    drawSprite(randLocation, wormSprite);
                    break;
                case 3:
                    drawSprite(randLocation, goldWormSprite);
                    break;
                case 4:
                    drawSprite(randLocation, heartSprite);
                    break;
                case 5:
                    drawSprite(randLocation, bombSprite);
                    break;
                case 6:
                    drawSprite(randLocation, ghostPepperSprite);
                    break;
            }
        }
    }

    private boolean rollItem(double spawnRate){
        // This method is used to determine if an item will spawn.
        double randomNumber = Math.random() * 100;
        return randomNumber <= spawnRate;
    }

    private Location randomRowLocation() {
        // This method is used to randomly determine where an item spawns on the last column.
        int randRow = rowBounds - 1; // Limit spawns to the playing area by choosing a random location till its within bounds.
        while (randRow == rowBounds - 1) {
            randRow = (int)(Math.random() * (rowBounds - 1) + 1);
        }
        return new Location(randRow, colBounds - 1);
    }

    private Location randomColLocation() {
        // This method is used to randomly determine where an item spawns on the first row.
        int randCol = playerCol;
        while (randCol == playerCol || randCol > ((colBounds - 1) / 2)) {
            randCol = (int)(Math.random() * (colBounds - 1));
        }
        return new Location(0, randCol);
    }

    private Location randomLocation() {
        // This method is used to randomly determine where an item spawns.
        int randRow;
        int randCol;
        Location currentChoice;
        do {
            randRow = (int) (Math.random() * (rowBounds - 1) + 1);
            randCol = (int) (Math.random() * ((colBounds - 1) / 2));
            currentChoice = new Location(randRow, randCol);
        } while (randRow == playerRow && randCol == playerCol || grid.getImage(currentChoice) != null );
        return currentChoice;
    }


    private void scroll() {
        // Update each tile.
        for (int row = rowBounds - 1; row >= 0; row--) {
            for (int col = 0; col < colBounds; col++) {
                // Set the background
                Location currentL = new Location(row, col);
                grid.setColor(currentL, new Color(0, 110, 51));
                if (row == 0) {
                    grid.setColor(currentL, new Color(4, 75, 37));
                }
                if (row == rowBounds - 1) {
                    grid.setColor(currentL, new Color(128,128,128));
                }

                String currentSprite = grid.getImage(new Location(row, col));
                // Deal with fire afterburn.
                if(smFireSprite.equals(currentSprite)) {
                    drawSprite(row, col, mdFireSprite);
                } else if(mdFireSprite.equals(currentSprite)){
                    drawSprite(row, col, smFireSprite);
                }
                if (!ghostPepperActive && (smFireSprite.equals(currentSprite) || mdFireSprite.equals(currentSprite))) {
                    drawNull(row, col);
                }
                // Scroll eggs and foxes
                if (eggSprite.equals(currentSprite)) {
                    if (col == 0) {
                        drawNull(row, col);
                        continue;
                    }
                    // Don't overwrite items.
                    String offSetSprite = grid.getImage(new Location(row, col - 1));
                    if (foxRunSprite.equals(offSetSprite) || foxStillSprite.equals(offSetSprite) || bombSprite.equals(offSetSprite) || heartSprite.equals(offSetSprite) || ghostPepperSprite.equals(offSetSprite)) {
                        if(col == 1) {
                            drawNull(row, col);
                            continue;
                        }
                        drawNull(row, col);
                        drawSprite(row, col - 2, currentSprite);
                        continue;
                    } else if ( smFireSprite.equals(offSetSprite) || mdFireSprite.equals(offSetSprite)) {
                        drawNull(row, col);
                        continue;
                    }
                    drawNull(row, col);
                    drawSprite(row, col - 1, currentSprite);
                } else if (foxRunSprite.equals(currentSprite) || foxStillSprite.equals(currentSprite)) {
                    String offSetSprite = grid.getImage(new Location(row + 1, col));
                    if (row == rowBounds - 1 || smFireSprite.equals(offSetSprite) || mdFireSprite.equals(offSetSprite)) {
                        drawNull(row, col);
                        continue;
                    }
                    drawNull(row, col);
                    drawSprite(row + 1, col, foxSprite);
                }
            }
        }
    }

    private void handleCollision(Location loc) {
        int row = loc.getRow();
        int col = loc.getCol();
        String sprite = grid.getImage(new Location(row, col));
        if (eggSprite.equals(sprite) && !ghostPepperActive) { // egg collision detected
            playSound("./tunes/hurtChicken.wav");
            if (col != 0) {
                drawSprite(row, col - 1, crackedEggSprite);
            }
            playerHealth--;
        } else if (crackedEggSprite.equals(sprite)) {
            playerSprite = "./sprites/yokedChicken2.png";
            runningPlayer = "./sprites/runningYokedChicken.png";
        } else if (wormSprite.equals(sprite)) {
            playSound("./tunes/eat.wav");
            score++;
        } else if (goldWormSprite.equals(sprite)) {
            playSound("./tunes/goldWorm.wav");
            score += 5;
        } else if (heartSprite.equals(sprite)) {
            playSound("./tunes/heart.wav");
            if (playerHealth < 100) {
                playerHealth++;
            }
        } else if (foxSprite.equals(sprite) && !ghostPepperActive) {
            playSound("./tunes/hurtChicken.wav");
            playerHealth -= 2;
        } else if (bombSprite.equals(sprite)) {
            playSound("./tunes/getBomb.wav");
            if (bombs < 100) {
                bombs++;
            }
        } else if (ghostPepperSprite.equals(sprite)) {
            playSound("./tunes/eat.wav");
            useGhostPepper();
        }
    }

    private void gameVictory() {
        for (int i = 0; i < rowBounds; i++) {
            for (int n = 0; n < colBounds; n++){
                Location currentL = new Location(i, n);
                grid.setColor(currentL, new Color(0, 110, 51));
                drawNull(i, n);
            }
        }
        OST.close();
        grid.showMessageDialog("You win! Thanks for playing :)\nHit 'OK' for your victory party!");
        playSound("./tunes/victory.wav");
        while(true) {
            grid.pause(15);
            drawSprite(0, 0, "./sprites/kirbyRainbowVibe.gif");
            drawSprite(0, 14, "./sprites/kirbyRainbowVibe.gif");
            drawSprite(4, 5, "./sprites/dancingChicken.gif");
            drawSprite(4, 6, "./sprites/dancingChicken.gif");
            drawSprite(4, 7, "./sprites/dancingChicken.gif");
            drawSprite(4, 8, "./sprites/dancingChicken.gif");
            drawSprite(4, 9, "./sprites/dancingChicken.gif");
            drawSprite(5, 5, "./sprites/dancingChicken.gif");
            drawSprite(5, 6, "./sprites/dancingChicken.gif");
            drawSprite(5, 7, "./sprites/dancingChicken.gif");
            drawSprite(5, 8, "./sprites/dancingChicken.gif");
            drawSprite(5, 9, "./sprites/dancingChicken.gif");

            drawSprite(9, 10, "./sprites/kirbyDance.gif");
            drawSprite(8, 11, "./sprites/kirbyDance.gif");
            drawSprite(9, 12, "./sprites/kirbyDance.gif");
            drawSprite(8, 13, "./sprites/kirbyDance.gif");
            drawSprite(9, 14, "./sprites/kirbyDance.gif");
            drawSprite(9, 4, "./sprites/kirbyDance.gif");
            drawSprite(8, 3, "./sprites/kirbyDance.gif");
            drawSprite(9, 2, "./sprites/kirbyDance.gif");
            drawSprite(8, 1, "./sprites/kirbyDance.gif");
            drawSprite(9, 0, "./sprites/kirbyDance.gif");

            drawSprite(4, 14, "./sprites/cursedChicken.gif");
            drawSprite(4, 0, "./sprites/cursedChicken.gif");
            drawSprite(9, 7, "./sprites/dancingChickenOriginal.gif");

        }
    }

    private int getScore() {
        return score;
    }

    private int getHealth() {
        return playerHealth;
    }

    private long getTime() {
        return timeAlive;
    }

    private void updateTitle() {
        grid.setTitle("ROOSTER RUSH");
    }

    private static void test() {
        Game game = new Game();
        game.play();
    }

    private void logPress(int key){
        if (key != - 1) {
            System.out.println("\nPressed: ");
            log(key);
        }
    }

    private void log(int key){
        String direction;
        switch (key) {
            case 87:
                direction = "up";
                break;
            case 65:
                direction = "left";
                break;
            case 83:
                direction = "down";
                break;
            case 68:
                direction = "right";
                break;
            default:
                direction = "";
                break;
        }
        System.out.print(direction + "||" + System.currentTimeMillis());
    }

    public static void main(String[] args) {
        Game.test();
    }
}

/**
 References:
    Links:
        Creating showConfirmDialog https://mkyong.com/swing/java-swing-joptionpane-showoptiondialog-example/
        Checking if a point is on a circle: https://stackoverflow.com/questions/481144/equation-for-testing-if-a-point-is-inside-a-circle
    Sounds from: Super Mario 64, Mario Kart Wii, Minecraft, Terraria, and Final Fantasy
    OST from: https://www.youtube.com/watch?v=ccZtR6NqZBU
 */