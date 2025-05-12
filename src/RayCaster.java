import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class RayCaster {
	public static final int TILE_SIZE = 64;
	public static final float TWO_PI = (float)(2 * Math.PI);
	public static final int NUM_RAYS = 320;
	public static final float PROJ_PLANE = NUM_RAYS / (2 * (float)Math.tan(Math.toRadians(30)));
	
	private Player player;
	private GameMap map;
	public Ray[] rays;
	
	
	public RayCaster(Player player, GameMap map) {
		this.player = player;
		this.map = map;
		
		rays = new Ray[NUM_RAYS];
		for (int i = 0; i < NUM_RAYS; i++)
			rays[i] = new Ray();
	}
	
	private boolean isRayFacingDown(float angle) { return angle > 0 && angle < Math.PI; }
	private boolean isRayFacingUp(float angle) { return !isRayFacingDown(angle); }
	private boolean isRayFacingRight(float angle) { return angle < 0.5 * Math.PI || angle > 1.5 * Math.PI; }
	private boolean isRayFacingLeft(float angle) { return !isRayFacingRight(angle); }
	
	private boolean isInsideMap(float x, float y) {
		return x >= 0 && x < map.width * TILE_SIZE && y >= 0 && y < map.height * TILE_SIZE;
	}
	
	private float distance(float x1, float y1, float x2, float y2) {
		return (float)Math.hypot(x2 - x1, y2 - y1);
	}
	
	/*public void castAllRays() {
		for (int i = 0; i < NUM_RAYS; i++) {
			float rayAngle = player.rotationAngle + (float)Math.atan((i - NUM_RAYS / 2.0f) / PROJ_PLANE);
			castRay(rayAngle, i);
		}
	}*/
	/*
	
	private void castRay(float rayAngle, int stripId) {
		rayAngle %= TWO_PI;
		
		if (rayAngle < 0) rayAngle += TWO_PI;
		
		Intersection horz = horizontalIntersection(rayAngle);
		Intersection vert = verticalIntersection(rayAngle);
		
		float horzDist = horz.found ? distance(player.x, player.y, horz.x, horz.y) : Float.MAX_VALUE;
		float vertDist = vert.found ? distance(player.x, player.y, vert.x, vert.y) : Float.MAX_VALUE;
		
		Ray ray = rays[stripId];
		if (vertDist < horzDist) {
			ray.distance = vertDist;
			ray.wallHitX = vert.x;
			ray.wallHitY = vert.y;
			ray.wallHitContent = vert.content;
			ray.wasHitVertical = true;
		} else {
			ray.distance = horzDist;
			ray.wallHitX = horz.x;
			ray.wallHitY = horz.y;
			ray.wallHitContent = horz.content;
			ray.wasHitVertical = false;
		}
		
		ray.distance *= Math.cos(rayAngle - player.rotationAngle);
		ray.rayAngle = rayAngle;
	}*/
	
	private Intersection horizontalIntersection(float rayAngle) {
		boolean facingDown = isRayFacingDown(rayAngle);
		float yIntercept = (float)(Math.floor(player.y / TILE_SIZE) * TILE_SIZE);
		yIntercept += facingDown ? TILE_SIZE : 0;
		
		float xIntercept = player.x + (yIntercept - player.y) / (float)Math.tan(rayAngle);
		
		float yStep = TILE_SIZE * (facingDown ? 1 : -1);
		float xStep = TILE_SIZE / (float)Math.tan(rayAngle);
		if (isRayFacingLeft(rayAngle) && xStep > 0) xStep *= -1;
		if (isRayFacingRight(rayAngle) && xStep < 0) xStep *= -1;
		
		float nextX = xIntercept;
		float nextY = yIntercept;
		
		while (isInsideMap(nextX, nextY)) {
			float checkX = nextX;
			float checkY = nextY + (isRayFacingUp(rayAngle) ? -1 : 0);
			if (map.hasWallAt(checkX, checkY)) {
				return new Intersection(nextX, nextY, true, map.getCellAt(checkX, checkY));
			}
			nextX += xStep;
			nextY += yStep;
		}
		
		return new Intersection(0, 0, false, 0);
	}
	
	private Intersection verticalIntersection(float rayAngle) {
		boolean facingRight = isRayFacingRight(rayAngle);
		float xIntercept = (float)(Math.floor(player.x / TILE_SIZE) * TILE_SIZE);
		xIntercept += facingRight ? TILE_SIZE : 0;
		
		float yIntercept = player.y + (xIntercept - player.x) * (float)Math.tan(rayAngle);
		float xStep = TILE_SIZE * (facingRight ? 1 : -1);
		float yStep = TILE_SIZE * (float)Math.tan(rayAngle);
		if (isRayFacingUp(rayAngle) && yStep > 0) yStep *= -1;
		if (isRayFacingDown(rayAngle) && yStep < 0) yStep *= -1;
		
		
		float nextX = xIntercept;
		float nextY = yIntercept;
		
		while (isInsideMap(nextX, nextY)) {
			float checkX = nextX + (isRayFacingLeft(rayAngle) ? -1 : 0);
			float checkY = nextY;
			if (map.hasWallAt(checkX, checkY)) {
				return new Intersection(nextX, nextY, true, map.getCellAt(checkX, checkY));
			}
			nextX += xStep;
			nextY += yStep;
		}
		return new Intersection(0, 0, false, 0);
	}
	
/*	public void renderRays(Graphics g, int scale) {
		g.setColor(java.awt.Color.RED);
		for (int i = 0; i < NUM_RAYS; i += 20) {
			int x1 = (int)(player.x * scale);
			int y1 = (int)(player.y * scale);
			int x2 = (int)(rays[i].wallHitX * scale);
			int y2 = (int)(rays[i].wallHitY * scale);
			g.drawLine(x1, y1, x2, y2);
		}
	}*/
	
	private static class Intersection {
		float x, y;
		boolean found;
		int content;
		Intersection(float x, float y, boolean found, int content) {
			this.x = x;
			this.y = y;
			this.found = found;
			this.content = content;
		}
	}
}
