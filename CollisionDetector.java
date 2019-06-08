/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robot1;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import java.util.Enumeration;
import javax.media.j3d.*;


/**
 *
 * @author tomok
 */
class CollisionDetector extends Behavior {
  /** The separate criteria used to wake up this beahvior. */
  protected WakeupCriterion[] theCriteria;
 
  /** The OR of the separate criteria. */
  protected WakeupOr oredCriteria;
 
  /** The shape that is watched for collision. */
  protected Sphere collidingShape;
  
  public boolean inCollision = false;
 
  
  public CollisionDetector(Sphere theShape, Bounds theBounds) {
    collidingShape = theShape;
    theShape.setCollisionBounds(theBounds);
  }
 
  /**
   * This creates an entry, exit and movement collision criteria. These are
   * then OR'ed together, and the wake up condition set to the result.
   */
  public void initialize() {
    theCriteria = new WakeupCriterion[2];
    theCriteria[0] = new WakeupOnCollisionEntry(collidingShape);
    theCriteria[1] = new WakeupOnCollisionExit(collidingShape);
    oredCriteria = new WakeupOr(theCriteria);
    wakeupOn(oredCriteria);
  }
 
  /**
   * Where the work is done in this class. A message is printed out using the
   * userData of the object collided with. The wake up condition is then set
   * to the OR'ed criterion again.
   */
  public void processStimulus(Enumeration criteria) {
    WakeupCriterion theCriterion = (WakeupCriterion) criteria.nextElement();
    
    if (theCriterion instanceof WakeupOnCollisionEntry) {
        if(!inCollision)
            inCollision = true;

      System.out.println("Collided");
    } 
    else if (theCriterion instanceof WakeupOnCollisionExit) {
        if(inCollision)
            inCollision = false;

      System.out.println("Stopped colliding");
    } 
    else
        inCollision = false;
    
    wakeupOn(oredCriteria);  }
}
