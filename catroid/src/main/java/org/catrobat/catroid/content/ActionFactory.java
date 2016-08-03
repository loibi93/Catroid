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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.camera.CameraManager;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.BroadcastEvent.BroadcastType;
import org.catrobat.catroid.content.actions.AddItemToUserListAction;
import org.catrobat.catroid.content.actions.ArduinoSendDigitalValueAction;
import org.catrobat.catroid.content.actions.ArduinoSendPWMValueAction;
import org.catrobat.catroid.content.actions.BroadcastAction;
import org.catrobat.catroid.content.actions.BroadcastNotifyAction;
import org.catrobat.catroid.content.actions.CameraBrickAction;
import org.catrobat.catroid.content.actions.ChangeBrightnessByNAction;
import org.catrobat.catroid.content.actions.ChangeColorByNAction;
import org.catrobat.catroid.content.actions.ChangeSizeByNAction;
import org.catrobat.catroid.content.actions.ChangeTransparencyByNAction;
import org.catrobat.catroid.content.actions.ChangeVariableAction;
import org.catrobat.catroid.content.actions.ChangeVolumeByNAction;
import org.catrobat.catroid.content.actions.ChangeXByNAction;
import org.catrobat.catroid.content.actions.ChangeYByNAction;
import org.catrobat.catroid.content.actions.ChooseCameraAction;
import org.catrobat.catroid.content.actions.ClearGraphicEffectAction;
import org.catrobat.catroid.content.actions.ComeToFrontAction;
import org.catrobat.catroid.content.actions.DeleteItemOfUserListAction;
import org.catrobat.catroid.content.actions.DroneEmergencyAction;
import org.catrobat.catroid.content.actions.DroneFlipAction;
import org.catrobat.catroid.content.actions.DroneMoveBackwardAction;
import org.catrobat.catroid.content.actions.DroneMoveDownAction;
import org.catrobat.catroid.content.actions.DroneMoveForwardAction;
import org.catrobat.catroid.content.actions.DroneMoveLeftAction;
import org.catrobat.catroid.content.actions.DroneMoveRightAction;
import org.catrobat.catroid.content.actions.DroneMoveUpAction;
import org.catrobat.catroid.content.actions.DronePlayLedAnimationAction;
import org.catrobat.catroid.content.actions.DroneSwitchCameraAction;
import org.catrobat.catroid.content.actions.DroneTakeoffAndLandAction;
import org.catrobat.catroid.content.actions.DroneTurnLeftAction;
import org.catrobat.catroid.content.actions.DroneTurnLeftWithMagnetometerAction;
import org.catrobat.catroid.content.actions.DroneTurnRightAction;
import org.catrobat.catroid.content.actions.DroneTurnRightWithMagnetometerAction;
import org.catrobat.catroid.content.actions.FlashAction;
import org.catrobat.catroid.content.actions.GoNStepsBackAction;
import org.catrobat.catroid.content.actions.HideAction;
import org.catrobat.catroid.content.actions.HideTextAction;
import org.catrobat.catroid.content.actions.IfLogicAction;
import org.catrobat.catroid.content.actions.InsertItemIntoUserListAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorMoveAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorStopAction;
import org.catrobat.catroid.content.actions.LegoNxtMotorTurnAngleAction;
import org.catrobat.catroid.content.actions.LegoNxtPlayToneAction;
import org.catrobat.catroid.content.actions.MoveNStepsAction;
import org.catrobat.catroid.content.actions.NextLookAction;
import org.catrobat.catroid.content.actions.PhiroMotorMoveBackwardAction;
import org.catrobat.catroid.content.actions.PhiroMotorMoveForwardAction;
import org.catrobat.catroid.content.actions.PhiroMotorStopAction;
import org.catrobat.catroid.content.actions.PhiroPlayToneAction;
import org.catrobat.catroid.content.actions.PhiroRGBLightAction;
import org.catrobat.catroid.content.actions.PhiroSensorAction;
import org.catrobat.catroid.content.actions.PlaySoundAction;
import org.catrobat.catroid.content.actions.PointInDirectionAction;
import org.catrobat.catroid.content.actions.PointToAction;
import org.catrobat.catroid.content.actions.RaspiIfLogicAction;
import org.catrobat.catroid.content.actions.RaspiPwmAction;
import org.catrobat.catroid.content.actions.RaspiSendDigitalValueAction;
import org.catrobat.catroid.content.actions.RepeatAction;
import org.catrobat.catroid.content.actions.RepeatUntilAction;
import org.catrobat.catroid.content.actions.ReplaceItemInUserListAction;
import org.catrobat.catroid.content.actions.SetBrightnessAction;
import org.catrobat.catroid.content.actions.SetColorAction;
import org.catrobat.catroid.content.actions.SetLookAction;
import org.catrobat.catroid.content.actions.SetSizeToAction;
import org.catrobat.catroid.content.actions.SetTextAction;
import org.catrobat.catroid.content.actions.SetTransparencyAction;
import org.catrobat.catroid.content.actions.SetVariableAction;
import org.catrobat.catroid.content.actions.SetVolumeToAction;
import org.catrobat.catroid.content.actions.SetXAction;
import org.catrobat.catroid.content.actions.SetYAction;
import org.catrobat.catroid.content.actions.ShowAction;
import org.catrobat.catroid.content.actions.ShowTextAction;
import org.catrobat.catroid.content.actions.SpeakAction;
import org.catrobat.catroid.content.actions.StopAllSoundsAction;
import org.catrobat.catroid.content.actions.TurnLeftAction;
import org.catrobat.catroid.content.actions.TurnRightAction;
import org.catrobat.catroid.content.actions.UserBrickAction;
import org.catrobat.catroid.content.actions.VibrateAction;
import org.catrobat.catroid.content.actions.WaitAction;
import org.catrobat.catroid.content.actions.WaitUntilAction;
import org.catrobat.catroid.content.actions.conditional.GlideToAction;
import org.catrobat.catroid.content.actions.conditional.IfOnEdgeBounceAction;
import org.catrobat.catroid.content.bricks.LegoNxtMotorMoveBrick;
import org.catrobat.catroid.content.bricks.LegoNxtMotorStopBrick;
import org.catrobat.catroid.content.bricks.LegoNxtMotorTurnAngleBrick;
import org.catrobat.catroid.content.bricks.PhiroMotorMoveBackwardBrick;
import org.catrobat.catroid.content.bricks.PhiroMotorMoveForwardBrick;
import org.catrobat.catroid.content.bricks.PhiroMotorStopBrick;
import org.catrobat.catroid.content.bricks.PhiroPlayToneBrick;
import org.catrobat.catroid.content.bricks.PhiroRGBLightBrick;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserList;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.physics.PhysicsObject;

import java.util.ArrayList;

public class ActionFactory extends Actions {

	public static Action createBroadcastAction(Sprite sprite, String broadcastMessage) {
		BroadcastAction action = Actions.action(BroadcastAction.class);
		BroadcastEvent event = new BroadcastEvent();
		event.setSenderSprite(sprite);
		event.setBroadcastMessage(broadcastMessage);
		event.setType(BroadcastType.broadcast);
		action.setBroadcastEvent(event);
		return action;
	}

	public static Action createBroadcastNotifyAction(BroadcastEvent event) {
		BroadcastNotifyAction action = Actions.action(BroadcastNotifyAction.class);
		action.setEvent(event);
		return action;
	}

	public Action createWaitAction(Sprite sprite, ArrayList<Integer> indexes, Formula delay) {
		WaitAction action = action(WaitAction.class);
		action.setSprite(sprite);
		action.setDelay(delay);
		action.setIndexes(indexes);
		return action;
	}

	public Action createBroadcastActionFromWaiter(Sprite sprite, ArrayList<Integer> indexes, String broadcastMessage) {
		BroadcastAction action = Actions.action(BroadcastAction.class);
		BroadcastEvent event = new BroadcastEvent();
		event.setSenderSprite(sprite);
		event.setBroadcastMessage(broadcastMessage);
		event.setRun(false);
		event.setType(BroadcastType.broadcastWait);
		action.setBroadcastEvent(event);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeBrightnessByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula changeBrightness) {
		ChangeBrightnessByNAction action = Actions.action(ChangeBrightnessByNAction.class);
		action.setSprite(sprite);
		action.setBrightness(changeBrightness);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeColorByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula changeColor) {
		ChangeColorByNAction action = Actions.action(ChangeColorByNAction.class);
		action.setSprite(sprite);
		action.setColor(changeColor);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeTransparencyByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula transparency) {
		ChangeTransparencyByNAction action = Actions.action(ChangeTransparencyByNAction.class);
		action.setSprite(sprite);
		action.setTransparency(transparency);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeSizeByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula size) {
		ChangeSizeByNAction action = Actions.action(ChangeSizeByNAction.class);
		action.setSprite(sprite);
		action.setSize(size);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeVolumeByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula volume) {
		ChangeVolumeByNAction action = Actions.action(ChangeVolumeByNAction.class);
		action.setVolume(volume);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeXByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula xMovement) {
		ChangeXByNAction action = Actions.action(ChangeXByNAction.class);
		action.setSprite(sprite);
		action.setxMovement(xMovement);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeYByNAction(Sprite sprite, ArrayList<Integer> indexes, Formula yMovement) {
		ChangeYByNAction action = Actions.action(ChangeYByNAction.class);
		action.setSprite(sprite);
		action.setyMovement(yMovement);
		action.setIndexes(indexes);
		return action;
	}

	public Action createClearGraphicEffectAction(Sprite sprite, ArrayList<Integer> indexes) {
		ClearGraphicEffectAction action = Actions.action(ClearGraphicEffectAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createComeToFrontAction(Sprite sprite, ArrayList<Integer> indexes) {
		ComeToFrontAction action = Actions.action(ComeToFrontAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createGlideToAction(Sprite sprite, ArrayList<Integer> indexes, Formula x, Formula y, Formula duration) {
		GlideToAction action = Actions.action(GlideToAction.class);
		action.setPosition(x, y);
		action.setDuration(duration);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createGlideToAction(Sprite sprite, ArrayList<Integer> indexes, Formula x, Formula y, Formula duration, Interpolation interpolation) {
		GlideToAction action = Actions.action(GlideToAction.class);
		action.setPosition(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPlaceAtAction(Sprite sprite, ArrayList<Integer> indexes, Formula x, Formula y) {
		GlideToAction action = Actions.action(GlideToAction.class);
		action.setPosition(x, y);
		action.setDuration(0);
		action.setInterpolation(null);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createGoNStepsBackAction(Sprite sprite, ArrayList<Integer> indexes, Formula steps) {
		GoNStepsBackAction action = Actions.action(GoNStepsBackAction.class);
		action.setSprite(sprite);
		action.setSteps(steps);
		action.setIndexes(indexes);
		return action;
	}

	public Action createHideAction(Sprite sprite, ArrayList<Integer> indexes) {
		HideAction action = Actions.action(HideAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createIfOnEdgeBounceAction(Sprite sprite, ArrayList<Integer> indexes) {
		IfOnEdgeBounceAction action = Actions.action(IfOnEdgeBounceAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createLegoNxtMotorMoveAction(Sprite sprite, ArrayList<Integer> indexes, LegoNxtMotorMoveBrick.Motor motorEnum, Formula speed) {
		LegoNxtMotorMoveAction action = Actions.action(LegoNxtMotorMoveAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setSpeed(speed);
		action.setIndexes(indexes);
		return action;
	}

	public Action createLegoNxtMotorStopAction(Sprite sprite, ArrayList<Integer> indexes, LegoNxtMotorStopBrick.Motor motorEnum) {
		LegoNxtMotorStopAction action = Actions.action(LegoNxtMotorStopAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createLegoNxtMotorTurnAngleAction(Sprite sprite, ArrayList<Integer> indexes,
			LegoNxtMotorTurnAngleBrick.Motor motorEnum, Formula degrees) {
		LegoNxtMotorTurnAngleAction action = Actions.action(LegoNxtMotorTurnAngleAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setDegrees(degrees);
		action.setIndexes(indexes);
		return action;
	}

	public Action createLegoNxtPlayToneAction(Sprite sprite, ArrayList<Integer> indexes, Formula hertz, Formula durationInSeconds) {
		LegoNxtPlayToneAction action = Actions.action(LegoNxtPlayToneAction.class);
		action.setHertz(hertz);
		action.setSprite(sprite);
		action.setDurationInSeconds(durationInSeconds);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroPlayToneActionAction(Sprite sprite, ArrayList<Integer> indexes, PhiroPlayToneBrick.Tone toneEnum,
			Formula duration) {
		PhiroPlayToneAction action = action(PhiroPlayToneAction.class);
		action.setSelectedTone(toneEnum);
		action.setSprite(sprite);
		action.setDurationInSeconds(duration);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroMotorMoveForwardActionAction(Sprite sprite, ArrayList<Integer> indexes, PhiroMotorMoveForwardBrick.Motor motorEnum,
			Formula speed) {
		PhiroMotorMoveForwardAction action = action(PhiroMotorMoveForwardAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setSpeed(speed);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroMotorMoveBackwardActionAction(Sprite sprite, ArrayList<Integer> indexes, PhiroMotorMoveBackwardBrick.Motor motorEnum,
			Formula speed) {
		PhiroMotorMoveBackwardAction action = action(PhiroMotorMoveBackwardAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setSpeed(speed);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroRgbLedEyeActionAction(Sprite sprite, ArrayList<Integer> indexes, PhiroRGBLightBrick.Eye eye,
			Formula red, Formula green, Formula blue) {
		PhiroRGBLightAction action = action(PhiroRGBLightAction.class);
		action.setSprite(sprite);
		action.setEyeEnum(eye);
		action.setRed(red);
		action.setGreen(green);
		action.setBlue(blue);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroSendSelectedSensorAction(Sprite sprite, ArrayList<Integer> indexes, int sensorNumber, Action ifAction, Action
			elseAction) {
		PhiroSensorAction action = action(PhiroSensorAction.class);
		action.setSprite(sprite);
		action.setSensor(sensorNumber);
		action.setIfAction(ifAction);
		action.setElseAction(elseAction);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPhiroMotorStopActionAction(Sprite sprite, ArrayList<Integer> indexes, PhiroMotorStopBrick.Motor motorEnum) {
		PhiroMotorStopAction action = action(PhiroMotorStopAction.class);
		action.setMotorEnum(motorEnum);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createMoveNStepsAction(Sprite sprite, ArrayList<Integer> indexes, Formula steps) {
		MoveNStepsAction action = Actions.action(MoveNStepsAction.class);
		action.setSprite(sprite);
		action.setSteps(steps);
		action.setIndexes(indexes);
		return action;
	}

	public Action createNextLookAction(Sprite sprite, ArrayList<Integer> indexes) {
		NextLookAction action = Actions.action(NextLookAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPlaySoundAction(Sprite sprite, ArrayList<Integer> indexes, SoundInfo sound) {
		PlaySoundAction action = Actions.action(PlaySoundAction.class);
		action.setSprite(sprite);
		action.setSound(sound);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPointInDirectionAction(Sprite sprite, ArrayList<Integer> indexes, Formula degrees) {
		PointInDirectionAction action = Actions.action(PointInDirectionAction.class);
		action.setSprite(sprite);
		action.setDegreesInUserInterfaceDimensionUnit(degrees);
		action.setIndexes(indexes);
		return action;
	}

	public Action createPointToAction(Sprite sprite, ArrayList<Integer> indexes, Sprite pointedSprite) {
		PointToAction action = Actions.action(PointToAction.class);
		action.setSprite(sprite);
		action.setPointedSprite(pointedSprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetBrightnessAction(Sprite sprite, ArrayList<Integer> indexes, Formula brightness) {
		SetBrightnessAction action = Actions.action(SetBrightnessAction.class);
		action.setSprite(sprite);
		action.setBrightness(brightness);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetColorAction(Sprite sprite, ArrayList<Integer> indexes, Formula color) {
		SetColorAction action = Actions.action(SetColorAction.class);
		action.setSprite(sprite);
		action.setColor(color);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetTransparencyAction(Sprite sprite, ArrayList<Integer> indexes, Formula transparency) {
		SetTransparencyAction action = Actions.action(SetTransparencyAction.class);
		action.setSprite(sprite);
		action.setTransparency(transparency);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetLookAction(Sprite sprite, ArrayList<Integer> indexes, LookData lookData) {
		SetLookAction action = Actions.action(SetLookAction.class);
		action.setSprite(sprite);
		action.setLookData(lookData);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetSizeToAction(Sprite sprite, ArrayList<Integer> indexes, Formula size) {
		SetSizeToAction action = Actions.action(SetSizeToAction.class);
		action.setSprite(sprite);
		action.setSize(size);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetVolumeToAction(Sprite sprite, ArrayList<Integer> indexes, Formula volume) {
		SetVolumeToAction action = Actions.action(SetVolumeToAction.class);
		action.setVolume(volume);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetXAction(Sprite sprite, ArrayList<Integer> indexes, Formula x) {
		SetXAction action = Actions.action(SetXAction.class);
		action.setSprite(sprite);
		action.setX(x);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetYAction(Sprite sprite, ArrayList<Integer> indexes, Formula y) {
		SetYAction action = Actions.action(SetYAction.class);
		action.setSprite(sprite);
		action.setY(y);
		action.setIndexes(indexes);
		return action;
	}

	public Action createShowAction(Sprite sprite, ArrayList<Integer> indexes) {
		ShowAction action = Actions.action(ShowAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSpeakAction(Sprite sprite, ArrayList<Integer> indexes, Formula text) {
		SpeakAction action = action(SpeakAction.class);
		action.setSprite(sprite);
		action.setText(text);
		action.setIndexes(indexes);
		return action;
	}

	public Action createStopAllSoundsAction(Sprite sprite, ArrayList<Integer> indexes) {
		StopAllSoundsAction action = Actions.action(StopAllSoundsAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createTurnLeftAction(Sprite sprite, ArrayList<Integer> indexes, Formula degrees) {
		TurnLeftAction action = Actions.action(TurnLeftAction.class);
		action.setSprite(sprite);
		action.setDegrees(degrees);
		action.setIndexes(indexes);
		return action;
	}

	public Action createTurnRightAction(Sprite sprite, ArrayList<Integer> indexes, Formula degrees) {
		TurnRightAction action = Actions.action(TurnRightAction.class);
		action.setSprite(sprite);
		action.setDegrees(degrees);
		action.setIndexes(indexes);
		return action;
	}

	public Action createChangeVariableAction(Sprite sprite, ArrayList<Integer> indexes, Formula variableFormula, UserVariable userVariable) {
		ChangeVariableAction action = Actions.action(ChangeVariableAction.class);
		action.setSprite(sprite);
		action.setChangeVariable(variableFormula);
		action.setUserVariable(userVariable);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetVariableAction(Sprite sprite, ArrayList<Integer> indexes, Formula variableFormula, UserVariable userVariable) {
		SetVariableAction action = Actions.action(SetVariableAction.class);
		action.setSprite(sprite);
		action.setChangeVariable(variableFormula);
		action.setUserVariable(userVariable);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDeleteItemOfUserListAction(Sprite sprite, ArrayList<Integer> indexes, Formula userListFormula, UserList userList) {
		DeleteItemOfUserListAction action = action(DeleteItemOfUserListAction.class);
		action.setSprite(sprite);
		action.setFormulaIndexToDelete(userListFormula);
		action.setUserList(userList);
		action.setIndexes(indexes);
		return action;
	}

	public Action createAddItemToUserListAction(Sprite sprite, ArrayList<Integer> indexes, Formula userListFormula, UserList userList) {
		AddItemToUserListAction action = action(AddItemToUserListAction.class);
		action.setSprite(sprite);
		action.setFormulaItemToAdd(userListFormula);
		action.setUserList(userList);
		action.setIndexes(indexes);
		return action;
	}

	public Action createInsertItemIntoUserListAction(Sprite sprite, ArrayList<Integer> indexes, Formula userListFormulaIndexToInsert,
			Formula userListFormulaItemToInsert, UserList userList) {
		InsertItemIntoUserListAction action = action(InsertItemIntoUserListAction.class);
		action.setSprite(sprite);
		action.setFormulaIndexToInsert(userListFormulaIndexToInsert);
		action.setFormulaItemToInsert(userListFormulaItemToInsert);
		action.setUserList(userList);
		action.setIndexes(indexes);
		return action;
	}

	public Action createReplaceItemInUserListAction(Sprite sprite, ArrayList<Integer> indexes, Formula userListFormulaIndexToReplace,
			Formula userListFormulaItemToInsert, UserList userList) {
		ReplaceItemInUserListAction action = action(ReplaceItemInUserListAction.class);
		action.setSprite(sprite);
		action.setFormulaIndexToReplace(userListFormulaIndexToReplace);
		action.setFormulaItemToInsert(userListFormulaItemToInsert);
		action.setUserList(userList);
		action.setIndexes(indexes);
		return action;
	}

	public Action createIfLogicAction(Sprite sprite, ArrayList<Integer> indexes, Formula condition, Action ifAction, Action elseAction) {
		IfLogicAction action = Actions.action(IfLogicAction.class);
		action.setIfAction(ifAction);
		action.setIfCondition(condition);
		action.setElseAction(elseAction);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createRepeatAction(Sprite sprite, ArrayList<Integer> indexes, Formula count, Action repeatedAction) {
		RepeatAction action = Actions.action(RepeatAction.class);
		action.setRepeatCount(count);
		action.setAction(repeatedAction);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createWaitUntilAction(Sprite sprite, ArrayList<Integer> indexes, Formula condition) {
		WaitUntilAction action = Actions.action(WaitUntilAction.class);
		action.setSprite(sprite);
		action.setCondition(condition);
		action.setIndexes(indexes);
		return action;
	}

	public Action createRepeatUntilAction(Sprite sprite, ArrayList<Integer> indexes, Formula condition, Action repeatedAction) {
		RepeatUntilAction action = action(RepeatUntilAction.class);
		action.setRepeatCondition(condition);
		action.setAction(repeatedAction);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDelayAction(Sprite sprite, ArrayList<Integer> indexes, Formula delay) {
		WaitAction action = Actions.action(WaitAction.class);
		action.setSprite(sprite);
		action.setDelay(delay);
		action.setIndexes(indexes);
		return action;
	}

	public Action createForeverAction(Sprite sprite, ArrayList<Integer> indexes, SequenceAction foreverSequence) {
		RepeatAction action = Actions.action(RepeatAction.class);
		action.setIsForeverRepeat(true);
		action.setAction(foreverSequence);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createUserBrickAction(Sprite sprite, ArrayList<Integer> indexes, Action userBrickAction, UserBrick userBrick) {
		UserBrickAction action = action(UserBrickAction.class);
		action.setAction(userBrickAction);
		action.setUserBrick(userBrick);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSequence() {
		return Actions.sequence();
	}

	public Action createSetBounceFactorAction(Sprite sprite, ArrayList<Integer> indexes, Formula bounceFactor) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createTurnRightSpeedAction(Sprite sprite, ArrayList<Integer> indexes, Formula degreesPerSecond) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createTurnLeftSpeedAction(Sprite sprite, ArrayList<Integer> indexes, Formula degreesPerSecond) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createSetVelocityAction(Sprite sprite, ArrayList<Integer> indexes, Formula velocityX, Formula velocityY) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createSetPhysicsObjectTypeAction(Sprite sprite, ArrayList<Integer> indexes, PhysicsObject.Type type) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createSetMassAction(Sprite sprite, ArrayList<Integer> indexes, Formula mass) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createSetGravityAction(Sprite sprite, ArrayList<Integer> indexes, Formula gravityX, Formula gravityY) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createSetFrictionAction(Sprite sprite, ArrayList<Integer> indexes, Formula friction) {
		throw new RuntimeException("No physics action available in non-physics sprite!");
	}

	public Action createDroneTakeOffAndLandAction(Sprite sprite, ArrayList<Integer> indexes) {
		DroneTakeoffAndLandAction action = action(DroneTakeoffAndLandAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveUpAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveUpAction action = action(DroneMoveUpAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveDownAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveDownAction action = action(DroneMoveDownAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveLeftAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveLeftAction action = action(DroneMoveLeftAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveRightAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveRightAction action = action(DroneMoveRightAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveForwardAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveForwardAction action = action(DroneMoveForwardAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneMoveBackwardAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneMoveBackwardAction action = action(DroneMoveBackwardAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneTurnRightAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneTurnRightAction action = action(DroneTurnRightAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneTurnLeftAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneTurnLeftAction action = action(DroneTurnLeftAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneTurnLeftMagnetoAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneTurnLeftWithMagnetometerAction action = action(DroneTurnLeftWithMagnetometerAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneTurnRightMagnetoAction(Sprite sprite, ArrayList<Integer> indexes, Formula seconds, Formula powerInPercent) {
		DroneTurnRightWithMagnetometerAction action = action(DroneTurnRightWithMagnetometerAction.class);
		action.setSprite(sprite);
		action.setDelay(seconds);
		action.setPower(powerInPercent);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDronePlayLedAnimationAction(Sprite sprite, ArrayList<Integer> indexes) {
		DronePlayLedAnimationAction action = action(DronePlayLedAnimationAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneFlipAction(Sprite sprite, ArrayList<Integer> indexes) {
		DroneFlipAction action = action(DroneFlipAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneSwitchCameraAction(Sprite sprite, ArrayList<Integer> indexes) {
		DroneSwitchCameraAction action = action(DroneSwitchCameraAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createDroneGoEmergencyAction(Sprite sprite, ArrayList<Integer> indexes) {
		DroneEmergencyAction action = action(DroneEmergencyAction.class);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetTextAction(Sprite sprite, ArrayList<Integer> indexes, Formula x, Formula y, Formula text) {
		SetTextAction action = action(SetTextAction.class);

		action.setPosition(x, y);
		action.setText(text);
		action.setDuration(5);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createShowTextAction(Sprite sprite, ArrayList<Integer> indexes, Formula x, Formula y, String variableName) {
		ShowTextAction action = action(ShowTextAction.class);
		action.setPosition(x, y);
		action.setVariableName(variableName);
		action.setSprite(sprite);
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		action.setUserBrick(userBrick);
		action.setIndexes(indexes);
		return action;
	}

	public Action createHideTextAction(Sprite sprite, ArrayList<Integer> indexes, String variableName) {
		HideTextAction action = action(HideTextAction.class);
		action.setVariableName(variableName);
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		action.setUserBrick(userBrick);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createTurnFlashOnAction(Sprite sprite, ArrayList<Integer> indexes) {
		FlashAction action = action(FlashAction.class);
		action.turnFlashOn();
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createTurnFlashOffAction(Sprite sprite, ArrayList<Integer> indexes) {
		FlashAction action = action(FlashAction.class);
		action.turnFlashOff();
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createVibrateAction(Sprite sprite, ArrayList<Integer> indexes, Formula duration) {
		VibrateAction action = action(VibrateAction.class);
		action.setSprite(sprite);
		action.setDuration(duration);
		action.setIndexes(indexes);
		return action;
	}

	public Action createUpdateCameraPreviewAction(Sprite sprite, ArrayList<Integer> indexes, CameraManager.CameraState state) {
		CameraBrickAction action = action(CameraBrickAction.class);
		action.setCameraAction(state);
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetFrontCameraAction(Sprite sprite, ArrayList<Integer> indexes) {
		ChooseCameraAction action = action(ChooseCameraAction.class);
		action.setFrontCamera();
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSetBackCameraAction(Sprite sprite, ArrayList<Integer> indexes) {
		ChooseCameraAction action = action(ChooseCameraAction.class);
		action.setBackCamera();
		action.setSprite(sprite);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSendDigitalArduinoValueAction(Sprite sprite, ArrayList<Integer> indexes, Formula pinNumber,
			Formula
					pinValue) {
		ArduinoSendDigitalValueAction action = action(ArduinoSendDigitalValueAction.class);
		action.setSprite(sprite);
		action.setPinNumber(pinNumber);
		action.setPinValue(pinValue);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSendPWMArduinoValueAction(Sprite sprite, ArrayList<Integer> indexes, Formula pinNumber, Formula
			pinValue) {
		ArduinoSendPWMValueAction action = action(ArduinoSendPWMValueAction.class);
		action.setSprite(sprite);
		action.setPinNumber(pinNumber);
		action.setPinValue(pinValue);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSendDigitalRaspiValueAction(Sprite sprite, ArrayList<Integer> indexes, Formula pinNumber,
			Formula pinValue) {
		RaspiSendDigitalValueAction action = action(RaspiSendDigitalValueAction.class);
		action.setSprite(sprite);
		action.setPinNumber(pinNumber);
		action.setPinValue(pinValue);
		action.setIndexes(indexes);
		return action;
	}

	public Action createSendRaspiPwmValueAction(Sprite sprite, ArrayList<Integer> indexes, Formula pinNumber, Formula
			pwmFrequency, Formula pwmPercentage) {
		RaspiPwmAction action = action(RaspiPwmAction.class);
		action.setSprite(sprite);
		action.setPinNumberFormula(pinNumber);
		action.setPwmFrequencyFormula(pwmFrequency);
		action.setPwmPercentageFormula(pwmPercentage);
		action.setIndexes(indexes);
		return action;
	}

	public Action createRaspiIfLogicActionAction(Sprite sprite, ArrayList<Integer> indexes, Formula pinNumber, Action ifAction,
			Action elseAction) {
		RaspiIfLogicAction action = action(RaspiIfLogicAction.class);
		action.setSprite(sprite);
		action.setPinNumber(pinNumber);
		action.setIfAction(ifAction);
		action.setElseAction(elseAction);
		action.setIndexes(indexes);
		return action;
	}
}
