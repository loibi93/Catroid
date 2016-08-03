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
package org.catrobat.catroid.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.DataContainer;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.adapter.DebugVariablesExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DebugVariablesActivity extends BaseActivity {

	private static final String TAG = DebugVariablesActivity.class.getSimpleName();
	private List<String> headers = new ArrayList<>();
	private HashMap<String, List<String>> childs = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_variables);

		prepareVariableData();
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.debug_variables_activity_expandable_listview);
		listView.setAdapter(new DebugVariablesExpandableListAdapter(this, headers, childs));

		if (headers.size() == 0) {
			findViewById(R.id.no_variable_mark).setVisibility(View.VISIBLE);
			findViewById(R.id.debug_variables_activity_expandable_listview).setVisibility(View.GONE);
		}
		BottomBar.hideAddButton(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		final ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		String projectName = ProjectManager.getInstance().getCurrentProject().getName();
		actionBar.setTitle(projectName);
	}

	private void prepareVariableData() {
		DataContainer container = ProjectManager.getInstance().getCurrentProject().getDataContainer();
		List<String> temp = new ArrayList<>();

		for (UserVariable userVariable : container.getProjectVariables()) {
			temp.add(userVariable.getName() + " (" + userVariable.getValue() + ")");
		}

		if (temp.size() > 0) {
			headers.add(getApplication().getString(R.string.global_variables));
			childs.put(getApplication().getString(R.string.global_variables), temp);
		}
		temp = new ArrayList<>();

		for (Sprite sprite : ProjectManager.getInstance().getCurrentProject().getSpriteList()) {
			for (UserVariable userVariable : container.getVariableListForSprite(sprite)) {
				temp.add(userVariable.getName() + " (" + userVariable.getValue() + ")");
			}
			if (temp.size() > 0) {
				headers.add(sprite.getName());
				childs.put(sprite.getName(), temp);
			}
			temp = new ArrayList<>();
		}
	}

	public void handlePlayButton(View view) {
		StageActivity.stageListener.dismissDialogs();
		finish();

	}
}
