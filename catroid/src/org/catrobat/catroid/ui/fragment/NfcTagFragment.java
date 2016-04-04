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
package org.catrobat.catroid.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.NfcTagData;
import org.catrobat.catroid.content.NfcDataHistory;
import org.catrobat.catroid.content.commands.NfcDataCommands;
import org.catrobat.catroid.nfc.NfcHandler;
import org.catrobat.catroid.ui.BottomBar;
import org.catrobat.catroid.ui.NfcTagViewHolder;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.adapter.NfcTagAdapter;
import org.catrobat.catroid.ui.adapter.NfcTagBaseAdapter;
import org.catrobat.catroid.ui.controller.NfcTagController;
import org.catrobat.catroid.ui.dialogs.CustomAlertDialogBuilder;
import org.catrobat.catroid.ui.dialogs.DeleteNfcTagDialog;
import org.catrobat.catroid.ui.dialogs.RenameNfcTagDialog;
import org.catrobat.catroid.utils.ToastUtil;
import org.catrobat.catroid.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public class NfcTagFragment extends ScriptActivityFragment implements NfcTagBaseAdapter.OnNfcTagEditListener, Dialog.OnKeyListener {

	public static final String TAG = NfcTagFragment.class.getSimpleName();

	private static int selectedNfcTagPosition = Constants.NO_POSITION;

	private static String actionModeTitle;

	private static String singleItemAppendixDeleteActionMode;
	private static String multipleItemAppendixDeleteActionMode;

	private ListView listView;

	private NfcTagDeletedReceiver nfcTagDeletedReceiver;
	private NfcTagRenamedReceiver nfcTagRenamedReceiver;
	private NfcTagCopiedReceiver nfcTagCopiedReceiver;

	private NfcTagsListInitReceiver nfcTagsListInitReceiver;

	private ActionMode actionMode;
	private View selectAllActionModeButton;

	private NfcTagBaseAdapter adapter;
	private List<NfcTagData> nfcTagDataList;
	private NfcTagData selectedNfcTag;

	private boolean isRenameActionMode;
	private boolean isResultHandled = false;

	NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;

	private OnNfcTagDataListChangedAfterNewListener nfcTagDataListChangedAfterNewListener;

	public void setOnNfcTagDataListChangedAfterNewListener(OnNfcTagDataListChangedAfterNewListener listener) {
		nfcTagDataListChangedAfterNewListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
		pendingIntent = PendingIntent.getActivity(getActivity(), 0,
				new Intent(getActivity(), getActivity().getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
			ToastUtil.showError(getActivity(), R.string.nfc_not_activated);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
				startActivity(intent);
			} else {
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(intent);
			}
		} else if (nfcAdapter == null) {
			ToastUtil.showError(getActivity(), R.string.no_nfc_available);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_nfctags, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView = getListView();
		if (listView != null) {
			registerForContextMenu(listView);
		}

		if (savedInstanceState != null) {
			selectedNfcTag = (NfcTagData) savedInstanceState
					.getSerializable(NfcTagController.BUNDLE_ARGUMENTS_SELECTED_NFCTAG);
		}

		try {
			nfcTagDataList = ProjectManager.getInstance().getCurrentSprite().getNfcTagList();
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		}

		adapter = new NfcTagAdapter(getActivity(), R.layout.fragment_nfctag_nfctaglist_item,
				R.id.fragment_nfctag_item_title_text_view, nfcTagDataList, false);

		adapter.setOnNfcTagEditListener(this);
		setListAdapter(adapter);
		((NfcTagAdapter) adapter).setNfcTagFragment(this);

		Utils.loadProjectIfNeeded(getActivity());
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.copy).setVisible(true);
		menu.findItem(R.id.backpack).setVisible(false);
		menu.findItem(R.id.cut).setVisible(false);
		menu.findItem(R.id.show_details).setVisible(true);
		/*
		menu.findItem(R.id.settings).setVisible(true);
		menu.findItem(R.id.context_menu_move_up).setVisible(true);
		menu.findItem(R.id.context_menu_move_down).setVisible(true);
		menu.findItem(R.id.context_menu_move_to_top).setVisible(true);
		menu.findItem(R.id.context_menu_move_to_bottom).setVisible(true);
		*/

		MenuItem undo = menu.findItem(R.id.menu_undo);
		if (!getHistory().isUndoable()) {
			undo.setIcon(R.drawable.icon_undo_disabled);
			undo.setEnabled(false);
		} else {
			undo.setIcon(R.drawable.icon_undo);
			undo.setEnabled(true);
		}

		MenuItem redo = menu.findItem(R.id.menu_redo);
		if (!getHistory().isRedoable()) {
			redo.setIcon(R.drawable.icon_redo_disabled);
			redo.setEnabled(false);
		} else {
			redo.setIcon(R.drawable.icon_redo);
			redo.setEnabled(true);
		}

		undo.setVisible(true);
		redo.setVisible(true);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(NfcTagController.BUNDLE_ARGUMENTS_SELECTED_NFCTAG, selectedNfcTag);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		initClickListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		BottomBar.showBottomBar(getActivity());
		BottomBar.showPlayButton(getActivity());
		BottomBar.hideAddButton(getActivity());

		if (!Utils.checkForExternalStorageAvailableAndDisplayErrorIfNot(getActivity())) {
			return;
		}

		if (nfcTagRenamedReceiver == null) {
			nfcTagRenamedReceiver = new NfcTagRenamedReceiver();
		}

		if (nfcTagDeletedReceiver == null) {
			nfcTagDeletedReceiver = new NfcTagDeletedReceiver();
		}

		if (nfcTagCopiedReceiver == null) {
			nfcTagCopiedReceiver = new NfcTagCopiedReceiver();
		}

		if (nfcTagsListInitReceiver == null) {
			nfcTagsListInitReceiver = new NfcTagsListInitReceiver();
		}

		IntentFilter intentFilterRenameNfcTag = new IntentFilter(ScriptActivity.ACTION_NFCTAG_RENAMED);
		getActivity().registerReceiver(nfcTagRenamedReceiver, intentFilterRenameNfcTag);

		IntentFilter intentFilterDeleteNfcTag = new IntentFilter(ScriptActivity.ACTION_NFCTAG_DELETED);
		getActivity().registerReceiver(nfcTagDeletedReceiver, intentFilterDeleteNfcTag);

		IntentFilter intentFilterCopyNfcTag = new IntentFilter(ScriptActivity.ACTION_NFCTAG_COPIED);
		getActivity().registerReceiver(nfcTagCopiedReceiver, intentFilterCopyNfcTag);

		IntentFilter intentFilterNfcTagsListInit = new IntentFilter(ScriptActivity.ACTION_NFCTAGS_LIST_INIT);
		getActivity().registerReceiver(nfcTagsListInitReceiver, intentFilterNfcTagsListInit);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());

		setShowDetails(settings.getBoolean(NfcTagController.SHARED_PREFERENCE_NAME, false));

		if (nfcAdapter != null) {
			Log.d(TAG, "onResume()enableForegroundDispatch()");
			nfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, null);
		}
	}

	public void onNewIntent(Intent intent) {
		Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
		Log.d(TAG, "activity:" + getActivity().getClass().getSimpleName());
		Log.d(TAG, "got intent:" + intent.getAction());
		String uid = NfcHandler.getUid(intent);
		if (uid != null) {
			NfcTagData newNfcTagData = new NfcTagData();
			String newTagName = Utils.getUniqueNfcTagName(getString(R.string.default_tag_name));
			newNfcTagData.setNfcTagName(newTagName);
			newNfcTagData.setNfcTagUid(uid);
			ArrayList<NfcTagData> toAdd = new ArrayList<>();
			toAdd.add(newNfcTagData);
			NfcDataCommands.AddNfcCommand command = new NfcDataCommands.AddNfcCommand(toAdd);
			command.execute();
			getHistory().add(command);
			getActivity().invalidateOptionsMenu();
			adapter.notifyDataSetChanged();
			//getActivity().setIntent(new Intent());
		} else {
			Log.d(TAG, "no nfc tag found");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (nfcAdapter != null) {
			Log.d(TAG, "onPause()disableForegroundDispatch()");
			nfcAdapter.disableForegroundDispatch(getActivity());
		}

		ProjectManager projectManager = ProjectManager.getInstance();
		if (projectManager.getCurrentProject() != null) {
			projectManager.saveProject(getActivity().getApplicationContext());
		}

		adapter.notifyDataSetChanged();

		if (nfcTagRenamedReceiver != null) {
			getActivity().unregisterReceiver(nfcTagRenamedReceiver);
		}

		if (nfcTagDeletedReceiver != null) {
			getActivity().unregisterReceiver(nfcTagDeletedReceiver);
		}

		if (nfcTagCopiedReceiver != null) {
			getActivity().unregisterReceiver(nfcTagCopiedReceiver);
		}

		if (nfcTagsListInitReceiver != null) {
			getActivity().unregisterReceiver(nfcTagsListInitReceiver);
		}

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean(NfcTagController.SHARED_PREFERENCE_NAME, getShowDetails());
		editor.commit();
	}

	@Override
	public boolean getShowDetails() {
		// TODO CHANGE THIS!!! (was just a quick fix)
		if (adapter != null) {
			return adapter.getShowDetails();
		} else {
			return false;
		}
	}

	@Override
	public void setShowDetails(boolean showDetails) {
		// TODO CHANGE THIS!!! (was just a quick fix)
		if (adapter != null) {
			adapter.setShowDetails(showDetails);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void setSelectMode(int selectMode) {
		adapter.setSelectMode(selectMode);
		adapter.notifyDataSetChanged();
	}

	@Override
	public int getSelectMode() {
		return adapter.getSelectMode();
	}

	@Override
	public void startCopyActionMode() {
		if (actionMode == null) {
			actionMode = getActivity().startActionMode(copyModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = false;
		}
	}

	@Override
	public void startRenameActionMode() {
		if (actionMode == null) {
			actionMode = getActivity().startActionMode(renameModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = true;
		}
	}

	@Override
	public void startDeleteActionMode() {
		if (actionMode == null) {
			actionMode = getActivity().startActionMode(deleteModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = false;
		}
	}

	@Override
	public void startBackPackActionMode() {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onNfcTagEdit(View view) {
	}

	public void onNfcTagChecked() {
		if (isRenameActionMode || actionMode == null) {
			return;
		}

		updateActionModeTitle();
		Utils.setSelectAllActionModeButtonVisibility(selectAllActionModeButton,
				adapter.getCount() > 0 && adapter.getAmountOfCheckedItems() != adapter.getCount());
	}

	private void updateActionModeTitle() {
		int numberOfSelectedItems = adapter.getAmountOfCheckedItems();

		if (numberOfSelectedItems == 0) {
			actionMode.setTitle(actionModeTitle);
		} else {
			String appendix = multipleItemAppendixDeleteActionMode;

			if (numberOfSelectedItems == 1) {
				appendix = singleItemAppendixDeleteActionMode;
			}

			String numberOfItems = Integer.toString(numberOfSelectedItems);
			String completeTitle = actionModeTitle + " " + numberOfItems + " " + appendix;

			int titleLength = actionModeTitle.length();

			Spannable completeSpannedTitle = new SpannableString(completeTitle);
			completeSpannedTitle.setSpan(
					new ForegroundColorSpan(getResources().getColor(R.color.actionbar_title_color)), titleLength + 1,
					titleLength + (1 + numberOfItems.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			actionMode.setTitle(completeSpannedTitle);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		selectedNfcTag = adapter.getItem(selectedNfcTagPosition);
		menu.setHeaderTitle(selectedNfcTag.getNfcTagName());
		adapter.addCheckedItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position);

		getActivity().getMenuInflater().inflate(R.menu.context_menu_default, menu);
		menu.findItem(R.id.context_menu_copy).setVisible(true);
		menu.findItem(R.id.context_menu_unpacking).setVisible(false);
		menu.findItem(R.id.context_menu_backpack).setVisible(false);

		menu.findItem(R.id.context_menu_move_up).setVisible(true);
		menu.findItem(R.id.context_menu_move_down).setVisible(true);
		menu.findItem(R.id.context_menu_move_to_top).setVisible(true);
		menu.findItem(R.id.context_menu_move_to_bottom).setVisible(true);

		menu.findItem(R.id.context_menu_move_down).setEnabled(selectedNfcTagPosition != nfcTagDataList.size() - 1);
		menu.findItem(R.id.context_menu_move_to_bottom).setEnabled(selectedNfcTagPosition != nfcTagDataList.size() - 1);

		menu.findItem(R.id.context_menu_move_up).setEnabled(selectedNfcTagPosition != 0);
		menu.findItem(R.id.context_menu_move_to_top).setEnabled(selectedNfcTagPosition != 0);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.context_menu_backpack:
				break;

			case R.id.context_menu_copy:
				NfcTagData newNfcTagData = NfcTagController.getInstance().copyNfcTag(selectedNfcTag, nfcTagDataList,
						adapter);
				ArrayList<NfcTagData> toAdd = new ArrayList<>();
				toAdd.add(newNfcTagData);
				NfcDataCommands.AddNfcCommand command = new NfcDataCommands.AddNfcCommand(toAdd);
				getHistory().add(command);
				getActivity().invalidateOptionsMenu();
				updateNfcTagAdapter(newNfcTagData);
				break;

			case R.id.context_menu_cut:
				break;

			case R.id.context_menu_insert_below:
				break;

			case R.id.context_menu_move:
				break;

			case R.id.context_menu_rename:
				showRenameDialog();
				break;

			case R.id.context_menu_delete:
				deleteNfcTags();
				break;

			case R.id.context_menu_move_down:
				moveTagDataDown();
				break;
			case R.id.context_menu_move_up:
				moveTagDataUp();
				break;
			case R.id.context_menu_move_to_bottom:
				moveTagDataToBottom();
				break;
			case R.id.context_menu_move_to_top:
				moveTagDataToTop();
		}
		return super.onContextItemSelected(item);
	}

	private void updateNfcTagAdapter(NfcTagData newNfcTagData) {

		if (nfcTagDataListChangedAfterNewListener != null) {
			nfcTagDataListChangedAfterNewListener.onNfcTagDataListChangedAfterNew(newNfcTagData);
		}

		//scroll down the list to the new item:
		final ListView listView = getListView();
		listView.post(new Runnable() {
			@Override
			public void run() {
				listView.setSelection(listView.getCount() - 1);
			}
		});

		if (isResultHandled) {
			isResultHandled = false;

			ScriptActivity scriptActivity = (ScriptActivity) getActivity();
			if (scriptActivity.getIsNfcTagFragmentFromWhenNfcBrickNew()
					&& scriptActivity.getIsNfcTagFragmentHandleAddButtonHandled()) {
				NfcTagController.getInstance().switchToScriptFragment(this);
			}
		}
	}

	@Override
	public void handleAddButton() {
		//Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	}

	@Override
	public void startUndoActionMode() {
		try {
			getHistory().undo();
		} catch (Exception exception) {
			ToastUtil.showError(getActivity(), getString(R.string.error_undo));
			Log.e(TAG, Log.getStackTraceString(exception));
		}
		adapter.notifyDataSetChanged();
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void startRedoActionMode() {
		try {
			getHistory().redo();
		} catch (Exception exception) {
			ToastUtil.showError(getActivity(), getString(R.string.error_redo));
			Log.e(TAG, Log.getStackTraceString(exception));
		}
		adapter.notifyDataSetChanged();
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void showRenameDialog() {
		RenameNfcTagDialog renameNfcTagDialog = RenameNfcTagDialog.newInstance(selectedNfcTag.getNfcTagName());
		renameNfcTagDialog.show(getFragmentManager(), RenameNfcTagDialog.DIALOG_FRAGMENT_TAG);
	}

	@Override
	protected void showDeleteDialog() {
		DeleteNfcTagDialog deleteNfcTagDialog = DeleteNfcTagDialog.newInstance(selectedNfcTagPosition);
		deleteNfcTagDialog.show(getFragmentManager(), DeleteNfcTagDialog.DIALOG_FRAGMENT_TAG);
	}

	private void moveTagDataDown() {
		NfcDataCommands.MoveNfcCommand command = new NfcDataCommands.MoveNfcCommand(selectedNfcTagPosition + 1, selectedNfcTagPosition);
		command.execute();
		getHistory().add(command);
		getActivity().invalidateOptionsMenu();
		adapter.notifyDataSetChanged();
	}

	private void moveTagDataUp() {
		NfcDataCommands.MoveNfcCommand command = new NfcDataCommands.MoveNfcCommand(selectedNfcTagPosition - 1, selectedNfcTagPosition);
		command.execute();
		getHistory().add(command);
		getActivity().invalidateOptionsMenu();
		adapter.notifyDataSetChanged();
	}

	private void moveTagDataToBottom() {
		NfcDataCommands.MoveNfcToBottomCommand command = new NfcDataCommands.MoveNfcToBottomCommand(selectedNfcTagPosition);
		command.execute();
		getHistory().add(command);
		getActivity().invalidateOptionsMenu();
		adapter.notifyDataSetChanged();
	}

	private void moveTagDataToTop() {
		NfcDataCommands.MoveNfcToTopCommand command = new NfcDataCommands.MoveNfcToTopCommand(selectedNfcTagPosition);
		command.execute();
		getHistory().add(command);
		getActivity().invalidateOptionsMenu();
		adapter.notifyDataSetChanged();
	}

	private class NfcTagRenamedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_NFCTAG_RENAMED)) {
				String newTagName = intent.getExtras().getString(RenameNfcTagDialog.EXTRA_NEW_NFCTAG_TITLE);

				if (newTagName != null && !newTagName.equalsIgnoreCase("") && !newTagName.equalsIgnoreCase(context.getString(R.string.brick_when_nfc_default_all))) {
					NfcDataCommands.RenameNfcCommand command = new NfcDataCommands.RenameNfcCommand(selectedNfcTag,
							newTagName);
					command.execute();
					getHistory().add(command);
					getActivity().invalidateOptionsMenu();
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	private class NfcTagDeletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_NFCTAG_DELETED)) {
				adapter.notifyDataSetChanged();
				getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
			}
		}
	}

	private class NfcTagCopiedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ScriptActivity.ACTION_NFCTAG_COPIED)) {
				adapter.notifyDataSetChanged();
				getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
			}
		}
	}

	private void addSelectAllActionModeButton(ActionMode mode, Menu menu) {
		selectAllActionModeButton = Utils.addSelectAllActionModeButton(getActivity().getLayoutInflater(), mode,
				menu);
		selectAllActionModeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				for (int position = 0; position < nfcTagDataList.size(); position++) {
					adapter.addCheckedItem(position);
				}
				adapter.notifyDataSetChanged();
				onNfcTagChecked();
			}
		});
	}

	private ActionMode.Callback renameModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_SINGLE);
			mode.setTitle(R.string.rename);

			setActionModeActive(true);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			((NfcTagAdapter) adapter).onDestroyActionModeRename(mode, listView);
		}
	};

	private ActionMode.Callback copyModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			actionModeTitle = getString(R.string.copy);
			singleItemAppendixDeleteActionMode = getString(R.string.nfctag);
			multipleItemAppendixDeleteActionMode = getString(R.string.nfctags);

			mode.setTitle(actionModeTitle);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			Iterator<Integer> iterator = adapter.getCheckedItems().iterator();
			ArrayList<NfcTagData> toAdd = new ArrayList<>();

			while (iterator.hasNext()) {
				int position = iterator.next();
				NfcTagData newNfcTagData = NfcTagController.getInstance().copyNfcTag(nfcTagDataList.get(position),
						nfcTagDataList,
						adapter);
				toAdd.add(newNfcTagData);
			}
			if (toAdd.isEmpty()) return;
			NfcDataCommands.AddNfcCommand command = new NfcDataCommands.AddNfcCommand(toAdd);
			getHistory().add(command);
			getActivity().invalidateOptionsMenu();
			clearCheckedNfcTagsAndEnableButtons();
		}
	};

	private ActionMode.Callback deleteModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			actionModeTitle = getString(R.string.delete);
			singleItemAppendixDeleteActionMode = getString(R.string.nfctag);
			multipleItemAppendixDeleteActionMode = getString(R.string.nfctags);

			mode.setTitle(R.string.delete);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (adapter.getAmountOfCheckedItems() == 0) {
				clearCheckedNfcTagsAndEnableButtons();
			} else {
				deleteNfcTags();
			}
		}
	};

	private void initClickListener() {
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				selectedNfcTagPosition = position;
				return false;
			}
		});
	}

	private void deleteNfcTags() {
		SortedSet<Integer> checkedNfcTags = adapter.getCheckedItems();
		Iterator<Integer> iterator = checkedNfcTags.iterator();
		ArrayList<NfcTagData> toDelete = new ArrayList<>();
		while (iterator.hasNext()) {
			int position = iterator.next();
			toDelete.add(nfcTagDataList.get(position));
		}
		if (toDelete.isEmpty()) return;
		NfcDataCommands.DeleteNfcCommand command = new NfcDataCommands.DeleteNfcCommand(toDelete);
		command.execute();
		getHistory().add(command);
		getActivity().invalidateOptionsMenu();
		getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_NFCTAG_DELETED));
		clearCheckedNfcTagsAndEnableButtons();
	}

	public void clearCheckedNfcTagsAndEnableButtons() {
		setSelectMode(ListView.CHOICE_MODE_NONE);
		adapter.clearCheckedItems();

		actionMode = null;
		setActionModeActive(false);

		registerForContextMenu(listView);
		BottomBar.showBottomBar(getActivity());
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				ScriptActivity scriptActivity = (ScriptActivity) getActivity();
				if (scriptActivity.getIsNfcTagFragmentFromWhenNfcBrickNew()) {
					NfcTagController.getInstance().switchToScriptFragment(this);
					BottomBar.showAddButton(getActivity());
					return true;
				}
			default:
				break;
		}
		return false;
	}

	public View getView(int position, View convertView) {
		NfcTagViewHolder holder;

		if (convertView == null) {
			convertView = View.inflate(getActivity(), R.layout.fragment_nfctag_nfctaglist_item, null);

			holder = new NfcTagViewHolder();
			holder.scanNewTagButton = (ImageButton) convertView.findViewById(R.id.fragment_nfctag_item_image_button);
			holder.scanNewTagButton.setImageResource(R.drawable.ic_program_menu_nfc);
			holder.scanNewTagButton.setContentDescription(getString(R.string.nfctag_scan));

			holder.nfcTagFragmentButtonLayout = (LinearLayout) convertView
					.findViewById(R.id.fragment_nfctag_item_main_linear_layout);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.fragment_nfctag_item_checkbox);
			holder.titleTextView = (TextView) convertView.findViewById(R.id.fragment_nfctag_item_title_text_view);

			holder.nfcTagUidPrefixTextView = (TextView) convertView
					.findViewById(R.id.fragment_nfctag_item_uid_prefix_text_view);
			holder.nfcTagUidTextView = (TextView) convertView.findViewById(R.id.fragment_nfctag_item_uid_text_view);
			holder.nfcTagDetailsLinearLayout = (LinearLayout) convertView.findViewById(R.id.fragment_nfctag_item_detail_linear_layout);

			convertView.setTag(holder);
		} else {
			holder = (NfcTagViewHolder) convertView.getTag();
		}

		NfcTagController controller = NfcTagController.getInstance();
		controller.updateNfcTagLogic(position, holder, adapter);
		return convertView;
	}

	public interface OnNfcTagDataListChangedAfterNewListener {

		void onNfcTagDataListChangedAfterNew(NfcTagData nfcTagData);
	}

	public NfcTagDeletedReceiver getNfcTagDeletedReceiver() {
		return nfcTagDeletedReceiver;
	}

	public void setNfcTagDeletedReceiver(NfcTagDeletedReceiver nfcTagDeletedReceiver) {
		this.nfcTagDeletedReceiver = nfcTagDeletedReceiver;
	}

	public NfcTagRenamedReceiver getNfcTagRenamedReceiver() {
		return nfcTagRenamedReceiver;
	}

	public void setNfcTagRenamedReceiver(NfcTagRenamedReceiver nfcTagRenamedReceiver) {
		this.nfcTagRenamedReceiver = nfcTagRenamedReceiver;
	}

	public NfcTagCopiedReceiver getNfcTagCopiedReceiver() {
		return nfcTagCopiedReceiver;
	}

	public void setNfcTagCopiedReceiver(NfcTagCopiedReceiver nfcTagCopiedReceiver) {
		this.nfcTagCopiedReceiver = nfcTagCopiedReceiver;
	}

	public void setSelectedNfcTagData(NfcTagData selectedNfcTagData) {
		this.selectedNfcTag = selectedNfcTagData;
	}

	public List<NfcTagData> getNfcTagDataList() {
		return nfcTagDataList;
	}

	private class NfcTagsListInitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_NFCTAGS_LIST_INIT)) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	private NfcDataHistory getHistory() {
		return NfcDataHistory.getInstance(ProjectManager.getInstance().getCurrentSprite());
	}
}
