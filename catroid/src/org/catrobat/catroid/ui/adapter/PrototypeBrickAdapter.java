/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
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
package org.catrobat.catroid.ui.adapter;

/**
 * @author DENISE, DANIEL
 *
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.ui.BrickView;
import org.catrobat.catroid.ui.bricks.BrickViewFactory;

import java.util.ArrayList;
import java.util.List;

public class PrototypeBrickAdapter extends BaseAdapter {

	private List<Brick> brickList;

//	private OnBrickCheckedListener addBrickFragment;
	private List<Brick> checkedBricks = new ArrayList<Brick>();
	private BrickViewFactory brickViewFactory;
	private boolean useSelection;

	public PrototypeBrickAdapter(Context context, List<Brick> brickList) {
		this.brickList = brickList;
		brickViewFactory = new BrickViewFactory(context);
	}

//	public void addBrickToList(Brick brick) {
//		brickList.add(brick);
//		notifyDataSetChanged();
//	}

	@Override
	public int getCount() {
		return brickList.size();
	}

	@Override
	public Brick getItem(int position) {
		return brickList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}


	public int getAmountOfCheckedItems() {
		return getCheckedBricks().size();
	}

	public List<Brick> getCheckedBricks() {
		return checkedBricks;
	}

	public List<Brick> getBrickList() {
		return brickList;
	}

	public void removeUserBrick(Brick brick) {
		brickList.remove(brick);
		UserBrick deleteThisBrick = (UserBrick) brick;
		ProjectManager.getInstance().getCurrentSprite().removeUserBrick(deleteThisBrick);

		notifyDataSetChanged();
	}

	public void enableSelection(boolean enableSelection) {
		useSelection = enableSelection;
		notifyDataSetChanged();

	}

	public interface OnBrickCheckedListener {
		void onBrickChecked();
	}

	public void setOnBrickCheckedListener(OnBrickCheckedListener listener) {
//		addBrickFragment = listener;
	}

//	public void handleCheck(Brick brick, boolean isChecked) {
//		if (brick != null && brick.getCheckBox() != null) {
//			brick.getCheckBox().setChecked(isChecked);
//			if (isChecked) {
//				checkedBricks.add(brick);
//			} else {
//				checkedBricks.remove(brick);
//			}
//		}
//		if (addBrickFragment != null) {
//			addBrickFragment.onBrickChecked();
//		}
//	}

	public List<Brick> getReversedCheckedBrickList() {
		List<Brick> reverseCheckedList = new ArrayList<Brick>();
		for (int counter = checkedBricks.size() - 1; counter >= 0; counter--) {
			reverseCheckedList.add(checkedBricks.get(counter));
		}
		return reverseCheckedList;
	}

	public void clearCheckedItems() {
		checkedBricks.clear();
//		setCheckboxVisibility(View.GONE);
//		uncheckAllItems();
//		enableAllBricks();
		enableSelection(false);
//		notifyDataSetChanged();
	}

//	private void enableAllBricks() {
//		for (Brick brick : brickList) {
//			if (brick.getCheckBox() != null) {
//				brick.getCheckBox().setEnabled(true);
//			}
//			brick.getViewWithAlpha(BrickAdapter.ALPHA_FULL);
//		}
//		notifyDataSetChanged();
//	}

//	private void uncheckAllItems() {
//		for (Brick brick : brickList) {
//			CheckBox checkbox = brick.getCheckBox();
//			if (checkbox != null) {
//				checkbox.setChecked(false);
//			}
//		}
//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Brick brick = brickList.get(position);
		BrickView view = brickViewFactory.createView(brick, parent);
		view.addMode(BrickView.Mode.PROTOTYPE);
		if (useSelection) {
			view.addMode(BrickView.Mode.SELECTION);
		} else {
			view.removeMode(BrickView.Mode.SELECTION);
		}
		return view;
	}
}
