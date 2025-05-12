public class RayCaster {
    private GameMap map;
    private Player player;

    public RayCaster(GameMap map, Player player) {
        this.map = map;
        this.player = player;
    }

    public Ray castRay(float rayAngle) {
        rayAngle = normalizeAngle(rayAngle);

        float xIntercept, yIntercept;
        float xStep, yStep;

        boolean facingDown = rayAngle > 0 && rayAngle < Math.PI;
        boolean facingUp = !facingDown;
        boolean facingRight = rayAngle < 0.5f * Math.PI || rayAngle > 1.5f * Math.PI;
        boolean facingLeft = !facingRight;

        // Horizontal
        boolean foundHorzHit = false;
        float horzHitX = 0, horzHitY = 0;
        int horzContent = 0;

        yIntercept = (float)(Math.floor(player.y / GameMap.TILE_SIZE) * GameMap.TILE_SIZE);
        yIntercept += facingDown ? GameMap.TILE_SIZE : 0;

        xIntercept = player.x + (yIntercept - player.y) / (float)Math.tan(rayAngle);

        yStep = GameMap.TILE_SIZE * (facingUp ? -1 : 1);
        xStep = yStep / (float)Math.tan(rayAngle);

        float nextHorzX = xIntercept;
        float nextHorzY = yIntercept;

        if (facingUp) nextHorzY--;

        while (nextHorzX >= 0 && nextHorzX < map.width * GameMap.TILE_SIZE &&
               nextHorzY >= 0 && nextHorzY < map.height * GameMap.TILE_SIZE) {
            if (map.hasWallAt(nextHorzX, nextHorzY)) {
                horzHitX = nextHorzX;
                horzHitY = nextHorzY;
                horzContent = map.getCellAt(horzHitX, horzHitY);
                foundHorzHit = true;
                break;
            } else {
                nextHorzX += xStep;
                nextHorzY += yStep;
            }
        }

        // Vertical
        boolean foundVertHit = false;
        float vertHitX = 0, vertHitY = 0;
        int vertContent = 0;

        xIntercept = (float)(Math.floor(player.x / GameMap.TILE_SIZE) * GameMap.TILE_SIZE);
        xIntercept += facingRight ? GameMap.TILE_SIZE : 0;

        yIntercept = player.y + (xIntercept - player.x) * (float)Math.tan(rayAngle);

        xStep = GameMap.TILE_SIZE * (facingLeft ? -1 : 1);
        yStep = xStep * (float)Math.tan(rayAngle);

        float nextVertX = xIntercept;
        float nextVertY = yIntercept;

        if (facingLeft) nextVertX--;

        while (nextVertX >= 0 && nextVertX < map.width * GameMap.TILE_SIZE &&
               nextVertY >= 0 && nextVertY < map.height * GameMap.TILE_SIZE) {
            if (map.hasWallAt(nextVertX, nextVertY)) {
                vertHitX = nextVertX;
                vertHitY = nextVertY;
                vertContent = map.getCellAt(vertHitX, vertHitY);
                foundVertHit = true;
                break;
            } else {
                nextVertX += xStep;
                nextVertY += yStep;
            }
        }

        float horzDist = foundHorzHit ? distance(player.x, player.y, horzHitX, horzHitY) : Float.MAX_VALUE;
        float vertDist = foundVertHit ? distance(player.x, player.y, vertHitX, vertHitY) : Float.MAX_VALUE;

        Ray ray = new Ray();
        if (vertDist < horzDist) {
            ray.wallHitX = vertHitX;
            ray.wallHitY = vertHitY;
            ray.distance = vertDist;
            ray.wasHitVertical = true;
            ray.wallHitContent = vertContent;
        } else {
            ray.wallHitX = horzHitX;
            ray.wallHitY = horzHitY;
            ray.distance = horzDist;
            ray.wasHitVertical = false;
            ray.wallHitContent = horzContent;
        }

        ray.rayAngle = rayAngle;
        return ray;
    }

    private float normalizeAngle(float angle) {
        angle = angle % ((float)(2 * Math.PI));
        if (angle < 0) angle += 2 * Math.PI;
        return angle;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
    }
}

