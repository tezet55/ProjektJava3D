
package robot1;



import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GraphicsConfiguration;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;




public class Robot1 extends Applet implements KeyListener, ActionListener {
    
    
    private Button home = new Button("Home");
    private Button startLearning = new Button("Start learning");
    private Button stopLearning = new Button("Stop learning");
    private Button play = new Button("Play");
    
    
    private TransformGroup transArm1, transJoint1, transArm2, transGrab, transBase, transBox;
    private TransformGroup objRotate;
    private Transform3D shiftObserver = new Transform3D();
    private Transform3D turn1 = new Transform3D();
    private Transform3D turn2 = new Transform3D();
    private Transform3D shift1 = new Transform3D();
    private Transform3D shift2 = new Transform3D();
    private Transform3D shift3 = new Transform3D();
    private Transform3D pozBox;
    private SimpleUniverse u;
    private BranchGroup element;
    private Timer timer;
    private boolean spacePressed = false;
    private boolean transport = false;
    
    CollisionDetector detect;
    
    private boolean key[];

    
    // Zmienna opisująca aktualne położenie przedmiotu
    public float pozX = 0.0f, pozY = 0.0f, pozZ = 0.0f;
    
    // Zmienna opisujaca kat obrotu ramienia nr 1 względem podstawy robota
    public float angle1 = 0.0f; 
    // Zmienna opisujaca kat obrotu ramienia nr 2 względem ramienia nr 1
    public float angle2 = 0.0f; 
    // Zmienna informująca na jakiej wysokości względem ramienia nr 2 jest środek ciężkości chwytaka
    public float armHeight = 0.0f;
    
    public int lastMove = 0;
    
    public int learnStep[];
    public int learnNumber = 0;
    public boolean isLearning = false;
    public boolean isPlaying = false;
    public int number = 0;
    
    public float startPozX, startPozY,startPozZ, startAngle1, startAngle2, startArmHeight;
    public boolean startTransport;

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== home)
        {
            angle1=0;
            angle2=0;
            armHeight=0;
            
            turn1.rotY(angle1);
            shift1.setTranslation(new Vector3f(0.2f,0.25f,0.0f)); 
            turn1.mul(shift1); 
            transArm1.setTransform(turn1);
            
            turn2.rotY(angle2);  
            shift2.setTranslation(new Vector3f(0.21f,0.1f,0.0f)); 
            turn2.mul(shift2); 
            transArm2.setTransform(shift2);
            
            shift3.setTranslation(new Vector3f(0.25f,-0.1f,0.0f)); 
            transGrab.setTransform(shift3);
            
            
            if(transport)
            {
                transGrab.removeChild(element);
                pozX = 0.86f;
                pozY = 0.065f;
                pozZ = 0;
                pozBox.setTranslation(new Vector3f(pozX, pozY, pozZ));
                Transform3D temp = new Transform3D();
                temp.mul(pozBox);
                transBox.setTransform(temp);
                objRotate.addChild(element);
                transport = false;
            }
            else
            {
                pozX = 0.86f;
                pozY = 0.065f;
                pozZ = 0;
                pozBox.setTranslation(new Vector3f(pozX, pozY, pozZ));
                Transform3D temp = new Transform3D();
                temp.mul(pozBox);
                transBox.setTransform(temp);
                transport = false;
            }
            
        }
        else if(e.getSource() == startLearning)
        {
            isLearning = true;
            learnStep = new int [5000];
            learnNumber = 0;
            startPozX = pozX;
            startPozY = pozY;
            startPozZ = pozZ;
            startAngle1 = angle1;
            startAngle2 = angle2;
            startArmHeight = armHeight;
            startTransport = transport;
            number = 0;
        }
        else if(e.getSource() == stopLearning)
        {
            isLearning = false;
        }
        else if(e.getSource() == play)
        {
            System.out.println(armHeight);
            number = 0;
            angle1 = startAngle1;
            angle2 = startAngle2;
            armHeight = startArmHeight;
            pozX = startPozX;
            pozY = startPozY;
            pozZ = startPozZ;
            turn1.rotY(angle1);
            shift1.setTranslation(new Vector3f(0.2f,0.25f,0.0f)); 
            turn1.mul(shift1); 
            transArm1.setTransform(turn1);
            
            turn2.rotY(angle2);  
            shift2.setTranslation(new Vector3f(0.21f,0.1f,0.0f)); 
            turn2.mul(shift2); 
            transArm2.setTransform(shift2);
            
            shift3.setTranslation(new Vector3f(0.25f,-0.1f,0.0f)); 
            transGrab.setTransform(shift3);
            
            if(transport==false && startTransport==false)
            {
                pozBox.setTranslation(new Vector3f(startPozX, startPozY, startPozZ));
                transBox.setTransform(pozBox);
            }
            if(transport==true && startTransport==false)
            {
                transGrab.removeChild(element);
                pozBox.setTranslation(new Vector3f(startPozX, startPozY, startPozZ));
                transBox.setTransform(pozBox);
                objRotate.addChild(element);
                transport = false;
            }
            if(transport==false && startTransport==true)
            {
                objRotate.removeChild(element);
                pozBox.setTranslation(new Vector3f(0.0f, -0.36f, 0.0f));
                Transform3D temp = new Transform3D();
                temp.mul(pozBox);
                transBox.setTransform(temp);
                transGrab.addChild(element);
                transport = true;
            }
            isPlaying = true;
        }
    }
    
    
    class Task extends TimerTask{

        @Override
        public void run()
        {
            if(isPlaying && number == learnNumber)
            {
                isPlaying = false;
                for(int i = 1; i<8; i++)
                    key[i] = false;
                System.out.println(armHeight);
                
            }
            if(isPlaying)
                play();
            
            if(lastMove==0)
                detect.inCollision = false;
            
            if(key[1])
            {
                if((detect.inCollision && lastMove != 1 && lastMove != 3 || detect.inCollision == false) || transport)
                    {

                    if(!detect.inCollision) lastMove = 1;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 1;
                        learnNumber++;
                    }
                    turnArm1Left(0.005f);

                    }
            }
            if(key[2])
            {
                if(((detect.inCollision && lastMove != 2 && lastMove != 4) || detect.inCollision == false) || transport)
                    {

                    if(!detect.inCollision) lastMove = 2;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 2;
                        learnNumber++;
                    }
                    turnArm1Right(0.005f);
                    }
            }
            if(key[3])
            {
                 if(((detect.inCollision && lastMove != 3 && lastMove != 1) || detect.inCollision == false) || transport)
                    {

                    if(!detect.inCollision) lastMove = 3;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 3;
                        learnNumber++;
                    }
                    turnArm2Left(0.005f);
                    }
            }
            if(key[4])
            {
                if(((detect.inCollision && lastMove != 4 && lastMove != 2) || detect.inCollision == false)|| transport)
                    {

                    if(!detect.inCollision) lastMove = 4;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 4;
                        learnNumber++;
                    }
                    turnArm2Right(0.005f); 
                    }
            }
            if(key[5])
            {
                if(((detect.inCollision && lastMove != 5) || detect.inCollision == false))
                    {

                    if(!detect.inCollision) lastMove = 5;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 5;
                        learnNumber++;
                    }
                    liftGrab(0.005f);
                    }
            }
            if(key[6])
            {
                if(((detect.inCollision && lastMove != 6) || detect.inCollision == false) && armHeight>-0.02)
                    {

                    if(!detect.inCollision) lastMove = 6;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 6;
                        learnNumber++;
                    }
                    lowerGrab(0.005f);
                    }
            }
            if(key[7])
            {
                if(detect.inCollision && !transport)
                {   
                    transport = !transport;
                    System.err.println(armHeight);
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 7;
                        learnNumber++;
                    }
                    if(transport == true)
                    {
                        objRotate.removeChild(element);
                        Transform3D temp = new Transform3D();
                        pozBox.set(new Vector3f(0.0f, -0.36f, 0.0f));
                        temp.mul(pozBox);
                        transBox.setTransform(temp);
                        transGrab.addChild(element);
                                
                                
                    }
                }
                else if(transport)
                {
                    transport = !transport;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 7;
                        learnNumber++;
                    }
                    transGrab.removeChild(element);
                    pozX = (float) (0.435f*Math.cos(angle1)+0.435f*Math.cos(angle1+angle2));
                    pozY = (float) armHeight+0.09f;
                    pozZ = (float) (-(0.435f*Math.sin(angle1)+0.435f*Math.sin(angle1+angle2)));
                    System.out.println(pozX +" "+ pozY+"" +pozZ);
                    pozBox.set(new Vector3f(pozX, pozY, pozZ));
                    Transform3D temp = new Transform3D();
                    temp.mul(pozBox);
                    transBox.setTransform(temp);
                    objRotate.addChild(element);
                    detect.inCollision = false;
                    lastMove=0;
                }
                key[7] = false;
            }

        }
        
        public void play()
        {
            for(int i = 1; i<8; i++)
                key[i] = false;
            switch(learnStep[number])
            {
                case 1: key[1]=true; break;
                case 2: key[2]=true; break;
                case 3: key[3]=true; break;
                case 4: key[4]=true; break;
                case 5: key[5]=true; break;
                case 6: key[6]=true; break;
                case 7: key[7]=true; break;
            }
            number++;
        }
  }
    
    
    // Metody służące do obrotu ramienia nr 1
    
    public void turnArm1Left(float step)
    {
        angle1 += step; 
        turn1.rotY(angle1);  
        shift1.setTranslation(new Vector3f(0.2f,0.25f,0.0f)); 
        turn1.mul(shift1); 
        transArm1.setTransform(turn1);
    }
    
    public void turnArm1Right(float step)
    {
        angle1 -= step;
        turn1.rotY(angle1);
        shift1.setTranslation(new Vector3f(0.2f,0.25f,0.0f));
        turn1.mul(shift1);
        transArm1.setTransform(turn1);
    }
    
    // Metody służące do obrotu ramienia nr 2

    public void turnArm2Left(float step)
    {
        if(angle2<2.355f) {
            angle2 += step;
            turn2.rotY(angle2);
            shift2.setTranslation(new Vector3f(0.21f,0.1f,0.0f));
            turn2.mul(shift2);
            transArm2.setTransform(turn2);
        }
    }

    public void turnArm2Right(float step)
    {
        if(angle2>-2.355f){
            angle2 -= step;
            turn2.rotY(angle2);
            shift2.setTranslation(new Vector3f(0.21f,0.1f,0.0f));
            turn2.mul(shift2);
            transArm2.setTransform(turn2);
        }
    }
    
    // Metody służące do porusznia chwytakiem

    public void liftGrab(float step)
    {
        if(armHeight<0.24f){
            armHeight += step;
            shift3.setTranslation(new Vector3f(0.25f,-0.1f+armHeight,0.0f));
            transGrab.setTransform(shift3);
            }
    }

    void lowerGrab(float step)
    {
        if(armHeight>-0.17f){
            armHeight -= step;
            shift3.setTranslation(new Vector3f(0.25f,-0.1f+armHeight,0.0f));
            transGrab.setTransform(shift3);
            }
    }


    
    Robot1(){
    
          
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        
        Canvas3D canvas = new Canvas3D(config);
        add(BorderLayout.CENTER, canvas);
        canvas.addKeyListener(this);
        
        Panel panel = new Panel();
        panel.add(home);
        add("South", panel);
        home.addActionListener(this);
        home.addKeyListener(this);
        
        
        panel.add(startLearning);
        add("South",panel);
        startLearning.addActionListener(this);
        startLearning.addKeyListener(this);
        
        panel.add(stopLearning);
        add("South",panel);
        stopLearning.addActionListener(this);
        stopLearning.addKeyListener(this);
        
        panel.add(play);
        add("South",panel);
        play.addActionListener(this);
        play.addKeyListener(this);
        
        u = new SimpleUniverse(canvas);
     
        shiftObserver.set(new Vector3f(0.0f,0.2f,3.0f));

        u.getViewingPlatform().getViewPlatformTransform().setTransform(shiftObserver);
        BranchGroup scene = createScene(u);
        u.addBranchGraph(scene);
        
        key = new boolean[8];
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new Task(),0,25);
        
        
    }
    
    BranchGroup createScene(SimpleUniverse su)
    {
        BranchGroup Scena = new BranchGroup();
        TransformGroup vpTrans = null;
        vpTrans = su.getViewingPlatform().getViewPlatformTransform();
        
        //Ustawienie materiału dla wszystkich elementów
        Material mat = new Material();
        mat.setAmbientColor(new Color3f(1.0f, 0.0f, 0.0f));
        
        Material mat2 = new Material();
        mat2.setAmbientColor(new Color3f(0.0f, 0.0f, 0.0f));
        
        //Ustawienie wyglądu dla wszystkich elementów zawartych w Scenie
        Appearance appearance = new Appearance();
        appearance.setColoringAttributes(new ColoringAttributes(0.5f,0.5f,0.9f,ColoringAttributes.NICEST));
        appearance.setMaterial(mat);
        
        Appearance appearance2 = new Appearance();
        appearance2.setColoringAttributes(new ColoringAttributes(0.5f,0.5f,0.9f,ColoringAttributes.NICEST));
        appearance2.setMaterial(mat2);
        
        //PODSTAWA
        Box base = new Box(0.15f,0.01f,0.15f, appearance);
        transBase = new TransformGroup();
        transBase.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transBase.addChild(base);
        
        //WALEC
        Cylinder cylinder = new Cylinder(0.1f, 0.4f, appearance);
        Transform3D pozCylinder = new Transform3D();
        pozCylinder.set(new Vector3f(0.0f, 0.2f, 0));
        TransformGroup transCylinder = new TransformGroup(pozCylinder);
        transCylinder.addChild(cylinder);
        transBase.addChild(transCylinder);
        
        /*
            Stworzenie ramienia nr 1 robota, które składa się z:
            jednego elementu typu Box i dwóch elementów typu Cylinder.
            Element ten jest rodzicem przegubu nr 1.
         */
        Box arm1 = new Box(0.20f, 0.05f, 0.1f, appearance);
        Cylinder cylArm11 = new Cylinder(0.1f, 0.1f, appearance);
        Cylinder cylArm12 = new Cylinder(0.1f, 0.1f, appearance);
         
        Transform3D  pozArm1   = new Transform3D();
        pozArm1.set(new Vector3f(0.2f,0.25f,0.0f));
         
        Transform3D pozCylArm11 = new Transform3D();
        pozCylArm11.set(new Vector3f(-0.2f, 0.0f, 0));
        TransformGroup transCylArm11 = new TransformGroup(pozCylArm11);
        transCylArm11.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transCylArm11.addChild(cylArm11);
         
        Transform3D pozCylArm12 = new Transform3D();
        pozCylArm12.set(new Vector3f(0.2f, 0.0f, 0));
        TransformGroup transCylArm12 = new TransformGroup(pozCylArm12);
        transCylArm12.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transCylArm12.addChild(cylArm12);
         
        transArm1 = new TransformGroup(pozArm1);
        transArm1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transArm1.addChild(arm1);
        transArm1.addChild(transCylArm11);
        transArm1.addChild(transCylArm12);
         
        /*
            Stworzenie przegubu nr 1 robota.
            Jest on rodzicecem ramienia nr 2.
         */
        Cylinder Joint1 = new Cylinder(0.05f, 0.01f, appearance);
        Transform3D  pozJoint1   = new Transform3D();   
        pozJoint1.set(new Vector3f(0.21f,0.0f,0.0f));
        transJoint1 = new TransformGroup(pozJoint1);
        transJoint1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transJoint1.addChild(Joint1);
         
         /*
            Stworzenie ramienia nr 2 robota, które składa się z:
            jednego elementu typu Box i dwóch elementów typu Cylinder.
            Element ten jest rodzicem przegubu chwytaka.
         */
        Box arm2 = new Box(0.2f, 0.05f, 0.1f, appearance);
        Cylinder CylArm21 = new Cylinder(0.1f, 0.1f, appearance);
        Cylinder CylArm22 = new Cylinder(0.1f, 0.1f, appearance);
         
        Transform3D  poz_ramie2 = new Transform3D();
        poz_ramie2.set(new Vector3f(0.21f,0.1f,0.0f));
        
        Transform3D pozCylArm21 = new Transform3D();
        pozCylArm21.set(new Vector3f(-0.2f, 0.0f, 0));
        
        Transform3D pozCylArm22 = new Transform3D();
        pozCylArm22.set(new Vector3f(0.2f, 0.0f, 0));
        
        TransformGroup transCylArm21 = new TransformGroup(pozCylArm21);
        transCylArm21.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transCylArm21.addChild(CylArm21);
        
        TransformGroup transCylArm22 = new TransformGroup(pozCylArm22);
        transCylArm22.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transCylArm22.addChild(CylArm22);
         
        transArm2 = new TransformGroup(poz_ramie2);
        transArm2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transArm2.addChild(arm2);
        transArm2.addChild(transCylArm21);
        transArm2.addChild(transCylArm22);

        //Stworzenie chwytaka, odpowiedzialnego za podnoszenie elementów
        Cylinder grab = new Cylinder(0.02f, 0.6f, appearance);
        Transform3D  pozGrab = new Transform3D();
        pozGrab.set(new Vector3f(0.25f,-0.1f,0.0f));
        transGrab = new TransformGroup(pozGrab);
        transGrab.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGrab.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transGrab.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        transGrab.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        transGrab.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        transGrab.addChild(grab);
        
        //USTAWIENIE DZIECI
        transArm1.addChild(transJoint1);
        transJoint1.addChild(transArm2);
        transArm2.addChild(transGrab);
        transCylinder.addChild(transArm1);
       
        //PRZEDMIOT
        pozX = 0.86f;  pozY = 0.065f;
        Sphere box = new Sphere(0.07f, Sphere.GENERATE_NORMALS,80, appearance);
        pozBox = new Transform3D();
        pozBox.set(new Vector3f(pozX, pozY, pozZ));
        transBox = new TransformGroup(pozBox);
        transBox.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transBox.addChild(box);
        
        //PODLOGA
        Box bottom = new Box(10,0.001f,10,appearance2);
        Transform3D pozBottom = new Transform3D();
        pozBottom.set(new Vector3f(0, -0.01f, 0));
        TransformGroup transBottom = new TransformGroup(pozBottom);
        transBottom.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transBottom.addChild(bottom);
         
        
        
        //ŚWIATŁO KIERUNKOWE
        BoundingSphere bounds = new BoundingSphere (new Point3d(0.0,0.0,0.0),100.0);
        Color3f light1Color = new Color3f(1.0f,1.0f,1.0f);
        Vector3f light1Direction = new Vector3f(4.0f,-7.0f,-12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        Scena.addChild(light1);
        
        //ŚWIATŁO PUNKTOWE
        Color3f ambientColor = new Color3f(1.0f,1.0f,1.0f);
        AmbientLight ambientLightNode= new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        Scena.addChild(ambientLightNode);
        
        //MYSZKA - obsługa
        objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
         
        objRotate.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        objRotate.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        Scena.addChild(objRotate);
        objRotate.addChild(transBase);
        objRotate.addChild(transBottom);
    
        element = new BranchGroup();
        element.setCapability(element.ALLOW_DETACH);
        element.setCapability(element.ALLOW_CHILDREN_WRITE);
        element.setCapability(element.ALLOW_CHILDREN_READ);
        element.addChild(transBox);

        objRotate.addChild(element);

         
        MouseRotate myMouseRotate = new MouseRotate();
        myMouseRotate.setTransformGroup(objRotate);
        myMouseRotate.setSchedulingBounds(new BoundingSphere());
        Scena.addChild(myMouseRotate);

        MouseWheelZoom myMouseZoom = new MouseWheelZoom();
        myMouseZoom.setTransformGroup(vpTrans);
        myMouseZoom.setSchedulingBounds(new BoundingSphere());
        Scena.addChild(myMouseZoom);
        
        detect = new CollisionDetector(box, new BoundingSphere(new Point3d(), 0.06d));
        detect.setSchedulingBounds(new BoundingSphere(new Point3d(), 0.05d));
        Scena.addChild(detect);
        
        
        Scena.compile();
        return Scena;
    }
    
  
    @Override
    public void keyPressed(KeyEvent e){

        switch(e.getKeyCode()){

            case KeyEvent.VK_LEFT:
                key[1] = true;
                break;
            case KeyEvent.VK_RIGHT:
                key[2] = true;
                break;
            case KeyEvent.VK_Z:
                key[3] = true;
                break;
            case KeyEvent.VK_X:
                key[4] = true;
                break;

            case KeyEvent.VK_UP:
                key[5] = true;
                break;
            case KeyEvent.VK_DOWN:
                key[6] = true;
                break;    
            case KeyEvent.VK_V:
                if(!spacePressed)
                {
                    key[7] = true;
                    spacePressed = true;
                }
                break;
       }
    }

    public void keyReleased(KeyEvent e){
        switch(e.getKeyCode()){

            case KeyEvent.VK_LEFT:
                key[1] = false;
                break;
            case KeyEvent.VK_RIGHT:
                key[2] = false;
                break;
            case KeyEvent.VK_Z:
                key[3] = false;
                break;
            case KeyEvent.VK_X:
                key[4] = false;
                break;

            case KeyEvent.VK_UP:
                key[5] = false;
                break;
            case KeyEvent.VK_DOWN:
                key[6] = false;
                break;    
            case KeyEvent.VK_V: 
                if(spacePressed)
                {
                    key[7] = false;
                    spacePressed=false;
                }
                break;
        }

    }

    public void keyTyped(KeyEvent e){

    }
    
    public static void main(String[] args) {
        Robot1 window = new Robot1();
        window.addKeyListener(window);
        MainFrame mf = new MainFrame(window, 900, 600); 
    }
    
    
}


