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
package org.catrobat.catroid.uitest.ui.dialog;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.ChangeVariableBrick;
import org.catrobat.catroid.content.bricks.ForeverBrick;
import org.catrobat.catroid.content.bricks.LoopEndlessBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.ui.ProjectActivity;
import org.catrobat.catroid.ui.fragment.ScriptFragment;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCase;
import org.catrobat.catroid.uitest.util.UiTestUtils;

public class StageDebugDialogTest extends BaseActivityInstrumentationTestCase<MainMenuActivity> {

	private String testProject = UiTestUtils.PROJECTNAME1;
	private String variableName = "test";
	private UserVariable variable = new UserVariable(variableName);

	public StageDebugDialogTest() {
		super(MainMenuActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		UiTestUtils.prepareStageForTest();
	}

	public void testDebugModeOnOffAndBackButton() {
		openDebugDialog();
		solo.goBack();
		solo.sleep(500);
		assertTrue("Not in Stage dialog", solo.waitForView(R.id.stage_dialog_button_debug));
		solo.clickOnView(solo.getView(R.id.stage_dialog_button_debug));
		solo.sleep(500);
		assertTrue("Not in Stage Debug dialog", solo.waitForView(R.id.stage_debug_dialog_button_debug));
		solo.clickOnView(solo.getView(R.id.stage_debug_dialog_button_debug));
		solo.sleep(500);
		assertTrue("Not in Stage dialog", solo.waitForView(R.id.stage_dialog_button_debug));
	}

	public void testStepButton() {
		openDebugDialog();
		int prev = (int) ((double) variable.getValue());
		solo.clickOnView(solo.getView(R.id.stage_debug_dialog_button_step));
		solo.sleep(500);
		int next = (int) ((double) variable.getValue());
		//Is possible when step is doing the forever brick
		if (prev == next) {
			solo.clickOnView(solo.getView(R.id.stage_debug_dialog_button_step));
			solo.sleep(500);
		}
		assertTrue("variable did not change (prev: " + prev + ", next: " + next + ")", next == (prev + 1));
	}

	public void testVariableButton() {
		openDebugDialog();
		solo.clickOnView(solo.getView(R.id.stage_debug_dialog_button_show_variables));
		solo.sleep(500);
		solo.clickOnText(solo.getString(R.string.global_variables));
		solo.sleep(500);
		String expected = Double.toString((double) variable.getValue());
		assertTrue("variable name not found (" + variableName + ")", solo.waitForText(variableName));
		assertTrue("variable value not found (" + expected + ")", solo.waitForText(expected));
	}

	public void testCodeButton() {
		openDebugDialog();
		solo.clickOnView(solo.getView(R.id.stage_debug_dialog_button_show_code));
		solo.sleep(500);
		assertTrue("sprite dialog not shown", solo.waitForDialogToOpen());
		solo.clickOnText(variableName);
		assertTrue("scriptFragment not shown", solo.waitForFragmentByTag(ScriptFragment.TAG));
		String brickWhenStarted = solo.getString(R.string.brick_when_started);

		solo.waitForText(brickWhenStarted);
		solo.clickOnText(brickWhenStarted);
		assertFalse("user interaction should be disabled", solo.waitForDialogToOpen());
	}

	private Project createTestProject(String projectName) {
		Project project = new Project(getActivity(), projectName);
		project.getDataContainer().addProjectUserVariable(variableName);
		variable = project.getDataContainer().findUserVariable(variableName, project.getDataContainer()
				.getProjectVariables());

		Sprite sprite = new Sprite(variableName);
		Script startScript = new StartScript();
		ForeverBrick foreverBrick = new ForeverBrick();
		startScript.addBrick(foreverBrick);

		ChangeVariableBrick changeVariableBrick = new ChangeVariableBrick(new Formula(1), variable);
		startScript.addBrick(changeVariableBrick);

		LoopEndlessBrick loopEndlessBrick = new LoopEndlessBrick(foreverBrick);
		startScript.addBrick(loopEndlessBrick);
		sprite.addScript(startScript);
		project.addSprite(sprite);

		return project;
	}

	private void openDebugDialog() {
		Project project = createTestProject(testProject);
		ProjectManager.getInstance().setProject(project);

		solo.clickOnText(solo.getString(R.string.main_menu_continue));
		solo.waitForActivity(ProjectActivity.class.getSimpleName());
		UiTestUtils.clickOnBottomBar(solo, R.id.button_play);
		solo.waitForActivity(StageActivity.class.getSimpleName());
		solo.sleep(1000);
		solo.goBack();
		solo.sleep(500);
		solo.clickOnView(solo.getView(R.id.stage_dialog_button_debug));
		solo.sleep(500);
		assertTrue("Not in Stage Debug dialog", solo.waitForView(R.id.stage_debug_dialog_button_debug));
	}
}
