package com.sapphirevirtual.sentilife;

import android.location.Location;

public interface GPSCallback
{
    public abstract void onGPSUpdate(Location location);
}
