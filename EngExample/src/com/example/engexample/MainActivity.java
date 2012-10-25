package com.example.engexample;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.color.Color;

import android.graphics.Typeface;

public class MainActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static int CAMERA_WIDTH = 800;
	private static int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Font mFont;
	private ITextureRegion mBackgroundTextureRegion, mTowerTextureRegion,
			mRing1, mRing2, mRing3;
	private Sprite mTower1, mTower2, mTower3;
	private Stack<Ring> mStack1, mStack2, mStack3;
	private Text mMoves;
	private static int mCount = 0;
	private Scene mScene;
	private FPSLogger mUpdateHandler;

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
		Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mCount = 0;
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		mUpdateHandler = new FPSLogger();
		this.mEngine.registerUpdateHandler(mUpdateHandler);
		// 1-Setup bitmap textures
		try {
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

			// Font
			this.mFont = FontFactory.create(getFontManager(),
					getTextureManager(), 100, 100,
					Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 30);
			this.mFont.load();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Scene onCreateScene() {
		// 1 - Create new scene
		mScene = new Scene();
		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();

		Sprite backgroundSprite = new Sprite(0, 0,
				this.mBackgroundTextureRegion, vertexBufferObjectManager);
		mScene.attachChild(backgroundSprite);

		// 2 - Add the towers
		mTower1 = new Sprite(192, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mTower2 = new Sprite(400, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mTower3 = new Sprite(604, 63, this.mTowerTextureRegion,
				vertexBufferObjectManager);
		mScene.attachChild(mTower1);
		mScene.attachChild(mTower2);
		mScene.attachChild(mTower3);

		// 3 - Create the rings
		Ring ring1 = new Ring(1, 139, 174, this.mRing1,
				vertexBufferObjectManager) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (((Ring) this.getStack().peek()).getWeight() != this
						.getWeight()) {
					return false;
				}

				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2,
						pSceneTouchEvent.getY() - this.getHeight() / 2);

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					checkForCollisionsWithTowers(this);
				}

				return true;
			}
		};
		Ring ring2 = new Ring(2, 118, 212, this.mRing2,
				vertexBufferObjectManager) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (((Ring) this.getStack().peek()).getWeight() != this
						.getWeight()) {
					return false;
				}

				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2,
						pSceneTouchEvent.getY() - this.getHeight() / 2);

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					checkForCollisionsWithTowers(this);
				}

				return true;
			}
		};
		Ring ring3 = new Ring(3, 97, 255, this.mRing3,
				vertexBufferObjectManager) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (((Ring) this.getStack().peek()).getWeight() != this
						.getWeight()) {
					return false;
				}

				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2,
						pSceneTouchEvent.getY() - this.getHeight() / 2);

				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					checkForCollisionsWithTowers(this);
				}

				return true;
			}
		};

		mScene.attachChild(ring1);
		mScene.attachChild(ring2);
		mScene.attachChild(ring3);

		this.mStack1 = new Stack<Ring>();
		this.mStack2 = new Stack<Ring>();
		this.mStack3 = new Stack<Ring>();

		// 4 - Add all rings to stack one
		this.mStack1.add(ring3);
		this.mStack1.add(ring2);
		this.mStack1.add(ring1);

		// 5 - Initialize starting position for each ring
		ring1.setStack(mStack1);
		ring2.setStack(mStack1);
		ring3.setStack(mStack1);
		ring1.setTower(mTower1);
		ring2.setTower(mTower1);
		ring3.setTower(mTower1);

		// 6 - Add touch handlers
		mScene.registerTouchArea(ring1);
		mScene.registerTouchArea(ring2);
		mScene.registerTouchArea(ring3);
		mScene.setTouchAreaBindingOnActionDownEnabled(true);

		// Texts
		final Text movesText = new Text(10, 440, mFont, "Total moves : ",
				vertexBufferObjectManager);
		mMoves = new Text(200, 440, mFont, "0", 4, vertexBufferObjectManager);
		mScene.attachChild(movesText);
		mScene.attachChild(mMoves);
		return mScene;
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
			this.mEngine.start();
		}

	}
}
