
package robot1;



import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextField;
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




public final class Robot1 extends Applet implements KeyListener, ActionListener {
    
    
    private Button home = new Button("Home");
    private Button camera = new Button("Camera Reset");
    private Button startLearning = new Button("Start learning");
    private Button stopLearning = new Button("Stop learning");
    private Button play = new Button("Play");
    private TextField textPozX = new TextField("Angle1");
    private TextField textPozY = new TextField("Angle2");
    private TextField textPozZ = new TextField("PozZ");
    private Button transform = new Button("Transform");

    
    
    private TransformGroup transArm1, transJoint1, transArm2, transGripper, transBase, transBox;
    private TransformGroup transMain;
    private Transform3D shiftObserver = new Transform3D();
    private Transform3D turn1 = new Transform3D();
    private Transform3D turn2 = new Transform3D();
    private Transform3D shift1 = new Transform3D();
    private Transform3D shift2 = new Transform3D();
    private Transform3D shift3 = new Transform3D();
    private Transform3D pozBox;
    private SimpleUniverse u;
    private BranchGroup object;
    private Timer timer;
    private boolean spacePressed = false;
    private boolean transport = false;
    
    CollisionDetector detect;
    
    OrbitBehavior orbit;
    
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
    
    /* Zmienna informująca o tym, jaki był ostatni wykonany ruch. Jeżeli: 
      1 - obrót w lewo ramieniem nr 1, 
      2 - obrót w prawo ramieniem nr 1, 
      3 - obrót w lewo ramieniem nr 2, 
      4 - obrót w prawo ramieniem nr 2, 
      5 - podniesienie chwytaka, 
      6 - opuszczenie chwytaka. 
      7 - chwytanie/odkładanie obiektu
    */
    public int learnStep[];
    
    public int learnNumber = 0;
    public boolean isLearning = false;
    public boolean isPlaying = false;
    public int number = 0;
    public float startPozX, startPozY,startPozZ, startAngle1, startAngle2, startArmHeight;
    public boolean startTransport;

    
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

    public void liftGripper(float step)
    {
        if(armHeight<0.24f){
            armHeight += step;
            shift3.setTranslation(new Vector3f(0.25f,-0.1f+armHeight,0.0f));
            transGripper.setTransform(shift3);
            }
    }

    void lowerGripper(float step)
    {
        if(armHeight>-0.17f){
            armHeight -= step;
            shift3.setTranslation(new Vector3f(0.25f,-0.1f+armHeight,0.0f));
            transGripper.setTransform(shift3);
            }
    }


    
    Robot1(){
    
          
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        
        Canvas3D canvas = new Canvas3D(config);
        add("Center", canvas);
        canvas.addKeyListener(this);
        
        
        //Dodanie przyciskow oraz pol tekstowch
        Panel panel = new Panel(new GridLayout(1, 1, 1, 1));
        add("South", panel);
        panel.add(home);
        home.addActionListener(this);
        home.addKeyListener(this);
        
        panel.add(camera);
        camera.addActionListener(this);
        camera.addKeyListener(this);
        
        
        
        panel.add(startLearning);
        startLearning.addActionListener(this);
        startLearning.addKeyListener(this);
        
        
        panel.add(stopLearning);
        stopLearning.addActionListener(this);
        stopLearning.addKeyListener(this);
        
        
        panel.add(play);
        play.addActionListener(this);
        play.addKeyListener(this);
        
        
        
        panel.add(textPozX);
        panel.add(textPozY);
        panel.add(textPozZ);
        
        panel.add(transform);
        transform.addActionListener(this);
        transform.addKeyListener(this);
        
        
        u = new SimpleUniverse(canvas);
        
        //Obrot kamery za pomoca myszki
        orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        orbit.setRotationCenter(new Point3d(0.0, 0.0, 0.0));
        u.getViewingPlatform().setViewPlatformBehavior(orbit);
        
        Transform3D orbitHome= new Transform3D();
        orbitHome.set(new Vector3f(0.0f,0.2f,3.0f));    
        orbit.setHomeTransform(orbitHome);
        orbit.goHome();
        
        BranchGroup scene = createScene(u);        
        u.addBranchGraph(scene);
        
        key = new boolean[8];
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new Task(),0,25);
        
               
        
    }
    
    BranchGroup createScene(SimpleUniverse su)
    {
        BranchGroup Scena = new BranchGroup();
    
        //Ustawienie materiału dla elementów
        Material mat = new Material(new Color3f(0.1f, 0.02f, 0.1f),
                new Color3f(0.1f, 0.1f, 0.1f),
                new Color3f(0.7f, 0.1f, 0.1f),
                new Color3f(0.4f, 0.8f, 0.2f),
                20);
        
 
        Material mat2 = new Material(new Color3f(0.3f, 0.0f, 0.9f),
                new Color3f(0.0f, 0.0f, 0.6f),
                new Color3f(0.0f, 0.0f, 0.0f),
                new Color3f(0.0f, 0.0f, 0.0f),
                50);
       
        Material mat3 = new  Material(new Color3f(1.0f, 0.0f, 0.0f),
                new Color3f(1.0f, 0.1f, 0.1f),
                new Color3f(0.9f, 0.2f, 0.7f),
                new Color3f(1.0f, 1.0f, 1.0f),
                30);
       
       ColoringAttributes shade = new ColoringAttributes();
       shade.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
        
        //Ustawienie wyglądu dla wszystkich elementów zawartych w Scenie
        Appearance appearanceRobot = new Appearance();
        appearanceRobot.setColoringAttributes(new ColoringAttributes(0.5f,0.5f,0.9f,ColoringAttributes.NICEST));
        appearanceRobot.setMaterial(mat);
        
        Appearance appearanceFloor = new Appearance();
        appearanceFloor.setMaterial(mat2);
        
        Appearance appearanceBox = new Appearance();
        appearanceBox.setMaterial(mat3);
        appearanceBox.setColoringAttributes(shade);
        
        //PODSTAWA
        Box base = new Box(0.15f,0.01f,0.15f, appearanceRobot);
        transBase = new TransformGroup();
        transBase.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transBase.addChild(base);
        
        //WALEC
        Cylinder cylinder = new Cylinder(0.1f, 0.4f, appearanceRobot);
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
        Box arm1 = new Box(0.20f, 0.05f, 0.1f, appearanceRobot);
        Cylinder cylArm11 = new Cylinder(0.1f, 0.1f, appearanceRobot);
        Cylinder cylArm12 = new Cylinder(0.1f, 0.1f, appearanceRobot);
         
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
        Cylinder Joint1 = new Cylinder(0.05f, 0.01f, appearanceRobot);
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
        Box arm2 = new Box(0.2f, 0.05f, 0.1f, appearanceRobot);
        Cylinder CylArm21 = new Cylinder(0.1f, 0.1f, appearanceRobot);
        Cylinder CylArm22 = new Cylinder(0.1f, 0.1f, appearanceRobot);
         
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
        Cylinder gripper = new Cylinder(0.02f, 0.6f, appearanceRobot);
        Transform3D  pozGripper = new Transform3D();
        pozGripper.set(new Vector3f(0.25f,-0.1f,0.0f));
        transGripper = new TransformGroup(pozGripper);
        transGripper.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transGripper.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transGripper.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        transGripper.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        transGripper.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        transGripper.addChild(gripper);

       
        //PRZEDMIOT
        pozX = 0.86f;  pozY = 0.065f;
        Sphere ball = new Sphere(0.07f, Sphere.GENERATE_NORMALS,30, appearanceBox);
        pozBox = new Transform3D();
        pozBox.set(new Vector3f(pozX, pozY, pozZ));
        transBox = new TransformGroup(pozBox);
        transBox.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transBox.addChild(ball);
        
        //PODLOGA
        Box floor = new Box(10,0.001f,10,appearanceFloor);
        Transform3D pozBottom = new Transform3D();
        pozBottom.set(new Vector3f(0, -0.01f, 0));
        TransformGroup transFloor = new TransformGroup(pozBottom);
        transFloor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transFloor.addChild(floor);
         
        
        
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
        
        //USTAWIENIE DZIECI
        transArm1.addChild(transJoint1);
        transJoint1.addChild(transArm2);
        transArm2.addChild(transGripper);
        transCylinder.addChild(transArm1);
        
        transMain = new TransformGroup();
        transMain.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transMain.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transMain.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        transMain.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        transMain.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        Scena.addChild(transMain);
        transMain.addChild(transBase);
        Scena.addChild(transFloor);
    
        object = new BranchGroup();
        object.setCapability(object.ALLOW_DETACH);
        object.setCapability(object.ALLOW_CHILDREN_WRITE);
        object.setCapability(object.ALLOW_CHILDREN_READ);
        object.addChild(transBox);

        transMain.addChild(object);
        
        //Dodanie kolizji
        detect = new CollisionDetector(ball, new BoundingSphere(new Point3d(), 0.06d));
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
            case KeyEvent.VK_A:
                key[3] = true;
                break;
            case KeyEvent.VK_D:
                key[4] = true;
                break;

            case KeyEvent.VK_UP:
                key[5] = true;
                break;
            case KeyEvent.VK_DOWN:
                key[6] = true;
                break;    
            case KeyEvent.VK_S:
                if(!spacePressed)
                {
                    key[7] = true;
                    spacePressed = true;
                }
                break;
       }
    }

    @Override
    public void keyReleased(KeyEvent e){
        switch(e.getKeyCode()){

            case KeyEvent.VK_LEFT:
                key[1] = false;
                break;
            case KeyEvent.VK_RIGHT:
                key[2] = false;
                break;
            case KeyEvent.VK_A:
                key[3] = false;
                break;
            case KeyEvent.VK_D:
                key[4] = false;
                break;

            case KeyEvent.VK_UP:
                key[5] = false;
                break;
            case KeyEvent.VK_DOWN:
                key[6] = false;
                break;    
            case KeyEvent.VK_S: 
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
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== home)
        {
            //Powrot wszystkich obiektow i kamery do pozycji poczatkowej
            
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
            transArm2.setTransform(turn2);
            
            shift3.setTranslation(new Vector3f(0.25f,-0.1f,0.0f)); 
            transGripper.setTransform(shift3);
            
            
            orbit.goHome();
            
            
            
            if(transport)
            {
                learnStep = new int [10000];
                learnNumber = 0;
                isLearning = false;
                number = 0;
                transGripper.removeChild(object);
                pozX = 0.86f;
                pozY = 0.065f;
                pozZ = 0;
                pozBox.setTranslation(new Vector3f(pozX, pozY, pozZ));
                Transform3D temp = new Transform3D();
                temp.mul(pozBox);
                transBox.setTransform(temp);
                transMain.addChild(object);
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
        else if(e.getSource() == camera)
        {
            //Powrot kamery do pozycji poczatkowej
            orbit.goHome();
            
        }
        else if(e.getSource() == startLearning)
        {
            //Stworzenie tablicy zapisujacej ruchy
            isLearning = true;
            learnStep = new int [10000];
            learnNumber = 0;
            
            //Zapisanie pozycji starotwej
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
            //Odtworzenie nauczonej trasy
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
            transArm2.setTransform(turn2);
            
            shift3.setTranslation(new Vector3f(0.25f,armHeight,0.0f)); 
            transGripper.setTransform(shift3);
            
            if(transport==false && startTransport==false)
            {
                pozBox.setTranslation(new Vector3f(startPozX, startPozY, startPozZ));
                transBox.setTransform(pozBox);
            }
            if(transport==true && startTransport==false)
            {
                transGripper.removeChild(object);
                transMain.addChild(object);
                pozBox.setTranslation(new Vector3f(startPozX, startPozY, startPozZ));
                transBox.setTransform(pozBox);
                transport = false;
            }
            if(transport==false && startTransport==true)
            {
                transMain.removeChild(object);
                transGripper.addChild(object);
                pozBox.setTranslation(new Vector3f(0.0f, -0.36f, 0.0f));
                transBox.setTransform(pozBox);
                transport = true;
            }
            isPlaying = true;
        }
        else if(e.getSource() == transform)
        {
            //Ustawienie robota w zadanej pozycji
            
            float newAngle1=0f, newAngle2=0f, newPozY=0f;
            boolean correct = true;
            
            try
            {
                newAngle1 = Float.valueOf(textPozX.getText());
                newAngle2 = Float.valueOf(textPozY.getText());
                newPozY = Float.valueOf(textPozZ.getText());
            }
            catch(IllegalArgumentException exception){ 
                correct = false;
            }
            if(correct&&newPozY>=0 && newPozY<=1 && newAngle2<135 && newAngle2>-135)
            {
                angle1 = newAngle1*(2*(float)Math.PI)/360;
                angle2 = newAngle2*(2*(float)Math.PI)/360;
                armHeight = newPozY*(0.24f+0.17f)-0.17f;
                turn1.rotY(angle1);
                shift1.setTranslation(new Vector3f(0.2f,0.25f,0.0f)); 
                turn1.mul(shift1); 
                transArm1.setTransform(turn1);

                turn2.rotY(angle2);  
                shift2.setTranslation(new Vector3f(0.21f,0.1f,0.0f)); 
                turn2.mul(shift2); 
                transArm2.setTransform(turn2);

                shift3.set(new Vector3f(0.25f,armHeight-0.10f,0.0f)); 
                transGripper.setTransform(shift3);
            }
            
            
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
                
            }
            if(isPlaying)
                play();
            
            if(lastMove==0)
                detect.inCollision = false;
            
            //Obsługa streowania z klawiatury
            
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
                    liftGripper(0.005f);
                    }
            }
            if(key[6])
            {
                if(((detect.inCollision && lastMove != 6) || detect.inCollision == false) && (armHeight>-0.02 || !transport))
                    {

                    if(!detect.inCollision) lastMove = 6;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 6;
                        learnNumber++;
                    }
                    lowerGripper(0.005f);
                    }
            }
            if(key[7])
            {
                if(detect.inCollision && !transport&&armHeight>-0.1)
                {   
                    transport = !transport;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 7;
                        learnNumber++;
                    }
                    if(transport == true)
                    {
                        transMain.removeChild(object);
                        Transform3D temp = new Transform3D();
                        pozBox.set(new Vector3f(0.0f, -0.36f, 0.0f));
                        temp.mul(pozBox);
                        transBox.setTransform(temp);
                        transGripper.addChild(object);
                                
                                
                    }
                }
                else if(transport && armHeight < 0.03)
                {
                    transport = !transport;
                    if(isLearning)
                    {
                        learnStep[learnNumber] = 7;
                        learnNumber++;
                    }
                    transGripper.removeChild(object);
                    
                    pozX = (float) (0.4f*Math.cos(angle1)+0.47f*Math.cos(angle1 + angle2));
                    pozZ = (float) -(0.4f*Math.sin(angle1)+0.47f*Math.sin(angle1 + angle2));
                    
                    
                    pozBox.set(new Vector3f(pozX, 0.07f, pozZ));
                    Transform3D temp = new Transform3D();
                    temp.mul(pozBox);
                    transBox.setTransform(temp);
                    transMain.addChild(object);
                    detect.inCollision = false;
                    lastMove = 0;
                }
                key[7] = false;
            }

        }
        
        public void play()
        {
            for(int i = 1; i<8; i++)
                key[i] = false;
            key[learnStep[number]]=true;
            number++;
        }
  }

    
    public static void main(String[] args) {
        Robot1 window = new Robot1();
        window.addKeyListener(window);
        MainFrame mf = new MainFrame(window, 800, 600);
    }
    
    
}


