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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.BrickValues;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.ui.fragment.ColorSeekbar;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.util.List;

public class SetPenColorBrick extends FormulaBrick {
	private static final long serialVersionUID = 1L;

	private transient View prototypeView;
	private transient AdapterView<?> adapterView;

	private transient TextView editRedValue;
	private transient TextView editGreenValue;
	private transient TextView editBlueValue;

	private transient ColorSeekbar colorSeekbar = new ColorSeekbar(this, BrickField.PHIRO_LIGHT_RED,
			BrickField.PHIRO_LIGHT_GREEN, BrickField.PHIRO_LIGHT_BLUE);

	protected Object readResolve() {
		return this;
	}

	public SetPenColorBrick() {
		addAllowedBrickField(BrickField.PHIRO_LIGHT_RED);
		addAllowedBrickField(BrickField.PHIRO_LIGHT_GREEN);
		addAllowedBrickField(BrickField.PHIRO_LIGHT_BLUE);
	}

	public SetPenColorBrick(int red, int green, int blue) {
		initializeBrickFields(new Formula(red), new Formula(green), new Formula(blue));
	}

	public SetPenColorBrick(Formula red, Formula green, Formula blue) {
		initializeBrickFields(red, green, blue);
	}

	private void initializeBrickFields(Formula red, Formula green, Formula blue) {
		addAllowedBrickField(BrickField.PHIRO_LIGHT_RED);
		addAllowedBrickField(BrickField.PHIRO_LIGHT_GREEN);
		addAllowedBrickField(BrickField.PHIRO_LIGHT_BLUE);
		setFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED, red);
		setFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN, green);
		setFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE, blue);
	}

	@Override
	public int getRequiredResources() {
		return getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED).getRequiredResources()
				| getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN).getRequiredResources()
				| getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE).getRequiredResources();
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_set_pen_color, null);

		TextView textValueRed = (TextView) prototypeView.findViewById(R.id.brick_set_pen_color_red_prototype_text_view);
		textValueRed.setText(String.valueOf(BrickValues.PEN_COLOR.r * 255));

		TextView textValueGreen = (TextView) prototypeView.findViewById(R.id.brick_set_pen_color_green_prototype_text_view);
		textValueGreen.setText(String.valueOf(BrickValues.PEN_COLOR.g * 255));

		TextView textValueBlue = (TextView) prototypeView.findViewById(R.id.brick_set_pen_color_blue_prototype_text_view);
		textValueBlue.setText(String.valueOf(BrickValues.PEN_COLOR.b * 255));

		return prototypeView;
	}

	@Override
	public Brick clone() {
		return new SetPenColorBrick(getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED).clone(),
				getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN).clone(),
				getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE).clone());
	}

	@Override
	public View getCustomView(Context context, int brickId, BaseAdapter baseAdapter) {
		return colorSeekbar.getView(context);
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {

		if (animationState) {
			return view;
		}
		if (view == null) {
			alphaValue = 255;
		}

		view = View.inflate(context, R.layout.brick_set_pen_color, null);
		setCheckboxView(R.id.brick_set_pen_color_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		TextView textRed = (TextView) view.findViewById(R.id.brick_set_pen_color_red_prototype_text_view);
		editRedValue = (TextView) view.findViewById(R.id.brick_set_pen_color_action_red_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED).setTextFieldId(R.id.brick_set_pen_color_action_red_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED).refreshTextField(view);

		textRed.setVisibility(View.GONE);
		editRedValue.setVisibility(View.VISIBLE);

		editRedValue.setOnClickListener(this);

		TextView textGreen = (TextView) view.findViewById(R.id.brick_set_pen_color_green_prototype_text_view);
		editGreenValue = (TextView) view.findViewById(R.id.brick_set_pen_color_action_green_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN).setTextFieldId(R.id.brick_set_pen_color_action_green_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN).refreshTextField(view);

		textGreen.setVisibility(View.GONE);
		editGreenValue.setVisibility(View.VISIBLE);

		editGreenValue.setOnClickListener(this);

		TextView textBlue = (TextView) view.findViewById(R.id.brick_set_pen_color_blue_prototype_text_view);
		editBlueValue = (TextView) view.findViewById(R.id.brick_set_pen_color_action_blue_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE).setTextFieldId(R.id.brick_set_pen_color_action_blue_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE).refreshTextField(view);

		textBlue.setVisibility(View.GONE);
		editBlueValue.setVisibility(View.VISIBLE);

		editBlueValue.setOnClickListener(this);

		return view;
	}

	@Override
	public void showFormulaEditorToEditFormula(View view) {

		BrickField clickedBrickField = getClickedBrickField(view);
		if (clickedBrickField == null) {
			return;
		}

		if (areAllBrickFieldsNumbers()) {
			FormulaEditorFragment.showCustomFragment(view, this, getClickedBrickField(view));
		} else {
			FormulaEditorFragment.showFragment(view, this, getClickedBrickField(view));
		}
	}

	private boolean areAllBrickFieldsNumbers() {
		return (getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED).getRoot().getElementType() == FormulaElement.ElementType.NUMBER)
				&& (getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN).getRoot().getElementType() == FormulaElement.ElementType.NUMBER)
				&& (getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE).getRoot().getElementType() == FormulaElement.ElementType.NUMBER);
	}

	private BrickField getClickedBrickField(View view) {
		switch (view.getId()) {
			case R.id.brick_set_pen_color_action_red_edit_text:
				return BrickField.PHIRO_LIGHT_RED;
			case R.id.brick_set_pen_color_action_green_edit_text:
				return BrickField.PHIRO_LIGHT_GREEN;
			case R.id.brick_set_pen_color_action_blue_edit_text:
				return BrickField.PHIRO_LIGHT_BLUE;
		}

		return null;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {

			View layout = view.findViewById(R.id.brick_set_pen_color_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			TextView textPhiroProLabel = (TextView) view.findViewById(R.id.brick_set_pen_color_action_label);
			TextView textPhiroProEyeRed = (TextView) view.findViewById(R.id.brick_set_pen_color_red_text_view);
			TextView editRed = (TextView) view.findViewById(R.id.brick_set_pen_color_action_red_edit_text);

			//red
			TextView textPhiroProEyeRedView = (TextView) view.findViewById(R.id.brick_set_pen_color_red_text_view);
			textPhiroProLabel.setTextColor(textPhiroProLabel.getTextColors().withAlpha(alphaValue));
			textPhiroProEyeRed.setTextColor(textPhiroProEyeRed.getTextColors().withAlpha(alphaValue));
			ColorStateList color = textPhiroProEyeRedView.getTextColors().withAlpha(alphaValue);
			if (adapterView != null) {
				((TextView) adapterView.getChildAt(0)).setTextColor(color);
			}
			editRed.setTextColor(editRed.getTextColors().withAlpha(alphaValue));
			editRed.getBackground().setAlpha(alphaValue);

			//green
			TextView textPhiroProEyeGreen = (TextView) view.findViewById(R.id.brick_set_pen_color_green_text_view);
			TextView editGreen = (TextView) view.findViewById(R.id.brick_set_pen_color_action_green_edit_text);
			TextView textPhiroProEyeGreenView = (TextView) view.findViewById(R.id.brick_set_pen_color_green_text_view);
			editGreen.setTextColor(editGreen.getTextColors().withAlpha(alphaValue));
			editGreen.getBackground().setAlpha(alphaValue);
			ColorStateList color2 = textPhiroProEyeGreenView.getTextColors().withAlpha(alphaValue);
			if (adapterView != null) {
				((TextView) adapterView.getChildAt(0)).setTextColor(color2);
			}
			textPhiroProEyeGreen.setTextColor(textPhiroProEyeGreen.getTextColors().withAlpha(alphaValue));

			//blue
			TextView textPhiroProEyeBlue = (TextView) view.findViewById(R.id.brick_set_pen_color_blue_text_view);
			TextView editBlue = (TextView) view.findViewById(R.id.brick_set_pen_color_action_blue_edit_text);
			TextView textPhiroProEyeBlueView = (TextView) view.findViewById(R.id.brick_set_pen_color_action_blue_edit_text);
			editBlue.setTextColor(editGreen.getTextColors().withAlpha(alphaValue));
			editBlue.getBackground().setAlpha(alphaValue);
			ColorStateList color3 = textPhiroProEyeBlueView.getTextColors().withAlpha(alphaValue);
			if (adapterView != null) {
				((TextView) adapterView.getChildAt(0)).setTextColor(color3);
			}
			textPhiroProEyeBlue.setTextColor(textPhiroProEyeBlue.getTextColors().withAlpha(alphaValue));

			this.alphaValue = alphaValue;
		}

		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		sequence.addAction(sprite.getActionFactory().createSetPenColorAction(sprite,
				getFormulaWithBrickField(BrickField.PHIRO_LIGHT_RED),
				getFormulaWithBrickField(BrickField.PHIRO_LIGHT_GREEN),
				getFormulaWithBrickField(BrickField.PHIRO_LIGHT_BLUE)));
		return null;
	}

	@Override
	public void updateReferenceAfterMerge(Scene into, Scene from) {
	}
}
