import java.awt.Polygon;
import java.io.Serializable;

public class Shipstate implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	  boolean active;                // Active flag.
	  double  angle;                 // Current angle of rotation.
	  double  deltaAngle;            // Amount to change the rotation angle.
	  double  currentX, currentY;    // Current position on screen.
	  double  deltaX, deltaY;        // Amount to change the screen position.
	  Polygon sprite;
	  
	  AsteroidsSprite[] asteroids;
	  AsteroidsSprite[] photons;
	  AsteroidsSprite ufo;
	  AsteroidsSprite missle;
	  int[] photonCounter;
	  int photonIndex; 
	  int ufoPassesLeft;
	  int ufoCounter;
	  boolean[] asteroidIsSmall;
	  int       asteroidsCounter; 
	  int       asteroidsSpeed; 
	  int       asteroidsLeft;  
	  
	  
	  
	  public void setasteroidIsSmall( boolean[] asteroidIsSmall) 
	  {
		  this.asteroidIsSmall = asteroidIsSmall;
	  }
	  public void setasteroidsCounter(int asteroidsCounter) 
	  {
		  this.asteroidsCounter = asteroidsCounter;
	  }
	  
	  public void setphotonCounter(int[] photonCounter) 
	  {
		  this.photonCounter = photonCounter;
	  }
	  public void setphotonIndex(int photonIndex) 
	  {
		  this.photonIndex = photonIndex;
	  }
	  public void setufoPassesLeft(int ufoPassesLeft) 
	  {
		  this.ufoPassesLeft = ufoPassesLeft;
	  }
	  public void setufoCounter(int ufoCounter) 
	  {
		  this.ufoCounter = ufoCounter;
	  }
	  public void setasteroidsSpeed( int asteroidsSpeed) 
	  {
		  this.asteroidsSpeed = asteroidsSpeed;
	  }
	  public void setasteroidsLeft(int asteroidsLeft) 
	  {
		  this.asteroidsLeft = asteroidsLeft;
	  }
	 
	  public void setphotons(AsteroidsSprite[] photons) 
	  {
		  this.photons = photons;
	  }
	  public void setufo(AsteroidsSprite ufo) 
	  {
		  this.ufo = ufo;
	  }
	  public void setmissle(AsteroidsSprite missle) 
	  {
		  this.missle = missle;
	  }
	  public void setasteroids(AsteroidsSprite[] asteroids) 
	  {
		  this.asteroids = asteroids;
	  }
	  
	  public void setactive(boolean active)
	  {
		  this.active = active;
	  }
	  
	  public void setangle(double angle)
	  {
		  this.angle = angle;
	  }
	  public void setdeltaAnglee(double  deltaAngle)
	  {
		  this.deltaAngle = deltaAngle;
	  }
	  public void setcurrentX(double  currentX)
	  {
		  this.currentX=currentX;
	  }
	  public void setcurrentY(double currentY)
	  {
		  this.currentY=currentY;
	  }
	  public void setdeltaY(double deltaY)
	  {
		  this.deltaY=deltaY;
	  }
	  public void setdeltaX(double deltaX)
	  {
		  this.deltaY=deltaX;
	  }
	  public void setsprite(Polygon sprite)
	  {
		  this.sprite=sprite;
		  
	  }
}
