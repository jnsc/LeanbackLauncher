package com.google.android.tvlauncher.appsview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import com.google.android.tvlauncher.analytics.LoggingActivity;

public class AppsViewActivity
  extends LoggingActivity
{
  private static final String TAG = "AppsViewActivity";
  static final String TAG_APPS_VIEW_FRAGMENT = "apps_view_fragment";
  static final String TAG_EDIT_MODE_FRAGMENT = "edit_mode_fragment";
  
  public AppsViewActivity()
  {
    super("Apps");
  }
  
  private void checkIntentForEditMode()
  {
    Intent localIntent = getIntent();
    AppsViewFragment localAppsViewFragment = (AppsViewFragment)getFragmentManager().findFragmentByTag("apps_view_fragment");
    if ((localIntent != null) && (localIntent.getExtras() != null) && (localAppsViewFragment != null))
    {
      if (!localIntent.getExtras().getBoolean("extra_start_customize_apps", false)) {
        break label52;
      }
      localAppsViewFragment.startEditMode(0);
    }
    label52:
    while (!localIntent.getExtras().getBoolean("extra_start_customize_games")) {
      return;
    }
    localAppsViewFragment.startEditMode(1);
  }
  
  private void onShowAppsView()
  {
    AppsViewFragment localAppsViewFragment = new AppsViewFragment();
    getFragmentManager().beginTransaction().add(16908290, localAppsViewFragment, "apps_view_fragment").commit();
  }
  
  public static void startAppsViewActivity(@Nullable Integer paramInteger, Context paramContext)
  {
    Intent localIntent = new Intent("android.intent.action.ALL_APPS");
    if ((paramInteger != null) && (paramInteger.intValue() == 0)) {
      localIntent.putExtra("extra_start_customize_apps", true);
    }
    for (;;)
    {
      try
      {
        paramContext.startActivity(localIntent);
        return;
      }
      catch (ActivityNotFoundException paramInteger)
      {
        Log.e("AppsViewActivity", "AppsViewActivity not found :  " + paramInteger);
      }
      if ((paramInteger != null) && (paramInteger.intValue() == 1)) {
        localIntent.putExtra("extra_start_customize_games", true);
      }
    }
  }
  
  public void finish()
  {
    final Object localObject = getFragmentManager().findFragmentByTag("apps_view_fragment");
    if ((localObject != null) && (((Fragment)localObject).isResumed()))
    {
      Scene localScene = new Scene((ViewGroup)findViewById(16908290));
      localScene.setEnterAction(new Runnable()
      {
        public void run()
        {
          AppsViewActivity.this.getFragmentManager().beginTransaction().remove(localObject).commitNow();
        }
      });
      localObject = new Slide(8388613);
      ((Slide)localObject).addListener(new Transition.TransitionListener()
      {
        public void onTransitionCancel(Transition paramAnonymousTransition) {}
        
        public void onTransitionEnd(Transition paramAnonymousTransition)
        {
          paramAnonymousTransition.removeListener(this);
          AppsViewActivity.this.finish();
        }
        
        public void onTransitionPause(Transition paramAnonymousTransition) {}
        
        public void onTransitionResume(Transition paramAnonymousTransition) {}
        
        public void onTransitionStart(Transition paramAnonymousTransition)
        {
          AppsViewActivity.this.getWindow().setDimAmount(0.0F);
        }
      });
      TransitionManager.go(localScene, (Transition)localObject);
      return;
    }
    super.finish();
  }
  
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    if (paramBundle == null)
    {
      onShowAppsView();
      TransitionManager.go(new Scene((ViewGroup)findViewById(16908290)), new Slide(8388613));
    }
  }
  
  protected void onNewIntent(Intent paramIntent)
  {
    super.onNewIntent(paramIntent);
    setIntent(paramIntent);
  }
  
  protected void onResume()
  {
    super.onResume();
    if ((getFragmentManager().findFragmentByTag("edit_mode_fragment") != null) && (getFragmentManager().getBackStackEntryCount() != 0)) {
      getFragmentManager().popBackStack();
    }
    sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    checkIntentForEditMode();
  }
}


/* Location:              /home/evan/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/tvlauncher/appsview/AppsViewActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */