# Add project specific ProGuard rules here.
# Preserve line numbers for Crashlytics/stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.scrap2stack.app.**$$serializer { *; }
-keepclassmembers class com.scrap2stack.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.scrap2stack.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Supabase Kotlin SDK & Ktor Client
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.atomicfu.**
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.bouncycastle.**
