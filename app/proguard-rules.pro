# Keep the serializable navigation keys so Navigation 3 can restore the back stack after process death
-keep,includedescriptorclasses class com.quare.tunescout.**$$serializer { *; }
-keepclassmembers class com.quare.tunescout.** {
    *** Companion;
}
-keepclasseswithmembers class com.quare.tunescout.** {
    kotlinx.serialization.KSerializer serializer(...);
}
