# R8 / ProGuard rules for PhonePeToCashew

# ML Kit Text Recognition
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-keep class com.google.mlkit.vision.text.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Android Compose
-keepclassmembers class * extends androidx.compose.runtime.Composable { *; }
