package io.monetize.kit.sdk.core.utils.in_app_review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
interface ReviewListener {
    fun onFail()
    fun onComplete()
}
class AdKitInAppReviewManager {



    companion object {
        @Volatile
        private var instance: AdKitInAppReviewManager? = null

        internal fun getInstance(
        ): AdKitInAppReviewManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitInAppReviewManager().also { instance = it }
            }
        }
    }

    fun startReview(activity: Activity, reviewListener: ReviewListener) {
        try {
            with(ReviewManagerFactory.create(activity)) {
                requestReviewFlow()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            launchReviewFlow(activity, task.result)
                                .addOnCompleteListener { _ ->
                                    reviewListener.onComplete()
                                }
                                .addOnFailureListener {
                                    reviewListener.onFail()
                                }
                        } else {
                            reviewListener.onFail()
                        }
                    }
            }
        } catch (e: Exception) {
            reviewListener.onFail()
        }
    }
}