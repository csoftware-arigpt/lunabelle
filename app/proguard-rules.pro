# SQLCipher
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
# Keep Room generated
-keep class * extends androidx.room.RoomDatabase { <init>(); }
