package com.notxx.icon

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.support.v4.util.LruCache
import android.util.Log

/**
* Author: TimothyZhang023
* Icon Cache
*
* Code implements port from
* https://github.com/MiPushFramework/MiPushFramework
*/
class IconCache private constructor() {
	class Cached {
		var background: Bitmap? = null
		var foreground: Bitmap? = null
		var ext: Icon? = null
		var extLoaded: Boolean = false
		var extColor: Int? = null
		var extColorLoaded: Boolean = false
		var icon: Icon? = null
		var mipush: Icon? = null
		var mipushLoaded: Boolean = false
		var color: Int? = null
	}
	private val cache:LruCache<String, Cached>
	private val squareBitmap:Bitmap
	private val square:Icon
	
	init {
		//TODO check cacheSizes is correct ?
		cache = LruCache(100)
		squareBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
		squareBitmap.eraseColor(Color.WHITE)
		square = Icon.createWithBitmap(squareBitmap)
	}

	private fun get(key:String, predicate:(Cached) -> Boolean, gen:(Cached) -> Unit):Cached {
		var cached = cache.get(key)
		if (cached == null) cached = Cached()
		if (!predicate(cached)) gen(cached)
		cache.put(key, cached)
		return cached
	}

	val MIPUSH_SMALL_ICON = "mipush_small_notification"
	fun getMiPushIcon(ctx:Context, pkg:String) = get(pkg, { it.mipush != null }, fun(it) {
		if (ctx.packageName != pkg) { Log.w(T, "packageName should be $pkg") }
		if (it.mipushLoaded) return
		val iconId  = ctx.resources.getIdentifier(MIPUSH_SMALL_ICON, "drawable", pkg)
		it.mipushLoaded = true
		if (iconId != 0) {
			it.mipush = Icon.createWithResource(pkg, iconId)
		}
	}).mipush

	fun getExtIcon(ctx:Context, pkg:String) = get(pkg, { it.ext != null }, fun(it) {
		if (ctx.packageName != RES_PACKAGE) { Log.w(T, "packageName should be $RES_PACKAGE") }
		if (it.extLoaded) return
		val key = pkg.toLowerCase().replace(".", "_");
		val iconId  = ctx.resources.getIdentifier(key, "drawable", RES_PACKAGE)
		it.extLoaded = true
		if (iconId != 0) {
			it.ext = Icon.createWithResource(RES_PACKAGE, iconId)
		}
	}).ext

	fun getExtColor(ctx:Context, pkg:String) = get(pkg, { it.extColor != null }, fun(it) {
		if (ctx.packageName != RES_PACKAGE) { Log.w(T, "packageName should be $RES_PACKAGE") }
		if (it.extColorLoaded) return
		val key = pkg.toLowerCase().replace(".", "_");
		val colorId  = ctx.resources.getIdentifier(key, "string", RES_PACKAGE)
		it.extColorLoaded = true
		if (colorId != 0) {
			it.extColor = Color.parseColor(ctx.resources.getString(colorId))
		}
	}).extColor

	fun renderForeground(drawable:AdaptiveIconDrawable): Bitmap {
		val recommand = { width:Int -> if (width > 0) width * 72 / 108 else 72 }
		val slice = { width:Int -> if (width > 0) width * -18 / 72 else -18 }
		return render(drawable.getForeground(), recommand, recommand,
						setBounds = { d, width, height -> val w = slice(width); val h = slice(height); d.setBounds(w, h, width - w, height - h) })
	}

	/** 获取图标前景 */
	fun getIconForeground(ctx:Context, pkg:String):Bitmap? = get(pkg, {
		it.foreground != null
	}, {
		try {
			// Log.d(T, "foreground $pkg")
			val icon = ctx.getPackageManager().getApplicationIcon(pkg)
			if (icon is AdaptiveIconDrawable) {
				// Log.d(T, "foreground $pkg $SIZE")
				it.foreground = renderForeground(icon)
				//it.foreground = render(icon.getForeground())
			} else {
				// Log.d(T, "legacy foreground $pkg $SIZE ${icon.getIntrinsicWidth()}, ${icon.getIntrinsicHeight()}")
				it.foreground = render(icon)
			}
		} catch (ignored:Exception) {
			Log.d(T, "foreground", ignored)
			it.foreground = null
		}
	}).foreground

	fun renderBackground(drawable:AdaptiveIconDrawable): Bitmap {
		val recommand = { width:Int -> if (width > 0) width * 72 / 108 else 72 }
		val slice = { width:Int -> if (width > 0) width * -18 / 72 else -18 }
		return render(drawable.getBackground(), recommand, recommand,
						setBounds = { d, width, height -> val w = slice(width); val h = slice(height); d.setBounds(w, h, width - w, height - h) })
	}

	/** 获取图标背景 */
	fun getIconBackground(ctx:Context, pkg:String):Bitmap? = get(pkg, {
		it.background != null
	}, {
		try {
			val icon = ctx.getPackageManager().getApplicationIcon(pkg)
			if (icon is AdaptiveIconDrawable) {
				//val recommand = { width:Int -> if (width > 0) width * 72 / 108 else 72 }
				//val slice = { width:Int -> if (width > 0) width * -18 / 72 else -18 }
				//it.background = render(icon.getBackground(), recommand, recommand,
				//		setBounds = { d, width, height -> val w = slice(width); val h = slice(height); d.setBounds(w, h, width - w, height - h) })
				it.background = renderBackground(icon).huefy()
			} else {
				it.background = render(icon).huefy()
			}
		} catch (ignored:Exception) {
			Log.d(T, "background", ignored)
			it.background = null
		}
	}).background

	fun getIcon(ctx:Context, pkg:String) = get(pkg, {
		it.icon != null
	}, {
		val icon = ctx.getPackageManager().getApplicationIcon(pkg)
		if (icon is AdaptiveIconDrawable) {
			val fore = renderForeground(icon)
			val back = renderBackground(icon)
			// val foreColors = fore.colors()
			// foreColors.filter(Alphaize.colorful)
			val foreHues = fore.hues(false)
			foreHues.removeAll { it.value < 10 }
			// // val backColors = back.colors()
			// // backColors.filter(Alphaize.colorful)
			val backHues = back.hues(true)
			backHues.removeAll { it.value < 10 }
			// // Log.d(T, "$pkg fore ${foreColors.size} ${foreHues.size}")
			// // Log.d(T, "$pkg back ${backColors.size} ${backHues.size}")
			if (foreHues.size == 1) { // 前景单色
				it.icon = Icon.createWithBitmap(Alphaize.process(fore, Alphaize.keyByAlpha))
				// it.color = Color.BLACK
				it.color = fore.majorColor(Alphaize.colorful) ?: back.majorColor(Alphaize.colorful) ?: Color.BLACK
			} else if (foreHues.size == 2) { // 前景两色
				foreHues.removeAll { it.key == HUE_GRAY }
				if (foreHues.size == 1) { // 有一色是黑白灰
					it.icon = Icon.createWithBitmap(Alphaize.process(fore, Alphaize.keyByPlus, Alphaize.autoLevel))
					// it.color = Color.BLACK
				} else {
					it.icon = Icon.createWithBitmap(Alphaize.process(fore, Alphaize.keyByMinus, Alphaize.autoLevel))
					// it.color = Color.BLACK
				}
				it.color = fore.majorColor(Alphaize.colorful) ?: back.majorColor(Alphaize.colorful) ?: Color.BLACK
			} else if (backHues.size <= 3) {
				foreHues.removeAll { it.key == HUE_GRAY }
				it.icon = Icon.createWithBitmap(Alphaize.process(fore, Alphaize.keyByPlus, Alphaize.removeMajor, Alphaize.autoLevel))
				// it.color = Color.BLACK
				it.color = back.majorColor(Alphaize.colorful) ?: fore.majorColor(Alphaize.colorful) ?: Color.BLACK
			} else {
				val bitmap = render(icon)
				it.icon = Icon.createWithBitmap(Alphaize.process(bitmap, Alphaize.keyByMinus))
				// it.color = Color.BLUE
				it.color = fore.majorColor(Alphaize.colorful) ?: Color.BLACK
			}
		} else {
			val bitmap = render(icon)
			val hues = bitmap.hues(false)
			hues.removeAll { it.value < 10 }
			val majorHue = hues.maxBy { it.value } !!.key
			if (majorHue == HUE_GRAY) { // 背景色是黑白灰
				it.icon = Icon.createWithBitmap(Alphaize.process(bitmap, Alphaize.keyByMinus, Alphaize.removeMajor, Alphaize.autoLevel))
				// it.color = Color.YELLOW
			} else {
				it.icon = Icon.createWithBitmap(Alphaize.process(bitmap, Alphaize.keyByRemoveHue(majorHue), Alphaize.autoLevel))
				// it.color = Color.RED
			}
			it.color = bitmap.majorColor(Alphaize.colorful) ?: Color.BLACK
		}
	}).icon

	fun getAppColor(ctx:Context, pkg:String):Int? = get(pkg, {
		it.color != null
	}, {
		it.color = getIconForeground(ctx, pkg)?.majorColor()
				?: getIconBackground(ctx, pkg)?.majorColor()
				?: Color.BLACK
	}).color

	private object Holder {
		val instance = IconCache()
	}

	companion object {
		private val T = "SmallIcon"
		@JvmField public val RES_PACKAGE = "com.notxx.icon.res"
		@JvmField public val SIZE = 144 // 建议的边长
		private val ADAPTIVE_CANVAS = Rect(-18, -18, 90, 90) // TODO density
		@JvmStatic public val BOUNDS = Rect(0, 0, 72, 72) // TODO density

		@JvmStatic fun getInstance():IconCache {
			return Holder.instance
		}

		/**
		 * 转换Drawable为Bitmap
		 *
		 * @param drawable
		 *
		 * @return
		 */
		@JvmStatic fun render(drawable:Drawable,
				recommandWidth:((Int) -> Int) = { width -> if (width > 0) width else SIZE },
				recommandHeight:((Int) -> Int) = { height -> if (height > 0) height else SIZE },
				createBitmap:((Int, Int) -> Bitmap) = { width, height -> Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) },
				setBounds:((Drawable, Int, Int) -> Unit) = { d, width, height -> d.setBounds(0, 0, width, height) }):Bitmap {
			var width = recommandWidth(drawable.getIntrinsicWidth())
			var height = recommandHeight(drawable.getIntrinsicHeight())
			val bitmap = createBitmap(width, height)
			val canvas = Canvas(bitmap)
			setBounds(drawable, width, height)
			drawable.draw(canvas)
			return bitmap
		}

		// 将图像灰度化然后转化为透明度形式
		@JvmStatic @JvmOverloads fun alphaize(bitmap:Bitmap, autoLevel:Boolean = true):Bitmap
			= if (autoLevel) {
				Alphaize.process(bitmap, Alphaize.keyByPlus, Alphaize.autoLevel)
			} else {
				Alphaize.process(bitmap, Alphaize.keyByPlus)
			}

		// 混合颜色
		fun blend(pixels:IntArray) {
			for (pos in 0 until pixels.size) {
				val pixel = pixels[pos] // 颜色值
				if (pixel == Color.TRANSPARENT) continue
				val alpha = ((pixel.toLong() and 0xFF000000) shr 24).toFloat() // 透明度通道
				val red = ((pixel and 0x00FF0000) shr 16).toFloat() // 红色通道
				val green = ((pixel and 0x0000FF00) shr 8).toFloat() // 绿色通道
				val blue = (pixel and 0x000000FF).toFloat() // 蓝色通道
				pixels[pos] = (0xFF000000 or // 透明度
						((red * alpha / 0xFF + (0xFF - alpha)).toLong() shl 16) or
						((green * alpha / 0xFF + (0xFF - alpha)).toLong() shl 8) or
						(blue * alpha / 0xFF + (0xFF - alpha)).toLong()).toInt()
			}
		}

		fun blend(pixel:Int, background:Int):Int {
			if (pixel == Color.TRANSPARENT) return background
			val alpha = ((pixel.toLong() and 0xFF000000) shr 24) // 透明度通道
			// val a1 = ((background.toLong() and 0xFF000000) shr 24) // 透明度通道
			val r0 = ((pixel and 0x00FF0000) shr 16) // 红色通道
			val r1 = ((background and 0x00FF0000) shr 16) // 红色通道
			val g0 = ((pixel and 0x0000FF00) shr 8) // 绿色通道
			val g1 = ((background and 0x0000FF00) shr 8) // 绿色通道
			val b0 = (pixel and 0x000000FF) // 蓝色通道
			val b1 = (background and 0x000000FF) // 蓝色通道
			return (0xFF000000 or // 透明度
					(((r0 * alpha + r1 * (0xFF - alpha)) / 0xFF).toLong() shl 16) or
					(((g0 * alpha + g1 * (0xFF - alpha)) / 0xFF).toLong() shl 8) or
					((b0 * alpha + b1 * (0xFF - alpha)) / 0xFF).toLong()).toInt()
		}

		@JvmStatic fun removeBackground(pkg:String, pixels:IntArray, width:Int, height:Int, dest:Int = Color.TRANSPARENT):Boolean {
			// blend(pixels)
			
			// 方形
			// val lt = bitmap.getPixel(0, 0); val rt = bitmap.getPixel(width - 1, 0)
			// val lb = bitmap.getPixel(0, height - 1); val rb = bitmap.getPixel(width - 1, height - 1)
			// if ((lt == rt) && (rt == lb) && (lb == rb) && (lt != dest)) { // 四角颜色一致
			// 	// Log.d(T, "removeBackground1($pixels, $width, $height, ${Integer.toHexString(lt)}, ${Integer.toHexString(dest)})")
			// 	// floodFill(pixels, width, height, lt, dest)
			// 	removeColor(pixels, lt, dest)
			// 	bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
			// 	return false
			// }

			// 圆形
			// pixels.fill(0xFFFFFFFF.toInt())
			// 		// val hypot = hypot(x - cx, y - cy)
			// 		// if (outside < hypot || inside > hypot ) continue
			val outside = width.toFloat() / 2; val inside = width.toFloat() / 4
			assert(outside > inside)
			// circularFill(pixels, width, height) { dx, dy, pixel ->
			// 	val hypot = hypot(dx, dy)
			// 	// Log.d(T, "hypot = $hypot dx,dy = $dx, $dy")
			// 	(outside < hypot || inside > hypot)
			// }
			val map = mutableMapOf<Int, Int>()
			val total = circularScan(pixels, width, height, map) { dx, dy, pixel ->
				val hypot = hypot(dx, dy)
				pixel != Color.TRANSPARENT || outside < hypot || inside > hypot
			}
			// Log.d(T, "$pkg circularScan() ${map.size} / $total")
			if (map.size > 1) {
				val maxBy = map.maxBy { it.value }
				if (maxBy != null) {
					// maxBy?.key
					// Log.d(T, "$pkg circularScan() ${Integer.toHexString(maxBy!!.key)} = ${maxBy!!.value} ${maxBy!!.value.toFloat() / total}")
					val q = maxBy.value.toFloat() / total
					if (q > 0.2 && q < 0.8) {
						// Log.d(T, "$pkg removeBackground1($pixels, ${Integer.toHexString(maxBy.key)}, ${Integer.toHexString(dest)})")
						removeColor(pkg, pixels, maxBy.key, dest) // TODO 考虑改用播种加洪泛式的颜色移除
						return true
					}
				}
			}

			return false
		}

		// 环状填充
		fun circularFill(pixels:IntArray, width:Int, height:Int, included:(Float,Float,Int) -> Boolean) {
			Log.d(T, "circularFill(width,height = $width, $height)")

			val cx = width.toFloat() / 2; val cy = height.toFloat() / 2
			for (y in 0 until height) {
				for (x in 0 until width) {
					val pos = width * y + x // 偏移
					if (!included(x - cx, y - cy, pixels[pos])) continue
					pixels[pos] = 0xFFFF0000.toInt()
				}
			}
		}

		// 环状扫描
		fun circularScan(pixels:IntArray, width:Int, height:Int, colorMap:MutableMap<Int, Int>, included:(Float,Float,Int) -> Boolean):Int {
			// Log.d(T, "circularScan(width,height = $width, $height)")

			val cx = width.toFloat() / 2; val cy = height.toFloat() / 2; var count = 0
			for (y in 0 until height) {
				for (x in 0 until width) {
					val pos = width * y + x // 偏移
					val pixel = pixels[pos]
					if (!included(x - cx, y - cy, pixel)) continue
					count++
					if (colorMap.containsKey(pixel)) {
						var count = colorMap[pixel]
						if (count != null) { colorMap[pixel] = count + 1 }
					} else {
						colorMap[pixel] = 1
					}
				}
			}
			return count
		}

		private val STATE_SWING:Byte = 0.toByte() // 未决
		private val STATE_KEEP:Byte = 1.toByte() // 保留
		private val STATE_KEEPR:Byte = 2.toByte() // 保留
		private val STATE_REMOVE:Byte = 0x11.toByte() // 移除
		private val STATE_REMOVER:Byte = 0x12.toByte() // 移除

		// 从四角开始洪泛法移除相同颜色
		fun floodFill(pixels:IntArray, width:Int, height:Int, target:Int, dest:Int = Color.TRANSPARENT) {
			val states = ByteArray(width * height)
			states.fill(STATE_SWING)

			// pixels[0] = pixels[width - 1] = pixels[width * (height - 1)] = pixels[width * (height - 1) + width - 1] = Color.TRANSPARENT
			states[0] = STATE_REMOVE // 种子
			states[width - 1] = STATE_REMOVE // 种子
			states[width * (height - 1)] = STATE_REMOVE // 种子
			states[width * (height - 1) + width - 1] = STATE_REMOVE // 种子
			for (y in 0 until height) { // 纵向播种
				val pos0 = width * y // 偏移0
				if (pixels[pos0] == target) { states[pos0] = STATE_REMOVE }
				val pos1 = width * y + width - 1 // 偏移1
				if (pixels[pos1] == target) { states[pos1] = STATE_REMOVE }
			}
			for (pos in 0 until states.size) { // 正向填充
				if ((pos % width) + 1 == width) continue // 避免行末出错
				val pixel = pixels[pos]; val state = states[pos]
				val np = pixels[pos + 1]; val ns = states[pos + 1]
				if ((pixel == np) && (ns == STATE_SWING) && (state == STATE_REMOVE || state == STATE_REMOVER)) {
					states[pos + 1] = STATE_REMOVE
				}
			}
			for (pos in states.size - 1 downTo 0) { // 反向填充
				if ((pos % width) == 0) continue // 避免行首出错
				val pixel = pixels[pos]; val state = states[pos]
				val np = pixels[pos - 1]; val ns = states[pos - 1]
				if ((pixel == np) && (ns == STATE_SWING) && (state == STATE_REMOVE || state == STATE_REMOVER)) {
					states[pos - 1] = STATE_REMOVE
				}
			}
			// for (y in 0 until height) {
			// 	for (x in 0 until width) {
			// 		val pos = width * y + x // 偏移
			// 		val state = states[pos] // 状态
			// 		val pixel = pixels[pos] // 颜色值
			// 	}
			// }
			for (pos in 0 until states.size) {
				if (states[pos] == STATE_REMOVE) {
					pixels[pos] = dest
				}
			}
		}

		fun _h(_int:Int) = Integer.toHexString(_int)

		// 颜色容差
		private val DIFF = 1 shl 13
		// 直接把特定颜色移除
		fun removeColor(pkg:String, pixels:IntArray, target:Int, dest:Int = Color.TRANSPARENT) {
			val r = ((target and 0x00FF0000) shr 16) // 红色
			val g = ((target and 0x0000FF00) shr 8) // 绿色
			val b = (target and 0x000000FF) // 蓝色
			for (pos in 0 until pixels.size) { // 正向填充
				var pixel = pixels[pos]
				val alpha = ((pixel.toLong() and 0xFF000000) ushr 24).toInt() // 透明度通道
				if (alpha == 0) continue
				pixel = blend(pixel, target)
				val dr = ((pixel and 0x00FF0000) shr 16) - r // 红色差异
				val dg = ((pixel and 0x0000FF00) shr 8) - g // 绿色差异
				val db = (pixel and 0x000000FF) - b // 蓝色差异
				val diff = dr * dr + dg * dg + db * db
				// if (pkg == "com.apple.android.music" && diff > DIFF) {
				// 	Log.d(T, "$pkg $pos $dr($r) $dg($g) $db($b) ${_h(pixel)} $diff")
				// }
				if (diff <= DIFF) {
					pixels[pos] = dest
				}
			}
		}

		fun backgroundColor(bitmap:Bitmap?) = bitmap?.majorColor() ?: Color.BLACK

		fun RGB2Hue(red: Int, green: Int, blue: Int): Float {
			val r = red.toFloat() / 255
			val g = green.toFloat() / 255
			val b = blue.toFloat() / 255
			// max, min
			val max = maxOf(r, g, b); val min = minOf(r, g, b)
			// val lum = (max + min) / 2
			if (max == min) {
				return 0f
			} else {
				val c = max - min // chroma
				//val sat = c / (1 - Math.abs(2 * lum - 1))
				var segment = 0f; var shift = 0f;
				when (max) {
					r -> {
						segment = (g - b) / c
						shift = if (segment > 0) { 0f / 60 } else { 360f / 60 }
					}
					g -> {
						segment = (b - r) / c
						shift = 120f / 60
					}
					b -> {
						segment = (r - g) / c
						shift = 240f / 60
					}
				}
				return (segment + shift) * 60
			}
		}
	}
}

typealias KeyMaker = (pixel:Int) -> Int?
typealias PostProcess = (keys:IntArray, map:MutableMap<Int, Int>) -> Unit
class Alphaize {
	companion object {
		val KEY_MAX = 0xFF; val KEY_MIN = 0
		val colorful: ColorPredicate = {
			val hsv = FloatArray(3)
			Color.colorToHSV(it.key, hsv)
			val sat = hsv[1]
			//Log.d("SmallIcon", "rgb ${rgb.toString(16)} sat $sat")
			sat > 0.01
		}

		val removeMajor: PostProcess = fun(keys, map) {
			val threshold = keys.size / 2
			map.entries.removeAll { it.value > threshold }
		}

		val invert: PostProcess = fun(keys, map) {
			val major = map.maxBy { it.value }
			if (major != null) {
				for (key in map.keys) {
					map[key] = if (key == major.key) {
						KEY_MIN
					} else {
						KEY_MAX
					}
				}
			}
		}

		val autoLevel: PostProcess = fun(keys, map) {
			val threshold = keys.size / 100
			//map.entries.removeAll { it.value < threshold }
			val max = map.filter { it.value > threshold }.maxBy { it.key }; val min = map.minBy { it.key }
			if (max != null && min != null) {
				if (max.key > min.key) {
					val q = (KEY_MAX - KEY_MIN).toFloat() / (max.key - min.key)
					for (key in map.keys) {
						map[key] = if (key >= max.key) {
							KEY_MAX
						} else if (key >= min.key) {
							((key - min.key) * q + KEY_MIN).toInt()
						} else {
							KEY_MIN
						}
					}
				} else {
					for (key in map.keys) {
						map[key] = if (key == max.key) {
							KEY_MAX
						} else {
							KEY_MIN
						}
					}
				}
			}
		}

		// fun colorful(it:Map.Entry<Int, Int>) = true
		val keyByAlpha:KeyMaker = {
			val alpha = Color.alpha(it)
			// Log.d("SmallIcon", "alpha $alpha")
			if (alpha == 0) {
				null
			} else {
				alpha
			}
		}

		val keyByNotGray:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				if (hsv[1] < 0.1f) {
					KEY_MIN
				} else {
					KEY_MAX
				}
			}
		}

		fun keyByRemoveHue(hue:Int):KeyMaker {
			return {
				val alpha = Color.alpha(it) // 透明度通道
				if (alpha == 0) {
					null
				} else {
					val hsv = FloatArray(3)
					Color.colorToHSV(it, hsv)
					if (hue == (hsv[0] / 10).toInt()) {
						null
					} else {
						(hsv[2] * alpha).toInt()
					}
				}
			}
		}

		val keyBySat:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				(hsv[1] * alpha).toInt()
			}
		}

		val keyByMinusSat:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				((1 - hsv[1]) * alpha).toInt()
			}
		}

		val keyByBri:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				(hsv[2] * alpha).toInt()
			}
		}

		val keyByMinusBri:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				((1 - hsv[2]) * alpha).toInt()
			}
		}

		val keyByMinus:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				((hsv[1] + 1 - hsv[2]) * alpha / 2).toInt()
			}
		}

		val keyByPlus:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				((hsv[1] + hsv[2]) * alpha / 2).toInt()
			}
		}

		val keyByMultiply:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				(hsv[1] * hsv[2] * alpha).toInt()
			}
		}

		val keyByMM:KeyMaker = {
			val alpha = Color.alpha(it) // 透明度通道
			if (alpha == 0) {
				null
			} else {
				val hsv = FloatArray(3)
				Color.colorToHSV(it, hsv)
				(hsv[1] * (1 - hsv[2]) * alpha).toInt()
			}
		}

		fun process(bitmap:Bitmap, keyMaker:KeyMaker, vararg processes:PostProcess):Bitmap {
			val width = bitmap.getWidth(); val height = bitmap.getHeight(); val size = width * height
			val pixels = IntArray(size)
			val temp = bitmap.copy(Bitmap.Config.ARGB_8888, true)
			try {
				temp.getPixels(pixels, 0, width, 0, 0, width, height)
			} finally { temp.recycle() }
			val keys = IntArray(size)
			val map = mutableMapOf<Int, Int>()
			for (pos in 0 until pixels.size) {
				val pixel = pixels[pos] // 颜色值
				var key = keyMaker(pixel)
				if (key == null) continue
				if (key == 0) key = 1 // 强制加1，避免与透明背景混同
				keys[pos] = key
				map.addWeight(key)
			}
			if (processes.size > 0) {
				for (process in processes)
					process(keys, map)
			} else {
				for (key in map.keys) {
					map[key] = key
				}
			}
			for (pos in 0 until pixels.size) {
				val key = keys[pos]
				val a = map[key] ?: 0
				pixels[pos] = Color.argb(a, 255, 255, 255)
			}

			val r = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
			r.setPixels(pixels, 0, width, 0, 0, width, height)
			return r
		}
	}
}

typealias WeightedColors = List<Map.Entry<Int, Int>>

fun Bitmap.colors(): WeightedColors { // 获取颜色
	val width = this.getWidth(); val height = this.getHeight()
	val map = mutableMapOf<Int, Int>()
	val pixels = IntArray(width * height)
	val temp = this.copy(Bitmap.Config.ARGB_8888, true)
	try {
		temp.getPixels(pixels, 0, width, 0, 0, width, height)
	} finally { temp.recycle() }
	for (pos in 0 until pixels.size) {
		val pixel = pixels[pos]
		val alpha = Color.alpha(pixel) // 透明度通道
		if (alpha == 0) continue
		//val hsv = FloatArray(3)
		val rgb = pixel and 0xFFFFFF // RGB颜色值
		//Color.colorToHSV(rgb, hsv)
		//val sat = hsv[1]
		//if (sat == 0f) continue
		map.addWeight(rgb)
	}
	val result = map.toMap().entries.toMutableList()
	result.sortBy { it.value }
	return result
}

typealias WeightedHues = MutableList<Map.Entry<Int, Int>>

fun MutableMap<Int, Int>.addWeight(key:Int) {
	this[key] = (this[key]?: 0) + 1
}

val HUE_GRAY = 50
fun Bitmap.hues(colorful:Boolean): WeightedHues { // 获取颜色
	val width = this.getWidth(); val height = this.getHeight()
	val map = mutableMapOf<Int, Int>()
	val pixels = IntArray(width * height)
	val temp = this.copy(Bitmap.Config.ARGB_8888, true)
	try {
		temp.getPixels(pixels, 0, width, 0, 0, width, height)
	} finally { temp.recycle() }
	for (pos in 0 until pixels.size) {
		val pixel = pixels[pos]
		val alpha = Color.alpha(pixel) // 透明度通道
		if (alpha == 0) continue
		val hsv = FloatArray(3)
		//val rgb = pixel and 0xFFFFFF // RGB颜色值
		Color.colorToHSV(pixel, hsv)
		if (hsv[1] < 0.01) {
			if (!colorful) map.addWeight(HUE_GRAY)
			continue
		}
		val hue = (hsv[0] / 10).toInt() // 0 ~ 36
		map.addWeight(hue)
	}
	return map.toMap().entries.toMutableList()
}

fun Bitmap.huefy(colorful:Boolean = false): Bitmap {
	val width = this.getWidth(); val height = this.getHeight()
	val map = mutableMapOf<Int, Int>()
	val pixels = IntArray(width * height)
	val temp = this.copy(Bitmap.Config.ARGB_8888, true)
	try {
		temp.getPixels(pixels, 0, width, 0, 0, width, height)
	} finally { temp.recycle() }
	for (pos in 0 until pixels.size) {
		val pixel = pixels[pos]
		val alpha = Color.alpha(pixel) // 透明度通道
		if (alpha == 0) continue
		val hsv = FloatArray(3)
		//val rgb = pixel and 0xFFFFFF // RGB颜色值
		Color.colorToHSV(pixel, hsv)
		if (hsv[1] < 0.01) {
			pixels[pos] = Color.HSVToColor(if (colorful) { 0 } else { alpha }, hsv)
		} else {
			hsv[0] = ((hsv[0] / 10).toInt() * 10).toFloat()
			hsv[1] = 1f; hsv[2] = 0.5f
			pixels[pos] = Color.HSVToColor(alpha, hsv)
		}
	}
	val r = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
	r.setPixels(pixels, 0, width, 0, 0, width, height)
	return r
}

typealias ColorPredicate = (Map.Entry<Int, Int>) -> Boolean

fun Bitmap.majorColor(predicate: ColorPredicate? = null): Int? {
	var colors = this.colors();
	//val filtered = map.filter { it.key != 0 && it.key != 0xFFFFFF } // 预先剔除黑色和白色
	if (predicate != null) {
		colors = colors.filter(predicate)
	}
	val max = colors.maxBy { it.value } // 获得最多的颜色
	//Log.d("SmallIcon", "max.key ${max?.key?.toString(16)}")
//	return max?.key ?: null
	return if (max != null) {
		(max.key.toLong() or 0xFF000000).toInt()
	} else { null }
}
