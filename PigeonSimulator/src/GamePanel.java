import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

	Timer timer;

	int x = 300;
	int y = 450;
	boolean left = false;
	boolean right = false;
	boolean up = false;
	boolean down = false;
	ArrayList<NpcPigeon> npcPigeons = new ArrayList<>();

	BufferedImage standRight;
	BufferedImage walkRight1;
	BufferedImage walkRight2;

	BufferedImage standLeft;
	BufferedImage walkLeft1;
	BufferedImage walkLeft2;

	BufferedImage gameBg;
	boolean faceRight = true;
	boolean foodExist = false;
	boolean autoMode = false;
	boolean autoMoving = false;
	boolean feedMode = false;
	
	boolean reached100 = false;
	boolean reached1000 = false;

	

	int foodX = 600;
	int foodY = 300;

	int frame = 0;
	int foodCount = 0;
	int spawnTimer = 0;
	int pigeonCount = npcPigeons.size() + 1;
	String foodName = "パン";

	private BufferedImage[] eatRightFrames;
	private BufferedImage[] eatLeftFrames;

	private boolean eatingAnimation = false;
	private int eatingTimer = 0;

	int gameState = 0;
	BufferedImage introBg;

	/*
	0 = 質問画面
	1 = タイトル
	2 = ゲーム
	3 = フラれた
	*/

	public GamePanel() {

		try {
			introBg = ImageIO.read(
					new File("images/top_hato.png"));
			gameBg = ImageIO.read(
					new File("images/city_bg.jpg"));
			standRight = ImageIO.read(
					new File("images/stand_right.png"));
			walkRight1 = ImageIO.read(
					new File("images/walk1_right.png"));

			walkRight2 = ImageIO.read(
					new File("images/walk2_right.png"));

			standLeft = ImageIO.read(
					new File("images/stand_left.png"));

			walkLeft1 = ImageIO.read(
					new File("images/walk1_left.png"));

			walkLeft2 = ImageIO.read(
					new File("images/walk2_left.png"));
			eatRightFrames = new BufferedImage[] {
					ImageIO.read(new File("images/eat_right1.png")),
					ImageIO.read(new File("images/eat_right2.png")),
					ImageIO.read(new File("images/eat_right3.png"))
			};

			eatLeftFrames = new BufferedImage[] {
					ImageIO.read(new File("images/eat_left1.png")),
					ImageIO.read(new File("images/eat_left2.png")),
					ImageIO.read(new File("images/eat_left3.png"))
			};

			System.out.println("画像読込成功");

		} catch (Exception e) {

			e.printStackTrace();
		}
		setFocusable(true);
		addKeyListener(this);

		timer = new Timer(16, this);
		timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		if (gameState == 0) {
			drawIntro(g);
			return;
		}

		if (gameState == 1) {

			drawSettings(g);
			return;
		}

		if (gameState == 2) {
			drawGame(g);
			return;

		}

		if (gameState == 3) {
			drawReject(g);
			return;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (gameState != 2 && gameState != 3) {
			repaint();
			return;
		}

		if (gameState == 3) {

			frame++;

			if (frame > 180) {

				System.exit(0);
			}

			return;
		}
		autoMoving = false;

		if (autoMode && foodExist) {
			if (x < foodX - 20) {

				x += 2;
				faceRight = true;
				autoMoving = true;
			}

			if (x > foodX + 20) {

				x -= 2;
				faceRight = false;
				autoMoving = true;
			}

			if (y < foodY - 20) {

				y += 2;
				autoMoving = true;
			}

			if (y > foodY + 20) {

				y -= 2;
				autoMoving = true;
			}

			if (autoMoving) {
				frame++;
			}
		}

		/* ←ここから手動移動 */

		if (left) {
			x -= 3;
			faceRight = false;
			frame++;
		}

		if (right) {
			x += 3;
			faceRight = true;
			frame++;
		}

		if (up) {
			y -= 3;
			frame++;
		}

		if (down) {
			y += 3;
			frame++;
		}
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		if (x > getWidth() - 200)
			x = getWidth() - 200;

		if (y > getHeight() - 200)
			y = getHeight() - 200;
		repaint();
		// 首伸ばしアニメーション
		if (eatingAnimation) {

			eatingTimer--;

			if (eatingTimer <= 0) {
				eatingAnimation = false;
			}
		}

		// ランダムエサ生成
		if (!feedMode && !foodExist) {

			spawnFood();

		}
		updateNpcPigeons();

		// ハト増殖タイマー
		spawnTimer++;

		if (spawnTimer > 1800) {

			npcPigeons.add(
					new NpcPigeon(
							(int) (Math.random() * 900),
							(int) (Math.random() * 500)));
			playSound("sound/pigeon2.wav");
			spawnTimer = 0;

			System.out.println(
					"ハト増殖！ 現在 "
							+ (npcPigeons.size() + 1)
							+ " 羽");
		}
		int pigeonCount = npcPigeons.size() + 1;

		if (!reached100 && pigeonCount >= 100) {

		    playSound("sound/born100.wav");

		    reached100 = true;

		    System.out.println("100羽達成！");
		}

		if (!reached1000 && pigeonCount >= 1000) {

		    playSound("sound/cuckoo.wav");

		    reached1000 = true;

		    System.out.println("1000羽達成！！！");
		}

	}

	private void checkNpcFoodCollision() {

		for (NpcPigeon p : npcPigeons) {

			Rectangle pigeon = new Rectangle(
					p.x,
					p.y,
					150,
					150);

			Rectangle food = new Rectangle(
					foodX - 20,
					foodY - 50,
					180,
					120);

			if (foodExist &&
					pigeon.intersects(food)) {
				playSound("sound/pigeon.wav");
				foodExist = false;

				npcPigeons.add(
						new NpcPigeon(
								p.x + 30,
								p.y + 30));

				playSound("sound/pigeon2.wav");
				if (!feedMode) {
					spawnFood();
				}

				return;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		// 質問画面
		if (gameState == 0) {

			if (e.getKeyCode() == KeyEvent.VK_1) {
				System.out.println("1押された");
				gameState = 2; // ゲームへ
				repaint();
			}

			if (e.getKeyCode() == KeyEvent.VK_2) {

				gameState = 1; // 後で設定画面にする
				repaint();
			}

			if (e.getKeyCode() == KeyEvent.VK_3) {

				playSound("sound/reject.wav");

			    gameState = 3;
			    repaint();
			}

			return;
		}

		// タイトル画面
		if (gameState == 1) {
			if (e.getKeyCode() == KeyEvent.VK_E) {

				String input = JOptionPane.showInputDialog(
						this,
						"エサ名を入力してください（4文字以内）",
						foodName);

				if (input != null && !input.isBlank()) {

					if (input.length() > 4) {
						input = input.substring(0, 4);
					}

					foodName = input;
				}
			}

			if (e.getKeyCode() == KeyEvent.VK_A) {

				autoMode = true;
				System.out.println("自走ON");
			}

			if (e.getKeyCode() == KeyEvent.VK_S) {

				autoMode = false;
				System.out.println("自走OFF");
			}

			if (e.getKeyCode() == KeyEvent.VK_F) {

				feedMode = !feedMode;

				System.out.println(
						feedMode ? "エサやりモード" : "ランダムモード");
			}

			if (e.getKeyCode() == KeyEvent.VK_ENTER) {

				gameState = 2;
			}

			repaint();
			return;
		}

		// ゲーム中
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = true;
		}
		if (gameState == 2 &&
				feedMode &&
				e.getKeyCode() == KeyEvent.VK_SPACE &&
				!foodExist) {

			spawnFood();

			System.out.println("エサ設置");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	private void drawIntro(Graphics g) {
		// 背景色
		g.setColor(
				new Color(225, 225, 215));

		g.fillRect(
				0,
				0,
				getWidth(),
				getHeight());
		g.drawImage(
				introBg,
				700,
				50,
				400,
				400,
				null);
		g.setFont(
				new Font("MS Gothic",
						Font.PLAIN,
						20));

		g.drawString(
				"キーボードの数字キーで選択してください",
				250,
				500);

		// 文字色を黒に戻す
		g.setColor(Color.BLACK);

		g.setFont(
				new Font(
						"MS Gothic",
						Font.BOLD,
						30));

		g.drawString(
				"ふふふ、ハトは好きかい？",
				250,
				150);

		g.drawString(
				"1. うん、大好きさ！",
				250,
				250);

		g.drawString(
				"2. 僕は嫌いですね！",
				250,
				320);

		g.drawString(
				"3. 知らんな",
				250,
				390);

		return;
	}

	private void drawSettings(Graphics g) {
		g.setFont(
				new Font(
						"MS Gothic",
						Font.BOLD,
						40));
		g.drawString(
				"【自走設定】",
				250,
				180);

		g.drawString(
				"A : 自走 ON",
				300,
				220);

		g.drawString(
				"S : 自走 OFF",
				300,
				280);

		g.drawString(
				"現在 : " +
						(autoMode ? "ON" : "OFF"),
				300,
				340);
		g.drawString(
				"F : エサやりモード",
				300,
				560);

		g.drawString(
				"現在 : " +
						(feedMode ? "エサやり" : "ランダム"),
				300,
				620);
		g.drawString(
				"【エサ設定】",
				250,
				380);
		g.drawString(
				"E : エサ名変更",
				300,
				420);

		g.drawString(
				"現在のエサ : " + foodName,
				300,
				500);

		g.drawString(
				"ENTER : ゲーム開始",
				300,
				700);
	}

	private void drawReject(Graphics g) {
		g.setFont(
				new java.awt.Font(
						"MS Gothic",
						java.awt.Font.BOLD,
						40));

		g.drawString(
				"あのハト達フラれたわ",
				180,
				300);

		return;
	}

	private void drawGame(Graphics g) {

		drawBackground(g);

		checkFoodCollision();

		checkNpcFoodCollision();

		BufferedImage img = getPigeonImage();

		drawPigeon(g, img);

		drawNpcPigeons(g);

		drawFood(g);

		drawScore(g);
	}

	private void drawBackground(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		// 透明度40%
		g2.setComposite(
				AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER,
						0.3f));

		g2.drawImage(
				gameBg,
				0,
				0,
				getWidth(),
				getHeight(),
				null);

		// 元に戻す
		g2.setComposite(
				AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER,
						1.0f));
	}

	private void drawPigeon(
			Graphics g,
			BufferedImage img) {

		g.drawImage(
				img,
				x,
				y,
				200,
				200,
				null);

	}

	private void drawNpcPigeons(Graphics g) {

		for (NpcPigeon p : npcPigeons) {

			BufferedImage img;

			int anim = (p.frame / 10) % 4;

			if (p.faceRight) {

				if (anim == 0) {
					img = standRight;
				} else if (anim == 1) {
					img = walkRight1;
				} else if (anim == 2) {
					img = walkRight2;
				} else {
					img = walkRight1;
				}

			} else {

				if (anim == 0) {
					img = standLeft;
				} else if (anim == 1) {
					img = walkLeft1;
				} else if (anim == 2) {
					img = walkLeft2;
				} else {
					img = walkLeft1;
				}
			}

			g.drawImage(
					img,
					p.x,
					p.y,
					150,
					150,
					null);
		}
	}

	private void drawScore(Graphics g) {

		g.setFont(
				new Font(
						"MS Gothic",
						Font.PLAIN,
						24));

		g.drawString(
				"食べた数 : " + foodCount,
				20,
				30);

		g.drawString(
				"ハト数 : " + (npcPigeons.size() + 1),
				20,
				60);
	}

	private void updateNpcPigeons() {

		for (NpcPigeon p : npcPigeons) {

			if (foodExist) {

				moveToFood(p);

			} else {

				wander(p);
			}
		}
	}

	private void wander(NpcPigeon p) {

		if (Math.abs(p.x - p.targetX) < 20 &&
				Math.abs(p.y - p.targetY) < 20) {

			p.targetX = (int) (Math.random() * getWidth());

			p.targetY = (int) (Math.random() * getHeight());
		}

		moveTowards(
				p,
				p.targetX,
				p.targetY);
	}

	private void moveToFood(NpcPigeon p) {

		moveTowards(
				p,
				foodX,
				foodY);
	}

	private void moveTowards(
			NpcPigeon p,
			int targetX,
			int targetY) {

		double dx = targetX - p.x;
		double dy = targetY - p.y;

		double distance = Math.sqrt(dx * dx + dy * dy);

		if (distance > 3) {

			p.x += (int) (dx / distance * p.speed);

			p.y += (int) (dy / distance * p.speed);

			p.faceRight = dx > 0;

			p.frame++;
		}
	}

	private void checkFoodCollision() {

		Rectangle pigeon = new Rectangle(
				x + 30,
				y + 30,
				140,
				140);

		Rectangle food = new Rectangle(
				foodX - 20,
				foodY - 50,
				180,
				120);

		if (foodExist &&
				pigeon.intersects(food)) {

			playSound("sound/pigeon.wav");
			
			foodCount++;
			// 首伸ばしアニメーション開始
			eatingAnimation = true;
			eatingTimer = 15;

			if (feedMode) {

				foodExist = false;

			} else {

				spawnFood();
			}

			System.out.println("ぽ");
		}
	}

	private BufferedImage getPigeonImage() {

		boolean moving = left ||
				right ||
				up ||
				down ||
				autoMoving;

		if (eatingAnimation) {

			int eatFrame;

			if (eatingTimer > 45) {
				eatFrame = 0;
			} else if (eatingTimer > 40) {
				eatFrame = 1;
			} else if (eatingTimer > 35) {
				eatFrame = 2;
			} else if (eatingTimer > 30) {
				eatFrame = 1;
			} else if (eatingTimer > 25) {
				eatFrame = 0;
			} else if (eatingTimer > 20) {
				eatFrame = 1;
			} else if (eatingTimer > 15) {
				eatFrame = 2;
			} else if (eatingTimer > 10) {
				eatFrame = 1;
			} else {
				eatFrame = 0;
			}

			if (faceRight) {
				return eatRightFrames[eatFrame];
			} else {
				return eatLeftFrames[eatFrame];
			}
		}

		if (faceRight) {

			if (!moving) {
				return standRight;
			}

			int anim = (frame / 10) % 4;

			if (anim == 0)
				return standRight;
			if (anim == 1)
				return walkRight1;
			if (anim == 2)
				return walkRight2;

			return walkRight1;
		}

		if (!moving) {
			return standLeft;
		}

		int anim = (frame / 10) % 4;

		if (anim == 0)
			return standLeft;
		if (anim == 1)
			return walkLeft1;
		if (anim == 2)
			return walkLeft2;

		return walkLeft1;
	} // ← getPigeonImage終了

	private void spawnFood() {

		int width = Math.max(getWidth(), 1200);
		int height = Math.max(getHeight(), 800);

		foodX = (int) (Math.random() * (width - 200) + 100);
		foodY = (int) (Math.random() * (height - 200) + 100);

		foodExist = true;
	}

	private void drawFood(Graphics g) {

		g.setColor(Color.BLACK);

		if (!foodExist) {
			return;
		}

		g.setFont(
				new Font(
						"MS Gothic",
						Font.BOLD,
						28));

		g.drawString(
				"┌────┐",
				foodX,
				foodY);

		g.drawString(
				"│" + foodName + "│",
				foodX,
				foodY + 30);

		g.drawString(
				"└────┘",
				foodX,
				foodY + 60);
	}
	private void playSound(String fileName) {

	    try {

	        AudioInputStream audio =
	                AudioSystem.getAudioInputStream(
	                        new File(fileName));

	        Clip clip = AudioSystem.getClip();

	        clip.open(audio);

	        clip.start();

	    } catch (Exception e) {

	        e.printStackTrace();
	    }
	}
}
