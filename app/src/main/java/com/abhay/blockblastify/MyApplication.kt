package com.abhay.blockblastify

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var currentActivity: Activity? = null

    // Real App Open Ad Unit ID
    private val adUnitId = "ca-app-pub-9423620095795922/6601193594"

    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        MobileAds.initialize(this) {}
        loadAd()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAdIfAvailable()
    }

    fun loadAd() {
        if (isLoadingAd || appOpenAd != null) return
        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null
                }
            }
        )
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && appOpenAd != null) {
            currentActivity?.let { activity ->
                appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
                        loadAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        loadAd()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
                appOpenAd?.show(activity)
            }
        } else {
            loadAd()
        }
    }

    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) currentActivity = null
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
}
