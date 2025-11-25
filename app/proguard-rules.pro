# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# CRITICAL: Preserve generic signatures for Gson TypeToken and Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

# Gson TypeToken - MUST preserve generic signatures
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>(...);
}
# Preserve anonymous TypeToken classes (e.g., new TypeToken<List<Vehicle>>() {})
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>();
}

# Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-dontwarn sun.misc.**

# Retrofit - keep interface and all methods
-keep interface com.sendajapan.sendasnap.networking.ApiService { *; }
-keep class retrofit2.** { *; }
-keep class retrofit2.Call { *; }
-keep class retrofit2.Response { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Keep all Retrofit service interfaces
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Keep classes used as Retrofit request/response bodies
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep all model classes (for Gson and Retrofit)
# Keep all fields, constructors, and methods in model classes
-keep class com.sendajapan.sendasnap.models.** { 
    <fields>;
    <init>(...);
    <methods>;
}
-keep class com.sendajapan.sendasnap.data.model.** { 
    <fields>;
    <init>(...);
    <methods>;
}
-keep class com.sendajapan.sendasnap.data.dto.** { 
    <fields>;
    <init>(...);
    <methods>;
}

# Keep classes with @SerializedName annotations (Gson models)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep generic types used with Retrofit (e.g., ApiResponse<T>)
-keep class com.sendajapan.sendasnap.models.ApiResponse { *; }
-keep class com.sendajapan.sendasnap.models.ApiResponse$* { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Android components
-keep class com.sendajapan.sendasnap.MyApplication { *; }
-keep class com.sendajapan.sendasnap.utils.SharedPrefsManager { *; }
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
