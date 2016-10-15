package vandy.mooc.assignments.framework.application.views;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.framework.utils.ViewUtils;

/**
 * Custom SnackBar style view wrapper that contains a single EditText view for
 * user text input. Unlike SnackBar, this custom view will remain open at the
 * bottom of the screen until the user has entered text and explicitly closes
 * the view by pressing the enter key on the soft-keyboard, or when the user
 * clicks the back button.
 */
public class InputPanelView extends FrameLayout {
    /**
     * Logging tag.
     */
    private static final String TAG = "InputPanelView";
    /**
     * Public id for Espresso EditText input identification.
     */
    private static final int INPUT_PANEL_EDIT_VIEW_ID = 1;
    /**
     * Reference to the custom EditText child view.
     */
    private final KeyboardEditText mEditText;
    /**
     * Optional keyboard input listen to track the soft keyboard events.
     */
    private InputListener mInputListener;

    /**
     * Constructor.
     *
     * @param context Activity context.
     * @param attrs   View attributes to set.
     */
    public InputPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // This input panel dynamically constructs and manages
        // its own EditText view.
        mEditText = new KeyboardEditText(context, null);
        ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        mEditText.setLayoutParams(layoutParams);
        mEditText.setHint(R.string.input_url_hint);
        //noinspection ResourceType
        mEditText.setId(INPUT_PANEL_EDIT_VIEW_ID);
        mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        mEditText.setTextColor(
                ContextCompat.getColor(getContext(), android.R.color.white));
        mEditText.setHintTextColor(
                ContextCompat.getColor(getContext(), android.R.color.white));
        addView(mEditText);
    }

    /**
     * Installs a keyboard listen.
     *
     * @param listener An InputListener implementation.
     */
    public void setKeyboardListener(InputListener listener) {
        mInputListener = listener;
    }

    /**
     * Returns whether or not the input panel is displayed.
     *
     * @return {@code true} if the panel is showing; {@code false} if not.
     */
    public boolean isPanelShown() {
        return (BottomSheetBehavior.from(this).getState()
                & BottomSheetBehavior.STATE_EXPANDED) != 0;
    }

    public void show(boolean show) {
        if (show == isPanelShown()) {
            return;
        }
        if (show) {
            // Use bottom sheet behaviour handler to expand the sheet.
            BottomSheetBehavior.from(this).setState(BottomSheetBehavior.STATE_EXPANDED);
            // Get the focus (will set focus to the EditText).
            requestFocus();
        } else {
            // Use bottom sheet behaviour handler to collapse the sheet.
            BottomSheetBehavior.from(this).setState(BottomSheetBehavior.STATE_COLLAPSED);
            // Force the keyboard to be dismissed.
            ViewUtils.hideSoftKeyboard(mEditText);
        }
    }

    /**
     * Listener interface used for input notifications.
     */
    public interface InputListener {
        /**
         * Hook method called when input entry has been completed.
         *
         * @param text The input text.
         */
        void onInputReceived(String text);

        /**
         * Hook method called when input entry has been cancelled.
         */
        void onInputCancelled();
    }

    /**
     * Simple custom EditText to trap soft keyboard events to determine when the
     * soft keyboard is hidden. This event is then forwarded to the
     * InputPanelView's registered OnKeyboardListener (typically the Activity).
     */
    private class KeyboardEditText extends EditText
            implements OnKeyListener, OnFocusChangeListener {

        /**
         * Constructor.
         *
         * @param context Any context.
         * @param attrs   View attributes to set.
         */
        public KeyboardEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnKeyListener(this);
            setOnFocusChangeListener(this);
        }

        /**
         * Hook method called by Android framework before processing a soft
         * keyboard input event.
         *
         * @param keyCode The key code of the pressed key.
         * @param event   The key event.
         * @return always returns false to allow input to continue being
         * processed.
         */
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                    event.getAction() == KeyEvent.ACTION_UP) {
                if (mInputListener != null) {
                    mInputListener.onInputReceived(getText().toString());
                }
            }
            return false;
        }

        /**
         * Hook method called by Android framework when any soft key event
         * occurs.
         *
         * @param v       The target view for the key event.
         * @param keyCode The key code for the key.
         * @param event   The key event.
         * @return Always return true to allow processing to continue.
         */
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // We wil get an up and down event when the enter key
            // is pressed, so only accept the input on the up event.
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_UP) {
                ViewUtils.hideSoftKeyboard(this);
                if (mInputListener != null) {
                    mInputListener.onInputReceived(getText().toString());
                }

            }
            return false;
        }

        /**
         * Called by Android framework when the focus changes to or from this
         * EditText view.
         *
         * @param v        The view whose focus is changing.
         * @param hasFocus true if the view is gaining focus; {@code false} if
         *                 it is losing focus.
         */
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                if (mInputListener != null) {
                    mInputListener.onInputCancelled();
                }
                ViewUtils.hideSoftKeyboard(this);
            }
        }
    }
}
