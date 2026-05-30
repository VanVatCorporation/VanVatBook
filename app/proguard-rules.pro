# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keep class com.shockwave.**
-keep class java.net.** { *; }
-keep class java.io.** { *; }
-keepclassmembers class * {
    public void readObject(java.io.ObjectInputStream);
    public void writeObject(java.io.ObjectOutputStream);
}

-keep class * implements java.io.Serializable { *; }


-keep class okhttp3.** { *; }


# Prevent ProGuard from removing constructors
-keepclassmembers class * {
    public <init>(...);
}

-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*


# Keep TypeAdapter and TypeToken classes
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.reflect.TypeToken


-keep class at.favre.lib.crypto.bcrypt.** { *; }


-keep class com.vanvatcorporation.vanvatsach.** { *; }
