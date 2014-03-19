package mona.android.customcomponents;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * A custom TextView with support for clicking links
 */
public class HandleSpanClickTextView extends TextView {

	public HandleSpanClickTextView(final Context context) {
		super(context);
	}

	public HandleSpanClickTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public HandleSpanClickTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final Spannable buffer = SpannableString.valueOf(getText());
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= getTotalPaddingLeft();
			y -= getTotalPaddingTop();

			x += getScrollX();
			y += getScrollY();

			final Layout layout = getLayout();
			final int line = layout.getLineForVertical(y);
			final int off = layout.getOffsetForHorizontal(line, x);

			final ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

			if (links.length != 0) {
				final ClickableSpan link = links[0];
				if (action == MotionEvent.ACTION_UP) {
					link.onClick(this);
					setClickable(false);
					return true;
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link));
					setClickable(true);
				}
			} else {
				setClickable(false);
				Selection.removeSelection(buffer);
			}
		}
		return super.onTouchEvent(event);
	}
}