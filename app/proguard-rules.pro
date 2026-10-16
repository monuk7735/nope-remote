# Preserve line numbers and file names for crash reporting
-keepattributes SourceFile,LineNumberTable

# Keep data models used by Retrofit (Gson) and Room
-keep class com.monuk7735.nope.remote.models.** { *; }

# Keep Parcelable implementations (for @Parcelize)
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}