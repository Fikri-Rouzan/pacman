import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y;
        int width, height;
        Image image;

        int startX, startY;
        char direction = 'U';
        int velocityX = 0, velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private final int rowCount = 22;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;

    private final Image wallImage;
    private final Image blueGhostImage;
    private final Image orangeGhostImage;
    private final Image pinkGhostImage;
    private final Image redGhostImage;

    private final Image pacmanUpImage;
    private final Image pacmanDownImage;
    private final Image pacmanLeftImage;
    private final Image pacmanRightImage;

    private final String[] tileMap = {
            "-------------------",
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        int boardHeight = rowCount * tileSize;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/wall.png"))).getImage();
        blueGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/blueGhost.png"))).getImage();
        orangeGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/orangeGhost.png"))).getImage();
        pinkGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/pinkGhost.png"))).getImage();
        redGhostImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/redGhost.png"))).getImage();

        pacmanUpImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/pacmanUp.png"))).getImage();
        pacmanDownImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/pacmanDown.png"))).getImage();
        pacmanLeftImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/pacmanLeft.png"))).getImage();
        pacmanRightImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/pacmanRight.png"))).getImage();

        loadMap();

        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                } else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);

        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));

        int headerHeight = 35;
        int textY = headerHeight / 2 + 5;

        if (gameOver) {
            g.drawString("Game Over: " + score, tileSize / 2, textY);
        } else {
            g.drawString("Lives: " + lives, tileSize / 2, textY);
            g.drawString("Score: " + score, tileSize / 2 + 150, textY);
        }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        if (pacman.x < 0) {
            pacman.x = boardWidth - tileSize;
        } else if (pacman.x >= boardWidth) {
            pacman.x = 0;
        }

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            if (ghost.x % tileSize == 0 && ghost.y % tileSize == 0) {
                ArrayList<Character> availableDirections = new ArrayList<>();

                for (char dir : directions) {
                    int tempVX = 0, tempVY = 0;
                    if (dir == 'U') {
                        tempVY = -tileSize / 4;
                    } else if (dir == 'D') {
                        tempVY = tileSize / 4;
                    } else if (dir == 'L') {
                        tempVX = -tileSize / 4;
                    } else if (dir == 'R') {
                        tempVX = tileSize / 4;
                    }

                    int newX = ghost.x + tempVX;
                    int newY = ghost.y + tempVY;

                    Block tempBlock = new Block(ghost.image, newX, newY, ghost.width, ghost.height);
                    boolean canMove = true;

                    for (Block wall : walls) {
                        if (collision(tempBlock, wall)) {
                            canMove = false;
                            break;
                        }
                    }

                    if (canMove) {
                        availableDirections.add(dir);
                    }
                }

                char opposite;

                if (ghost.direction == 'U') {
                    opposite = 'D';
                } else if (ghost.direction == 'D') {
                    opposite = 'U';
                } else if (ghost.direction == 'L') {
                    opposite = 'R';
                } else if (ghost.direction == 'R') {
                    opposite = 'L';
                } else {
                    opposite = ' ';
                }

                availableDirections.removeIf(dir -> dir == opposite);

                if (!availableDirections.isEmpty() && random.nextInt(10) < 3) {
                    char newDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                    ghost.updateDirection(newDirection);
                }
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            if (ghost.x < 0) {
                ghost.x = boardWidth - tileSize;
            } else if (ghost.x >= boardWidth) {
                ghost.x = 0;
            }

            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                    break;
                }
            }
        }

        Block foodEaten = null;

        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }

        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) { return
            a.x < b.x + b.width &&
                    a.x + a.width > b.x &&
                    a.y < b.y + b.height &&
                    a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        } else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        } else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        } else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
}
