/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.iosched.ui;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.ListPreloader;
import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.ui.widget.CollectionView;
import com.google.samples.apps.iosched.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link android.app.ListFragment} showing a list of sessions. The fragment arguments
 * indicate what is the list of sessions to show. It may be a set of tag
 * filters or a search query.
 */
public class SessionsFragment extends Fragment        {


    private static final int ROWS_TO_PRELOAD = 2;


    private boolean mSessionDataIsFullReload = false;

    private ImageLoader mImageLoader;
    private int mDefaultSessionColor;

    private CollectionView mCollectionView;
    private TextView mEmptyView;
    private View mLoadingView;
//    private TagMetadata mTagMetadata = null;

    private Preloader mPreloader;

    private Cursor mCursor;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_sessions, container, false);
        mCollectionView = (CollectionView) root.findViewById(R.id.sessions_collection_view);
        mPreloader = new Preloader(ROWS_TO_PRELOAD);
        mCollectionView.setOnScrollListener(mPreloader);
        mEmptyView = (TextView) root.findViewById(R.id.empty_text);
        mLoadingView = root.findViewById(R.id.loading);
        return root;
    }
    private class Preloader extends ListPreloader<String> {

        private int[] photoDimens;
        private int displayCols;

        public Preloader(int maxPreload) {
            super(maxPreload);
        }

        public void setDisplayCols(int displayCols) {
            this.displayCols = displayCols;
        }

        public boolean isDimensSet() {
            return photoDimens != null;
        }

        public void setDimens(int width, int height) {
            if (photoDimens == null) {
                photoDimens = new int[] { width, height };
            }
        }

        @Override
        protected int[] getDimensions(String s) {
            return photoDimens;
        }

        @Override
        protected List<String> getItems(int start, int end) {
            // Our start and end are rows, we need to adjust them into data columns
            // The keynote is 1 row with 1 data item, so we need to adjust.
            int keynoteDataOffset = (displayCols - 1);
            int dataStart = start * displayCols - keynoteDataOffset;
            int dataEnd = end * displayCols - keynoteDataOffset;
            List<String> urls = new ArrayList<String>();
            if (mCursor != null) {
                for (int i = dataStart; i < dataEnd; i++) {
                    if (mCursor.moveToPosition(i)) {
                        urls.add(mCursor.getString(SessionsQuery.PHOTO_URL));
                    }
                }
            }
            return urls;
        }

        @Override
        protected GenericRequestBuilder getRequestBuilder(String url) {
            return mImageLoader.beginImageLoad(url, null, true /*crop*/);
        }
    }

    private interface SessionsQuery {
        int NORMAL_TOKEN = 0x1;
        int SEARCH_TOKEN = 0x3;

//        String[] NORMAL_PROJECTION = {
//                BaseColumns._ID,
//                ScheduleContract.Sessions.SESSION_ID,
//                ScheduleContract.Sessions.SESSION_TITLE,
//                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE,
//                ScheduleContract.Sessions.SESSION_START,
//                ScheduleContract.Sessions.SESSION_END,
//                ScheduleContract.Rooms.ROOM_NAME,
//                ScheduleContract.Rooms.ROOM_ID,
//                ScheduleContract.Sessions.SESSION_HASHTAG,
//                ScheduleContract.Sessions.SESSION_URL,
//                ScheduleContract.Sessions.SESSION_LIVESTREAM_URL,
//                ScheduleContract.Sessions.SESSION_TAGS,
//                ScheduleContract.Sessions.SESSION_SPEAKER_NAMES,
//                ScheduleContract.Sessions.SESSION_ABSTRACT,
//                ScheduleContract.Sessions.SESSION_COLOR,
//                ScheduleContract.Sessions.SESSION_PHOTO_URL,
//        };
//
//        String[] SEARCH_PROJECTION = {
//                BaseColumns._ID,
//                ScheduleContract.Sessions.SESSION_ID,
//                ScheduleContract.Sessions.SESSION_TITLE,
//                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE,
//                ScheduleContract.Sessions.SESSION_START,
//                ScheduleContract.Sessions.SESSION_END,
//                ScheduleContract.Rooms.ROOM_NAME,
//                ScheduleContract.Rooms.ROOM_ID,
//                ScheduleContract.Sessions.SESSION_HASHTAG,
//                ScheduleContract.Sessions.SESSION_URL,
//                ScheduleContract.Sessions.SESSION_LIVESTREAM_URL,
//                ScheduleContract.Sessions.SESSION_TAGS,
//                ScheduleContract.Sessions.SESSION_SPEAKER_NAMES,
//                ScheduleContract.Sessions.SESSION_ABSTRACT,
//                ScheduleContract.Sessions.SESSION_COLOR,
//                ScheduleContract.Sessions.SESSION_PHOTO_URL,
//                ScheduleContract.Sessions.SEARCH_SNIPPET,
//        };


        int _ID = 0;
        int SESSION_ID = 1;
        int TITLE = 2;
        int IN_MY_SCHEDULE = 3;
        int SESSION_START = 4;
        int SESSION_END = 5;
        int ROOM_NAME = 6;
        int ROOM_ID = 7;
        int HASHTAGS = 8;
        int URL = 9;
        int LIVESTREAM_URL = 10;
        int TAGS = 11;
        int SPEAKER_NAMES = 12;
        int ABSTRACT = 13;
        int COLOR = 14;
        int PHOTO_URL = 15;
        int SNIPPET = 16;
    }

}
