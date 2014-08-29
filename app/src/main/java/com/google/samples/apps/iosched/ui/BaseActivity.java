package com.google.samples.apps.iosched.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.samples.apps.iosched.R;
import com.google.samples.apps.iosched.ui.widget.MultiSwipeRefreshLayout;
import com.google.samples.apps.iosched.ui.widget.SwipeRefreshLayout;
import com.google.samples.apps.iosched.util.ImageLoader;
import com.google.samples.apps.iosched.util.LPreviewUtilsBase;
import com.google.samples.apps.iosched.util.LoginAndAuthHelper;
import com.google.samples.apps.iosched.util.PrefUtils;

import java.util.ArrayList;

import static com.google.samples.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Created by vihaan on 16/8/14.
 */
public abstract class BaseActivity extends Activity implements
        LoginAndAuthHelper.Callbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MultiSwipeRefreshLayout.CanChildScrollUpCallback {

    private static final String TAG = makeLogTag(BaseActivity.class);

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;
    private LPreviewUtilsBase.ActionBarDrawerToggleWrapper mDrawerToggle;

    // allows access to L-Preview APIs through an abstract interface so we can compile with
    // both the L Preview SDK and with the API 19 SDK
    private LPreviewUtilsBase mLPreviewUtils;

    private ObjectAnimator mStatusBarColorAnimator;
    private LinearLayout mAccountListContainer;
    private ViewGroup mDrawerItemsListContainer;
    private Handler mHandler;

    private ImageView mExpandAccountBoxIndicator;
    private boolean mAccountBoxExpanded = false;

    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_MY_SCHEDULE = 0;
    protected static final int NAVDRAWER_ITEM_EXPLORE = 1;
    protected static final int NAVDRAWER_ITEM_MAP = 2;
    protected static final int NAVDRAWER_ITEM_SOCIAL = 3;
    protected static final int NAVDRAWER_ITEM_VIDEO_LIBRARY = 4;
    protected static final int NAVDRAWER_ITEM_SIGN_IN = 5;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 6;
    protected static final int NAVDRAWER_ITEM_EXPERTS_DIRECTORY = 7;
    protected static final int NAVDRAWER_ITEM_PEOPLE_IVE_MET = 8;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_my_schedule,
            R.string.navdrawer_item_explore,
            R.string.navdrawer_item_map,
            R.string.navdrawer_item_social,
            R.string.navdrawer_item_video_library,
            R.string.navdrawer_item_sign_in,
            R.string.navdrawer_item_settings,
            R.string.navdrawer_item_experts_directory,
            R.string.navdrawer_item_people_ive_met
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_drawer_my_schedule,  // My Schedule
            R.drawable.ic_drawer_explore,  // Explore
            R.drawable.ic_drawer_map, // Map
            R.drawable.ic_drawer_social, // Social
            R.drawable.ic_drawer_video_library, // Video Library
            0, // Sign in
            R.drawable.ic_drawer_settings,
            R.drawable.ic_drawer_experts,
            R.drawable.ic_drawer_people_met,
    };

    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // asynctask that performs GCM registration in the backgorund
    private AsyncTask<Void, Void, Void> mGCMRegisterTask;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;

    // data bootstrap thread. Data bootstrap is the process of initializing the database
    // with the data cache that ships with the app.
    Thread mDataBootstrapThread = null;

    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private boolean mManualSyncRequest;

    private int mThemedStatusBarColor;
    private int mProgressBarTopWhenActionBarShown;
    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private ImageLoader mImageLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AnalyticsManager.initializeAnalyticsTracker(getApplicationContext());

        PrefUtils.init(this);

        // Check if the EULA has been accepted; if not, show it.
        if (!PrefUtils.isTosAccepted(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

    }

}