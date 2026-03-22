package com.build.building.hud;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;

public class AnimationHelper {
    
    public static void fadeIn(View view, int duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    public static void fadeOut(View view, int duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
    
    public static void scalePop(View view, int duration) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(new BounceInterpolator())
            .start();
    }
    
    public static void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
    
    public static void pulse(View view) {
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }
    
    public static void slideInLeft(View view, int duration) {
        view.setTranslationX(-view.getWidth());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationX(0)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    public static void slideOutRight(View view, int duration) {
        view.animate()
            .translationX(view.getWidth())
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
    
    public static void rotate(View view, float from, float to, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
    
    public static void coinAnimation(View view, Runnable onEnd) {
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(150)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .withEndAction(onEnd)
                    .start();
            })
            .start();
    }
}
