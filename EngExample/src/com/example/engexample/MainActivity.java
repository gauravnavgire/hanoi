package com.example.engexample;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;

import android.graphics.Typeface;
import android.view.KeyEvent;

public class MainActivity extends BaseGameActivity implements
		IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static int CAMERA_WIDTH = 800;
	private static int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================
	private Camera mCamera;
	private Font mFont;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mPausedTextureRegion;
	private ITextureRegion mBackgroundTextureRegion, mTowerTextureRegion,
			mRing1, mRing2, mRing3;
	private Sprite mTower1, mTower2, mTower3;
	private Ring mRing1Sprite, mRing2Sprite, mRing3Sprite;
	private Stack<Ring> mStack1, mStack2, mStack3;
	private Text mMoves;
	private static int mCount = 0;
	private Scene mGameScene;
	private CameraScene mPauseScene;

	private FPSLogger mUpdateHandler;

	// Splash screen fields
	private BitmapTextureAtlas splashTextureAtlas;
	private ITextureRegion splashTextureRegion;
	private Scene mSplashScene;
	private Sprite mSplash;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mCount = 0;
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		loadSplashSceneResources();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		initSplashScreen();
		pOnCreateSceneCallback.onCreateSceneFinished(this.mSplashScene);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		mEngine.registerUpdateHandler(new TimerHandler(4f,
				new ITimerCallback() {

					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						mEngine.unregisterUpdateHandler(pTimerHandler);
						loadFont();
						loadGameLevelSceneResources();
						loadGameLevelScene();
						initPauseScreen();
						mSplash.detachSelf();
						mEngine.setScene(mGameScene);
					}
				}));
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	private void initSplashScreen() {
		mSplashScene = new Scene();
		mSplash = new Sprite(0, 0, splashTextureRegion,
				getVertexBufferObjectManager()) {
			protected void preDraw(GLState pGLState, Camera pCamera) {
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			};
		};
		mSplash.setScale(1.5f);
		mSplash.setPosition(
				getCenter(CAMERA_WIDTH, splashTextureRegion.getWidth()),
				getCenter(CAMERA_HEIGHT, splashTextureRegion.getHeight()));
		mSplashScene.attachChild(mSplash);
	}

	public float getCenter(float total, float size) {
		return (total - size) / 2f;
	}

	private void loadSplashSceneResources() {
		// Splash screen
		splashTextureAtlas = new BitmapTextureAtlas(getTextureManager(), 350,
				256, TextureOptions.DEFAULT);
		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(splashTextureAtlas, this, "gfx/splash.png", 0,
						0);
		splashTextureAtlas.load();
	}

	private void loadGameLevelSceneResources() {
		mUpdateHandler = new FPSLogger();
		this.mEngine.registerUpdateHandler(mUpdateHandler);
		// 1-Setup bitmap textures
		try {
			// Game scene parts
			ITexture backgroundTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {

						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/background.png");
						}
					});

			ITexture towerTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/tower.png");
						}
					});
			ITexture ring1 = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/ring1.png");
						}
					});
			ITexture ring2 = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/ring2.png");
						}
					});
			ITexture ring3 = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/ring3.png");
						}
					});

			// 2-Load these resources to VRAM
			backgroundTexture.load();
			ring1.load();
			ring2.load();
			ring3.load();
			towerTexture.load();

			// 3 - setup texture regions
			this.mBackgroundTextureRegion = TextureRegionFactory
					.extractFromTexture(backgroundTexture);
			this.mTowerTextureRegion = TextureRegionFactory
					.extractFromTexture(towerTexture);
			this.mRing1 = TextureRegionFactory.extractFromTexture(ring1);
			this.mRing2 = TextureRegionFactory.extractFromTexture(ring2);
			this.mRing3 = TextureRegionFactory.extractFromTexture(ring3);

			// Pause
			this.mBitmapTextureAtlas = new BitmapTextureAtlas(
					this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
			this.mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory
					.createFromAsset(this.mBitmapTextureAtlas, this,
							"gfx/paused.png", 0, 0);

			this.mBitmapTextureAtlas.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initPauseScreen() {
		mPauseScene = new CameraScene(mCamera);
		/* Make the 'PAUSED'-label centered on the camera. */
		Sprite pause = new Sprite(getCenter(CAMERA_WIDTH,
				mPausedTextureRegion.getWidth()), getCenter(CAMERA_HEIGHT,
				mPausedTextureRegion.getHeight()), mPausedTextureRegion,
				getVertexBufferObjectManager());
		mPauseScene.attachChild(pause);
		mPauseScene.setBackgroundEnabled(false);
	}

	private void loadFont() {
		// Font
		this.mFont = FontFactory.create(getFontManager(), getTextureManager(),
				100, 100, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 30);
		this.mFont.load();
	}

	private void loadGameLevelScene() {
		// 1 - Create new scene
		mGameScene = new Scene();
		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();

		Sprite backgroundSprite = new Sprite(0, 0,
				this.mBackgroundTextureRegion, vertexBufferObjectManager);
		mGameScene.attachChild(backgroundSprite);

		// 2 - Add the towers
		mTower1 = new Sprite(192, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mTower2 = new Sprite(400, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mTower3 = new Sprite(604, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mGameScene.attachChild(mTower1);
		mGameScene.attachChild(mTower2);
		mGameScene.attachChild(mTower3);

		// 3 - Create the rings
		mRing1Sprite = new Ring(1, 139, 174, this.mRing1,
				vertexBufferObjectManager);
		mRing2Sprite = new Ring(2, 118, 212, this.mRing2,
				vertexBufferObjectManager);
		mRing3Sprite = new Ring(3, 97, 255, this.mRing3,
				vertexBufferObjectManager);

		mGameScene.attachChild(mRing1Sprite);
		mGameScene.attachChild(mRing2Sprite);
		mGameScene.attachChild(mRing3Sprite);

		this.mStack1 = new Stack<Ring>();
		this.mStack2 = new Stack<Ring>();
		this.mStack3 = new Stack<Ring>();

		// 4 - Add all rings to stack one
		this.mStack1.add(mRing3Sprite);
		this.mStack1.add(mRing2Sprite);
		this.mStack1.add(mRing1Sprite);

		// 5 - Initialize starting position for each ring
		mRing1Sprite.setStack(mStack1);
		mRing2Sprite.setStack(mStack1);
		mRing3Sprite.setStack(mStack1);
		mRing1Sprite.setTower(mTower1);
		mRing2Sprite.setTower(mTower1);
		mRing3Sprite.setTower(mTower1);

		// 6 - Add touch handlers
		mGameScene.registerTouchArea(mRing1Sprite);
		mGameScene.registerTouchArea(mRing2Sprite);
		mGameScene.registerTouchArea(mRing3Sprite);
		mGameScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mGameScene.setOnAreaTouchListener(this);

		// Texts
		final Text movesText = new Text(10, 440, mFont, "Total moves : ",
				vertexBufferObjectManager);
		mMoves = new Text(200, 440, mFont, "0", 4, vertexBufferObjectManager);
		mGameScene.attachChild(movesText);
		mGameScene.attachChild(mMoves);
	}

	private void checkForCollisionsWithTowers(Ring ring) {
		Stack<Ring> stack = null;
		Sprite tower = null;

		if (ring.collidesWith(mTower1)
				&& (mStack1.size() == 0 || ring.getWeight() < mStack1.peek()
						.getWeight())) {
			stack = mStack1;
			tower = mTower1;
		} else if (ring.collidesWith(mTower2)
				&& (mStack2.size() == 0 || ring.getWeight() < mStack2.peek()
						.getWeight())) {
			stack = mStack2;
			tower = mTower2;
		} else if (ring.collidesWith(mTower3)
				&& (mStack3.size() == 0 || ring.getWeight() < mStack3.peek()
						.getWeight())) {
			stack = mStack3;
			tower = mTower3;
		} else {
			stack = ring.getStack();
			tower = ring.getTower();
		}

		ring.getStack().remove(ring);

		if (stack != null && tower != null && stack.size() == 0) {
			ring.setPosition(
					tower.getX() + tower.getWidth() / 2 - ring.getWidth() / 2,
					tower.getY() + tower.getHeight() - ring.getHeight());

			mMoves.setText("" + ++mCount);
		} else if (stack != null && tower != null && stack.size() > 0) {
			ring.setPosition(
					tower.getX() + tower.getWidth() / 2 - ring.getWidth() / 2,
					((Ring) stack.peek()).getY() - ring.getHeight());
			mMoves.setText("" + ++mCount);

		}
		mMoves.invalidateText();

		stack.add(ring);
		// Check if the last tower is complete
		if (tower.equals(mTower3) && stack.size() == 3) {

			if (this.mEngine.isRunning()) {
				if (mMoves.getText().toString().equals("7")) {
					toastOnUIThread("** 7 Minimum numer of steps to solve this puzzel."
							+ " Congratulations.Level Complete **");
				} else {
					toastOnUIThread("** GAME OVER **");
				}
				// this.mScene.setChildScene(this.mScene, false, true, true);
				this.mEngine.stop();
			} else {
				// this.mScene.clearChildScene();
				this.mEngine.start();
			}
		}
		ring.setStack(stack);
		ring.setTower(tower);
	}

	@Override
	public void onBackPressed() {
		if (this.mEngine.isRunning()) {
			this.mEngine.stop();
			super.onBackPressed();
		} else {
			toastOnUIThread("** New Game Started **");
			resetGameLevel();
			this.mEngine.start();
		}

	}

	private void resetGameLevel() {
		// 4 - Add all rings to stack one
		this.mStack1.add(mRing3Sprite);
		this.mStack1.add(mRing2Sprite);
		this.mStack1.add(mRing1Sprite);

		// 5 - Initialize starting position for each ring
		mRing1Sprite.setStack(mStack1);
		mRing2Sprite.setStack(mStack1);
		mRing3Sprite.setStack(mStack1);
		mRing1Sprite.setTower(mTower1);
		mRing2Sprite.setTower(mTower1);
		mRing3Sprite.setTower(mTower1);

		mRing1Sprite.setPosition(139, 174);
		mRing2Sprite.setPosition(118, 212);
		mRing3Sprite.setPosition(97, 255);

		mCount = 0;
		mMoves.setText("" + mCount);
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		Ring ring = ((Ring) pTouchArea);

		if (ring.getStack().peek().getWeight() != ring.getWeight()) {
			return false;
		}

		ring.setPosition(pSceneTouchEvent.getX() - ring.getWidth() / 2,
				pSceneTouchEvent.getY() - ring.getHeight() / 2);

		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
			checkForCollisionsWithTowers(ring);
		}

		return true;
	}

//	@Override
//	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
//		if ((pKeyCode == KeyEvent.KEYCODE_MENU)
//				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
//			if (this.mEngine.isRunning()) {
//				this.mGameScene.setChildScene(this.mPauseScene, false, true,
//						true);
//				this.mEngine.stop();
//			} else {
//				this.mGameScene.clearChildScene();
//				this.mEngine.start();
//			}
//			return true;
//		} else {
//			return super.onKeyDown(pKeyCode, pEvent);
//		}
//	}

}
