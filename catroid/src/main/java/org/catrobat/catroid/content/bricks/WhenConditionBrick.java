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
package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.BrickValues;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.WhenConditionScript;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.util.List;

public class WhenConditionBrick extends FormulaBrick implements ScriptBrick {

	private WhenConditionScript script;

	public WhenConditionBrick() {
		init();
	}

	public WhenConditionBrick(Formula condition) {
		init();
		setFormulaWithBrickField(BrickField.IF_CONDITION, condition);
	}

	public WhenConditionBrick(WhenConditionScript script) {
		this.script = script;
		init();
	}

	private void init() {
		getScriptSafe();
		formulaMap = this.script.getFormulaMap();
		addAllowedBrickField(BrickField.IF_CONDITION);
	}

	@Override
	public void showFormulaEditorToEditFormula(View view) {
		FormulaEditorFragment.showFragment(view, this, BrickField.IF_CONDITION);
	}

	@Override
	public void updateReferenceAfterMerge(Scene into, Scene from) {
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		if (view != null) {

			View layout = view.findViewById(R.id.brick_when_condition_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			TextView whenLabel = (TextView) view.findViewById(R.id.when_conditon_label);
			TextView whenLabel2 = (TextView) view.findViewById(R.id.when_condition_label_second_part);
			TextView conditionEditText = (TextView) view.findViewById(R.id.brick_when_condition_edit_text);
			whenLabel.setTextColor(whenLabel.getTextColors().withAlpha(alphaValue));
			whenLabel2.setTextColor(whenLabel2.getTextColors().withAlpha(alphaValue));
			conditionEditText.setTextColor(conditionEditText.getTextColors().withAlpha(alphaValue));
			conditionEditText.getBackground().setAlpha(alphaValue);

			this.alphaValue = alphaValue;
		}

		return view;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}
		if (view == null) {
			alphaValue = 255;
		}

		view = View.inflate(context, R.layout.brick_when_condition_true, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_when_condition_checkbox);
		final Brick brickInstance = this;

		checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		TextView prototypeTextView = (TextView) view.findViewById(R.id.brick_when_condition_prototype_text_view);
		TextView conditionEditText = (TextView) view.findViewById(R.id.brick_when_condition_edit_text);

		getFormulaWithBrickField(BrickField.IF_CONDITION).setTextFieldId(R.id.brick_when_condition_edit_text);
		getFormulaWithBrickField(BrickField.IF_CONDITION).refreshTextField(view);

		prototypeTextView.setVisibility(View.GONE);
		conditionEditText.setVisibility(View.VISIBLE);

		conditionEditText.setOnClickListener(this);

		return view;
	}

	public Formula getConditionFormula() {
		return getFormulaWithBrickField(BrickField.IF_CONDITION);
	}

	@Override
	public View getPrototypeView(Context context) {
		View prototypeView = View.inflate(context, R.layout.brick_when_condition_true, null);
		TextView textView = (TextView) prototypeView.findViewById(R.id.brick_when_condition_prototype_text_view);
		textView.setText(String.valueOf(BrickValues.IF_CONDITION));
		return prototypeView;
	}

	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		sequence.addAction(sprite.getActionFactory().createWaitUntilAction(sprite, getFormulaWithBrickField(BrickField.IF_CONDITION)));
		return null;
	}

	@Override
	public Script getScriptSafe() {
		if (script == null) {
			script = new WhenConditionScript(this);
			formulaMap = script.getFormulaMap();
		}
		return script;
	}

	@Override
	public Brick clone() {
		return new WhenConditionBrick();
	}
}
