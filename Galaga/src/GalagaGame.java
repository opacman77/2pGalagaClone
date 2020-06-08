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
	/*기존과 다르게 외계 적들 배열, 플레이어1이 쏠 포탄 배열, 플레이어2가 쏠 포탄 배열을
	각각 설정해주었다.*/
	private Sprite starship;//플레이어 1의 우주선객체
	private Sprite starship2;//플레이어 2의 우주선객체

	private BufferedImage alienImage; 
	private BufferedImage shotImage;
	private BufferedImage shipImage;

	//메인화면, 플레이화면, 게임오버 화면의 배경이 될 이미지 불러옴
	Image startImg=Toolkit.getDefaultToolkit().getImage("images/start.png");
	Image spaceImg=Toolkit.getDefaultToolkit().getImage("images/space.png");
	Image gameoverImg=Toolkit.getDefaultToolkit().getImage("images/gameover.png");
	
	//쏠 수 있는 최대 포탄 수 설정
	static final  int MAX_SHOT=1000;
	
	//화면전환 모드
	static final int MAIN_MODE=0; //시작화면
	static final int PLAY_MODE=1; //게임 플레이화면
	static final int GAME_OVER=2; //게임 오버 화면
	
	int currentMode=MAIN_MODE;//시작은 메인모드에서
	
	static int ALIEN_WIDTH=8;
	static int ALIEN_HEIGHT=4;
	static int ALIEN_SPEED=-3;
	
	//플레이어1과 2의 점수, 생명, 레벨 초기화
	int score1=0;
	int score2=0;
	int level=1;
	int life1=100;
	int life2=100;
	
	//글자 반짝이게 하는 효과 주기 위한 변수들
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
	
	//게임에 필요한 객체들 초기화해주는 메소드
	private void initSprites() {
		Random random=new Random();
		starship = new StarShipSprite(this, shipImage, 370, 550);
		starship2 = new StarShipSprite(this, shipImage, 370, 550);
		for (int y = 0; y < ALIEN_HEIGHT; y++) {
			for (int x = 0; x < ALIEN_WIDTH; x++) {
				ALIEN_SPEED=3-random.nextInt(7);//-3~3의 속도 랜덤으로
				if(ALIEN_SPEED==0)ALIEN_SPEED=-3;
				Sprite alien=new AlienSprite(this,alienImage,100+(x*50),50+(y*30));
				alien.setDx(ALIEN_SPEED);//랜덤 속도값 설정
				alienSprites.add(alien);//배열에 객체 추가
			}
		}
	}
	//게임 시작시 객체 초기화 
	private void startGame() {;
		alienSprites.clear();
		shotSprites.clear();
		shotSprites2.clear();
		initSprites();
	}

	public void endGame() {
		 //System.exit(0);
	}

	//외계 적 배열에서 해당 객체 삭제
	public void removeSprite(Sprite sprite) {
		alienSprites.remove(sprite);
	}
	//포탄 배열에서 해당 객체 삭제
	public void removeShotSprite(Sprite sprite) {
		shotSprites.remove(sprite);
		shotSprites2.remove(sprite);
	}

	//포탄 쏠 때 호출(플레이어1)
	public void fire() {
		if(shotSprites.size()<MAX_SHOT) {
			ShotSprite shot=new ShotSprite(this,shotImage,
					starship.getX()+15,starship.getY()-30);
			shotSprites.add(shot);
			Sound("pew.wav",false);
			//포탄 발사시 효과음 추가
		}
		else return;
	}
	//포탄 쏠 때 호출(플레이어2)
	public void fire2() {
		if(shotSprites2.size()<MAX_SHOT) {
			ShotSprite shot2=new ShotSprite(this,shotImage,
					starship2.getX()+15,starship2.getY()-30);
			shotSprites2.add(shot2);
			Sound("pew.wav",false);
			//포탄 발사시 효과음 추가
		}
		else return;
	}


	public void nextLevel() {
		Sound("levelup.wav",false);
		//게임 레벨업시 효과음 추가
		level++;
		ALIEN_HEIGHT++;
		//적 객체 수 증가
		ALIEN_SPEED++;
		//적 객체 속도 증가
		initSprites();
	}
	//게임 재시작할 때 실행되는 메소드->변수 초기화
	public void initLevel() {
		ALIEN_HEIGHT=4;
		ALIEN_SPEED=-3;
		
		score1=0;
		score2=0;
		level=1;
		life1=100;
		life2=100;
	}

	//현재 모드에 따라서 화면을 변경해 그려주는 메소드
	@Override
	public void paint(Graphics g) {
		switch(currentMode) {
		case MAIN_MODE:
			g.drawImage(startImg,0,0,this.getWidth(),this.getHeight(),this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.BOLD,30));//폰트 설정
			if(flag==1)
				//해당 문장 깜박이는 효과 넣음
				g.drawString("Press Space Key To Start", 190, 400);
			break;
		case PLAY_MODE:
			g.drawImage(spaceImg, 0, 0, this.getWidth(), this.getHeight(), this);
			for(int i=0;i<alienSprites.size();i++) {
				Sprite sprite=(Sprite)alienSprites.get(i);
				sprite.draw(g);
			}
			//외계 적 객체 그려줌
			for(int j=0;j<shotSprites.size();j++) {
				Sprite sprite1=(Sprite)shotSprites.get(j);
				sprite1.draw(g);
			}
			//플레이어1 포탄 객체 그려줌
			for(int k=0;k<shotSprites2.size();k++) {
				Sprite sprite2=(Sprite)shotSprites2.get(k);
				sprite2.draw(g);
			}
			//플레이어2 포탄 객체 그려줌
			starship.draw(g); //플레이어1 우주선 그려줌
			starship2.draw(g); //플레이어2 우주선 그려줌
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.PLAIN,20));
			
			g.drawString("1p: " +score1,5,17);//플레이어1 점수 표시
			g.drawString("2p: " +score2,600,17);//플레이어2 점수 표시
			g.drawString("life: "+life1,5, 35);//플레이어1 생명 표시
			g.drawString("life: "+life2,600,35);//플레이어2 생명 표시
			g.drawString("LEVEL " +level, 350,17);//게임 레벨 표시
			g.drawString("ENTER: pause/restart ", 550, 550);
			//엔터 누르면 일시정지 가능
			break;
		case GAME_OVER:
			g.drawImage(gameoverImg,0,0,this.getWidth(),this.getHeight(),this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas",Font.PLAIN,20));
			g.drawString("1pSCORE: "+ score1, 220, 340);//플레이어1 총점 
			g.drawString("2pSCORE: "+ score2, 420, 340);//플레이어2 총점
			g.setFont(new Font("Consolas",Font.BOLD,25));
			if(flag==1)//문장 깜박이게 해줌
				g.drawString("Press Space Key To Restart", 210, 380);
			g.setFont(new Font("Consolas",Font.BOLD,60));
			g.drawString("Winner is "+(score1>score2?"1p":"2p"), 200,150);
			//두 플레이어 중 점수가 더 높은 플레이어를 승자로 표시하도록
		}
		
	}

	public void gameLoop() {

		while(currentMode==MAIN_MODE) {
			//플래그 토글 이용해 글자 계속 깜박이도록
			flag=(byte)(flag^toggle);
			repaint();
			try {
				Thread.sleep(10);//0.1초마다 글자 깜박
			} catch (Exception e) {
			}
		}
		while(currentMode==PLAY_MODE&&running) {
			for(int i=0;i<alienSprites.size();i++) {
				Sprite sprite=(Sprite)alienSprites.get(i);
				sprite.move(); //외계 적 움직임
			}
			for(int j=0;j<shotSprites.size();j++) {
				Sprite sprite1=(Sprite)shotSprites.get(j);
				sprite1.move(); //플레이어1 포탄 움직임
			}
			for(int k=0;k<shotSprites2.size();k++) {
				Sprite sprite2=(Sprite)shotSprites2.get(k);
				sprite2.move(); //플레이어2 포탄 움직임
			}
			starship.move(); //플레이어1 우주선 움직임
			starship2.move(); //플레이어2 우주선 움직임
			
			//포탄과 외계 적 충돌 검사 
			for(int p=0;p<alienSprites.size();p++) {
				Sprite alien=(Sprite)alienSprites.get(p);
				
				if(starship.checkCollision(alien)) {
					life1--;
					if(life1==0) {
						currentMode=GAME_OVER;
					}
					//충돌하면 생명이 점점 줄어들고, 0이 되면 게임오버모드로
					break;
				}
				if(starship2.checkCollision(alien)) {
					life2--;
					if(life2==0) {
						currentMode=GAME_OVER;
					}
					//충돌하면 생명이 점점 줄어들고, 0이 되면 게임오버모드로
					break;
				}
				for(int s=0;s<shotSprites.size();s++) {
					Sprite shot=(Sprite)shotSprites.get(s);
					
					if(shot.checkCollision(alien)) {
						shot.handleCollision(alien);
						alien.handleCollision(shot);
						score1+=100;
						//포탄이 적 맞추면 플레이어1 점수 100씩 증가
					}
				}
				for(int s2=0;s2<shotSprites2.size();s2++) {
					Sprite shot2=(Sprite)shotSprites2.get(s2);
					
					if(shot2.checkCollision(alien)) {
						shot2.handleCollision(alien);
						alien.handleCollision(shot2);
						score2+=100;
						//포탄이 적 맞추면 플레이어2 점수 100씩 증가
					}
				}
			}
			
			if(alienSprites.size()==0) {
				nextLevel();
			}//적 모두 죽이면 다음 레벨로
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
				Thread.sleep(10);//0.1초마다 글자 깜박
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
			//메인 화면에서 스페이스 바 누르면 게임 시작->플레이모드로
			break;
		case PLAY_MODE:
			//플레이어1은 화살표 방향키로 상하좌우 이동, 스페이스 바로 포탄발사
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
			//플레이어2는 WASD로 상하좌우 이동, 왼쪽SHIFT로 포탄발사
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
			//엔터 키 누르면 게임 일시정지, 다시 시작 가능
			if(e.getKeyCode()==KeyEvent.VK_ENTER) {
				if(running)running=false;
				else running=true;
			}
			break;
		case GAME_OVER:
			//게임 오버 모드에서 스페이스 바 누르면 메인 화면에서 다시 시작 가능
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

	//게임 내 사운드 재생을 위한 메소드
	public void Sound(String file,boolean Loop) {
		//사운드 파일을 읽고, boolean값이 true이면 무한 반복, false면 한번재생
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
			//게임 플레이 내내 재생되는 배경음
		
		//게임 진행이 계속되도록 무한루프 설정
		while(true) {
			g.gameLoop();
		}
		
	}
}
