/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2015 The Catrobat Team
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

package org.catrobat.catroid.sensing;

import android.graphics.Bitmap;
import android.util.Log;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Look;

/**
 * Created by Lukas Loibnegger on 25.09.2015.
 */
public class CollisionDetection {
	public static double checkCollisionBetweenLooks(Look firstLook, Look secondLook) {
		//Basic check if hitboxes even collide
		if (!firstLook.getHitbox().overlaps(secondLook.getHitbox()) || !firstLook.visible || ! secondLook.visible) {
			return 0d;
		}
		//Get the rects in the bigger rects which interest us
		Rectangle[] rectsToCheck = getRectsOfInterest(firstLook.getHitbox(), secondLook.getHitbox());

		Rectangle checkFirst = rectsToCheck[0];
		Rectangle checkSecond = rectsToCheck[1];

		//Scale them according to our bitmask size
		checkFirst = scaleRectangle(checkFirst, firstLook);
		checkSecond = scaleRectangle(checkSecond, secondLook);

		Pixmap firstMask = firstLook.getCollisionMask();
		Pixmap secondMask = secondLook.getCollisionMask();

		//printPixmap(firstMask);

		if (firstMask == null || secondMask == null) {
			return 0d;
		}

		Pixmap firstRoi = new Pixmap((int) checkFirst.width, (int) checkFirst.height, firstMask.getFormat());
		Pixmap secondRoi = new Pixmap((int) checkSecond.width, (int) checkSecond.height, secondMask.getFormat());
		Pixmap firstRoiScaled;
		Pixmap secondRoiScaled;

		try {
			//Get the interresting area from our bitmask
			firstRoi.drawPixmap(firstMask, 0, 0, (int) checkFirst.x, (int) checkFirst.y, (int) checkFirst.width, (int) checkFirst.height);
			secondRoi.drawPixmap(secondMask, 0, 0, (int) checkSecond.x, (int) checkSecond.y, (int) checkSecond.width, (int) checkSecond.height);

			//Scale the smaller one up
			if (checkFirst.area() > checkSecond.area()) {
				firstRoiScaled = firstRoi;
				secondRoiScaled = new Pixmap((int) checkFirst.width, (int) checkFirst.height, secondRoi.getFormat());
				secondRoiScaled.drawPixmap(secondRoi, 0, 0, secondRoi.getWidth(), secondRoi.getHeight(), 0, 0, secondRoiScaled.getWidth(), secondRoiScaled.getHeight());
			} else {
				secondRoiScaled = secondRoi;
				firstRoiScaled = new Pixmap((int) checkSecond.width, (int) checkSecond.height, firstRoi.getFormat());
				firstRoiScaled.drawPixmap(firstRoi, 0, 0, firstRoi.getWidth(), firstRoi.getHeight(), 0, 0, firstRoiScaled.getWidth(), firstRoiScaled.getHeight());
			}
		} catch (IllegalArgumentException exception) {
			return 0d;
		}

		//If any two pixels on the same place are not white, we have a collision
		for (int x = 0; x < firstRoiScaled.getWidth(); x++) {
			for (int y = 0; y < firstRoiScaled.getHeight(); y++) {
				if ((firstRoiScaled.getPixel(x, y) != 0) && (secondRoiScaled.getPixel(x, y) != 0)) {
					return 1d;
				}
			}
		}

		return 0d;
	}

	//Use this for debugging
	private static void printPixmap(Pixmap map1) {
		for (int x = 0; x < map1.getWidth(); x++) {
			String row = " ";
			for (int y = 0; y < map1.getHeight(); y++) {
				if (map1.getPixel(x, y) != 0) {
					row += "O ";
				} else {
					row += "+ ";
				}
			}
			Log.d("testitest", row);
		}
		Log.d("testitest", "#######################################################");
	}

	public static double checkEdgeCollision(Look firstLook) {
		//Why should we collide when not visible ?
		if (!firstLook.visible) {
			return 0d;
		}
		int screenWidth = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenWidth;
		int screenHeigth = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenHeight;
		int treshhold_y = screenHeigth/2;
		int treshhold_x = screenWidth/2;
		//Where are we touching ?
		boolean touchingTop = (firstLook.getHitbox().y + firstLook.getHitbox().height) >= treshhold_y
				&& firstLook.getHitbox().y < treshhold_y;
		boolean touchingLeft = firstLook.getHitbox().x <= -treshhold_x
				&& firstLook.getHitbox().x + firstLook.getHitbox().width >= -treshhold_x;
		boolean touchingBottom = firstLook.getHitbox().y <= -treshhold_y
				&& firstLook.getHitbox().y + firstLook.getHitbox().height > -treshhold_y;
		boolean touchingRight = (firstLook.getHitbox().x + firstLook.getHitbox().width) >= treshhold_x
				&& firstLook.getHitbox().x < treshhold_x;

		//Basic check again
		if (!(touchingTop || touchingBottom || touchingLeft || touchingRight)) return 0d;

		Pixmap mask = firstLook.getCollisionMask();

		//Get the correct row of pixels, and check if any is not white, than we have a collision
		if (touchingTop) {
			int rowToCheck = (int) ((-1) * (firstLook.getHitbox().y - treshhold_y)
					* Constants.COLLISION_MASK_SIZE / firstLook.getHitbox().height);
			for (int pix = 0; pix < mask.getHeight(); pix++) {
				if (mask.getPixel(pix, rowToCheck) != 0) {
					return 1d;
				}
			}
		}

		if (touchingLeft) {
			int colToCheck = (int) ((-firstLook.getHitbox().x - treshhold_x)
					* Constants.COLLISION_MASK_SIZE / firstLook.getHitbox().width);
			for (int pix = 0; pix < mask.getWidth(); pix++) {
				if (mask.getPixel(colToCheck, pix) != 0) {
					return 1d;
				}
			}
		}

		if (touchingBottom) {
			int rowToCheck = (int) ((-1) * (firstLook.getHitbox().y + treshhold_y)
					* Constants.COLLISION_MASK_SIZE / firstLook.getHitbox().height);
			for (int pix = 0; pix < mask.getHeight(); pix++) {
				if (mask.getPixel(pix, rowToCheck) != 0) {
					return 1d;
				}
			}
		}

		if (touchingRight) {
			int colToCheck = (int) ((treshhold_x - firstLook.getHitbox().x)
					* Constants.COLLISION_MASK_SIZE / firstLook.getHitbox().width);
			for (int pix = 0; pix < mask.getWidth(); pix++) {
				if (mask.getPixel(colToCheck, pix) != 0) {
					return 1d;
				}
			}
		}

		return 0d;
	}

	private static Rectangle[] getRectsOfInterest(Rectangle hitboxOne, Rectangle hitboxTwo) {
		Rectangle firstRoi;
		Rectangle secondRoi;
		Rectangle roi = new Rectangle();

		Intersector.intersectRectangles(hitboxOne, hitboxTwo, roi);

		firstRoi = normalizeRectangle(hitboxOne, roi);
		secondRoi = normalizeRectangle(hitboxTwo, roi);

		Rectangle[] result = new Rectangle[2];
		result[0] = firstRoi;
		result[1] = secondRoi;

		return result;
	}

	private static Rectangle normalizeRectangle(Rectangle outer, Rectangle inner) {
		Rectangle result = new Rectangle();

		result.x = inner.x - outer.x;
		result.y = inner.y - outer.y;
		result.width = inner.width;
		result.height = inner.height;

		return result;
	}

	private static Rectangle scaleRectangle (Rectangle rect, Look look) {
		Rectangle result = new Rectangle();

		result.x = rect.x * (Constants.COLLISION_MASK_SIZE / look.getHitbox().width);
		result.y = rect.y * (Constants.COLLISION_MASK_SIZE / look.getHitbox().height);
		result.width = rect.width * (Constants.COLLISION_MASK_SIZE / look.getHitbox().width);
		result.height = rect.height * (Constants.COLLISION_MASK_SIZE / look.getHitbox().height);

		if (result.width < 1) result.width = 1;
		if (result.height < 1) result.height = 1;

		return result;
	}
}
