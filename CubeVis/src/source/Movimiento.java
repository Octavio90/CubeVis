/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Edgar
 */

package source;

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
class Movimiento extends Behavior{
  final static int HOMBRO=0;
  final static int CODO=1;
  final static int MUNECA=2;
  final static int PISO=3;
  private TransformGroup caja;
  private Transform3D tr;
  private Transform3D rox,roy,roz;
  private WakeupOnAWTEvent trigger;
  private double ahom,acod,amun;
  private double bhom,bcod,bmun;
  private double IpositionX, IpositionY;
  private double ApositionX, ApositionY;
  private int place;
  private Appearance pint1,pint2,pint3,pint4;
  private Material mat1,mat2;
  private float sizeX, sizeY, move;
  
  public Movimiento(TransformGroup box, float width, float posX, float high, float posY, float move){
    IpositionX = posX;
    IpositionY = posY;
    this.sizeX = width;
    this.sizeY = high;
    this.move = move;
    caja=box;
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
              
          case KeyEvent.VK_UP:    if((ApositionY + IpositionY) == sizeY/2){
                                      ApositionY = ApositionY - sizeY;
                                  }else{
                                      ApositionY += move;
                                  }
                                  break;
              
          case KeyEvent.VK_DOWN:  if((ApositionY + IpositionY) == -sizeY/2){
                                      ApositionY = ApositionY + sizeY;
                                  }else{
                                      ApositionY -= move;
                                  }
                                  break;
              
          case KeyEvent.VK_LEFT:  if((ApositionX + IpositionX) == -sizeX/2){
                                      ApositionX = ApositionX + sizeX;
                                  }else{
                                      ApositionX -= move;
                                  }
                                  break;
          case KeyEvent.VK_RIGHT: 
                                  if((ApositionX + IpositionX) == sizeX/2){
                                      ApositionX = ApositionX - sizeX;
                                  }else{
                                      ApositionX += move;
                                  }
                                                                        
                                  break;
          default:  break;
        }
      }
    }
    //modificar los Tgroups
    
    tr.set(new Vector3f((float)ApositionX,(float)ApositionY,0.0f));
    caja.setTransform(tr);
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

