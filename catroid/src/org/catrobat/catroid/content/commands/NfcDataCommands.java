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

package org.catrobat.catroid.content.commands;

import android.util.Log;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.NfcTagData;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.content.bricks.WhenNfcBrick;
import org.catrobat.catroid.ui.controller.BackPackListManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfcDataCommands {
	/*
	* This one is simple, just save the old and new Name, and the look itself
	 */
	public static class RenameNfcCommand implements MediaCommand {
		private String oldName;
		private String newName;
		private NfcTagData nfcTagData;

		public RenameNfcCommand(NfcTagData nfcTagData, String newName) {
			this.oldName = nfcTagData.getNfcTagName();
			this.newName = newName;
			this.nfcTagData = nfcTagData;
		}

		@Override
		public void execute() {
			nfcTagData.setNfcTagName(newName);
		}

		@Override
		public void undo() {
			nfcTagData.setNfcTagName(oldName);
		}

		@Override
		public void update() {
			NfcTagData temp = ProjectManager.getInstance().getNfcById(nfcTagData.getId());
			if (temp != null) {
				nfcTagData = temp;
			}
		}
	}

	public static class AddNfcCommand implements MediaCommand {
		private ArrayList<NfcTagData> nfcTagDatas;
		Map<NfcTagData, WhenNfcBrick> bricks = new HashMap<>();

		public AddNfcCommand(ArrayList<NfcTagData> nfcTagDatas) {
			this.nfcTagDatas = nfcTagDatas;
		}

		@Override
		public void execute() {
			ProjectManager.getInstance().getCurrentSprite().getNfcTagList().addAll(nfcTagDatas);

			for (NfcTagData nfcTagData : bricks.keySet()) {
				bricks.get(nfcTagData).setNfcTag(nfcTagData);
			}

			bricks.clear();
		}

		@Override
		public void undo() {
			for (WhenNfcBrick brick : ProjectManager.getInstance().getCurrentSprite().getNfcBrickList()) {
				if (nfcTagDatas.contains(brick.getNfcTag())) {
					bricks.put(brick.getNfcTag(), brick);
				}
			}
			ProjectManager.getInstance().getCurrentSprite().getNfcTagList().removeAll(nfcTagDatas);
		}

		@Override
		public void update() {
			//Update our lookList with new Objects, but same id (needed after project reload)
			for (int i = 0; i < this.nfcTagDatas.size(); i++) {
				NfcTagData temp = ProjectManager.getInstance().getNfcById(nfcTagDatas.get(i).getId());
				if (temp != null) {
					nfcTagDatas.remove(i);
					nfcTagDatas.add(i, temp);
				}
			}

			Map<NfcTagData, WhenNfcBrick> temp = new HashMap<>();

			//Update our brick list with new Objects with same ID
			for (Map.Entry<NfcTagData, WhenNfcBrick> entry : bricks.entrySet()) {
				Map.Entry<NfcTagData, WhenNfcBrick> tempEntry = entry;
				WhenNfcBrick tempBrick = ProjectManager.getInstance().getWhenNfcBrickById(entry.getValue().getId());
				NfcTagData tempNfc = ProjectManager.getInstance().getNfcById(entry.getKey().getId());
				if (tempBrick != null) {
					tempEntry.setValue(tempBrick);
				}

				if (tempNfc != null) {
					temp.put(tempNfc, tempEntry.getValue());
				} else {
					temp.put(tempEntry.getKey(), tempEntry.getValue());
				}
			}

			bricks.clear();
			bricks.putAll(temp);
		}
	}

	//Works basically the same as the Add comand, but the other way round
	public static class DeleteNfcCommand implements MediaCommand {
		private ArrayList<NfcTagData> nfcTagDatas;
		private ArrayList<Integer> positions = new ArrayList<>();
		Map<NfcTagData, WhenNfcBrick> bricks = new HashMap<>();

		public DeleteNfcCommand(ArrayList<NfcTagData> nfcTagDatas) {
			this.nfcTagDatas = nfcTagDatas;
		}

		@Override
		public void execute() {
			for (WhenNfcBrick brick : ProjectManager.getInstance().getCurrentSprite().getNfcBrickList()) {
				if (nfcTagDatas.contains(brick.getNfcTag())) {
					bricks.put(brick.getNfcTag(), brick);
				}
			}
			for (NfcTagData nfcTagData : nfcTagDatas) {
				positions.add(ProjectManager.getInstance().getCurrentSprite().getNfcPositionById(nfcTagData));
			}
			ProjectManager.getInstance().getCurrentSprite().getNfcTagList().removeAll(nfcTagDatas);
		}

		@Override
		public void undo() {
			for (int i = 0; i < nfcTagDatas.size(); i++) {
				ProjectManager.getInstance().getCurrentSprite().getNfcTagList().add(positions.get(i), nfcTagDatas.get(i));
			}

			for (NfcTagData nfcTagData : bricks.keySet()) {
				bricks.get(nfcTagData).setNfcTag(nfcTagData);
			}

			positions.clear();
			bricks.clear();
		}

		@Override
		public void update() {
			for (int i = 0; i < this.nfcTagDatas.size(); i++) {
				NfcTagData temp = ProjectManager.getInstance().getNfcById(nfcTagDatas.get(i).getId());
				if (temp != null) {
					nfcTagDatas.remove(i);
					nfcTagDatas.add(i, temp);
				}
			}

			Map<NfcTagData, WhenNfcBrick> temp = new HashMap<>();

			for (Map.Entry<NfcTagData, WhenNfcBrick> entry : bricks.entrySet()) {
				Map.Entry<NfcTagData, WhenNfcBrick> tempEntry = entry;
				WhenNfcBrick tempBrick = ProjectManager.getInstance().getWhenNfcBrickById(entry.getValue().getId());
				NfcTagData tempNfc = ProjectManager.getInstance().getNfcById(entry.getKey().getId());
				if (tempBrick != null) {
					tempEntry.setValue(tempBrick);
				}

				if (tempNfc != null) {
					temp.put(tempNfc, tempEntry.getValue());
				} else {
					temp.put(tempEntry.getKey(), tempEntry.getValue());
				}
			}

			bricks.clear();
			bricks.putAll(temp);
		}
	}

	public static class MoveNfcCommand implements MediaCommand {
		private int previousPosition;
		private int newPosition;

		public MoveNfcCommand(int previousPosition, int newPosition) {
			this.previousPosition = previousPosition;
			this.newPosition = newPosition;
		}

		@Override
		public void execute() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();
			Collections.swap(nfcTagDatas, previousPosition, newPosition);
		}

		@Override
		public void undo() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();
			Collections.swap(nfcTagDatas, newPosition, previousPosition);
		}

		@Override
		public void update() {
		}
	}

	public static class MoveNfcToBottomCommand implements MediaCommand {
		private int position;

		public MoveNfcToBottomCommand(int position) {
			this.position = position;
		}

		@Override
		public void execute() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();

			for (int i = position; i < nfcTagDatas.size() - 1; i++) {
				Collections.swap(nfcTagDatas, i, i + 1);
			}
		}

		@Override
		public void undo() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();

			for (int i = nfcTagDatas.size() - 1; i > position; i--) {
				Collections.swap(nfcTagDatas, i, i - 1);
			}
		}

		@Override
		public void update() {
		}
	}

	public static class MoveNfcToTopCommand implements MediaCommand {
		private int position;

		public MoveNfcToTopCommand(int position) {
			this.position = position;
		}

		@Override
		public void execute() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();

			for (int i = position; i > 0; i--) {
				Collections.swap(nfcTagDatas, i, i - 1);
			}
		}

		@Override
		public void undo() {
			List<NfcTagData> nfcTagDatas = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();

			for (int i = 0; i < position; i++) {
				Collections.swap(nfcTagDatas, i, i + 1);
			}
		}

		@Override
		public void update() {
		}
	}
}
