# The receivers are named in the manifest and the action callbacks are instantiated by Glance
# through reflection, so R8 sees no reference to either from the app's own code.
-keep class com.pierre.tunescout.feature.widget.presentation.widget.** { *; }
