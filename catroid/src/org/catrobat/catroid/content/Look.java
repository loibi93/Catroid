/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2016 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.content;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.utils.ImageEditing;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class Look extends Image {
	private static final float DEGREE_UI_OFFSET = 90.0f;
	private static ArrayList<Action> actionsToRestart = new ArrayList<Action>();
	public boolean visible = true;
	protected boolean imageChanged = false;
	protected boolean brightnessChanged = false;
	protected LookData lookData;
	protected Sprite sprite;
	protected Pixmap collisionMask;
	protected float alpha = 1f;
	protected float brightness = 1f;
	protected Pixmap pixmap;
	private ParallelAction whenParallelAction;
	private boolean allActionsAreFinished = false;
	private BrightnessContrastShader shader;
	private Point touchingPoint;

	public Look(final Sprite sprite) {
		this.sprite = sprite;
		setBounds(0f, 0f, 0f, 0f);
		setOrigin(0f, 0f);
		setScale(1f, 1f);
		setRotation(0f);
		setTouchable(Touchable.enabled);
		addListeners();
	}

	protected void addListeners() {
		this.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				//With this we can calculate, if we tapped on our Sprite
				touchingPoint = new Point((int) x, (int) y);
				if (doTouchDown(x, y, pointer)) {
					return true;
				}
				setTouchable(Touchable.disabled);
				Actor target = getParent().hit(event.getStageX(), event.getStageY(), true);
				if (target != null) {
					target.fire(event);
				}
				setTouchable(Touchable.enabled);
				return false;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				//Touch was dragged, update point
				touchingPoint = new Point((int) x, (int) y);
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				//We stoped our touch, delete point
				touchingPoint = null;
			}
		});

		this.addListener(new BroadcastListener() {
			@Override
			public void handleBroadcastEvent(BroadcastEvent event, String broadcastMessage) {
				doHandleBroadcastEvent(broadcastMessage);
			}

			@Override
			public void handleBroadcastFromWaiterEvent(BroadcastEvent event, String broadcastMessage) {
				doHandleBroadcastFromWaiterEvent(event, broadcastMessage);
			}
		});
	}

	public static boolean actionsToRestartContains(Action action) {
		return Look.actionsToRestart.contains(action);
	}

	public static void actionsToRestartAdd(Action action) {
		Look.actionsToRestart.add(action);
	}

	public Look copyLookForSprite(final Sprite cloneSprite) {
		Look cloneLook = cloneSprite.look;

		cloneLook.alpha = this.alpha;
		cloneLook.brightness = this.brightness;
		cloneLook.visible = this.visible;
		cloneLook.whenParallelAction = null;
		cloneLook.allActionsAreFinished = this.allActionsAreFinished;

		return cloneLook;
	}

	public boolean doTouchDown(float x, float y, int pointer) {
		if (sprite.isPaused) {
			return true;
		}
		if (!visible) {
			return false;
		}

		// We use Y-down, libgdx Y-up. This is the fix for accurate y-axis detection
		y = (getHeight() - 1) - y;

		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()
				&& ((pixmap != null && ((pixmap.getPixel((int) x, (int) y) & 0x000000FF) > 10)))) {
			if (whenParallelAction == null) {
				sprite.createWhenScriptActionSequence("Tapped");
			} else {
				whenParallelAction.restart();
			}
			return true;
		}

		return false;
	}

	public void createBrightnessContrastShader() {
		shader = new BrightnessContrastShader();
		shader.setBrightness(brightness);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		checkImageChanged();
		batch.setShader(shader);
		if (alpha == 0.0f) {
			setVisible(false);
		} else {
			setVisible(true);
		}

		//currently only used for dronevideo!
		if (lookData != null) {
			lookData.draw(batch, alpha);
		}

		if (this.visible && this.getDrawable() != null) {
			super.draw(batch, this.alpha);
		}
	}

	@Override
	public void act(float delta) {
		Array<Action> actions = getActions();
		allActionsAreFinished = false;
		int finishedCount = 0;

		for (Iterator<Action> iterator = Look.actionsToRestart.iterator(); iterator.hasNext(); ) {
			Action actionToRestart = iterator.next();
			actionToRestart.restart();
			iterator.remove();
		}

		int n = actions.size;
		for (int i = 0; i < n; i++) {
			Action action = actions.get(i);
			if (action.act(delta)) {
				finishedCount++;
			}
		}
		if (finishedCount == actions.size) {
			allActionsAreFinished = true;
		}
	}

	@Override
	public void addAction(Action action) {
		super.addAction(action);
		allActionsAreFinished = false;
	}

	protected void checkImageChanged() {
		if (imageChanged) {
			if (lookData == null) {
				setBounds(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0f, 0f);
				setDrawable(null);
				imageChanged = false;
				return;
			}

			pixmap = lookData.getPixmap();
			float newX = getX() - (pixmap.getWidth() - getWidth()) / 2f;
			float newY = getY() - (pixmap.getHeight() - getHeight()) / 2f;

			setPosition(newX, newY);
			setSize(pixmap.getWidth(), pixmap.getHeight());
			setOrigin(getWidth() / 2f, getHeight() / 2f);

			if (brightnessChanged) {
				shader.setBrightness(brightness);
				brightnessChanged = false;
			}

			TextureRegion region = lookData.getTextureRegion();
			TextureRegionDrawable drawable = new TextureRegionDrawable(region);
			setDrawable(drawable);

			imageChanged = false;
		}
	}

	public void refreshTextures() {
		this.imageChanged = true;
	}

	public LookData getLookData() {
		return lookData;
	}

	public void setLookData(LookData lookData) {
		//Our look changed, so we also need to change our collision mask !
		collisionMask = null;
		this.lookData = lookData;
		imageChanged = true;
	}

	//calculate if we are touching on the sprite
	public boolean isTouched() {
		if (touchingPoint != null) {
			return isSpriteTouched(touchingPoint.x, touchingPoint.y);
		}
		return false;
	}

	public boolean getAllActionsAreFinished() {
		return allActionsAreFinished;
	}

	public String getImagePath() {
		String path;
		if (this.lookData == null) {
			path = "";
		} else {
			path = this.lookData.getAbsolutePath();
		}
		return path;
	}

	public void setWhenParallelAction(ParallelAction action) {
		whenParallelAction = action;
	}

	public float getXInUserInterfaceDimensionUnit() {
		return getX() + getWidth() / 2f;
	}

	public void setXInUserInterfaceDimensionUnit(float x) {
		setX(x - getWidth() / 2f);
	}

	public float getYInUserInterfaceDimensionUnit() {
		return getY() + getHeight() / 2f;
	}

	public void setYInUserInterfaceDimensionUnit(float y) {
		setY(y - getHeight() / 2f);
	}

	public void setPositionInUserInterfaceDimensionUnit(float x, float y) {
		setXInUserInterfaceDimensionUnit(x);
		setYInUserInterfaceDimensionUnit(y);
	}

	public void changeXInUserInterfaceDimensionUnit(float changeX) {
		setX(getX() + changeX);
	}

	public void changeYInUserInterfaceDimensionUnit(float changeY) {
		setY(getY() + changeY);
	}

	public float getWidthInUserInterfaceDimensionUnit() {
		return getWidth() * getSizeInUserInterfaceDimensionUnit() / 100f;
	}

	public float getHeightInUserInterfaceDimensionUnit() {
		return getHeight() * getSizeInUserInterfaceDimensionUnit() / 100f;
	}

	public float getDirectionInUserInterfaceDimensionUnit() {
		float direction = (getRotation() + DEGREE_UI_OFFSET) % 360;
		if (direction < 0) {
			direction += 360f;
		}
		direction = 180f - direction;

		return direction;
	}

	//Here we can see, why we set to null before
	public Pixmap getCollisionMask() {
		if (collisionMask == null) {
			collisionMask = createCollisionMask();
		}

		return collisionMask;
	}

	//With the Polygon class, we are able to rotate our sprite accordingly
	//and get the BoundingBox
	public Rectangle getHitbox() {
		float x = getXInUserInterfaceDimensionUnit() - getWidthInUserInterfaceDimensionUnit()/2;
		float y = getYInUserInterfaceDimensionUnit() - getHeightInUserInterfaceDimensionUnit()/2;

		Polygon p = new Polygon(new float[] {
			x, y,
			x, y + getHeightInUserInterfaceDimensionUnit(),
			x + getWidthInUserInterfaceDimensionUnit(), y + getHeightInUserInterfaceDimensionUnit(),
			x + getWidthInUserInterfaceDimensionUnit(), y
		});

		p.rotate(getRotation());

		return p.getBoundingRectangle();
	}

	public void setDirectionInUserInterfaceDimensionUnit(float degrees) {
		//Same as above, our sprite changed, so the collision mask needs an update
		collisionMask = null;
		setRotation((-degrees + DEGREE_UI_OFFSET) % 360);
	}

	public void changeDirectionInUserInterfaceDimensionUnit(float changeDegrees) {
		//same here
		collisionMask = null;
		setRotation((getRotation() - changeDegrees) % 360);
	}

	public float getSizeInUserInterfaceDimensionUnit() {
		return getScaleX() * 100f;
	}

	public void setSizeInUserInterfaceDimensionUnit(float percent) {
		if (percent < 0) {
			percent = 0;
		}

		setScale(percent / 100f, percent / 100f);
	}

	public void changeSizeInUserInterfaceDimensionUnit(float changePercent) {
		setSizeInUserInterfaceDimensionUnit(getSizeInUserInterfaceDimensionUnit() + changePercent);
	}

	public float getTransparencyInUserInterfaceDimensionUnit() {
		return (1f - alpha) * 100f;
	}

	public void setTransparencyInUserInterfaceDimensionUnit(float percent) {
		if (percent < 0f) {
			percent = 0f;
		} else if (percent >= 100f) {
			percent = 100f;
			setVisible(false);
		}

		if (percent < 100.0f) {
			setVisible(true);
		}

		alpha = (100f - percent) / 100f;
	}

	public void changeTransparencyInUserInterfaceDimensionUnit(float changePercent) {
		setTransparencyInUserInterfaceDimensionUnit(getTransparencyInUserInterfaceDimensionUnit() + changePercent);
	}

	public float getBrightnessInUserInterfaceDimensionUnit() {
		return brightness * 100f;
	}

	public void setBrightnessInUserInterfaceDimensionUnit(float percent) {
		if (percent < 0f) {
			percent = 0f;
		} else if (percent > 200f) {
			percent = 200f;
		}

		brightness = percent / 100f;
		brightnessChanged = true;
		imageChanged = true;
	}

	public void changeBrightnessInUserInterfaceDimensionUnit(float changePercent) {
		setBrightnessInUserInterfaceDimensionUnit(getBrightnessInUserInterfaceDimensionUnit() + changePercent);
	}

	protected void doHandleBroadcastEvent(String broadcastMessage) {
		BroadcastHandler.doHandleBroadcastEvent(this, broadcastMessage);
	}

	protected void doHandleBroadcastFromWaiterEvent(BroadcastEvent event, String broadcastMessage) {
		BroadcastHandler.doHandleBroadcastFromWaiterEvent(this, event, broadcastMessage);
	}

	private class BrightnessContrastShader extends ShaderProgram {

		private static final String VERTEX_SHADER = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
				+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
				+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" + "uniform mat4 u_projTrans;\n" + "varying vec4 v_color;\n"
				+ "varying vec2 v_texCoords;\n" + "\n" + "void main()\n" + "{\n" + " v_color = "
				+ ShaderProgram.COLOR_ATTRIBUTE + ";\n" + " v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
				+ " gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}\n";
		private static final String FRAGMENT_SHADER = "#ifdef GL_ES\n" + "#define LOWP lowp\n"
				+ "precision mediump float;\n" + "#else\n" + "#define LOWP \n" + "#endif\n"
				+ "varying LOWP vec4 v_color;\n" + "varying vec2 v_texCoords;\n" + "uniform sampler2D u_texture;\n"
				+ "uniform float brightness;\n" + "uniform float contrast;\n" + "void main()\n" + "{\n"
				+ " vec4 color = v_color * texture2D(u_texture, v_texCoords);\n" + " color.rgb /= color.a;\n"
				+ " color.rgb = ((color.rgb - 0.5) * max(contrast, 0.0)) + 0.5;\n" //apply contrast
				+ " color.rgb += brightness;\n" //apply brightness
				+ " color.rgb *= color.a;\n" + " gl_FragColor = color;\n" + "}";

		private static final String BRIGHTNESS_STRING_IN_SHADER = "brightness";
		private static final String CONTRAST_STRING_IN_SHADER = "contrast";

		public BrightnessContrastShader() {
			super(VERTEX_SHADER, FRAGMENT_SHADER);
			ShaderProgram.pedantic = false;
			if (isCompiled()) {
				begin();
				setUniformf(BRIGHTNESS_STRING_IN_SHADER, 0.0f);
				setUniformf(CONTRAST_STRING_IN_SHADER, 1.0f);
				end();
			}
		}

		public void setBrightness(float brightness) {
			begin();
			setUniformf(BRIGHTNESS_STRING_IN_SHADER, brightness - 1f);
			end();
		}
	}


	private boolean isSpriteTouched(float x, float y) {
		//This is a fix for libGDX using a mirrored y Axis
		y = (getHeight() - 1) - y;
		//We take a square around our touching point, and if there is color,
		//we know that there is the Sprite, so we increase our pixelVal. When
		//this exceeds a threshhold, the sprite was touched
		x = x - Constants.COLLISION_WITH_FINGER_AREA_SIZE/2;
		y = y - Constants.COLLISION_WITH_FINGER_AREA_SIZE/2;
		int pixelVal = 0;
		for (int x_pixmap = (int) x; x_pixmap < x + Constants.COLLISION_WITH_FINGER_AREA_SIZE; x_pixmap++) {
			for (int y_pixmap = (int) y; y_pixmap < y + Constants.COLLISION_WITH_FINGER_AREA_SIZE; y_pixmap++) {
				if (pixmap != null && pixmap.getPixel(x_pixmap, y_pixmap) > 0) {
					pixelVal++;
				}
			}
		}

		if (pixelVal > Constants.COLLISION_WITH_FINGER_PIXEL_TRESHHOLD) {
			return true;
		}

		return false;
	}

	//Still needs some rework
	private Pixmap createCollisionMask() {
		//Load a square and much smaller version of our look
		Bitmap bitmap = ImageEditing.getScaledBitmapFromPath(lookData.getAbsolutePath(), Constants.COLLISION_MASK_SIZE, Constants.COLLISION_MASK_SIZE, ImageEditing.ResizeType.STRETCH_TO_RECTANGLE, true);
		Matrix matrix = new Matrix();
		matrix.preScale(1.0f, -1.0f);
		//Mirror it with a matrix (for some reason it is upside down when loading from file)
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bytes = stream.toByteArray();
		//Create a Pixmap from it (we can rotate these !)
		Pixmap pix = new Pixmap(bytes, 0, bytes.length);
		return pix;

		/*
		It seems that the above code still is not perfect, just test it out, you will see
		that the collision is not detected correctly. Maybe you want to fix this first, and
		then start working on the rotation
		 */

		/*int inset = (int) ((getHitbox().width - getWidthInUserInterfaceDimensionUnit()) * (Constants.COLLISION_MASK_SIZE / getHitbox().getWidth())) / 2;

		Pixmap.setFilter(Pixmap.Filter.BiLinear);
		pix.drawPixmap(pix, 0, 0, pix.getWidth(), pix.getHeight(),
				inset, inset, (pix.getWidth() - inset * 2), (pix.getHeight() - inset * 2));
		return rotateBitmap(pix, getRotation());*/
	}

	private Pixmap rotateBitmap (Pixmap src, float angle) {
		Pixmap rotated = new Pixmap(src.getWidth(), src.getHeight(), src.getFormat());
		final double radians = Math.toRadians(angle), cos = Math.cos(radians), sin = Math.sin(radians);

		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				final int
						centerx = src.getWidth()/2, centery = src.getHeight() / 2,
						m = x - centerx,
						n = y - centery,
						j = ((int) (m * cos + n * sin)) + centerx,
						k = ((int) (n * cos - m * sin)) + centery;
				if (j >= 0 && j < src.getWidth() && k >= 0 && k < src.getHeight()){
					rotated.drawPixel(x, y, src.getPixel(k, j));
				}
			}
		}

		return rotated;
	}
}
