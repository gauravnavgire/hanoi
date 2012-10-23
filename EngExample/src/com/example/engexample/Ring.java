package com.example.engexample;

import java.util.Stack;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Ring extends Sprite {
	private int mWeight;
	private Stack<Ring> mStack;
	private Sprite mTower;

	public Ring(int weight,float pX, float pY,ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY,pTextureRegion, pVertexBufferObjectManager);
		this.mWeight = weight;
	}

	public int getWeight() {
		return mWeight;
	}

	public void setWeight(int mWeight) {
		this.mWeight = mWeight;
	}

	public Stack<Ring> getStack() {
		return mStack;
	}

	public void setStack(Stack<Ring> mStack) {
		this.mStack = mStack;
	}

	public Sprite getTower() {
		return mTower;
	}

	public void setTower(Sprite mTower) {
		this.mTower = mTower;
	}
	
	
	
}
