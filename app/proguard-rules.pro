# Keep the serializable navigation keys so Navigation 3 can restore the back stack after process death
-keep,includedescriptorclasses class com.pierre.tunescout.**$$serializer { *; }
-keepclassmembers class com.pierre.tunescout.** {
    *** Companion;
}
-keepclasseswithmembers class com.pierre.tunescout.** {
    kotlinx.serialization.KSerializer serializer(...);
}
