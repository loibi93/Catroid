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
package org.catrobat.catroid.formulaeditor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.FormulaElement.ElementType;

import java.io.Serializable;
import java.util.List;

public class Formula implements Serializable {

	private static final long serialVersionUID = 1L;
	private FormulaElement formulaTree;
	private transient Integer formulaTextFieldId = null;
	private transient InternFormula internFormula = null;
	private transient String displayText = null;

	public Object readResolve() {

		if (formulaTree == null) {
			formulaTree = new FormulaElement(ElementType.NUMBER, "0 ", null);
		}

		internFormula = new InternFormula(formulaTree.getInternTokenList());

		return this;
	}

	public Formula(FormulaElement formulaElement) {
		formulaTree = formulaElement;
		internFormula = new InternFormula(formulaTree.getInternTokenList());
	}

	public Formula(Integer value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Long.toString(Math.abs((long) value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public Formula(Float value) {
		this(Double.valueOf(value));
	}

	public Formula(Double value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Double.toString(Math.abs(value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public void updateVariableReferences(String oldName, String newName, Context context) {
		internFormula.updateVariableReferences(oldName, newName, context);
		formulaTree.updateVariableReferences(oldName, newName);
		displayText = null;
	}

	public void getVariableAndListNames(List<String> variables, List<String> lists) {
		internFormula.getVariableAndListNames(variables, lists);
		formulaTree.getVariableAndListNames(variables, lists);
	}

	public Formula(String value) {
		if (value.equalsIgnoreCase(Functions.ARDUINOANALOG.toString())) {
			formulaTree = new FormulaElement(ElementType.SENSOR, Functions.ARDUINOANALOG.toString(), null);
		} else if (value.equalsIgnoreCase(Functions.ARDUINODIGITAL.toString())) {
			formulaTree = new FormulaElement(ElementType.SENSOR, Functions.ARDUINODIGITAL.toString(), null);
		} else {
			formulaTree = new FormulaElement(ElementType.STRING, value, null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public void setDisplayText(String text) {
		displayText = text;
	}

	public Boolean interpretBoolean(Sprite sprite) throws InterpretationException {
		int result = interpretDouble(sprite).intValue();
		return result != 0;
	}

	public Integer interpretInteger(Sprite sprite) throws InterpretationException {
		Double returnValue = interpretDouble(sprite);
		return returnValue.intValue();
	}

	public Double interpretDouble(Sprite sprite) throws InterpretationException {
		try {
			Object returnValue = formulaTree.interpretRecursive(sprite);
			Double doubleReturnValue;
			if (returnValue instanceof String) {
				doubleReturnValue = Double.valueOf((String) returnValue);
				if (doubleReturnValue.isNaN()) {
					throw new InterpretationException("NaN in interpretDouble()");
				}
				return doubleReturnValue;
			} else {
				doubleReturnValue = (Double) returnValue;
				if (doubleReturnValue.isNaN()) {
					throw new InterpretationException("NaN in interpretDouble()");
				}
				return (Double) returnValue;
			}
		} catch (ClassCastException classCastException) {
			throw new InterpretationException("Couldn't interpret Formula.", classCastException);
		} catch (NumberFormatException numberFormatException) {
			throw new InterpretationException("Couldn't interpret Formula.", numberFormatException);
		}
	}

	public Float interpretFloat(Sprite sprite) throws InterpretationException {
		Double returnValue = interpretDouble(sprite);
		return returnValue.floatValue();
	}

	public String interpretString(Sprite sprite) throws InterpretationException {
		Object interpretation = formulaTree.interpretRecursive(sprite);

		if (interpretation instanceof Double && ((Double) interpretation).isNaN()) {
			throw new InterpretationException("NaN in interpretString()");
		}

		return String.valueOf(interpretation);
	}

	public Object interpretObject(Sprite sprite) {
		return formulaTree.interpretRecursive(sprite);
	}

	public void setRoot(FormulaElement formula) {
		displayText = null;
		formulaTree = formula;
		internFormula = new InternFormula(formula.getInternTokenList());
	}

	public FormulaElement getRoot() {
		return formulaTree;
	}

	public void setTextFieldId(int id) {
		formulaTextFieldId = id;
	}

	public String getDisplayString(Context context) {
		if (displayText != null) {
			return displayText;
		}

		if (context != null) {
			internFormula.generateExternFormulaStringAndInternExternMapping(context);
		}
		return internFormula.getExternFormulaString();
	}

	public void refreshTextField(View view) {
		refreshTextField(view, getTrimmedFormulaString(view.getContext()));
	}

	public void refreshTextField(View view, String formulaString) {
		if (formulaTextFieldId != null && formulaTree != null && view != null) {
			TextView formulaTextField = (TextView) view.findViewById(formulaTextFieldId);
			if (formulaTextField != null) {
				formulaTextField.setText(formulaString);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void highlightTextField(View brickView) {
		Drawable highlightBackground;
		highlightBackground = brickView.getResources().getDrawable(R.drawable.textfield_pressed_android4);

		TextView formulaTextField = (TextView) brickView.findViewById(formulaTextFieldId);

		if (formulaTextField != null) {
			formulaTextField.setBackgroundDrawable(highlightBackground);
		}
	}

	public String getTrimmedFormulaString(Context context) {
		return internFormula.trimExternFormulaString(context);
	}

	public void prepareToRemove() {
		formulaTextFieldId = null;
	}

	public InternFormulaState getInternFormulaState() {
		return internFormula.getInternFormulaState();
	}

	public boolean containsElement(FormulaElement.ElementType elementType) {
		return formulaTree.containsElement(elementType);
	}

	public boolean isSingleNumberFormula() {
		return formulaTree.isSingleNumberFormula();
	}

	@Override
	public Formula clone() {
		if (formulaTree != null) {
			return new Formula(formulaTree.clone());
		}

		return new Formula(0);
	}

	public void removeVariableReferences(String name, Context context) {
		internFormula.removeVariableReferences(name, context);
	}

	public int getRequiredResources() {
		return formulaTree.getRequiredResources();
	}

	public String getResultForComputeDialog(Context context) {
		Sprite sprite = ProjectManager.getInstance().getCurrentSprite();
		ElementType type = formulaTree.getElementType();

		if (formulaTree.isLogicalOperator()) {
			boolean result;
			try {
				result = this.interpretBoolean(sprite);
			} catch (InterpretationException interpretationException) {
				return "ERROR";
			}
			int logicalFormulaResultIdentifier = result ? R.string.formula_editor_true : R.string.formula_editor_false;
			return context.getString(logicalFormulaResultIdentifier);
		} else if (type == ElementType.STRING
				|| type == ElementType.SENSOR
				|| (type == ElementType.FUNCTION
				&& (Functions.getFunctionByValue(formulaTree.getValue()) == Functions.LETTER
				|| Functions.getFunctionByValue(formulaTree.getValue()) == Functions.JOIN))) {
			try {
				return interpretString(sprite);
			} catch (InterpretationException interpretationException) {
				return "ERROR";
			}
		} else if (formulaTree.isUserVariableWithTypeString(sprite)) {
			DataContainer userVariables = ProjectManager.getInstance().getCurrentScene().getDataContainer();
			UserVariable userVariable = userVariables.getUserVariable(formulaTree.getValue(), sprite);
			return (String) userVariable.getValue();
		} else {
			Double interpretationResult;
			try {
				interpretationResult = this.interpretDouble(sprite);
			} catch (InterpretationException interpretationException) {
				return "ERROR";
			}
			return String.valueOf(interpretationResult);
		}
	}
}
