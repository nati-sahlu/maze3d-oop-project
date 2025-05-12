public class Player {
	public float x, y;
	public float rotationAngle;
	public final float moveSpeed = 5.0f;
	public final float rotationSpeed = (float)(Math.PI / 60);
	public int moveCount = 0;
	
	public Player(float x, float y, float angle) {
		this.x = x;
		this.y = y;
		this.rotationAngle = angle;
	}

	public void rotateLeft() { rotationAngle -= rotationSpeed;}

	public void rotateRight() { rotationAngle += rotationSpeed; }
	
	public void moveForward(GameMap map) {
		float moveStep = moveSpeed;
		float newX = x + (float) Math.cos(rotationAngle) * moveStep;
		float newY = y + (float) Math.sin(rotationAngle) * moveStep;
		if (!map.hasWallAt(newX, newY)) {
			x = newX;
			y = newY;
			moveCount++;
		}
	}
	
	public void moveBackward(GameMap map) {
		float moveStep = -moveSpeed;
		float newX = x + (float) Math.cos(rotationAngle) * moveStep;
		float newY = y + (float) Math.sin(rotationAngle) * moveStep;
		if (!map.hasWallAt(newX, newY)) {
			x = newX;
			y = newY;
			moveCount++;
		}
	}
}
