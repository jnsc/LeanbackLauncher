package android.support.v17.leanback.app;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.support.v17.leanback.R.dimen;
import android.support.v17.leanback.R.id;
import android.support.v17.leanback.R.layout;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter.ViewHolder;
import android.support.v17.leanback.widget.SearchBar;
import android.support.v17.leanback.widget.SearchBar.SearchBarListener;
import android.support.v17.leanback.widget.SearchBar.SearchBarPermissionListener;
import android.support.v17.leanback.widget.SearchOrbView.Colors;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class SearchSupportFragment
  extends Fragment
{
  private static final String ARG_PREFIX = SearchSupportFragment.class.getCanonicalName();
  private static final String ARG_QUERY = ARG_PREFIX + ".query";
  private static final String ARG_TITLE = ARG_PREFIX + ".title";
  static final int AUDIO_PERMISSION_REQUEST_CODE = 0;
  static final boolean DEBUG = false;
  private static final String EXTRA_LEANBACK_BADGE_PRESENT = "LEANBACK_BADGE_PRESENT";
  static final int QUERY_COMPLETE = 2;
  static final int RESULTS_CHANGED = 1;
  static final long SPEECH_RECOGNITION_DELAY_MS = 300L;
  static final String TAG = SearchSupportFragment.class.getSimpleName();
  final ObjectAdapter.DataObserver mAdapterObserver = new ObjectAdapter.DataObserver()
  {
    public void onChanged()
    {
      SearchSupportFragment.this.mHandler.removeCallbacks(SearchSupportFragment.this.mResultsChangedCallback);
      SearchSupportFragment.this.mHandler.post(SearchSupportFragment.this.mResultsChangedCallback);
    }
  };
  boolean mAutoStartRecognition = true;
  private Drawable mBadgeDrawable;
  private ExternalQuery mExternalQuery;
  final Handler mHandler = new Handler();
  private boolean mIsPaused;
  private OnItemViewClickedListener mOnItemViewClickedListener;
  OnItemViewSelectedListener mOnItemViewSelectedListener;
  String mPendingQuery = null;
  private boolean mPendingStartRecognitionWhenPaused;
  private SearchBar.SearchBarPermissionListener mPermissionListener = new SearchBar.SearchBarPermissionListener()
  {
    public void requestAudioPermission()
    {
      PermissionHelper.requestPermissions(SearchSupportFragment.this, new String[] { "android.permission.RECORD_AUDIO" }, 0);
    }
  };
  SearchResultProvider mProvider;
  ObjectAdapter mResultAdapter;
  final Runnable mResultsChangedCallback = new Runnable()
  {
    public void run()
    {
      if ((SearchSupportFragment.this.mRowsSupportFragment != null) && (SearchSupportFragment.this.mRowsSupportFragment.getAdapter() != SearchSupportFragment.this.mResultAdapter) && ((SearchSupportFragment.this.mRowsSupportFragment.getAdapter() != null) || (SearchSupportFragment.this.mResultAdapter.size() != 0)))
      {
        SearchSupportFragment.this.mRowsSupportFragment.setAdapter(SearchSupportFragment.this.mResultAdapter);
        SearchSupportFragment.this.mRowsSupportFragment.setSelectedPosition(0);
      }
      SearchSupportFragment.this.updateSearchBarVisibility();
      SearchSupportFragment localSearchSupportFragment = SearchSupportFragment.this;
      localSearchSupportFragment.mStatus |= 0x1;
      if ((SearchSupportFragment.this.mStatus & 0x2) != 0) {
        SearchSupportFragment.this.updateFocus();
      }
      SearchSupportFragment.this.updateSearchBarNextFocusId();
    }
  };
  RowsSupportFragment mRowsSupportFragment;
  SearchBar mSearchBar;
  private final Runnable mSetSearchResultProvider = new Runnable()
  {
    public void run()
    {
      if (SearchSupportFragment.this.mRowsSupportFragment == null) {
        return;
      }
      ObjectAdapter localObjectAdapter = SearchSupportFragment.this.mProvider.getResultsAdapter();
      if (localObjectAdapter != SearchSupportFragment.this.mResultAdapter) {
        if (SearchSupportFragment.this.mResultAdapter != null) {
          break label196;
        }
      }
      label196:
      for (int i = 1;; i = 0)
      {
        SearchSupportFragment.this.releaseAdapter();
        SearchSupportFragment.this.mResultAdapter = localObjectAdapter;
        if (SearchSupportFragment.this.mResultAdapter != null) {
          SearchSupportFragment.this.mResultAdapter.registerObserver(SearchSupportFragment.this.mAdapterObserver);
        }
        if ((i == 0) || ((SearchSupportFragment.this.mResultAdapter != null) && (SearchSupportFragment.this.mResultAdapter.size() != 0))) {
          SearchSupportFragment.this.mRowsSupportFragment.setAdapter(SearchSupportFragment.this.mResultAdapter);
        }
        SearchSupportFragment.this.executePendingQuery();
        SearchSupportFragment.this.updateSearchBarNextFocusId();
        if (!SearchSupportFragment.this.mAutoStartRecognition) {
          break;
        }
        SearchSupportFragment.this.mHandler.removeCallbacks(SearchSupportFragment.this.mStartRecognitionRunnable);
        SearchSupportFragment.this.mHandler.postDelayed(SearchSupportFragment.this.mStartRecognitionRunnable, 300L);
        return;
      }
      SearchSupportFragment.this.updateFocus();
    }
  };
  private SpeechRecognitionCallback mSpeechRecognitionCallback;
  private SpeechRecognizer mSpeechRecognizer;
  final Runnable mStartRecognitionRunnable = new Runnable()
  {
    public void run()
    {
      SearchSupportFragment.this.mAutoStartRecognition = false;
      SearchSupportFragment.this.mSearchBar.startRecognition();
    }
  };
  int mStatus;
  private String mTitle;
  
  private void applyExternalQuery()
  {
    if ((this.mExternalQuery == null) || (this.mSearchBar == null)) {
      return;
    }
    this.mSearchBar.setSearchQuery(this.mExternalQuery.mQuery);
    if (this.mExternalQuery.mSubmit) {
      submitQuery(this.mExternalQuery.mQuery);
    }
    this.mExternalQuery = null;
  }
  
  public static Bundle createArgs(Bundle paramBundle, String paramString)
  {
    return createArgs(paramBundle, paramString, null);
  }
  
  public static Bundle createArgs(Bundle paramBundle, String paramString1, String paramString2)
  {
    Bundle localBundle = paramBundle;
    if (paramBundle == null) {
      localBundle = new Bundle();
    }
    localBundle.putString(ARG_QUERY, paramString1);
    localBundle.putString(ARG_TITLE, paramString2);
    return localBundle;
  }
  
  private void focusOnResults()
  {
    if ((this.mRowsSupportFragment == null) || (this.mRowsSupportFragment.getVerticalGridView() == null) || (this.mResultAdapter.size() == 0)) {}
    while (!this.mRowsSupportFragment.getVerticalGridView().requestFocus()) {
      return;
    }
    this.mStatus &= 0xFFFFFFFE;
  }
  
  public static SearchSupportFragment newInstance(String paramString)
  {
    SearchSupportFragment localSearchSupportFragment = new SearchSupportFragment();
    localSearchSupportFragment.setArguments(createArgs(null, paramString));
    return localSearchSupportFragment;
  }
  
  private void onSetSearchResultProvider()
  {
    this.mHandler.removeCallbacks(this.mSetSearchResultProvider);
    this.mHandler.post(this.mSetSearchResultProvider);
  }
  
  private void readArguments(Bundle paramBundle)
  {
    if (paramBundle == null) {}
    do
    {
      return;
      if (paramBundle.containsKey(ARG_QUERY)) {
        setSearchQuery(paramBundle.getString(ARG_QUERY));
      }
    } while (!paramBundle.containsKey(ARG_TITLE));
    setTitle(paramBundle.getString(ARG_TITLE));
  }
  
  private void releaseRecognizer()
  {
    if (this.mSpeechRecognizer != null)
    {
      this.mSearchBar.setSpeechRecognizer(null);
      this.mSpeechRecognizer.destroy();
      this.mSpeechRecognizer = null;
    }
  }
  
  private void resultsAvailable()
  {
    if ((this.mStatus & 0x2) != 0) {
      focusOnResults();
    }
    updateSearchBarNextFocusId();
  }
  
  private void setSearchQuery(String paramString)
  {
    this.mSearchBar.setSearchQuery(paramString);
  }
  
  public void displayCompletions(List<String> paramList)
  {
    this.mSearchBar.displayCompletions(paramList);
  }
  
  public void displayCompletions(CompletionInfo[] paramArrayOfCompletionInfo)
  {
    this.mSearchBar.displayCompletions(paramArrayOfCompletionInfo);
  }
  
  void executePendingQuery()
  {
    if ((this.mPendingQuery != null) && (this.mResultAdapter != null))
    {
      String str = this.mPendingQuery;
      this.mPendingQuery = null;
      retrieveResults(str);
    }
  }
  
  public Drawable getBadgeDrawable()
  {
    if (this.mSearchBar != null) {
      return this.mSearchBar.getBadgeDrawable();
    }
    return null;
  }
  
  public Intent getRecognizerIntent()
  {
    boolean bool = true;
    Intent localIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
    localIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
    localIntent.putExtra("android.speech.extra.PARTIAL_RESULTS", true);
    if ((this.mSearchBar != null) && (this.mSearchBar.getHint() != null)) {
      localIntent.putExtra("android.speech.extra.PROMPT", this.mSearchBar.getHint());
    }
    if (this.mBadgeDrawable != null) {}
    for (;;)
    {
      localIntent.putExtra("LEANBACK_BADGE_PRESENT", bool);
      return localIntent;
      bool = false;
    }
  }
  
  public RowsSupportFragment getRowsSupportFragment()
  {
    return this.mRowsSupportFragment;
  }
  
  public String getTitle()
  {
    if (this.mSearchBar != null) {
      return this.mSearchBar.getTitle();
    }
    return null;
  }
  
  public void onCreate(Bundle paramBundle)
  {
    if (this.mAutoStartRecognition) {
      if (paramBundle != null) {
        break label24;
      }
    }
    label24:
    for (boolean bool = true;; bool = false)
    {
      this.mAutoStartRecognition = bool;
      super.onCreate(paramBundle);
      return;
    }
  }
  
  public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle)
  {
    paramLayoutInflater = paramLayoutInflater.inflate(R.layout.lb_search_fragment, paramViewGroup, false);
    this.mSearchBar = ((SearchBar)((FrameLayout)paramLayoutInflater.findViewById(R.id.lb_search_frame)).findViewById(R.id.lb_search_bar));
    this.mSearchBar.setSearchBarListener(new SearchBar.SearchBarListener()
    {
      public void onKeyboardDismiss(String paramAnonymousString)
      {
        SearchSupportFragment.this.queryComplete();
      }
      
      public void onSearchQueryChange(String paramAnonymousString)
      {
        if (SearchSupportFragment.this.mProvider != null)
        {
          SearchSupportFragment.this.retrieveResults(paramAnonymousString);
          return;
        }
        SearchSupportFragment.this.mPendingQuery = paramAnonymousString;
      }
      
      public void onSearchQuerySubmit(String paramAnonymousString)
      {
        SearchSupportFragment.this.submitQuery(paramAnonymousString);
      }
    });
    this.mSearchBar.setSpeechRecognitionCallback(this.mSpeechRecognitionCallback);
    this.mSearchBar.setPermissionListener(this.mPermissionListener);
    applyExternalQuery();
    readArguments(getArguments());
    if (this.mBadgeDrawable != null) {
      setBadgeDrawable(this.mBadgeDrawable);
    }
    if (this.mTitle != null) {
      setTitle(this.mTitle);
    }
    if (getChildFragmentManager().findFragmentById(R.id.lb_results_frame) == null)
    {
      this.mRowsSupportFragment = new RowsSupportFragment();
      getChildFragmentManager().beginTransaction().replace(R.id.lb_results_frame, this.mRowsSupportFragment).commit();
    }
    for (;;)
    {
      this.mRowsSupportFragment.setOnItemViewSelectedListener(new OnItemViewSelectedListener()
      {
        public void onItemSelected(Presenter.ViewHolder paramAnonymousViewHolder, Object paramAnonymousObject, RowPresenter.ViewHolder paramAnonymousViewHolder1, Row paramAnonymousRow)
        {
          SearchSupportFragment.this.updateSearchBarVisibility();
          if (SearchSupportFragment.this.mOnItemViewSelectedListener != null) {
            SearchSupportFragment.this.mOnItemViewSelectedListener.onItemSelected(paramAnonymousViewHolder, paramAnonymousObject, paramAnonymousViewHolder1, paramAnonymousRow);
          }
        }
      });
      this.mRowsSupportFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
      this.mRowsSupportFragment.setExpand(true);
      if (this.mProvider != null) {
        onSetSearchResultProvider();
      }
      return paramLayoutInflater;
      this.mRowsSupportFragment = ((RowsSupportFragment)getChildFragmentManager().findFragmentById(R.id.lb_results_frame));
    }
  }
  
  public void onDestroy()
  {
    releaseAdapter();
    super.onDestroy();
  }
  
  public void onPause()
  {
    releaseRecognizer();
    this.mIsPaused = true;
    super.onPause();
  }
  
  public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if ((paramInt == 0) && (paramArrayOfString.length > 0) && (paramArrayOfString[0].equals("android.permission.RECORD_AUDIO")) && (paramArrayOfInt[0] == 0)) {
      startRecognition();
    }
  }
  
  public void onResume()
  {
    super.onResume();
    this.mIsPaused = false;
    if ((this.mSpeechRecognitionCallback == null) && (this.mSpeechRecognizer == null))
    {
      this.mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
      this.mSearchBar.setSpeechRecognizer(this.mSpeechRecognizer);
    }
    if (this.mPendingStartRecognitionWhenPaused)
    {
      this.mPendingStartRecognitionWhenPaused = false;
      this.mSearchBar.startRecognition();
      return;
    }
    this.mSearchBar.stopRecognition();
  }
  
  public void onStart()
  {
    super.onStart();
    VerticalGridView localVerticalGridView = this.mRowsSupportFragment.getVerticalGridView();
    int i = getResources().getDimensionPixelSize(R.dimen.lb_search_browse_rows_align_top);
    localVerticalGridView.setItemAlignmentOffset(0);
    localVerticalGridView.setItemAlignmentOffsetPercent(-1.0F);
    localVerticalGridView.setWindowAlignmentOffset(i);
    localVerticalGridView.setWindowAlignmentOffsetPercent(-1.0F);
    localVerticalGridView.setWindowAlignment(0);
    localVerticalGridView.setFocusable(false);
    localVerticalGridView.setFocusableInTouchMode(false);
  }
  
  void queryComplete()
  {
    this.mStatus |= 0x2;
    focusOnResults();
  }
  
  void releaseAdapter()
  {
    if (this.mResultAdapter != null)
    {
      this.mResultAdapter.unregisterObserver(this.mAdapterObserver);
      this.mResultAdapter = null;
    }
  }
  
  void retrieveResults(String paramString)
  {
    if (this.mProvider.onQueryTextChange(paramString)) {
      this.mStatus &= 0xFFFFFFFD;
    }
  }
  
  public void setBadgeDrawable(Drawable paramDrawable)
  {
    this.mBadgeDrawable = paramDrawable;
    if (this.mSearchBar != null) {
      this.mSearchBar.setBadgeDrawable(paramDrawable);
    }
  }
  
  public void setOnItemViewClickedListener(OnItemViewClickedListener paramOnItemViewClickedListener)
  {
    if (paramOnItemViewClickedListener != this.mOnItemViewClickedListener)
    {
      this.mOnItemViewClickedListener = paramOnItemViewClickedListener;
      if (this.mRowsSupportFragment != null) {
        this.mRowsSupportFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
      }
    }
  }
  
  public void setOnItemViewSelectedListener(OnItemViewSelectedListener paramOnItemViewSelectedListener)
  {
    this.mOnItemViewSelectedListener = paramOnItemViewSelectedListener;
  }
  
  public void setSearchAffordanceColors(SearchOrbView.Colors paramColors)
  {
    if (this.mSearchBar != null) {
      this.mSearchBar.setSearchAffordanceColors(paramColors);
    }
  }
  
  public void setSearchAffordanceColorsInListening(SearchOrbView.Colors paramColors)
  {
    if (this.mSearchBar != null) {
      this.mSearchBar.setSearchAffordanceColorsInListening(paramColors);
    }
  }
  
  public void setSearchQuery(Intent paramIntent, boolean paramBoolean)
  {
    paramIntent = paramIntent.getStringArrayListExtra("android.speech.extra.RESULTS");
    if ((paramIntent != null) && (paramIntent.size() > 0)) {
      setSearchQuery((String)paramIntent.get(0), paramBoolean);
    }
  }
  
  public void setSearchQuery(String paramString, boolean paramBoolean)
  {
    if (paramString == null) {}
    do
    {
      return;
      this.mExternalQuery = new ExternalQuery(paramString, paramBoolean);
      applyExternalQuery();
    } while (!this.mAutoStartRecognition);
    this.mAutoStartRecognition = false;
    this.mHandler.removeCallbacks(this.mStartRecognitionRunnable);
  }
  
  public void setSearchResultProvider(SearchResultProvider paramSearchResultProvider)
  {
    if (this.mProvider != paramSearchResultProvider)
    {
      this.mProvider = paramSearchResultProvider;
      onSetSearchResultProvider();
    }
  }
  
  public void setSpeechRecognitionCallback(SpeechRecognitionCallback paramSpeechRecognitionCallback)
  {
    this.mSpeechRecognitionCallback = paramSpeechRecognitionCallback;
    if (this.mSearchBar != null) {
      this.mSearchBar.setSpeechRecognitionCallback(this.mSpeechRecognitionCallback);
    }
    if (paramSpeechRecognitionCallback != null) {
      releaseRecognizer();
    }
  }
  
  public void setTitle(String paramString)
  {
    this.mTitle = paramString;
    if (this.mSearchBar != null) {
      this.mSearchBar.setTitle(paramString);
    }
  }
  
  public void startRecognition()
  {
    if (this.mIsPaused)
    {
      this.mPendingStartRecognitionWhenPaused = true;
      return;
    }
    this.mSearchBar.startRecognition();
  }
  
  void submitQuery(String paramString)
  {
    queryComplete();
    if (this.mProvider != null) {
      this.mProvider.onQueryTextSubmit(paramString);
    }
  }
  
  void updateFocus()
  {
    if ((this.mResultAdapter != null) && (this.mResultAdapter.size() > 0) && (this.mRowsSupportFragment != null) && (this.mRowsSupportFragment.getAdapter() == this.mResultAdapter))
    {
      focusOnResults();
      return;
    }
    this.mSearchBar.requestFocus();
  }
  
  void updateSearchBarNextFocusId()
  {
    if ((this.mSearchBar == null) || (this.mResultAdapter == null)) {
      return;
    }
    if ((this.mResultAdapter.size() == 0) || (this.mRowsSupportFragment == null) || (this.mRowsSupportFragment.getVerticalGridView() == null)) {}
    for (int i = 0;; i = this.mRowsSupportFragment.getVerticalGridView().getId())
    {
      this.mSearchBar.setNextFocusDownId(i);
      return;
    }
  }
  
  void updateSearchBarVisibility()
  {
    SearchBar localSearchBar;
    if (this.mRowsSupportFragment != null)
    {
      i = this.mRowsSupportFragment.getSelectedPosition();
      localSearchBar = this.mSearchBar;
      if ((i > 0) && (this.mResultAdapter != null) && (this.mResultAdapter.size() != 0)) {
        break label54;
      }
    }
    label54:
    for (int i = 0;; i = 8)
    {
      localSearchBar.setVisibility(i);
      return;
      i = -1;
      break;
    }
  }
  
  static class ExternalQuery
  {
    String mQuery;
    boolean mSubmit;
    
    ExternalQuery(String paramString, boolean paramBoolean)
    {
      this.mQuery = paramString;
      this.mSubmit = paramBoolean;
    }
  }
  
  public static abstract interface SearchResultProvider
  {
    public abstract ObjectAdapter getResultsAdapter();
    
    public abstract boolean onQueryTextChange(String paramString);
    
    public abstract boolean onQueryTextSubmit(String paramString);
  }
}


/* Location:              /home/evan/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/android/support/v17/leanback/app/SearchSupportFragment.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */