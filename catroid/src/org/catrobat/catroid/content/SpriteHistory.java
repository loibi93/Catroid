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

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteHistory extends MediaHistory {
	private static SpriteHistory INSTANCE = new SpriteHistory();
	public static String projectName;

	public static SpriteHistory getInstance() {
		if (INSTANCE == null) INSTANCE = new SpriteHistory();
		return INSTANCE;
	}

	public static void clearHistory() {
		INSTANCE = null;
	}

	private static boolean getAllUndoRedoStatus() {
		boolean result = false;
		if (INSTANCE == null) return false;
		result |= INSTANCE.isRedoable();
		result |= INSTANCE.isUndoable();

		return result;
	}

	public static void applyChanges() {
		if (!LookDataHistory.getAllUndoRedoStatus() && !getAllUndoRedoStatus()) LookDataHistory.applyChanges();
		if (!SoundInfoHistory.getAllUndoRedoStatus() && !getAllUndoRedoStatus()) SoundInfoHistory.applyChanges();
	}
}
