/************************************************************************************************

Asteroids.java

  Usage:

  <applet code="Asteroids.class" width=w height=h></applet>

  Keyboard Controls:

  S            - Start Game    P           - Pause Game
  Cursor Left  - Rotate Left   Cursor Up   - Fire Thrusters
  Cursor Right - Rotate Right  Cursor Down - Fire Retro Thrusters
  Spacebar     - Fire Cannon   H           - Hyperspace
  M            - Toggle Sound  D           - Toggle Graphics Detail

************************************************************************************************/

import java.awt.*;
import java.net.*;




import java.applet.Applet;
import java.applet.AudioClip;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;//add by yipl




/************************************************************************************************
  The AsteroidsSprite class defines a game object, including it's shape, position, movement and
  rotation. It also can detemine if two objects collide.
************************************************************************************************/

class AsteroidsSprite implements Serializable{//add by yipl

	  // Fields:

	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;//add by yipl
	static int width;    // Dimensions of the graphics area.
	  static int height;

	  Polygon shape;                 // Initial sprite shape, centered at the origin (0,0).
	  public  volatile boolean active;                // Active flag.
	  public  volatile double  angle;                 // Current angle of rotation.
	  public  volatile double  deltaAngle;            // Amount to change the rotation angle.
	  public  volatile double  currentX, currentY;    // Current position on screen.
	  public  volatile double  deltaX, deltaY;        // Amount to change the screen position.
	  public  volatile Polygon sprite;                // Final location and shape of sprite after applying rotation and
	                                 // moving to screen position. Used for drawing on the screen and
	                                 // in detecting collisions.

	  // Constructors:

	  public AsteroidsSprite() {

	    this.shape = new Polygon();
	    this.active = false;
	    this.angle = 0.0;
	    this.deltaAngle = 0.0;
	    this.currentX = 0.0;
	    this.currentY = 0.0;
	    this.deltaX = 0.0;
	    this.deltaY = 0.0;
	    this.sprite = new Polygon();
	  }

	  // Methods:

	  public void advance() {

	    // Update the rotation and position of the sprite based on the delta values. If the sprite
	    // moves off the edge of the screen, it is wrapped around to the other side.

	    this.angle += this.deltaAngle;
	    if (this.angle < 0)
	      this.angle += 2 * Math.PI;
	    if (this.angle > 2 * Math.PI)
	      this.angle -= 2 * Math.PI;
	   
	    this.currentX += this.deltaX;
	    if (this.currentX < -width / 2)
	      this.currentX += width;
	    
	    if (this.currentX > width / 2)
	      this.currentX -= width;
	    
	    this.currentY -= this.deltaY;
	    if (this.currentY < -height / 2)
	      this.currentY += height;
	    
	    if (this.currentY > height / 2)
	      this.currentY -= height;
	  }

	  public void render() {

	    int i;

	    // Render the sprite's shape and location by rotating it's base shape and moving it to
	    // it's proper screen position.

	    this.sprite = new Polygon();
	    for (i = 0; i < this.shape.npoints; i++)
	      this.sprite.addPoint((int) Math.round(this.shape.xpoints[i] * Math.cos(this.angle) + this.shape.ypoints[i] * Math.sin(this.angle)) + (int) Math.round(this.currentX) + width / 2,
	                 (int) Math.round(this.shape.ypoints[i] * Math.cos(this.angle) - this.shape.xpoints[i] * Math.sin(this.angle)) + (int) Math.round(this.currentY) + height / 2);
	  }

	  public boolean isColliding(AsteroidsSprite s) {

	    int i;

	    // Determine if one sprite overlaps with another, i.e., if any vertice
	    // of one sprite lands inside the other.

	    for (i = 0; i < s.sprite.npoints; i++)
	      if (this.sprite.inside(s.sprite.xpoints[i], s.sprite.ypoints[i]))
	        return true;
	    for (i = 0; i < this.sprite.npoints; i++)
	      if (s.sprite.inside(this.sprite.xpoints[i], this.sprite.ypoints[i]))
	        return true;
	    return false;
	  }
	}


public class Asteroids extends Applet implements Runnable {

  // Thread control variables.

  Thread loadThread;
  Thread loopThread;


  // Constants

  static final int DELAY = 50;             // Milliseconds between screen updates.

  static final int MAX_SHIPS = 3;           // Starting number of ships per game.

  static final int MAX_SHOTS =  6;          // Maximum number of sprites for photons,
  static final int MAX_ROCKS =  8;          // asteroids and explosions.
  static final int MAX_SCRAP = 20;

  static final int SCRAP_COUNT = 30;        // Counter starting values.
  static final int HYPER_COUNT = 60;
  static final int STORM_PAUSE = 30;
  static final int UFO_PASSES  =  3;

  static final int MIN_ROCK_SIDES =  8;     // Asteroid shape and size ranges.
  static final int MAX_ROCK_SIDES = 12;
  static final int MIN_ROCK_SIZE  = 20;
  static final int MAX_ROCK_SIZE  = 40;
  static final int MIN_ROCK_SPEED =  2;
  static final int MAX_ROCK_SPEED = 12;

  static final int BIG_POINTS    =  25;     // Points for shooting different objects.
  static final int SMALL_POINTS  =  50;
  static final int UFO_POINTS    = 250;
  static final int MISSLE_POINTS = 500;

  static final int NEW_SHIP_POINTS = 5000;  // Number of points needed to earn a new ship.
  static final int NEW_UFO_POINTS  = 2750;  // Number of points between flying saucers.

  // Background stars.

  int     numStars;
  Point[] stars;

  // Game data.

  int score;
  int highScore;
  int newShipScore;
  int newUfoScore;

  boolean loaded = false;
  boolean paused;
  boolean playing;
  boolean sound;
  boolean detail;

  // Key flags.

  boolean left  = false;
  boolean right = false;
  boolean up    = false;
  boolean down  = false;

  // Sprite objects.

  public AsteroidsSprite   ship;
  public AsteroidsSprite   ship1;//add by yipl
  
  AsteroidsSprite   ufo;
  AsteroidsSprite   missle;
  AsteroidsSprite   missle1;//add by yipl
  AsteroidsSprite[] photons    = new AsteroidsSprite[MAX_SHOTS];
  AsteroidsSprite[] asteroids  = new AsteroidsSprite[MAX_ROCKS];
  AsteroidsSprite[] explosions = new AsteroidsSprite[MAX_SCRAP];

  // Ship data.

  int shipsLeft;       // Number of ships left to play, including current one.
  int shipCounter;     // Time counter for ship explosion.
  int hyperCounter;    // Time counter for hyperspace.
  
  int shipsLeft1;       // add by yipl
  int shipCounter1;     // add by yipl
  int hyperCounter1;    // add by yipl
  
  boolean active;                // Active flag.
  double  angle;                 // Current angle of rotation.
  double  deltaAngle;            // Amount to change the rotation angle.
  double  currentX, currentY;    // Current position on screen.
  double  deltaX, deltaY;        // Amount to change the screen position.
  Polygon sprite;

  // Photon data.

  int[] photonCounter = new int[MAX_SHOTS];    // Time counter for life of a photon.
  int   photonIndex;                           // Next available photon sprite.

  // Flying saucer data.

  int ufoPassesLeft;    // Number of flying saucer passes.
  int ufoCounter;       // Time counter for each pass.

  // Missle data.

  int missleCounter;    // Counter for life of missle.
  int missleCounter1;  

  // Asteroid data.

  boolean[] asteroidIsSmall = new boolean[MAX_ROCKS];    // Asteroid size flag.
  int       asteroidsCounter;                            // Break-time counter.
  int       asteroidsSpeed;                              // Asteroid speed.
  int       asteroidsLeft;                               // Number of active asteroids.

  // Explosion data.

  int[] explosionCounter = new int[MAX_SCRAP];  // Time counters for explosions.
  int   explosionIndex;                         // Next available explosion sprite.

  // Sound clips.

  AudioClip crashSound;
  AudioClip explosionSound;
  AudioClip fireSound;
  AudioClip missleSound;
  AudioClip saucerSound;
  AudioClip thrustersSound;
  AudioClip warpSound;

  // Flags for looping sound clips.

  boolean thrustersPlaying;
  boolean saucerPlaying;
  boolean misslePlaying;

  // Values for the offscreen image.

  Dimension offDimension;
  Image offImage;
  Graphics offGraphics;

  // Font data.

  Font font = new Font("Helvetica", Font.BOLD, 12);
  FontMetrics fm;
  int fontWidth;
  int fontHeight;

  // Applet information.

  public String getAppletInfo() {

    return("Asteroids, Copyright 1998 by Mike Hall.");
  }

  public void init() {

    Graphics g;
    Dimension d;
    int i;

    // Take credit.

    System.out.println("Asteroids, Copyright 1998 by Mike Hall.");

    // Find the size of the screen and set the values for sprites.

    resize(700,700);//yipl
    g = getGraphics();
    d = getSize();//yipl
    AsteroidsSprite.width = d.width;
    AsteroidsSprite.height = d.height;

    // Generate starry background.

    numStars = AsteroidsSprite.width * AsteroidsSprite.height / 5000;
    stars = new Point[numStars];
    for (i = 0; i < numStars; i++)
      stars[i] = new Point((int) (Math.random() * AsteroidsSprite.width), (int) (Math.random() * AsteroidsSprite.height));

    // Create shape for the ship sprite.
  
    ship = new AsteroidsSprite();
    ship.shape.addPoint(0, -10);
    ship.shape.addPoint(7, 10);
    ship.shape.addPoint(-7, 10);
    
   //add by yipl
    ship1 = new AsteroidsSprite();
    ship1.shape.addPoint(0, -10);
    ship1.shape.addPoint(7, 10);
    ship1.shape.addPoint(-7, 10);

    // Create shape for the photon sprites.

    for (i = 0; i < MAX_SHOTS; i++) {
      photons[i] = new AsteroidsSprite();
      photons[i].shape.addPoint(1, 1);
      photons[i].shape.addPoint(1, -1);
      photons[i].shape.addPoint(-1, 1);
      photons[i].shape.addPoint(-1, -1);
    }

    // Create shape for the flying saucer.

    ufo = new AsteroidsSprite();
    ufo.shape.addPoint(-15, 0);
    ufo.shape.addPoint(-10, -5);
    ufo.shape.addPoint(-5, -5);
    ufo.shape.addPoint(-5, -9);
    ufo.shape.addPoint(5, -9);
    ufo.shape.addPoint(5, -5);
    ufo.shape.addPoint(10, -5);
    ufo.shape.addPoint(15, 0);
    ufo.shape.addPoint(10, 5);
    ufo.shape.addPoint(-10, 5);

   // Create shape for the guided missle.

    missle = new AsteroidsSprite();
    missle.shape.addPoint(0, -4);
    missle.shape.addPoint(1, -3);
    missle.shape.addPoint(1, 3);
    missle.shape.addPoint(2, 4);
    missle.shape.addPoint(-2, 4);
    missle.shape.addPoint(-1, 3);
    missle.shape.addPoint(-1, -3);
    
    //add by yipl
    missle1 = new AsteroidsSprite();
    missle1.shape.addPoint(0, -4);
    missle1.shape.addPoint(1, -3);
    missle1.shape.addPoint(1, 3);
    missle1.shape.addPoint(2, 4);
    missle1.shape.addPoint(-2, 4);
    missle1.shape.addPoint(-1, 3);
    missle1.shape.addPoint(-1, -3);

    // Create asteroid sprites.

    for (i = 0; i < MAX_ROCKS; i++)
      asteroids[i] = new AsteroidsSprite();

    // Create explosion sprites.

    for (i = 0; i < MAX_SCRAP; i++)
      explosions[i] = new AsteroidsSprite();

    // Set font data.

    g.setFont(font);
    fm = g.getFontMetrics();
    fontWidth = fm.getMaxAdvance();
    fontHeight = fm.getHeight();

    // Initialize game data and put us in 'game over' mode.

    highScore = 0;
    sound = true;
    detail = true;
try {
		
	//add by yipl
	Sendstate sendstate = new Sendstate();
		
	
	 
}catch (Exception e) 
{
	
}
    initGame();
  
    endGame();
  }

  public void initGame() {

    // Initialize game data and sprites.

    score = 0;
    shipsLeft = MAX_SHIPS;
    asteroidsSpeed = MIN_ROCK_SPEED;
    newShipScore = NEW_SHIP_POINTS;
    newUfoScore = NEW_UFO_POINTS;
   
    initShip();  
    initPhotons();
    stopUfo();
    stopMissle();
    initAsteroids();
    initExplosions();
    playing = true;
    paused = false;
  
    
  }
  
  public void endGame() {

    // Stop ship, flying saucer, guided missle and associated sounds.

    playing = false;
    stopShip();
    stopUfo();
    stopMissle();
  }

  public void start() {

    if (loopThread == null) {
      loopThread = new Thread(this);
      loopThread.start();
    }
    if (!loaded && loadThread == null) {
      loadThread = new Thread(this);
      loadThread.start();
    }
  }

  public void stop() {

    if (loopThread != null) {
      loopThread.stop();
      loopThread = null;
    }
    if (loadThread != null) {
      loadThread.stop();
      loadThread = null;
    }
   
  }

  public void run() {

    int i, j;
    long startTime;

    // Lower this thread's priority and get the current time.
  
    
   
       
    
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    startTime = System.currentTimeMillis();

    // Run thread for loading sounds.

    if (!loaded && Thread.currentThread() == loadThread) {
      loadSounds();
      loaded = true;
      loadThread.stop();
    }

    // This is the main loop.
    
    
    while (Thread.currentThread() == loopThread) {

      if (!paused) {

        // Move and process all sprites.

        updateShip();
        updatePhotons();
        updateUfo();
        updateMissle();
        updateAsteroids();
        updateExplosions();

        // Check the score and advance high score, add a new ship or start the flying
        // saucer as necessary.

        if (score > highScore)
          highScore = score;
        if (score > newShipScore) {
          newShipScore += NEW_SHIP_POINTS;
          shipsLeft++;
        }
        if (playing && score > newUfoScore && !ufo.active) {
          newUfoScore += NEW_UFO_POINTS;
          ufoPassesLeft = UFO_PASSES;
          initUfo();
        }

        // If all asteroids have been destroyed create a new batch.

        if (asteroidsLeft <= 0)
            if (--asteroidsCounter <= 0)
              initAsteroids();
      }

      // Update the screen and set the timer for the next loop.

      repaint();
      try {
        startTime += DELAY;
        Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }
//client
  public void loadSounds() {

    // Load all sound clips by playing and immediately stopping them.

    try {
      crashSound     = getAudioClip(new URL(getDocumentBase(), "crash.au"));
      explosionSound = getAudioClip(new URL(getDocumentBase(), "explosion.au"));
      fireSound      = getAudioClip(new URL(getDocumentBase(), "fire.au"));
      missleSound    = getAudioClip(new URL(getDocumentBase(), "missle.au"));
      saucerSound    = getAudioClip(new URL(getDocumentBase(), "saucer.au"));
      thrustersSound = getAudioClip(new URL(getDocumentBase(), "thrusters.au"));
      warpSound      = getAudioClip(new URL(getDocumentBase(), "warp.au"));
    }
    catch (MalformedURLException e) {}

    crashSound.play();     crashSound.stop();
    explosionSound.play(); explosionSound.stop();
    fireSound.play();      fireSound.stop();
    missleSound.play();    missleSound.stop();
    saucerSound.play();    saucerSound.stop();
    thrustersSound.play(); thrustersSound.stop();
    warpSound.play();      warpSound.stop();
  }
//Client
  public void initShip() {

    ship.active = true;
    ship.angle = 0.0;
    ship.deltaAngle = 0.0;
    ship.currentX = 0.0;
    ship.currentY = 0.0;
    ship.deltaX = 0.0;
    ship.deltaY = 0.0;
    ship.render();
    if (loaded)
      thrustersSound.stop();
    thrustersPlaying = false;

    hyperCounter = 0;
 //   double [] position= {ship.angle,ship.deltaAngle,ship.currentX,ship.currentY,ship.deltaX,ship.deltaY};
 
    //add by yipl   
    ship1.active = false;
    ship1.angle =12;
    ship1.deltaAngle =11;
    ship1.currentX = 9;
    ship1.currentY = 10;
    ship1.deltaX = 12;
    ship1.deltaY = 11;
    ship1.render();
  }

  public void updateShip() {

    double dx, dy, limit;

    if (!playing)
      return;

    // Rotate the ship if left or right cursor key is down.

    if (left) {
      ship.angle += Math.PI / 16.0;
      if (ship.angle > 2 * Math.PI)
        ship.angle -= 2 * Math.PI;
    }
    if (right) {
      ship.angle -= Math.PI / 16.0;
      if (ship.angle < 0)
        ship.angle += 2 * Math.PI;
    }

    // Fire thrusters if up or down cursor key is down. Don't let ship go past
    // the speed limit.

    dx = -Math.sin(ship.angle);
    dy =  Math.cos(ship.angle);
    limit = 0.8 * MIN_ROCK_SIZE;
    if (up) {
      if (ship.deltaX + dx > -limit && ship.deltaX + dx < limit)
        ship.deltaX += dx;
      if (ship.deltaY + dy > -limit && ship.deltaY + dy < limit)
        ship.deltaY += dy;
    }
    if (down) {
      if (ship.deltaX - dx > -limit && ship.deltaX - dx < limit)
        ship.deltaX -= dx;
      if (ship.deltaY - dy > -limit && ship.deltaY - dy < limit)
        ship.deltaY -= dy;
    }

    // Move the ship. If it is currently in hyperspace, advance the countdown.
    
    if (ship.active) {
      ship.advance();
      ship.render();
      angle = ship.angle;
      if (hyperCounter > 0)
        hyperCounter--;
    }

    // Ship is exploding, advance the countdown or create a new ship if it is
    // done exploding. The new ship is added as though it were in hyperspace.
    // (This gives the player time to move the ship if it is in imminent danger.)
    // If that was the last ship, end the game.

    else
      if (--shipCounter <= 0)
        if (shipsLeft > 0) {
          initShip();
          hyperCounter = HYPER_COUNT;
        }
        else
          endGame();
  }

  public void stopShip() {

    ship.active = false;
    ship1.active = false;//add by yipl
    shipCounter = SCRAP_COUNT;
    if (shipsLeft > 0)
      shipsLeft--;
    if (loaded)
      thrustersSound.stop();
    thrustersPlaying = false;
  }

  public void initPhotons() {

    int i;

    for (i = 0; i < MAX_SHOTS; i++) {
      photons[i].active = false;
      photonCounter[i] = 0;
    }
    photonIndex = 0;
  }

  public void updatePhotons() {

    int i;

    // Move any active photons. Stop it when its counter has expired.

    for (i = 0; i < MAX_SHOTS; i++)
      if (photons[i].active) {
        photons[i].advance();
        photons[i].render();
        if (--photonCounter[i] < 0)
          photons[i].active = false;
      }
  }

  public void initUfo() {

    double temp;

    // Randomly set flying saucer at left or right edge of the screen.

    ufo.active = true;
    ufo.currentX = -AsteroidsSprite.width / 2;
    ufo.currentY = Math.random() * AsteroidsSprite.height;
    ufo.deltaX = MIN_ROCK_SPEED + Math.random() * (MAX_ROCK_SPEED - MIN_ROCK_SPEED);
    if (Math.random() < 0.5) {
      ufo.deltaX = -ufo.deltaX;
      ufo.currentX = AsteroidsSprite.width / 2;
    }
    ufo.deltaY = MIN_ROCK_SPEED + Math.random() * (MAX_ROCK_SPEED - MIN_ROCK_SPEED);
    if (Math.random() < 0.5)
      ufo.deltaY = -ufo.deltaY;
    ufo.render();
    saucerPlaying = true;
    if (sound)
      saucerSound.loop();

    // Set counter for this pass.

    ufoCounter = (int) Math.floor(AsteroidsSprite.width / Math.abs(ufo.deltaX));
  }

  public void updateUfo() {

    int i, d;

    // Move the flying saucer and check for collision with a photon. Stop it when its
    // counter has expired.

    if (ufo.active) {
      ufo.advance();
      ufo.render();
      if (--ufoCounter <= 0)
        if (--ufoPassesLeft > 0)
          initUfo();
        else
          stopUfo();
      else {
        for (i = 0; i < MAX_SHOTS; i++)
          if (photons[i].active && ufo.isColliding(photons[i])) {
            if (sound)
              crashSound.play();
            explode(ufo);
            stopUfo();
            score += UFO_POINTS;
          }

          // On occassion, fire a missle at the ship if the saucer is not
          // too close to it.

          d = (int) Math.max(Math.abs(ufo.currentX - ship.currentX), Math.abs(ufo.currentY - ship.currentY));
          if (ship.active && hyperCounter <= 0 && ufo.active && !missle.active &&
              d > 4 * MAX_ROCK_SIZE && Math.random() < .03)
            initMissle();

       }
    }
  }

  public void stopUfo() {

    ufo.active = false;
    ufoCounter = 0;
    ufoPassesLeft = 0;
    if (loaded)
      saucerSound.stop();
    saucerPlaying = false;
  }

  public void initMissle() {

    missle.active = true;
    missle.angle = 0.0;
    missle.deltaAngle = 0.0;
    missle.currentX = ufo.currentX;
    missle.currentY = ufo.currentY;
    missle.deltaX = 0.0;
    missle.deltaY = 0.0;
    missle.render();
    missleCounter = 3 * Math.max(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
    if (sound)
      missleSound.loop();
    misslePlaying = true;
  }
  
  

  public void updateMissle() {

    int i;

    // Move the guided missle and check for collision with ship or photon. Stop it when its
    // counter has expired.

    if (missle.active) {
      if (--missleCounter <= 0)
        stopMissle();
      else {
        guideMissle();
        missle.advance();
        missle.render();
        for (i = 0; i < MAX_SHOTS; i++)
          if (photons[i].active && missle.isColliding(photons[i])) {
            if (sound)
              crashSound.play();
            explode(missle);
            stopMissle();
            score += MISSLE_POINTS;
          }
        if (missle.active && ship.active && hyperCounter <= 0 && ship.isColliding(missle)) {
          if (sound)
            crashSound.play();
          explode(ship);
          stopShip();
          stopUfo();
          stopMissle();
        }
      }
    }
  }

 
  public void guideMissle() {

    double dx, dy, angle;

    if (!ship.active || hyperCounter > 0)
      return;

    // Find the angle needed to hit the ship.

    dx = ship.currentX - missle.currentX;
    dy = ship.currentY - missle.currentY;
    if (dx == 0 && dy == 0)
      angle = 0;
    if (dx == 0) {
      if (dy < 0)
        angle = -Math.PI / 2;
      else
        angle = Math.PI / 2;
    }
    else {
      angle = Math.atan(Math.abs(dy / dx));
      if (dy > 0)
        angle = -angle;
      if (dx < 0)
        angle = Math.PI - angle;
    }

    // Adjust angle for screen coordinates.

    missle.angle = angle - Math.PI / 2;

    // Change the missle's angle so that it points toward the ship.

    missle.deltaX = MIN_ROCK_SIZE / 3 * -Math.sin(missle.angle);
    missle.deltaY = MIN_ROCK_SIZE / 3 *  Math.cos(missle.angle);
  }

  public void stopMissle() {

    missle.active = false;
    missleCounter = 0;
    if (loaded)
      missleSound.stop();
    misslePlaying = false;
  }

  public void initAsteroids() {

    int i, j;
    int s;
    double theta, r;
    int x, y;

    // Create random shapes, positions and movements for each asteroid.

    for (i = 0; i < MAX_ROCKS; i++) {

      // Create a jagged shape for the asteroid and give it a random rotation.

      asteroids[i].shape = new Polygon();
      s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
      for (j = 0; j < s; j ++) {
        theta = 2 * Math.PI / s * j;
        r = MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE));
        x = (int) -Math.round(r * Math.sin(theta));
        y = (int)  Math.round(r * Math.cos(theta));
        asteroids[i].shape.addPoint(x, y);
      }
      asteroids[i].active = true;
      asteroids[i].angle = 0.0;
      asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;

      // Place the asteroid at one edge of the screen.

      if (Math.random() < 0.5) {
        asteroids[i].currentX = -AsteroidsSprite.width / 2;
        if (Math.random() < 0.5)
          asteroids[i].currentX = AsteroidsSprite.width / 2;
        asteroids[i].currentY = Math.random() * AsteroidsSprite.height;
      }
      else {
        asteroids[i].currentX = Math.random() * AsteroidsSprite.width;
        asteroids[i].currentY = -AsteroidsSprite.height / 2;
        if (Math.random() < 0.5)
          asteroids[i].currentY = AsteroidsSprite.height / 2;
      }

      // Set a random motion for the asteroid.

      asteroids[i].deltaX = Math.random() * asteroidsSpeed;
      if (Math.random() < 0.5)
        asteroids[i].deltaX = -asteroids[i].deltaX;
      asteroids[i].deltaY = Math.random() * asteroidsSpeed;
      if (Math.random() < 0.5)
        asteroids[i].deltaY = -asteroids[i].deltaY;

      asteroids[i].render();
      asteroidIsSmall[i] = false;
    }

    asteroidsCounter = STORM_PAUSE;
    asteroidsLeft = MAX_ROCKS;
    if (asteroidsSpeed < MAX_ROCK_SPEED)
      asteroidsSpeed++;
  }

  public void initSmallAsteroids(int n) {

    int count;
    int i, j;
    int s;
    double tempX, tempY;
    double theta, r;
    int x, y;

    // Create one or two smaller asteroids from a larger one using inactive asteroids. The new
    // asteroids will be placed in the same position as the old one but will have a new, smaller
    // shape and new, randomly generated movements.

    count = 0;
    i = 0;
    tempX = asteroids[n].currentX;
    tempY = asteroids[n].currentY;
    do {
      if (!asteroids[i].active) {
        asteroids[i].shape = new Polygon();
        s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
        for (j = 0; j < s; j ++) {
          theta = 2 * Math.PI / s * j;
          r = (MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE))) / 2;
          x = (int) -Math.round(r * Math.sin(theta));
          y = (int)  Math.round(r * Math.cos(theta));
          asteroids[i].shape.addPoint(x, y);
        }
        asteroids[i].active = true;
        asteroids[i].angle = 0.0;
        asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;
        asteroids[i].currentX = tempX;
        asteroids[i].currentY = tempY;
        asteroids[i].deltaX = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
        asteroids[i].deltaY = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
        asteroids[i].render();
        asteroidIsSmall[i] = true;
        count++;
        asteroidsLeft++;
      }
      i++;
    } while (i < MAX_ROCKS && count < 2);
  }

  public void updateAsteroids() {

    int i, j;

    // Move any active asteroids and check for collisions.

    for (i = 0; i < MAX_ROCKS; i++)
      if (asteroids[i].active) {
        asteroids[i].advance();
        asteroids[i].render();

        // If hit by photon, kill asteroid and advance score. If asteroid is large,
        // make some smaller ones to replace it.

        for (j = 0; j < MAX_SHOTS; j++)
          if (photons[j].active && asteroids[i].active && asteroids[i].isColliding(photons[j])) {
            asteroidsLeft--;
            asteroids[i].active = false;
            photons[j].active = false;
            if (sound)
              explosionSound.play();
            explode(asteroids[i]);
            if (!asteroidIsSmall[i]) {
              score += BIG_POINTS;
              initSmallAsteroids(i);
            }
            else
              score += SMALL_POINTS;
          }

        // If the ship is not in hyperspace, see if it is hit.

        if (ship.active && hyperCounter <= 0 && asteroids[i].active && asteroids[i].isColliding(ship)) {
          if (sound)
            crashSound.play();
          explode(ship);
          stopShip();
          stopUfo();
          stopMissle();
        }
    }
  }

  public void initExplosions() {

    int i;

    for (i = 0; i < MAX_SCRAP; i++) {
      explosions[i].shape = new Polygon();
      explosions[i].active = false;
      explosionCounter[i] = 0;
    }
    explosionIndex = 0;
  }

  public void explode(AsteroidsSprite s) {

    int c, i, j;

    // Create sprites for explosion animation. The each individual line segment of the given sprite
    // is used to create a new sprite that will move outward  from the sprite's original position
    // with a random rotation.

    s.render();
    c = 2;
    if (detail || s.sprite.npoints < 6)
      c = 1;
    for (i = 0; i < s.sprite.npoints; i += c) {
      explosionIndex++;
      if (explosionIndex >= MAX_SCRAP)
        explosionIndex = 0;
      explosions[explosionIndex].active = true;
      explosions[explosionIndex].shape = new Polygon();
      explosions[explosionIndex].shape.addPoint(s.shape.xpoints[i], s.shape.ypoints[i]);
      j = i + 1;
      if (j >= s.sprite.npoints)
        j -= s.sprite.npoints;
      explosions[explosionIndex].shape.addPoint(s.shape.xpoints[j], s.shape.ypoints[j]);
      explosions[explosionIndex].angle = s.angle;
      explosions[explosionIndex].deltaAngle = (Math.random() * 2 * Math.PI - Math.PI) / 15;
      explosions[explosionIndex].currentX = s.currentX;
      explosions[explosionIndex].currentY = s.currentY;
      explosions[explosionIndex].deltaX = -s.shape.xpoints[i] / 5;
      explosions[explosionIndex].deltaY = -s.shape.ypoints[i] / 5;
      explosionCounter[explosionIndex] = SCRAP_COUNT;
    }
  }

  public void updateExplosions() {

    int i;

    // Move any active explosion debris. Stop explosion when its counter has expired.

    for (i = 0; i < MAX_SCRAP; i++)
      if (explosions[i].active) {
        explosions[i].advance();
        explosions[i].render();
        if (--explosionCounter[i] < 0)
          explosions[i].active = false;
      }
  }

  public boolean keyDown(Event e, int key) {

    // Check if any cursor keys have been pressed and set flags.

    if (key == Event.LEFT)
      left = true;
    if (key == Event.RIGHT)
      right = true;
    if (key == Event.UP)
      up = true;
    if (key == Event.DOWN)
      down = true;

    if ((up || down) && ship.active && !thrustersPlaying) {
      if (sound && !paused)
        thrustersSound.loop();
      thrustersPlaying = true;
    }

    // Spacebar: fire a photon and start its counter.

    if (key == 32 && ship.active) {
      if (sound & !paused)
        fireSound.play();
      photonIndex++;
      if (photonIndex >= MAX_SHOTS)
        photonIndex = 0;
      photons[photonIndex].active = true;
      photons[photonIndex].currentX = ship.currentX;
      photons[photonIndex].currentY = ship.currentY;
      photons[photonIndex].deltaX = MIN_ROCK_SIZE * -Math.sin(ship.angle);
      photons[photonIndex].deltaY = MIN_ROCK_SIZE *  Math.cos(ship.angle);
      photonCounter[photonIndex] = Math.min(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
    }

    // 'H' key: warp ship into hyperspace by moving to a random location and starting counter.

    if (key == 104 && ship.active && hyperCounter <= 0) {
      ship.currentX = Math.random() * AsteroidsSprite.width;
      ship.currentX = Math.random() * AsteroidsSprite.height;
      hyperCounter = HYPER_COUNT;
      if (sound & !paused)
        warpSound.play();
    }

    // 'P' key: toggle pause mode and start or stop any active looping sound clips.

    if (key == 112) {
      if (paused) {
        if (sound && misslePlaying)
          missleSound.loop();
        if (sound && saucerPlaying)
          saucerSound.loop();
        if (sound && thrustersPlaying)
          thrustersSound.loop();
      }
      else {
        if (misslePlaying)
          missleSound.stop();
        if (saucerPlaying)
          saucerSound.stop();
        if (thrustersPlaying)
          thrustersSound.stop();
      }
      paused = !paused;
    }

    // 'M' key: toggle sound on or off and stop any looping sound clips.

    if (key == 109 && loaded) {
      if (sound) {
        crashSound.stop();
        explosionSound.stop();
        fireSound.stop();
        missleSound.stop();
        saucerSound.stop();
        thrustersSound.stop();
        warpSound.stop();
      }
      else {
        if (misslePlaying && !paused)
          missleSound.loop();
        if (saucerPlaying && !paused)
          saucerSound.loop();
        if (thrustersPlaying && !paused)
          thrustersSound.loop();
      }
      sound = !sound;
    }

    // 'D' key: toggle graphics detail on or off.

    if (key == 100)
      detail = !detail;

    // 'S' key: start the game, if not already in progress.

    if (key == 115 && loaded && !playing)
      
    	initGame();

    return true;
  }

  public boolean keyUp(Event e, int key) {

    // Check if any cursor keys where released and set flags.

    if (key == Event.LEFT)
      left = false;
    if (key == Event.RIGHT)
      right = false;
    if (key == Event.UP)
      up = false;
    if (key == Event.DOWN)
      down = false;

    if (!up && !down && thrustersPlaying) {
      thrustersSound.stop();
      thrustersPlaying = false;
    }


    return true;
  }

  public void paint(Graphics g) {

    update(g);
  }

  public void update(Graphics g) {

    Dimension d = getSize();
    
    int i;
    int c;
    String s;

    // Create the offscreen graphics context, if no good one exists.

    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
    }

    // Fill in background and stars.

    offGraphics.setColor(Color.black);
    offGraphics.fillRect(0, 0, d.width, d.height);
    if (detail) {
      offGraphics.setColor(Color.white);
      for (i = 0; i < numStars; i++)
        offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
    }

    // Draw photon bullets.

    offGraphics.setColor(Color.white);
    for (i = 0; i < MAX_SHOTS; i++)
      if (photons[i].active)
        offGraphics.drawPolygon(photons[i].sprite);

    // Draw the guided missle, counter is used to quickly fade color to black when near expiration.

    c = Math.min(missleCounter * 24, 255);
    offGraphics.setColor(new Color(c, c, c));
    if (missle.active) {
      offGraphics.drawPolygon(missle.sprite);
      offGraphics.drawLine(missle.sprite.xpoints[missle.sprite.npoints - 1], missle.sprite.ypoints[missle.sprite.npoints - 1],
                           missle.sprite.xpoints[0], missle.sprite.ypoints[0]);
      
   
      
      
    }
    //add by yipl
    if (missle1.active) {
        offGraphics.drawPolygon(missle1.sprite);
        offGraphics.drawLine(missle1.sprite.xpoints[missle1.sprite.npoints - 1], missle1.sprite.ypoints[missle1.sprite.npoints - 1],
                             missle1.sprite.xpoints[0], missle1.sprite.ypoints[0]);
      
   
        
      } 

    // Draw the asteroids.

    for (i = 0; i < MAX_ROCKS; i++)
      if (asteroids[i].active) {
        if (detail) {
          offGraphics.setColor(Color.black);
          offGraphics.fillPolygon(asteroids[i].sprite);
        }
        offGraphics.setColor(Color.white);
        offGraphics.drawPolygon(asteroids[i].sprite);
        offGraphics.drawLine(asteroids[i].sprite.xpoints[asteroids[i].sprite.npoints - 1], asteroids[i].sprite.ypoints[asteroids[i].sprite.npoints - 1],
                             asteroids[i].sprite.xpoints[0], asteroids[i].sprite.ypoints[0]);
      }

    // Draw the flying saucer.

    if (ufo.active) {
      if (detail) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ufo.sprite);
      }
      offGraphics.setColor(Color.white);
      offGraphics.drawPolygon(ufo.sprite);
      offGraphics.drawLine(ufo.sprite.xpoints[ufo.sprite.npoints - 1], ufo.sprite.ypoints[ufo.sprite.npoints - 1],
                           ufo.sprite.xpoints[0], ufo.sprite.ypoints[0]);
    }

    // Draw the ship, counter is used to fade color to white on hyperspace.

    c = 255 - (255 / HYPER_COUNT) * hyperCounter;
    if (ship.active) {
      if (detail && hyperCounter == 0) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ship.sprite);
      }
      offGraphics.setColor(new Color(c, c, c));
      offGraphics.drawPolygon(ship.sprite);
      offGraphics.drawLine(ship.sprite.xpoints[ship.sprite.npoints - 1], ship.sprite.ypoints[ship.sprite.npoints - 1],
                           ship.sprite.xpoints[0], ship.sprite.ypoints[0]);
    }
    //add by yipl for ship1
  if (ship1.active) {
        if (detail && hyperCounter == 0) {
          offGraphics.setColor(Color.black);
          offGraphics.fillPolygon(ship1.sprite);
        }
        offGraphics.setColor(new Color(c, c, c));
        offGraphics.drawPolygon(ship1.sprite);
        offGraphics.drawLine(ship1.sprite.xpoints[ship1.sprite.npoints - 1], ship1.sprite.ypoints[ship.sprite.npoints - 1],
                             ship1.sprite.xpoints[0], ship1.sprite.ypoints[0]);
      }
     
      
    // Draw any explosion debris, counters are used to fade color to black.

    for (i = 0; i < MAX_SCRAP; i++)
      if (explosions[i].active) {
        c = (255 / SCRAP_COUNT) * explosionCounter [i];
        offGraphics.setColor(new Color(c, c, c));
        offGraphics.drawPolygon(explosions[i].sprite);
      }

    // Display status and messages.

    offGraphics.setFont(font);
    offGraphics.setColor(Color.white);

    offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
    offGraphics.drawString("Ships: " + shipsLeft, fontWidth, d.height - fontHeight);
    s = "High: " + highScore;
    offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
    if (!sound) {
      s = "Mute";
      offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
    }

    if (!playing) {
      s = "A S T E R O I D S";
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2);
      s = "Copyright 1998 by Mike Hall";
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
      if (!loaded) {
        s = "Loading sounds...";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
      }
      else {
        s = "Game Over";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
        s = "'S' to Start";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
      }
    }
    else if (paused) {
      s = "Game Paused";
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
    }

    // Copy the off screen buffer to the screen.

    g.drawImage(offImage, 0, 0, this);
  }
 

  //add by yipl for communication with Server
  public class Sendstate extends Socket
  {
  	private static final String SERVER_IP ="127.0.0.1";
  	private static final int SERVER_PORT =2014;
      
      public Socket client;
      public ObjectInputStream objectInput;
      public ObjectOutputStream objectOutput;
      boolean advanceflag = false;//set the flag for Dead Reckoning
      double limit = 0.8 * MIN_ROCK_SIZE;
     
  	
      public Sendstate ()throws Exception
      {
         
    	  
    	  super(SERVER_IP, SERVER_PORT);
    	 
          client =this;
   
          
          objectOutput = new ObjectOutputStream(client.getOutputStream());
          objectInput = new ObjectInputStream(client.getInputStream());
   
 
  	      readThread readtheread = new readThread();
  	      readtheread.start();
  	      advanceThread advancethread = new advanceThread();
  	      advancethread.start();
  	      
      }
     //add by yipl for Dead Reckoning 
      class advanceThread extends Thread
      {
    	  public void run() 
    	  {
    		 long delaytime = System.currentTimeMillis();
    		  while(true)
    		 {
	 		
    			  if((ship1.active)&&(System.currentTimeMillis()- delaytime>50)&&advanceflag)
    			
	 			    {
	 			    	ship1.advance(); 
	 			    	ship1.render();
	 			    	delaytime = System.currentTimeMillis();

	 			    }
	 			   
	 			
	 			}
	
    		 }
    		
    	  }
      
   
      class readThread extends Thread 
      {
           public void run() 
           {
          	
          	 try {
          		 	
          		 		
          		 		Shipstate state = new Shipstate();
          		 	  	long starttime = System.currentTimeMillis();
          		 	  	long startship1time;
          		 	  	int i=0;
	  			    	double movespeed = 1;
	  			    	double x;
	  			    	double y;
	  			    	int slowtime = 10;
	  			    	boolean correctionflag = false;
       		 	  	      		 	  	
      			  		while (true)
      			  		{
  			
      		
      			  	    	  
      			  	    	state.setactive(ship.active);
    			  			state.setangle(ship.angle);
    			  			state.setcurrentX(ship.currentX);
    			  			state.setcurrentY(ship.currentY);
    			  			state.setdeltaAnglee(ship.deltaAngle);
    			  			state.setdeltaX(ship.deltaX);
    			  			state.setdeltaY(ship.deltaY);
    			  			state.setsprite(ship.sprite);
      	
      			  		
      			  			state.setphotons(photons);
      			  			state.setufo(ufo);
      			  			state.setmissle(missle);
      			  			state.setasteroids(asteroids);
      			  			
      			  			state.setphotonCounter(photonCounter);
      			  			state.setphotonIndex(photonIndex);
      			  			state.setufoPassesLeft(ufoPassesLeft);
      			  			state.setufoCounter(ufoCounter);
      			  			state.setasteroidIsSmall(asteroidIsSmall);
      			  			state.setasteroidsCounter(asteroidsCounter);
      			  			state.setasteroidsSpeed(asteroidsSpeed);
      			  			state.setasteroidsLeft(asteroidsLeft);
      			  			
      		
      			  			objectOutput.writeObject(state);
      			  			objectOutput.flush();
      			  			objectOutput.reset();
      			  			
      			  			startship1time = System.currentTimeMillis();
      			  			
      			  			advanceflag = true;
      			  			Shipstate message = (Shipstate)objectInput.readObject();
      			  			advanceflag = false;
      			  			
      			  			if ((System.currentTimeMillis()-startship1time)>500)
      			  			{
      			  				correctionflag = true;
      			  				
      			  			}
      			  			
      			  			ship1.active = message.active;
      			  			ship1.sprite =  message.sprite;
      			  			ship1.deltaAngle = message.deltaAngle;
      			  			ship1.angle=message.angle;
      			  			ship1.sprite =  message.sprite;
      			  			missle1=message.missle;
   	    
			  				// for correct the position when network delay
			  			    if((Math.abs(Math.abs(message.currentX)-Math.abs(ship1.currentX))>20.0 || Math.abs(Math.abs(message.currentY)-Math.abs(ship1.currentY))>20.0)&& correctionflag)		
			  				{
//			  			    		long testtime =  System.currentTimeMillis();
			  						x = message.currentX-ship1.currentX;
			  						y = (message.currentY-ship1.currentY)*(-1);
			  						ship1.deltaY=0.0;
			  						ship1.deltaX=0.0;
			  						int timex = Math.abs((int) (x/movespeed));
			  						int timey = Math.abs((int)(y/movespeed));
			  							
			  						if (x>0)
		  								ship1.deltaX =movespeed;
		  							else
		  								ship1.deltaX=-movespeed;
			  							
			  						if (y>0)
		  								ship1.deltaY =movespeed;
		  							else
		  								ship1.deltaY=-movespeed;
			  						
			  						int times = Math.min(timex,timey);
			  						for(i = 0;i<times;i++)
				  							
			  						{
			  							ship1.advance();
			  							ship1.render();
			  							Thread.sleep(slowtime);
			  					
			  						}
			  							
			  						if( times==timex)
			  						{
			  							ship1.deltaX=0.0;		
				  						for(i = 0;i<timey-timex;i++)
				  						{
				  							ship1.advance();
				  							ship1.render();
				  							Thread.sleep(slowtime);
				  						}
				  							
			  						}
				  					else
				  					{
				  						ship1.deltaY=0.0;
				  						for(i = 0;i<timex-timey;i++)
					  					{
				  							ship1.advance();
				  							ship1.render();
				  							Thread.sleep(slowtime);
					  					}
				  							
				  					}
			  						correctionflag=false;
	/*									System.out.println("timex:"+timex);
							  			System.out.println("timey:"+timey);
						  			    System.out.println("ship1.Xmoveto:"+ship1.currentX);
						  			    System.out.println("message.currentX:"+message.currentX);
						  			    System.out.println("ship1.Ymoveto:"+ship1.currentY);
						  			    System.out.println("message.currentY3:"+message.currentY);			  			    
						  			    System.out.println("runtime: "+(System.currentTimeMillis()-testtime));
		  					
						  			   
						  			    System.out.println("-----");
		*/	  		
		  						}
			  			   
			  			    	ship1.deltaX = message.deltaX;
				  				ship1.deltaY = message.deltaY;
				  				ship1.currentX = message.currentX;
				  				ship1.currentY = message.currentY;

			  					//for Bucket Sychronization 
		    		  			if ((System.currentTimeMillis()-starttime)>5000)
		    		  			{
		    		  				photons = message.photons;
		    		  				ufo=message.ufo;	  				
		    		  				asteroids=message.asteroids;
		    		  				
		    		  				photonCounter = message.photonCounter;
		    		  				photonIndex = message .photonIndex;
		    		  				ufoPassesLeft = message.ufoPassesLeft;
		    		  				ufoCounter = message.ufoCounter;
		    		  				asteroidIsSmall = message.asteroidIsSmall;
		    		  				asteroidsCounter = message.asteroidsCounter;
		    		  				asteroidsSpeed = message.asteroidsSpeed;
		    		  				asteroidsLeft = message.asteroidsLeft;
	
		    		  				starttime = System.currentTimeMillis();

		    		  			}
		    		  		
    		  	
      			  		}
          			 		}catch (Exception e) { e.printStackTrace();}

         	finally
          	{
          		try {objectInput.close();
          		objectOutput.close();
          		close();
          
          			}catch (IOException e) 
              	{ e.printStackTrace();}
              	} 
           	} 
      }
  }


 
 
  public static void main(String[] args)
  {

    Asteroids asteroids = new Asteroids();
    asteroids.run();

  
  }
}


