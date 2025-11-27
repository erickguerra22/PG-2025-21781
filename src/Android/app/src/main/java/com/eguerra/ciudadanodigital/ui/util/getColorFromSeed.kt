package com.eguerra.ciudadanodigital.ui.util

fun getColorFromSeed(seed: String): Int {
    val rnd = java.util.Random(seed.toLong())

    val red = 100 + rnd.nextInt(100)
    val green = 100 + rnd.nextInt(100)
    val blue = 100 + rnd.nextInt(100)

    return android.graphics.Color.rgb(red, green, blue)
}
