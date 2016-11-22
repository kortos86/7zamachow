package com.artpi.games.a7zamachow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;


public class RGView extends SurfaceView implements  Runnable{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    boolean firstrun = true;
    volatile boolean playing;
    Thread gameThread = null;
    //Game objects
    private PlayerCar player;
    public EnemyCar enemy1;
    public EnemyCar enemy2;
    public Coin coin;

    // Make some random space dust
    public ArrayList<SandPoint> sandPoints = new ArrayList<>();
    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;
    float x1,x2;
    int maxX;
    int maxY;
    boolean enemyBoom  = false;
    boolean removeShield = false;
    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;
    private boolean gameEnded;
    boolean collectedCoin = false;

    Handler handler = new Handler(Looper.getMainLooper());
    final Runnable r2 = new Runnable() {
        public void run() {
            enemyBoom = false;
        }
    };
    final Runnable r3 = new Runnable() {
        public void run() {
            enemyBoom = false;
            player.setSpeed(25);
        }
    };

    final Runnable r4 = new Runnable() {
        public void run() {
            collectedCoin = false;
            player.setSpeed(25);
        }
    };



    public RGView(Context context, int x, int y) {
        super(context);
        this.context = context;
        // Get a reference to a file called HiScores.
        // If id doesn't exist one is created
        prefs = context.getSharedPreferences("HiScores",context.MODE_PRIVATE);
        // Initialize the editor ready
        editor = prefs.edit();
        // Load fastest time from a entry in the file
        // labeled "fastestTime"
        // if not available highscore = 1000000
        fastestTime = prefs.getLong("fastestTime", 1000000);
        maxX=x;
        maxY=y;
        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();
        startGame();
    }

    private void startGame(){
        //Initialize game objects
        player = new PlayerCar(context, maxX, maxY);
        enemy1 = new EnemyCar(context, maxX, maxY, 1);
        enemy2 = new EnemyCar(context, maxX, maxY, 2);
        coin = new Coin(context, maxX, maxY);

        int howMuchSand = 100;

        for (int j = 1; j <= howMuchSand; j++) {
                SandPoint spec = new SandPoint( maxX, maxY);
                sandPoints.add(spec);
        }
        // Reset time and distance
        distanceRemaining = 30000;// 10 km
        timeTaken = 0;
        // Get start time
        timeStarted = System.currentTimeMillis();
        gameEnded= false;
    }


    @Override
    public void run() {
        while (playing) {
            if (!gameEnded) {
                update();

            draw();
            control();
            }
        }
    }


    private void update(){
        // Collision detection on new positions
        // Before move because we are testing last frames
        // position which has just been drawn
        // If you are using images in excess of 100 pixels
        // wide then increase the -100 value accordingly
        if(Rect.intersects
                (player.getHitbox(), enemy1.getHitbox())){
            enemy1.setY(+1000);
            enemyBoom  = true;
            removeShield  = true;
            handler.removeCallbacks(r2);
            handler.removeCallbacks(r3);
        }
        if(Rect.intersects
                (player.getHitbox(), enemy2.getHitbox())){
            enemy2.setY(+1000);
            enemyBoom  = true;
            removeShield  = true;
            handler.removeCallbacks(r2);
            handler.removeCallbacks(r3);
        }

        if(Rect.intersects
                (player.getHitbox(), coin.getHitbox())){
            coin.setY(+1000);
            collectedCoin  = true;
            handler.removeCallbacks(r4);
        }

        if (collectedCoin) {
            player.setSpeed(45);
            handler.postDelayed(r4, 1000);
            collectedCoin = false;
        }


        if (enemyBoom) {
            player.setSpeed(5);
            handler.postDelayed(r2, 500);
            handler.postDelayed(r3, 2000);
        }

        if (removeShield){
            player.reduceShieldStrength();
            if (player.getShieldStrength() < 1) {
                gameEnded = true;
            }
            removeShield=false;
        }
        // Update the player
        player.update();
        enemy1.update(getContext(), player.getSpeed());
        enemy2.update(getContext(), player.getSpeed());
        coin.update(getContext(),player.getSpeed());
        //enemy3.update(getContext());
        for (SandPoint sd : sandPoints) {
            sd.update(player.getSpeed());
        }
        if(!gameEnded) {
            //subtract distance to home planet based on current speed
            distanceRemaining -= player.getSpeed();
            //How long has the player been flying
            timeTaken = System.currentTimeMillis() - timeStarted;
        }
        //Completed the game!
        if(distanceRemaining < 0){
        //check for new fastest time
            if(timeTaken < fastestTime) {
                editor.putLong("fastestTime", timeTaken);
                editor.commit();
                fastestTime = timeTaken;
            }
            // avoid ugly negative numbers
            // in the HUD
            distanceRemaining = 0;
        // Now end the game
            gameEnded = true;
        }
    }


    private void draw(){
        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();
            // Rub out the last frame
            canvas.drawColor(Color.rgb(197,178,128));
              // White specs of dust
            paint.setColor(Color.rgb(59,51,28));
            for (SandPoint sd : sandPoints) {
                Log.println(Log.INFO,"xxx","xxxxxxxxxxxxxxxxx draw sand at "+ sd.getX() + " " + sd.getY() + "paint colro "+ paint.getColor());
               // canvas.drawPoint(sd.getX(), sd.getY(), paint);
                canvas.drawRect(sd.getX(),sd.getY(),sd.getX()+5,sd.getY()+5,paint);
            }

            // Draw the player
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawBitmap(enemy1.getBitmap(), enemy1.getX(), enemy1.getY(), paint);
            canvas.drawBitmap(enemy2.getBitmap(), enemy2.getX(), enemy2.getY(), paint);
            canvas.drawBitmap(coin.getBitmap(), coin.getX(), coin.getY(), paint);

            if(enemyBoom){
                Bitmap boom = BitmapFactory.decodeResource(this.getResources(), R.drawable.boom);
                canvas.drawBitmap( boom, player.getX(), player.getY()-50, paint);
            }
            if(!gameEnded){
            // Draw the hud
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.argb(255, 0, 0, 0));
            paint.setTextSize(40);
            canvas.drawText("Fastest:"+ fastestTime + "s", 10, 40, paint);
            canvas.drawText("Time:" + timeTaken + "s", 10, 80,paint);
            canvas.drawText("Distance:" + distanceRemaining  +" M", 10, 120, paint);
            canvas.drawText("Shield:" , 10, 170, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                for (int i = 0; i < player.getShieldStrength(); i++) {
                    Bitmap shield = BitmapFactory.decodeResource
                            (this.getResources(), R.drawable.shield);
                    canvas.drawBitmap(shield, 140 + i*40,130,paint);
                   // canvas.drawRect( 180 + i*40,140,180+ i*40+20,160,paint);
                }
                paint.setColor(Color.argb(255, 0, 0, 0));
            canvas.drawText("Speed:" + player.getSpeed() * 60 + " MPS", 10, 220, paint);
            }else{
                // Show pause screen
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.argb(255, 0, 0, 0));
                canvas.drawText("Game Over", maxX/2, 100, paint);
                paint.setTextSize(25);
                canvas.drawText("Fastest:"+
                        fastestTime + "s", maxX/2, 160, paint);
                canvas.drawText("Time:" + timeTaken +
                        "s", maxX / 2, 200, paint);
                canvas.drawText("Distance remaining:" +
                        distanceRemaining/1000 + " KM",maxX/2, 240, paint);
                paint.setTextSize(80);
                canvas.drawText("Tap to replay!", maxX/2, 350, paint);
            }
            // Unlock and draw the scene
            ourHolder.unlockCanvasAndPost(canvas);
        }



    }
    private void control(){

        try {
            gameThread.sleep(2);
        } catch (InterruptedException e) {
        }
    }

    // Clean up our thread if the game is interrupted or the player quits
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }
    // Make a new thread and start it
// Execution moves to our R
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


    // SurfaceView allows us to handle the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // There are many different events in MotionEvent
        // We care about just 2 - for now.
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // Has the player lifted their finger up?
            case MotionEvent.ACTION_UP:
                player.stopTurning();
                Log.println(Log.INFO,"1","xxxxxxx StopTurning");
                break;
            // Has the player touched the screen?
            case MotionEvent.ACTION_DOWN:
                // Do something here
                x1 = motionEvent.getX();
                x2 = motionEvent.getY();
                if (x1<(getWidth()/2)){
                    player.turnLeft();
                    Log.println(Log.INFO,"1","xxxxxxx left");
                }else{
                    player.turnRight();
                    Log.println(Log.INFO,"1","xxxxxxx right");
                }
                // If we are currently on the pause screen, start a new game
                if(gameEnded){
                    startGame();
                }
                break;
        }
        return true;
    }
}


