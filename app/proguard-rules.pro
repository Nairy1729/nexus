# NEXUS release rules. R8 keeps the app small; Compose and Room (phase 2) ship their own rules.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn org.slf4j.**
