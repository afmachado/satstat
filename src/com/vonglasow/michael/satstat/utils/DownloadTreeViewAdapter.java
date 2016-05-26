/*
 * Copyright (c) 2011, Polidea
 * Copyright © 2013–2016 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vonglasow.michael.satstat.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vonglasow.michael.satstat.R;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * simple item description.
 * 
 */
public class DownloadTreeViewAdapter extends AbstractTreeViewAdapter<RemoteFile> implements RemoteDirListListener {
	private static final String TAG = DownloadTreeViewAdapter.class.getSimpleName();
	
	TreeStateManager<RemoteFile> manager;
	Map<RemoteDirListTask, RemoteFile> listTasks;

    public DownloadTreeViewAdapter(final Activity activity,
            final TreeStateManager<RemoteFile> treeStateManager,
            final int numberOfLevels) {
        super(activity, treeStateManager, numberOfLevels);
        this.manager = treeStateManager;
        listTasks = new HashMap<RemoteDirListTask, RemoteFile>();
    }

    @Override
    public View getNewChildView(final TreeNodeInfo<RemoteFile> treeNodeInfo) {
    	final LinearLayout viewLayout;
    	viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.download_list_item, null);
    	return updateView(viewLayout, treeNodeInfo);
    }

    @Override
    public LinearLayout updateView(final View view,
            final TreeNodeInfo<RemoteFile> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) view;
        RemoteFile rfile = treeNodeInfo.getId();
        final String rfileName = rfile.name;
        
        /*
        final TextView descriptionView = (TextView) viewLayout
                .findViewById(R.id.demo_list_item_description);
        final TextView levelView = (TextView) viewLayout
                .findViewById(R.id.demo_list_item_level);
        descriptionView.setText(getDescription(treeNodeInfo.getId()));
        levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
        final CheckBox box = (CheckBox) viewLayout
                .findViewById(R.id.demo_list_checkbox);
        box.setTag(treeNodeInfo.getId());
        if (treeNodeInfo.isWithChildren()) {
            box.setVisibility(View.GONE);
        } else {
            box.setVisibility(View.VISIBLE);
            box.setChecked(selected.contains(treeNodeInfo.getId()));
        }
        box.setOnCheckedChangeListener(onCheckedChange);
        */

        TextView downloadListItem = (TextView) viewLayout.findViewById(R.id.downloadListItem);
        downloadListItem.setText(rfileName);

        return viewLayout;
    }

    @Override
    public void handleItemClick(final View view, final Object id) {
        final RemoteFile rfile = (RemoteFile) id;
        if (rfile.isDirectory) {
        	if (rfile.children != null)
        		super.handleItemClick(view, id);
        	else {
        		String urlStr = "";
        		try {
        			URL baseUrl = new URL(rfile.baseUrl);
        			URL url = new URL(baseUrl, rfile.name);
        			urlStr = url.toString();
        		} catch (MalformedURLException e) {
        			Log.w(TAG, String.format("Cannot determine URL for %s from %s", rfile.name, rfile.baseUrl));
        			return;
        		}
        		RemoteDirListTask task = new RemoteDirListTask(this, rfile);
        		listTasks.put(task, rfile);
        		task.execute(urlStr);
        	}
        } else {
        	// TODO download file
        }
    }

    @Override
    public long getItemId(final int position) {
        return getTreeId(position).hashCode();
    }

	@Override
	public void onRemoteDirListReady(RemoteDirListTask task, RemoteFile[] rfiles) {
		RemoteFile parent = listTasks.get(task);
		
		for (RemoteFile rf : rfiles)
			manager.addAfterChild(parent, rf, null);

		listTasks.remove(task);
		// TODO handle item click (view will be null, should work)
	}
}