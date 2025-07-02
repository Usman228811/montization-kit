# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all public classes/methods in a specific package
-keep public class io.monetize.kit.sdk.** { public *; }


-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# Firebase Crashlytics
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.google.firebase.crashlytics.* <fields>;
    @com.google.firebase.crashlytics.* <methods>;
}
-keepnames class com.google.firebase.crashlytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
# Google Play Billing
-keep class com.android.billingclient.** { *; }
-keep class com.android.billingclient.api.** { *; }

# Keep Google Play Services classes
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.ads.** { *; }
