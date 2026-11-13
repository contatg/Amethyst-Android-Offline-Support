package net.kdt.pojavlaunch.prefs.screens;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.GLInfoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);
        configureRendererPreference();
        requirePreference("legacyRenderHints").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
        int resolution = (int) (LauncherPreferences.PREF_SCALE_FACTOR * 100);

        //Disable notch checking behavior on android 8.1 and below.
        requirePreference("ignoreNotch").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0);

        CustomSeekBarPreference resolutionSeekbar = requirePreference("resolutionRatio",
                CustomSeekBarPreference.class);
        resolutionSeekbar.setSuffix(" %");

        // #724 bug fix
        if (resolution < 25) {
            resolutionSeekbar.setValue(100);
        } else {
            resolutionSeekbar.setValue(resolution);
        }

        // Sustained performance is only available since Nougat
        SwitchPreference sustainedPerfSwitch = requirePreference("sustainedPerformance",
                SwitchPreference.class);
        sustainedPerfSwitch.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        sustainedPerfSwitch.setChecked(LauncherPreferences.PREF_SUSTAINED_PERFORMANCE);

        requirePreference("alternate_surface", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
        requirePreference("force_vsync", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_FORCE_VSYNC);
        requirePreference("applyAdreno610").setVisible(GLInfoUtils.getGlInfo().isAdreno610());
        requirePreference("applyAdreno610").setOnPreferenceClickListener(preference -> {
            resolutionSeekbar.setValue(LauncherPreferences.resolutionForShortSide(requireContext(), 640));
            requirePreference("alternate_surface", SwitchPreferenceCompat.class).setChecked(true);
            requirePreference("force_vsync", SwitchPreferenceCompat.class).setChecked(false);
            LauncherPreferences.loadPreferences(requireContext());
            return true;
        });

    }

    private void configureRendererPreference() {
        ListPreference preference = requirePreference(LauncherPreferences.PREF_KEY_GLOBAL_RENDERER,
                ListPreference.class);
        Tools.RenderersList renderers = Tools.getCompatibleRenderers(requireContext());
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.global_renderer_auto));
        names.addAll(Arrays.asList(renderers.rendererDisplayNames));
        List<String> values = new ArrayList<>();
        values.add(LauncherPreferences.RENDERER_AUTO);
        values.addAll(renderers.rendererIds);
        preference.setEntries(names.toArray(new String[0]));
        preference.setEntryValues(values.toArray(new String[0]));
        preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
    }

}
