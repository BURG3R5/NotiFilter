-dontobfuscate

-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int i(...);
    public static int e(...);
}

-keep class net.fellbaum.jemoji.Emoji { *; }

-dontwarn javax.annotation.processing.Processor
-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedOptions
