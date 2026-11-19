# ProGuard / R8 rules for Vanijya (Room + SQLCipher + Hilt)
-keep class net.zetetic.database.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
