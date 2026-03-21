# ── OkHttp + WebSocket ────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.internal.publicsuffix.** { *; }

# ── NanoHTTPD ─────────────────────────────────────────────────────────────────
-keep class fi.iki.elonen.** { *; }

# ── Kotlinx Serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.signalnest.app.**$$serializer { *; }
-keepclassmembers class com.signalnest.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ── Data models ───────────────────────────────────────────────────────────────
-keep class com.signalnest.app.data.models.** { *; }
-keep class com.signalnest.app.network.model.** { *; }

# ── Keep R8 from removing coroutine state machines ───────────────────────────
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    public static final kotlinx.coroutines.flow.Flow *(...);
}
