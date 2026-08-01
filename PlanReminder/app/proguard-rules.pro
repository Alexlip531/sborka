# Proguard rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin metadata
-keep class kotlin.Metadata { *; }
