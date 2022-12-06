package one.yufz.hmspush.hook.hms.dummy

import android.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import one.yufz.hmspush.common.dp2px
import one.yufz.hmspush.hook.I18n
import one.yufz.xposed.child

class DummyFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireNotNull(context)

        val root = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            val dp16 = context.dp2px(16)

            setPadding(dp16, dp16, dp16, dp16)
        }

        val p = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        root.child<TextView>(p) {
            gravity = Gravity.CENTER
            textSize = 16f
            text = I18n.get(context).dummyFragmentDesc

        }
        return root
    }

}