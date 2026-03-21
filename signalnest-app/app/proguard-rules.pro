# ═══════════════════════════════════════════════════════════════════════════════
# SignalNest — ProGuard / R8 rules
# ═══════════════════════════════════════════════════════════════════════════════

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
# Prevent R8 from stripping coroutine state machines
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    private final java.lang.Object L$*;
}

# ── Kotlinx Serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.signalnest.app.**$$serializer { *; }
-keepclassmembers class com.signalnest.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *;
}

# ── OkHttp + Okio ─────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okhttp3.internal.publicsuffix.** { *; }
-keep class okhttp3.internal.ws.** { *; }

# ── NanoHTTPD (LAN server) ────────────────────────────────────────────────────
-keep class fi.iki.elonen.** { *; }
-keep interface fi.iki.elonen.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao interface * { *; }

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── App data models (network + DB) ────────────────────────────────────────────
-keep class com.signalnest.app.data.models.** { *; }
-keep class com.signalnest.app.network.** { *; }
-keepclassmembers class com.signalnest.app.data.models.** { *; }
-keepclassmembers class com.signalnest.app.network.** { *; }

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Rome RSS ──────────────────────────────────────────────────────────────────
-keep class com.rometools.** { *; }
-dontwarn com.rometools.**
-keep class org.jdom2.** { *; }
-dontwarn org.jdom2.**

# ── General Android ───────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class **.R$* { public static <fields>; }
-dontwarn android.support.**

# ── Debugging: keep source line numbers in stack traces ───────────────────────
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
