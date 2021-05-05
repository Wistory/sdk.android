package ru.vvdev.storiesdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.Wistory

class StoryEventFragment : Fragment() {

    private var baseServer: Int = R.string.wistory_dev_url

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val view = Wistory.openStoriesByEventId(requireContext(), 14)
        view {
            token =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZmM3NjJjYTFmZTcwMzc4MDg0ZmNjMzgiLCJpYXQiOjE2MDY5MDI0NzR9.oX3IXdwucxb73DCkNJVGhvYN1n4Zs5WddzEI8yiKtwE"
            serverUrl = getString(baseServer)
            config = UiConfig().apply { format = UiConfig.Format.FULLSCREEN }
        }
    }
}
