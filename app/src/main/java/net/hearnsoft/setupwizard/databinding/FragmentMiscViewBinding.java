// Generated by view binder compiler. Do not edit!
package net.hearnsoft.setupwizard.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.card.MaterialCardView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import net.hearnsoft.setupwizard.R;

public final class FragmentMiscViewBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final MaterialCardView fingerprintSettings;

  @NonNull
  public final MaterialCardView lockSettings;

  @NonNull
  public final MaterialCardView multiuserSettings;

  @NonNull
  public final MaterialCardView wallpaperSettings;

  private FragmentMiscViewBinding(@NonNull FrameLayout rootView,
      @NonNull MaterialCardView fingerprintSettings, @NonNull MaterialCardView lockSettings,
      @NonNull MaterialCardView multiuserSettings, @NonNull MaterialCardView wallpaperSettings) {
    this.rootView = rootView;
    this.fingerprintSettings = fingerprintSettings;
    this.lockSettings = lockSettings;
    this.multiuserSettings = multiuserSettings;
    this.wallpaperSettings = wallpaperSettings;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentMiscViewBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentMiscViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_misc_view, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentMiscViewBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.fingerprint_settings;
      MaterialCardView fingerprintSettings = ViewBindings.findChildViewById(rootView, id);
      if (fingerprintSettings == null) {
        break missingId;
      }

      id = R.id.lock_settings;
      MaterialCardView lockSettings = ViewBindings.findChildViewById(rootView, id);
      if (lockSettings == null) {
        break missingId;
      }

      id = R.id.multiuser_settings;
      MaterialCardView multiuserSettings = ViewBindings.findChildViewById(rootView, id);
      if (multiuserSettings == null) {
        break missingId;
      }

      id = R.id.wallpaper_settings;
      MaterialCardView wallpaperSettings = ViewBindings.findChildViewById(rootView, id);
      if (wallpaperSettings == null) {
        break missingId;
      }

      return new FragmentMiscViewBinding((FrameLayout) rootView, fingerprintSettings, lockSettings,
          multiuserSettings, wallpaperSettings);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}