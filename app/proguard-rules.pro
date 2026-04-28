# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class se.vedret.app.**$$serializer { *; }
-keepclassmembers class se.vedret.app.** {
    *** Companion;
}
-keepclasseswithmembers class se.vedret.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
