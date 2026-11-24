# Add project specific ProGuard rules here.
# Keep Compose and Hilt reflective/meta reflective bits used by Hilt.
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
