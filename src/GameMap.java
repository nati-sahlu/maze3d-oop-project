public class GameMap {
	public static final int TILE_SIZE = 64;
	public int[][] mapGrid;
	public int width, height;
	
	public GameMap(int[][] grid) {
		this.mapGrid = grid;
		this.height = grid.length;
		this.width = grid[0].length;
	}
	
	public boolean hasWallAt(float x, float y) {
		int col = (int)(x / TILE_SIZE);
		int row = (int)(y / TILE_SIZE);
		if (row < 0 || row >= height || col < 0 || col >= width) return true;
		return mapGrid[row][col] == 1; // 1 means wall
	}
	
	public int getTile(int x, int y) {
		if (x >= 0 && y >= 0 && y < height && x < width) {
			return mapGrid[y][x];
		}
		return 1; // treat as wall
	}
	
	public int getCellAt(float x, float y) {
		int col = (int)(x / TILE_SIZE);
		int row = (int)(y / TILE_SIZE);
		return mapGrid[row][col];
	}
}
