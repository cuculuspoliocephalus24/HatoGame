public class NpcPigeon {

	int x;
	int y;
	int targetX;
	int targetY;

	boolean faceRight = true;

	int frame = 0;

	int speed;

	boolean carryingFood = false;

	public NpcPigeon(int x, int y) {

		this.x = x;
		this.y = y;

		targetX = x;
		targetY = y;

		speed = 1 + (int) (Math.random() * 5);
	}

}