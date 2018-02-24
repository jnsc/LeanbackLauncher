package com.google.android.exoplayer2.upstream;

import android.content.Context;

public final class DefaultDataSourceFactory
  implements DataSource.Factory
{
  private final DataSource.Factory baseDataSourceFactory;
  private final Context context;
  private final TransferListener<? super DataSource> listener;
  
  public DefaultDataSourceFactory(Context paramContext, TransferListener<? super DataSource> paramTransferListener, DataSource.Factory paramFactory)
  {
    this.context = paramContext.getApplicationContext();
    this.listener = paramTransferListener;
    this.baseDataSourceFactory = paramFactory;
  }
  
  public DefaultDataSourceFactory(Context paramContext, String paramString)
  {
    this(paramContext, paramString, null);
  }
  
  public DefaultDataSourceFactory(Context paramContext, String paramString, TransferListener<? super DataSource> paramTransferListener)
  {
    this(paramContext, paramTransferListener, new DefaultHttpDataSourceFactory(paramString, paramTransferListener));
  }
  
  public DefaultDataSource createDataSource()
  {
    return new DefaultDataSource(this.context, this.listener, this.baseDataSourceFactory.createDataSource());
  }
}


/* Location:              /home/evan/Downloads/fugu-opr2.170623.027-factory-d4be396e/fugu-opr2.170623.027/image-fugu-opr2.170623.027/TVLauncher/TVLauncher/TVLauncher-dex2jar.jar!/com/google/android/exoplayer2/upstream/DefaultDataSourceFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */