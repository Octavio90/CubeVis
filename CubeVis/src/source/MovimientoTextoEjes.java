/*
 * To change this template, choose Tools | Templates

 * and open the template in the editor.
 */
package source;
/**
 *
 * @author Edgar
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.AWTEvent;
import java.util.Enumeration;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.Sphere;
/*
  Mi clase para controlar los Tankes
*/
class MovimientoTextoEjes extends Behavior{
  final static int HOMBRO=0;
  final static int CODO=1;
  final static int MUNECA=2;
  final static int PISO=3;
  private TransformGroup text;
  private Transform3D tr;
  private Transform3D rox,roy,roz;
  private WakeupOnAWTEvent trigger;
  private double ahom,acod,amun;
  private double bhom,bcod,bmun;
  private int IpositionX, IpositionY;
  private double ApositionX, ApositionY;
  private int place;
  private Appearance pint1,pint2,pint3,pint4;
  private Material mat1,mat2;
  private float moveX, moveY, restartX, restartY;
  private int sizeX;
  private int sizeY;
  
  public MovimientoTextoEjes(TransformGroup txt, int numX, int posX, float moveX, float restartX, int numY, int posY, float moveY, float restartY){
    IpositionX = posX;
    IpositionY = posY;
    this.sizeX = numX;
    this.sizeY = numY;
    this.moveX = moveX;
    this.restartX = restartX;
    this.moveY = moveY;
    this.restartY = restartY;
    text=txt;
    tr=new Transform3D();
    trigger=new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
    
  }
  public void initialize(){
    this.wakeupOn(trigger);
  }
  public void processStimulus(Enumeration criteria){
    while (criteria.hasMoreElements()) {
	  	WakeupCriterion wakeup = (WakeupCriterion) criteria.nextElement();
      if(wakeup instanceof WakeupOnAWTEvent){
        AWTEvent[] arr=((WakeupOnAWTEvent)(wakeup)).getAWTEvent();
        KeyEvent ke=(KeyEvent)arr[0];
        switch(ke.getKeyCode()){
          case KeyEvent.VK_ENTER: if(place==PISO){
                                    pint1.setMaterial(mat1);
                                    pint2.setMaterial(mat2);
                                    place=HOMBRO;
                                  }
                                  else if(place==HOMBRO){
                                    pint2.setMaterial(mat1);
                                    pint3.setMaterial(mat2);
                                    place=CODO;
                                  }
                                  else if(place==CODO){
                                    pint3.setMaterial(mat1);
                                    pint4.setMaterial(mat2);
                                    place=MUNECA;
                                  }
                                  else{
                                    pint4.setMaterial(mat1);
                                    pint1.setMaterial(mat2);
                                    place=PISO;
                                  }
                                  break;
              
          case KeyEvent.VK_UP:    if(IpositionY == 1){
                                    IpositionY = sizeY;
                                    ApositionY -= (moveY * (sizeY - 1) ) ;
                                  }else{
                                      IpositionY --;
                                      ApositionY += moveY;
                                  }
                                  break;
              
          case KeyEvent.VK_DOWN:  if(IpositionY == sizeY){
                                    IpositionY = 1;
                                    ApositionY += (moveY * (sizeY - 1) ) ;
                                  }else{
                                      IpositionY ++;
                                      ApositionY -= moveY;
                                  }
                                  break;
              
          case KeyEvent.VK_LEFT:  if(IpositionX == 1){
                                    IpositionX = sizeX;
                                    //ApositionX = restartX + (moveX * (sizeX - 1) ) ;
                                    ApositionX += (moveX * (sizeX - 1) ) ;
                                    
                                  }else{
                                      IpositionX --;
                                      ApositionX -= moveX ;
                                  }
                                  break;
          case KeyEvent.VK_RIGHT: 
                                  if(IpositionX == sizeX){
                                    IpositionX = 1;
                                    ApositionX -= moveX * (sizeX -1) ;
                                  }else{
                                      IpositionX ++;
                                      ApositionX += moveX;
                                  }
                                                                        
                                  break;
          default:  break;
        }
      }
    }
    //modificar los Tgroups
    
    tr.set(new Vector3f((float)ApositionX,(float)ApositionY,0.0f));
    text.setTransform(tr);
    //rox.rotX(bhom);
    //roz.rotZ(ahom);
    //roz.mul(rox);
    //rox.rotX(bcod);
    //roz.rotZ(acod);
    //roz.mul(rox);
    //roz.rotZ(amun);
    this.wakeupOn(trigger);
  }
}

