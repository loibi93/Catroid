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
package org.catrobat.catroid.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.BrickBaseType;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.stage.StageListener;
import org.catrobat.catroid.ui.DebugVariablesActivity;
import org.catrobat.catroid.ui.ScriptActivity;

public class StageDebugDialog extends Dialog implements View.OnClickListener {
	private static final String TAG = StageDebugDialog.class.getSimpleName();
	private StageActivity stageActivity;
	private StageListener stageListener;
	private StageDialog stageDialog;
	private Sprite currentSpriteBackup;

	public StageDebugDialog(StageActivity stageActivity, StageListener stageListener, StageDialog stageDialog, int
			theme) {
		super(stageActivity, theme);
		this.stageActivity = stageActivity;
		this.stageListener = stageListener;
		this.stageDialog = stageDialog;
		this.currentSpriteBackup = ProjectManager.getInstance().getCurrentSprite();
		stageListener.setStageDebugDialog(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_stage_debug);
		getWindow().getAttributes();

		getWindow().getAttributes();

		int width = LayoutParams.MATCH_PARENT;
		int height = LayoutParams.WRAP_CONTENT;

		getWindow().setLayout(width, height);

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		getWindow().setBackgroundDrawableResource(R.color.transparent);

		((Button) findViewById(R.id.stage_debug_dialog_button_show_code)).setOnClickListener(this);
		((Button) findViewById(R.id.stage_debug_dialog_button_step)).setOnClickListener(this);
		((Button) findViewById(R.id.stage_debug_dialog_button_show_variables)).setOnClickListener(this);
		((Button) findViewById(R.id.stage_debug_dialog_button_debug)).setOnClickListener(this);
		((Button) findViewById(R.id.stage_debug_dialog_button_step)).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				stageListener.startStep();
				return false;
			}
		});
		((Button) findViewById(R.id.stage_debug_dialog_button_step)).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						stageListener.stopStep();
						break;
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.stage_debug_dialog_button_show_code:
				showSpriteDialog();
				break;
			case R.id.stage_debug_dialog_button_step:
				stageListener.step();
				break;
			case R.id.stage_debug_dialog_button_show_variables:
				Intent intent = new Intent(stageActivity, DebugVariablesActivity.class);
				stageActivity.startActivity(intent);
				break;
			case R.id.stage_debug_dialog_button_debug:
				dismiss();
				ProjectManager.getInstance().setCurrentSprite(currentSpriteBackup);
				stageDialog.show();
				break;
			default:
				Log.w(TAG, "Unimplemented button clicked! This shouldn't happen!");
				break;
		}
	}

	@Override
	public void onBackPressed() {
		dismiss();
		ProjectManager.getInstance().setCurrentSprite(currentSpriteBackup);
		stageDialog.show();
	}

	@Override
	public void dismiss() {
		resetRunningStatus();
		super.dismiss();
	}

	public void resumeStage() {
		stageActivity.resume();
	}

	private void showSpriteDialog() {
		if (ProjectManager.getInstance().getCurrentProject().getSpriteList().size() == 1) {
			openScriptForSprite(ProjectManager.getInstance().getCurrentSprite());
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(stageActivity, android.R.style.Theme_Holo_Dialog));
		builder.setTitle(stageActivity.getString(R.string.choose_sprite_dialog_title));

		final ArrayAdapter<Sprite> arrayAdapter = new ArrayAdapter<>(stageActivity, R.layout.dialog_stage_debug_sprite_item, ProjectManager
				.getInstance()
				.getCurrentProject().getSpriteList());

		builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Sprite sprite = arrayAdapter.getItem(which);
				openScriptForSprite(sprite);
			}
		});
		builder.show();
	}

	private void openScriptForSprite(Sprite sprite) {
		resetRunningStatus();
		ProjectManager.getInstance().setCurrentSprite(sprite);
		for (Integer scriptIndex : sprite.currentBrickMap.keySet()) {
			BrickBaseType brick = (BrickBaseType) sprite.getScript(scriptIndex).getBrick(sprite
					.currentBrickMap.get(scriptIndex));
			brick.setRunningState(true);
		}
		Intent intent = new Intent(stageActivity, ScriptActivity.class);
		intent.putExtra(ScriptActivity.EXTRA_DISABLE_USER_INTERACTION, true);
		stageActivity.startActivity(intent);
	}

	private void resetRunningStatus() {
		for (Sprite sprite : ProjectManager.getInstance().getCurrentProject().getSpriteList()) {
			for (Brick brick : sprite.getAllBricks()) {
				((BrickBaseType) brick).setRunningState(false);
			}
		}
	}
}
