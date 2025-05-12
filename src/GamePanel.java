import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private GameMap map;
    private RayCaster rayCaster;
    private boolean gameOver = false;

    private final int screenWidth = 640;
    private final int screenHeight = 480;
    private final int fov = 60;
    private final int numRays = screenWidth;
    private final float angleStep = (float)Math.toRadians(fov) / numRays;

    public GamePanel() {
        int[][] grid = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    {1,0,0,0,1,0,0,0,0,1,0,0,0,2,1},
    {1,0,1,0,1,0,1,1,0,1,0,1,1,0,1},
    {1,0,1,0,0,0,1,1,0,1,0,0,1,0,1},
    {1,0,1,1,1,0,0,0,0,1,1,0,1,0,1},
    {1,0,0,0,1,1,1,1,0,0,1,0,1,0,1},
    {1,1,1,0,1,0,0,1,1,0,1,0,1,0,1},
    {1,0,0,0,0,0,1,1,1,0,1,0,0,0,1},
    {1,0,1,1,1,0,1,0,0,0,1,1,1,0,1},
    {1,0,1,0,1,0,1,0,1,1,1,0,1,0,1},
    {1,0,1,0,0,0,0,0,1,0,0,0,1,0,1},
    {1,1,1,1,1,1,1,0,1,1,1,1,1,0,1},
    {1,0,0,0,0,0,1,0,0,0,0,0,0,0,1},
    {1,0,1,1,1,0,1,1,1,1,1,1,1,0,1},
    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };

        this.map = new GameMap(grid);
        this.player = new Player(100, 100, 0);
        this.rayCaster = new RayCaster(map, player);

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            drawGameOver(g);
            return;
        
	}
	// Draw ceiling
    g.setColor(new Color(20, 20, 40)); // dark blue-gray
    g.fillRect(0, 0, screenWidth, screenHeight / 2);

    // Draw floor
    g.setColor(new Color(70, 70, 65)); // deep red for floor
    g.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);

        ArrayList<Ray> rays = new ArrayList<>();
        float startAngle = player.rotationAngle - (float)Math.toRadians(fov) / 2;
        for (int i = 0; i < numRays; i++) {
            float rayAngle = startAngle + i * angleStep;
            Ray ray = rayCaster.castRay(rayAngle);
            rays.add(ray);

            float correctedDistance = ray.distance * (float)Math.cos(rayAngle - player.rotationAngle);
            int lineHeight = (int)((GameMap.TILE_SIZE * screenHeight) / correctedDistance);

            int drawStart = (screenHeight / 2) - (lineHeight / 2);
            int shade = ray.wasHitVertical ? 180 : 255;
            if (ray.wallHitContent == 2) {
                g.setColor(new Color(0, 255, 125));
            } else {
                g.setColor(new Color(shade, shade, shade));
            }

            g.drawLine(i, drawStart, i, drawStart + lineHeight);
        }

        drawMiniMap(g);

        if (map.isFinishAt(player.x, player.y)) {
            gameOver = true;
        }
    }

    private void drawMiniMap(Graphics g) {
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                int cell = map.mapGrid[y][x];
                if (cell == 1) g.setColor(Color.DARK_GRAY);
                else if (cell == 2) g.setColor(Color.GREEN);
                else g.setColor(Color.LIGHT_GRAY);

                g.fillRect(x * 10, y * 10, 10, 10);
            }
        }

        g.setColor(Color.RED);
        g.fillOval((int)(player.x / GameMap.TILE_SIZE * 10), (int)(player.y / GameMap.TILE_SIZE * 10), 5, 5);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Game Over", screenWidth / 2 - 100, screenHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Moves: " + player.moveCount, screenWidth / 2 - 70, screenHeight / 2);

        String stars = "★".repeat(getStarRating()) + "☆".repeat(3 - getStarRating());
        g.drawString("Stars: " + stars, screenWidth / 2 - 70, screenHeight / 2 + 40);
    }

    private int getStarRating() {
        if (player.moveCount <= 30) return 3;
        else if (player.moveCount <= 50) return 2;
        else if (player.moveCount <= 70) return 1;
        return 0;
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        if (e.getKeyCode() == KeyEvent.VK_LEFT) player.rotateLeft();
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) player.rotateRight();
        if (e.getKeyCode() == KeyEvent.VK_UP) player.moveForward(map);
        if (e.getKeyCode() == KeyEvent.VK_DOWN) player.moveBackward(map);
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}

