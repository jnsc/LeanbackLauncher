package android.support.v7.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleRes;
import android.support.v7.appcompat.R.style;
import android.view.LayoutInflater;

@RestrictTo({android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP})
public class ContextThemeWrapper
  extends ContextWrapper
{
  private LayoutInflater mInflater;
  private Configuration mOverrideConfiguration;
  private Resources mResources;
  private Resources.Theme mTheme;
  private int mThemeResource;
  
  public ContextThemeWrapper()
  {
    super(null);
  }
  
  public ContextThemeWrapper(Context paramContext, @StyleRes int paramInt)
  {
    super(paramContext);
    this.mThemeResource = paramInt;
  }
  
  public ContextThemeWrapper(Context paramContext, Resources.Theme paramTheme)
  {
    super(paramContext);
    this.mTheme = paramTheme;
  }
  
  private Resources getResourcesInternal()
  {
    if (this.mResources == null)
    {
      if (this.mOverrideConfiguration != null) {
        break label27;
      }
      this.mResources = super.getResources();
    }
    for (;;)
    {
      return this.mResources;
      label27:
      if (Build.VERSION.SDK_INT >= 17) {
        this.mResources = createConfigurationContext(this.mOverrideConfiguration).getResources();
      }
    }
  }
  
  private void initializeTheme()
  {
    if (this.mTheme == null) {}
    for (boolean bool = true;; bool = false)
    {
      if (bool)
      {
        this.mTheme = getResources().newTheme();
        Resources.Theme localTheme = getBaseContext().getTheme();
        if (localTheme != null) {
          this.mTheme.setTo(localTheme);
        }
      }
      onApplyThemeResource(this.mTheme, this.mThemeResource, bool);
      return;
    }
  }
  
  public void applyOverrideConfiguration(Configuration paramConfiguration)
  {
    if (this.mResources != null) {
      throw new IllegalStateException("getResources() or getAssets() has already been called");
    }
    if (this.mOverrideConfiguration != null) {
      throw new IllegalStateException("Override configuration has already been set");
    }
    this.mOverrideConfiguration = new Configuration(paramConfiguration);
  }
  
  protected void attachBaseContext(Context paramContext)
  {
    super.attachBaseContext(paramContext);
  }
  
  public AssetManager getAssets()
  {
    return getResources().getAssets();
  }
  
  public Configuration getOverrideConfiguration()
  {
    return this.mOverrideConfiguration;
  }
  
  public Resources getResources()
  {
    return getResourcesInternal();
  }
  
  public Object getSystemService(String paramString)
  {
    if ("layout_inflater".equals(paramString))
    {
      if (this.mInflater == null) {
        this.mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
      }
      return this.mInflater;
    }
    return getBaseContext().getSystemService(paramString);
  }
  
  public Resources.Theme getTheme()
  {
    if (this.mTheme != null) {
      return this.mTheme;
    }
    if (this.mThemeResource == 0) {
      this.mThemeResource = R.style.Theme_AppCompat_Light;
    }
    initializeTheme();
    return this.mTheme;
  }
  
  public int getThemeResId()
  {
    return this.mThemeResource;
  }
  
  protected void onApplyThemeResource(Resources.Theme paramTheme, int paramInt, boolean paramBoolean)
  {
    paramTheme.applyStyle(paramInt, true);
  }
  
  public void setTheme(int paramInt)
  {
    if (this.mThemeResource != paramInt)
    {
      this.mThemeResource = paramInt;
      initializeTheme();
    }
  }
}


/* Location:              /home/evan/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/android/support/v7/view/ContextThemeWrapper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */