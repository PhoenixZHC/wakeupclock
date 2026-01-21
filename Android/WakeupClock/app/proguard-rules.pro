# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities
-keep class com.wakeup.clock.data.model.** { *; }

# Keep Media3/ExoPlayer
-keep class androidx.media3.** { *; }
