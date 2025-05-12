import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements KeyListener, Runnable{
	private final int screenWidth = 810  ;
	private  final int screenHeight = 640;
	private final int fov = 60;
	private final int numRays = screenWidth;
	private final float stripWidth = 1;
	private Thread gameThread;
	private Player player;
	private RayCaster rayCaster;
	private GameMap gameMap;
	private boolean movingForward, movingBackward, rotatingLeft, rotatingRight;
	private boolean showMiniMap = false;	
	public GamePanel() {
		int[][] map = {
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
			{1, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1},
			{1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1},
			{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1},
			{1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
			{1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
			{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
			{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
			{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
			{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1},
			{1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1},
			{1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1},
			{1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
			{1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1},
			{1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
		};
		
		this.gameMap = new GameMap(map);
		this.player = new Player(100, 100, 0);
		this.rayCaster = new RayCaster(player, gameMap);
		this.setPreferredSize(new Dimension(810, 640));
		this.setBackground(Color.BLACK);
		this.setFocusable(true);
		requestFocus();
		this.addKeyListener(this);
		
		Timer timer = new Timer(16, e -> {
			if (movingForward) player.moveForward(gameMap);
			if (movingBackward) player.moveBackward(gameMap);
			if (rotatingLeft) player.rotateLeft();
			if (rotatingRight) player.rotateRight();
			repaint();
		});
		timer.start();
		
		gameThread = new Thread(() -> {
			while (true) {
				repaint();
				try {
					Thread.sleep(16); // ~60 FPS
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		gameThread.start();
	}
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Draw background
		g.setColor(Color.BLUE); // ceiling
		g.fillRect(0, 0, screenWidth, screenHeight / 2);
		g.setColor(Color.GREEN); // floor
		g.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);
		if (gameMap.getTile((int)player.x, (int)player.x) == 2) {
			g.setColor(Color.GREEN);  // GUI code
			g.fillRect((int)player.x *  GameMap.TILE_SIZE, (int)player.y *  GameMap.TILE_SIZE,  GameMap.TILE_SIZE,  GameMap.TILE_SIZE);
		}
		// Draw the score (moves)
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 18));
		g.drawString("Moves: " + player.moveCount, 10, 20);
		
		float rayAngle = player.rotationAngle - (float)Math.toRadians(fov) / 2;
		float angleStep = (float)Math.toRadians(fov) / numRays;
		
		for (int ray = 0; ray < numRays; ray++) {
			float distanceToWall = castRay(rayAngle);
			// Correct fisheye
			float correctedDist = (float)(distanceToWall * Math.cos(rayAngle - player.rotationAngle));
			int lineHeight = (int)(((GameMap.TILE_SIZE * screenHeight)) / correctedDist);
			int drawStart = screenHeight / 2 - lineHeight / 2;
			int drawEnd = drawStart + lineHeight;
			
			 g.setColor(Color.DARK_GRAY);
			g.fillRect(ray, drawStart, (int)stripWidth, lineHeight);
			rayAngle += angleStep;
		if (showMiniMap) {
			drawMiniMap(g);
		}
		}
	}
	private void drawMiniMap(Graphics g) {
		int miniTileSize = 8;
		int offsetX = 70;
		int offsetY = 70;
		
		// Draw the map grid
		for (int y = 0; y < gameMap.height; y++) {
			for (int x = 0; x < gameMap.width; x++) {
				int tile = gameMap.mapGrid[y][x];
				switch (tile) {
					case 1:
						g.setColor(Color.DARK_GRAY); // wall
						break;
					case 2:
						g.setColor(Color.GREEN); // finish
						break;
					default:
						g.setColor(Color.LIGHT_GRAY); // empty
						break;
				}
				g.fillRect(offsetX + x * miniTileSize, offsetY + y * miniTileSize, miniTileSize, miniTileSize);
			}
		}
		// Draw player
		g.setColor(Color.RED);
		int playerMiniX = (int)(player.x / RayCaster.TILE_SIZE * miniTileSize);
		int playerMiniY = (int)(player.y / RayCaster.TILE_SIZE * miniTileSize);
		g.fillOval(offsetX + playerMiniX - 2, offsetY + playerMiniY - 2, 5, 5);
		
		// Draw ray (direction indicator)
		g.setColor(Color.RED);
		float rayLength = 2.0f; // length in tiles
		float dx = (float)Math.cos(player.rotationAngle) * rayLength * miniTileSize;
		float dy = (float)Math.sin(player.rotationAngle) * rayLength * miniTileSize;
		int rayEndX = (int)(playerMiniX + dx);
		int rayEndY = (int)(playerMiniY + dy);
		g.drawLine(offsetX + playerMiniX, offsetY + playerMiniY, offsetX + rayEndX, offsetY + rayEndY);
	}

	
	public void run() {
		while (true) {
			repaint();
			try {
				Thread.sleep(16); // ~60 FPS
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int tileX = (int)(player.x / GameMap.TILE_SIZE);
			int tileY = (int)(player.y / GameMap.TILE_SIZE);
			
			if (gameMap.getTile(tileX, tileY) == 2) {
				System.out.println("You reached the finish!");
				// Stop movement
				movingForward = false;
				movingBackward = false;
				rotatingLeft = false;
				rotatingRight = false;
			}
		}
	}
	private float castRay(float angle) {
		float rayX = player.x;
		float rayY = player.y;
		float stepSize = 1f;
		float rayStepX = (float)Math.cos(angle) * stepSize;
		float rayStepY = (float)Math.sin(angle) * stepSize;
		
		while (true) {
			rayX += rayStepX;
			rayY += rayStepY;
			
			if (gameMap.hasWallAt(rayX, rayY)) {
				float dx = rayX - player.x;
				float dy = rayY - player.y;
				return (float)Math.sqrt(dx * dx + dy * dy);
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// ...
		}
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
				movingForward = true;
				break;
			case KeyEvent.VK_S:
				movingBackward = true;
				break;
			case KeyEvent.VK_A:
				rotatingLeft = true;
				break;
			case KeyEvent.VK_D:
				rotatingRight = true;
				break;
			case KeyEvent.VK_M:
				showMiniMap = !showMiniMap;
				break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
				movingForward = false;
				break;
			case KeyEvent.VK_S:
				movingBackward = false;
				break;
			case KeyEvent.VK_A:
				rotatingLeft = false;
				break;
			case KeyEvent.VK_D:
				rotatingRight = false;
				break;
		}
	}

}
