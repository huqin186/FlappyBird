package com.huqin.bird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BirdGame extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Bird bird;
	Column column1, column2;
	Ground ground;
	BufferedImage background;
	//
	int state;
	public static final int START = 0;
	public static final int RUNNING = 1;
	public static final int GAME_OVER = 2;
	
	BufferedImage startImage;
	BufferedImage gameOverImage;
	
	//分数
	int score;
	
	public BirdGame() throws Exception {
		//
		state = START;
		startImage = ImageIO.read(getClass().getResource("/img/start.jpg"));
		//gameOver = false;
		gameOverImage = ImageIO.read(getClass().getResource("/img/gameOver.jpg"));
		//
		bird = new Bird();
		column1 = new Column(1);
		column2 = new Column(2);
		ground = new Ground();
		background = ImageIO.read(getClass().getResource("/img/background.jpg"));
	}
	
	/*
	 * 启动
	 */
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		BirdGame game = new BirdGame();
		frame.add(game);
		frame.setSize(440, 670);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		game.action();
	}
	
	public void paint(Graphics g){
		g.drawImage(background, 0, 0, null);
		g.drawImage(column1.imageDown, column1.x - column1.width/2, column1.y - column1.height/2, null);
		g.drawImage(column1.imageUp, column1.x - column1.width/2, column1.y + column1.height/2 + column1.gap, null);
		g.drawImage(column2.imageDown, column2.x - column2.width/2, column2.y - column2.height/2, null);
		g.drawImage(column2.imageUp, column2.x - column2.width/2, column2.y + column2.height/2 + column1.gap, null);
		g.drawImage(ground.image, ground.x, ground.y, null);
		Graphics2D g2 = (Graphics2D) g;
		g2.rotate(-bird.alpha, bird.x, bird.y);
		g.drawImage(bird.image, bird.x - bird.width/2, bird.y - bird.height/2, null);
		g2.rotate(bird.alpha, bird.x, bird.y);
		
		//在 paint 方法中添加绘制分数的算法
		Font f =  new Font(Font.SANS_SERIF, Font.BOLD, 40);
		g.setFont(f);
		g.drawString("" + score, 40, 60);
		g.setColor(Color.WHITE);
		g.drawString("" + score, 40 - 3, 60 - 3);
		
		switch (state) {
		case GAME_OVER:
			g.drawImage(gameOverImage, 82, 310, null);
			break;
		case START:
			g.drawImage(startImage, 82, 310, null);
			break;
		}
	}
	
	public void action() throws Exception {
		//鼠标按下事件
		MouseListener l = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				//bird.flappy();
				try {
					switch (state) {
					case GAME_OVER:
						column1 = new Column(1);
						column2 = new Column(2);
						bird = new Bird();
						score = 0;
						state = START;
						break;
					case START:
						state = RUNNING;
					case RUNNING:
						bird.flappy();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		addMouseListener(l);
		//
		while (true) {			
			switch (state) {
			case GAME_OVER:
			case START:
				bird.fly();
				ground.step();
				break;
			case RUNNING:
				column1.step();
				column2.step();
				bird.step();
				bird.fly();
				ground.step();
				//计分逻辑
				if (bird.x == column1.x || bird.x == column2.x) {
					score++;
				}
				//地面碰撞检测
				if (bird.hit(ground) || bird.hit(column1) || bird.hit(column2)) {
					state = GAME_OVER;
				}
				break;
			}
			repaint();
			Thread.sleep(1000 / 60);
		}
	}
	

}
/*
 * 地面
 */
class Ground {
	BufferedImage image;
	int x, y;
	int width;
	int height;
	
	public Ground() throws Exception {
		image = ImageIO.read(getClass().getResource("/img/land.jpg"));
		width = image.getWidth();
		height = image.getHeight();
		x = 0;
		y = 500;
	}
	//地面移动
	public void step() {
		x--;
		if (x == -109) {
			x = 0;
		}
	}
	
}
/*
 * 柱子
 */
class Column {
	BufferedImage imageUp;
	BufferedImage imageDown;
	int x, y;
	int width, height;
	int gap; //上下柱子之间缝隙
	int distance; //左右柱子之间的距离
	
	Random random = new Random();
	public Column(int n) throws Exception {
		imageUp = ImageIO.read(getClass().getResource("/img/pipe_up.jpg"));
		imageDown = ImageIO.read(getClass().getResource("/img/pipe_down.jpg"));
		width = imageUp.getWidth();
		height = imageUp.getHeight();
		gap = 144;
		distance = 245;
		x = 550 + (n - 1) * distance;
		y = random.nextInt(218) - 132;
	}
	//柱子移动
	public void step() {
		x--;
		if (x == -width / 2) {
			x = distance * 2 - width / 2;
			y = random.nextInt(218) - 132;
		}
	}
}
/*
 * 鸟
 */
class Bird {
	BufferedImage image;
	int x, y;
	int width, height;
	int size;
	//用于计算鸟的位置
	double g; //重力加速度
	double t; //两次位置时间间隔
	double v0; //初始上抛速度
	double speed; //当前上抛速度
	double s; //经过时间t后的位移
	double alpha; //鸟的倾角 弧度单位
	//定义一组图片，是鸟的动画帧
	BufferedImage[] images;
	//动画帧数据元素的下标
	int index;
	
	public Bird() throws Exception {
		image = ImageIO.read(getClass().getResource("/img/0.jpg"));
		width = image.getWidth();
		height = image.getHeight();
		x = 132;
		y= 280;
		size = 40;
		//
		g = 4;
		v0 = 20;
		t = 0.25;
		speed = v0;
		s = 0;
		alpha = 0;
		//
		images = new BufferedImage[8];
		for (int i = 0; i < 8; i++) {
			images[i] = ImageIO.read(getClass().getResource("/img/" + i + ".jpg"));
		}
		index = 0;
		
	}
	//飞翔
	public void fly() {
		index ++;
		image = images[(index/12) % 8];
	}
	//移动
	public void step() {
		double v0 = speed;
		s = v0 * t + g * t / 2; //计算上抛运动位移
		y = y - (int) s; //计算鸟的坐标
		double v = v0 - g * t; //计算下次的速度
		speed = v;
		//调用JAVA API 提供的反正切函数，计算倾角
		alpha = Math.atan(s / 8);
	}
	
	public void flappy() {
		speed  = v0;
	}
	
	public boolean hit(Ground ground) {
		boolean hit = y + size / 2 > ground.y;
		if (hit) {
			y = ground.y - size / 2;
			alpha = -3.14159265358979323 / 2;
		}
		return false;
	}
	
	public boolean hit(Column column) {
		//先检测时候在柱子的范围以内
		if (x > column.x - column.width / 2 - size /2 
				&& x < column.x + column.width / 2 + size / 2) {
			//检测是否在缝隙中
			if (y > column.y - column.gap / 2 + size / 2
					&& y < column.y + column.gap / 2 - size / 2) {
				return false;
			}
			System.out.println(y +">"+ (column.y + size / 2)+" &&"+ y +"<"+ (column.y + size / 2 + column.gap));
			return false;
		}
		return false;
	}
}
