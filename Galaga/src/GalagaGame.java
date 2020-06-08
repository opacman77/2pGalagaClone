import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GalagaGame extends JPanel implements KeyListener {

	private boolean running = true;

	private ArrayList alienSprites = new ArrayList();
	private ArrayList shotSprites=new ArrayList();
	private ArrayList shotSprites2=new ArrayList();
	/*������ �ٸ��� �ܰ� ���� �迭, �÷��̾�1�� �� ��ź �迭, �÷��̾�2�� �� ��ź �迭��
	���� �������־���.*/
	private Sprite starship;//�÷��̾� 1�� ���ּ���ü
	private Sprite starship2;//�÷��̾� 2�� ���ּ���ü

	private BufferedImage alienImage; 
	private BufferedImage shotImage;
	private BufferedImage shipImage;

	//����ȭ��, �÷���ȭ��, ���ӿ��� ȭ���� ����� �� �̹��� �ҷ���
	Image startImg=Toolkit.getDefaultToolkit().getImage("images/start.png");
	Image spaceImg=Toolkit.getDefaultToolkit().getImage("images/space.png");
	Image gameoverImg=Toolkit.getDefaultToolkit().getImage("images/gameover.png");
	
	//�� �� �ִ� �ִ� ��ź �� ����
	static final  int MAX_SHOT=1000;
	
	//ȭ����ȯ ���
	static final int MAIN_MODE=0; //����ȭ��
	static final int PLAY_MODE=1; //���� �÷���ȭ��
	static final int GAME_OVER=2; //���� ���� ȭ��
	
	int currentMode=MAIN_MODE;//������ ���θ�忡��
	
	static int ALIEN_WIDTH=8;
	static int ALIEN_HEIGHT=4;
	static int ALIEN_SPEED=-3;
	
	//�÷��̾�1�� 2�� ����, ����, ���� �ʱ�ȭ
	int score1=0;
	int score2=0;
	int level=1;
	int life1=100;
	int life2=100;
	
	//���� ��¦�̰� �ϴ� ȿ�� �ֱ� ���� ������
	byte flag=1;
	byte toggle=1;
	
	public GalagaGame() {
		JFrame frame = new JFrame("Galaga Game");

		frame.setSize(800, 600);
		frame.add(this);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			shotImage = ImageIO.read(new File("fire.png"));
			shipImage = ImageIO.read(new File("starship.png"));
			alienImage = ImageIO.read(new File("alien.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.requestFocus();
		this.initSprites();
		addKeyListener(this);

	}
	
	//���ӿ� �ʿ��� ��ü�� �ʱ�ȭ���ִ� �޼ҵ�
	private void initSprites() {
		Random random=new Random();
		starship = new StarShipSprite(this, shipImage, 370, 550);
		starship2 = new StarShipSprite(this, shipImage, 370, 550);
		for (int y = 0; y < ALIEN_HEIGHT; y++) {
			for (int x = 0; x < ALIEN_WIDTH; x++) {
				ALIEN_SPEED=3-random.nextInt(7);//-3~3�� �ӵ� ��������
				if(ALIEN_SPEED==0)ALIEN_SPEED=-3;
				Sprite alien=new AlienSprite(this,alienImage,100+(x*50),50+(y*30));
				alien.setDx(ALIEN_SPEED);//���� �ӵ��� ����
				alienSprites.add(alien);//�迭�� ��ü �߰�
			}
		}
	}
	//���� ���۽� ��ü �ʱ�ȭ 
	private void startGame() {;
		alienSprites.clear();
		shotSprites.clear();
		shotSprites2.clear();
		initSprites();
	}

	public void endGame() {
		 //System.exit(0);
	}

	//�ܰ� �� �迭���� �ش� ��ü ����
	public void removeSprite(Sprite sprite) {
		alienSprites.remove(sprite);
	}
	//��ź �迭���� �ش� ��ü ����
	public void removeShotSprite(Sprite sprite) {
		shotSprites.remove(sprite);
		shotSprites2.remove(sprite);
	}

	//��ź �� �� ȣ��(�÷��̾�1)
	public void fire() {
		if(shotSprites.size()<MAX_SHOT) {
			ShotSprite shot=new ShotSprite(this,shotImage,
					starship.getX()+15,starship.getY()-30);
			shotSprites.add(shot);
			Sound("pew.wav",false);
			//��ź �߻�� ȿ���� �߰�
		}
		else return;
	}
	//��ź �� �� ȣ��(�÷��̾�2)
	public void fire2() {
		if(shotSprites2.size()<MAX_SHOT) {
			ShotSprite shot2=new ShotSprite(this,shotImage,
					starship2.getX()+15,starship2.getY()-30);
			shotSprites2.add(shot2);
			Sound("pew.wav",false);
			//��ź �߻�� ȿ���� �߰�
		}
		else return;
	}


	public void nextLevel() {
		Sound("levelup.wav",false);
		//���� �������� ȿ���� �߰�
		level++;
		ALIEN_HEIGHT++;
		//�� ��ü �� ����
		ALIEN_SPEED++;
		//�� ��ü �ӵ� ����
		initSprites();
	}
	//���� ������� �� ����Ǵ� �޼ҵ�->���� �ʱ�ȭ
	public void initLevel() {
		ALIEN_HEIGHT=4;
		ALIEN_SPEED=-3;
		
		score1=0;
		score2=0;
		level=1;
		life1=100;
		life2=100;
	}

	//���� ��忡 ���� ȭ���� ������ �׷��ִ� �޼ҵ�
	@Override
	public void paint(Graphics g) {
		switch(currentMode) {
		case MAIN_MODE:
			g.drawImage(startImg,0,0,this.getWidth(),this.getHeight(),this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.BOLD,30));//��Ʈ ����
			if(flag==1)
				//�ش� ���� �����̴� ȿ�� ����
				g.drawString("Press Space Key To Start", 190, 400);
			break;
		case PLAY_MODE:
			g.drawImage(spaceImg, 0, 0, this.getWidth(), this.getHeight(), this);
			for(int i=0;i<alienSprites.size();i++) {
				Sprite sprite=(Sprite)alienSprites.get(i);
				sprite.draw(g);
			}
			//�ܰ� �� ��ü �׷���
			for(int j=0;j<shotSprites.size();j++) {
				Sprite sprite1=(Sprite)shotSprites.get(j);
				sprite1.draw(g);
			}
			//�÷��̾�1 ��ź ��ü �׷���
			for(int k=0;k<shotSprites2.size();k++) {
				Sprite sprite2=(Sprite)shotSprites2.get(k);
				sprite2.draw(g);
			}
			//�÷��̾�2 ��ź ��ü �׷���
			starship.draw(g); //�÷��̾�1 ���ּ� �׷���
			starship2.draw(g); //�÷��̾�2 ���ּ� �׷���
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.PLAIN,20));
			
			g.drawString("1p: " +score1,5,17);//�÷��̾�1 ���� ǥ��
			g.drawString("2p: " +score2,600,17);//�÷��̾�2 ���� ǥ��
			g.drawString("life: "+life1,5, 35);//�÷��̾�1 ���� ǥ��
			g.drawString("life: "+life2,600,35);//�÷��̾�2 ���� ǥ��
			g.drawString("LEVEL " +level, 350,17);//���� ���� ǥ��
			g.drawString("ENTER: pause/restart ", 550, 550);
			//���� ������ �Ͻ����� ����
			break;
		case GAME_OVER:
			g.drawImage(gameoverImg,0,0,this.getWidth(),this.getHeight(),this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.PLAIN,20));
			g.drawString("1pSCORE: "+ score1, 220, 340);//�÷��̾�1 ���� 
			g.drawString("2pSCORE: "+ score2, 420, 340);//�÷��̾�2 ����
			g.setFont(new Font("Consolas",Font.BOLD,25));
			if(flag==1)//���� �����̰� ����
				g.drawString("Press Space Key To Restart", 210, 380);
			g.setFont(new Font("Consolas",Font.BOLD,60));
			g.drawString("Winner is "+(score1>score2?"1p":"2p"), 200,150);
			//�� �÷��̾� �� ������ �� ���� �÷��̾ ���ڷ� ǥ���ϵ���
		}
		
	}

	public void gameLoop() {

		while(currentMode==MAIN_MODE) {
			//�÷��� ��� �̿��� ���� ��� �����̵���
			flag=(byte)(flag^toggle);
			repaint();
			try {
				Thread.sleep(10);//0.1�ʸ��� ���� ����
			} catch (Exception e) {
			}
		}
		while(currentMode==PLAY_MODE&&running) {
			for(int i=0;i<alienSprites.size();i++) {
				Sprite sprite=(Sprite)alienSprites.get(i);
				sprite.move(); //�ܰ� �� ������
			}
			for(int j=0;j<shotSprites.size();j++) {
				Sprite sprite1=(Sprite)shotSprites.get(j);
				sprite1.move(); //�÷��̾�1 ��ź ������
			}
			for(int k=0;k<shotSprites2.size();k++) {
				Sprite sprite2=(Sprite)shotSprites2.get(k);
				sprite2.move(); //�÷��̾�2 ��ź ������
			}
			starship.move(); //�÷��̾�1 ���ּ� ������
			starship2.move(); //�÷��̾�2 ���ּ� ������
			
			//��ź�� �ܰ� �� �浹 �˻� 
			for(int p=0;p<alienSprites.size();p++) {
				Sprite alien=(Sprite)alienSprites.get(p);
				
				if(starship.checkCollision(alien)) {
					life1--;
					if(life1==0) {
						currentMode=GAME_OVER;
					}
					//�浹�ϸ� ������ ���� �پ���, 0�� �Ǹ� ���ӿ�������
					break;
				}
				if(starship2.checkCollision(alien)) {
					life2--;
					if(life2==0) {
						currentMode=GAME_OVER;
					}
					//�浹�ϸ� ������ ���� �پ���, 0�� �Ǹ� ���ӿ�������
					break;
				}
				for(int s=0;s<shotSprites.size();s++) {
					Sprite shot=(Sprite)shotSprites.get(s);
					
					if(shot.checkCollision(alien)) {
						shot.handleCollision(alien);
						alien.handleCollision(shot);
						score1+=100;
						//��ź�� �� ���߸� �÷��̾�1 ���� 100�� ����
					}
				}
				for(int s2=0;s2<shotSprites2.size();s2++) {
					Sprite shot2=(Sprite)shotSprites2.get(s2);
					
					if(shot2.checkCollision(alien)) {
						shot2.handleCollision(alien);
						alien.handleCollision(shot2);
						score2+=100;
						//��ź�� �� ���߸� �÷��̾�2 ���� 100�� ����
					}
				}
			}
			
			if(alienSprites.size()==0) {
				nextLevel();
			}//�� ��� ���̸� ���� ������
			repaint();
			
			try {
				Thread.sleep(10);
			}catch(Exception e) {
			}
			}
		while(currentMode==GAME_OVER) {
			flag=(byte)(flag^toggle);
			repaint();
			try {
				Thread.sleep(10);//0.1�ʸ��� ���� ����
			} catch (Exception e) {
			}
			
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(currentMode) {
		case MAIN_MODE:
			if(e.getKeyCode()==KeyEvent.VK_SPACE) {
				currentMode=PLAY_MODE;
			}
			//���� ȭ�鿡�� �����̽� �� ������ ���� ����->�÷��̸���
			break;
		case PLAY_MODE:
			//�÷��̾�1�� ȭ��ǥ ����Ű�� �����¿� �̵�, �����̽� �ٷ� ��ź�߻�
			if(e.getKeyCode()==KeyEvent.VK_LEFT)
				starship.setDx(-3);
			if(e.getKeyCode()==KeyEvent.VK_RIGHT)
				starship.setDx(+3);
			if(e.getKeyCode()==KeyEvent.VK_UP)
				starship.setDy(-3);
			if(e.getKeyCode()==KeyEvent.VK_DOWN)
				starship.setDy(+3);
			if(e.getKeyCode()==KeyEvent.VK_SPACE) 
				fire();
			//�÷��̾�2�� WASD�� �����¿� �̵�, ����SHIFT�� ��ź�߻�
			if(e.getKeyCode()==KeyEvent.VK_A)
				starship2.setDx(-3);
			if(e.getKeyCode()==KeyEvent.VK_D)
				starship2.setDx(+3);
			if(e.getKeyCode()==KeyEvent.VK_W)
				starship2.setDy(-3);
			if(e.getKeyCode()==KeyEvent.VK_S)
				starship2.setDy(+3);
			if(e.getKeyCode()==KeyEvent.VK_SHIFT) 
				fire2();
			//���� Ű ������ ���� �Ͻ�����, �ٽ� ���� ����
			if(e.getKeyCode()==KeyEvent.VK_ENTER) {
				if(running)running=false;
				else running=true;
			}
			break;
		case GAME_OVER:
			//���� ���� ��忡�� �����̽� �� ������ ���� ȭ�鿡�� �ٽ� ���� ����
			if(e.getKeyCode()==KeyEvent.VK_SPACE) {
				currentMode=MAIN_MODE;
				initLevel();
				startGame();
			}
			break;
	}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
			starship.setDx(0);
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			starship.setDx(0);
		if(e.getKeyCode()==KeyEvent.VK_UP)
			starship.setDy(0);
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
			starship.setDy(0);
		if(e.getKeyCode()==KeyEvent.VK_A)
			starship2.setDx(0);
		if(e.getKeyCode()==KeyEvent.VK_D)
			starship2.setDx(0);
		if(e.getKeyCode()==KeyEvent.VK_W)
			starship2.setDy(0);
		if(e.getKeyCode()==KeyEvent.VK_S)
			starship2.setDy(0);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	//���� �� ���� ����� ���� �޼ҵ�
	public void Sound(String file,boolean Loop) {
		//���� ������ �а�, boolean���� true�̸� ���� �ݺ�, false�� �ѹ����
		Clip clip;
		try {
			AudioInputStream ais=AudioSystem.getAudioInputStream
					(new BufferedInputStream(new FileInputStream(file)));
			clip=AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			if(Loop)clip.loop(-1);
		}catch(Exception e) {
		e.printStackTrace();
		}
	}
	public static void main(String argv[]) {
		GalagaGame g = new GalagaGame();
			g.Sound("bgm.wav", true);
			//���� �÷��� ���� ����Ǵ� �����
		
		//���� ������ ��ӵǵ��� ���ѷ��� ����
		while(true) {
			g.gameLoop();
		}
		
	}
}
