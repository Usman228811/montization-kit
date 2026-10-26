# Applied to every app that uses this library (consumerProguardFiles).

# Mintegral (mediation adapter): its SDK uses reflection and reads its R fields by name,
# so release builds with R8 must keep them (otherwise its ad activities can't find resources)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** { *; }
-keep interface com.mbridge.** { *; }
-dontwarn com.mbridge.**
-keepclassmembers class **.R$* { public static final int mbridge*; }
