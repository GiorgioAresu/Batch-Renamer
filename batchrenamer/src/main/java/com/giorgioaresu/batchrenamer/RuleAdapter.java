package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class RuleAdapter extends ArrayAdapter<Rule> {
    // Handle to layout resource
    int layoutResource;
    ruleAdapter_Callbacks mCallbacks;

    public RuleAdapter(Context context, int resource, List<Rule> objects, ruleAdapter_Callbacks callbacks) {
        super(context, resource, objects);
        layoutResource = resource;
        mCallbacks = callbacks;
    }

    /**
     * {@inheritDoc}
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResource, null);

            //using the ViewHolder pattern to reduce lookups
            viewHolder = new ViewHolder(v);

            // If user clicks on trash icon we remove that list item
            v.findViewById(R.id.rulelistrow_button_remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallbacks.animateItemDeletion(v);
                }
            });

            v.setTag(viewHolder);
        } else {
            //we want to be sure it's not invisible in case of recycling view of dragged item
            v.setVisibility(View.VISIBLE);
            viewHolder = (ViewHolder) v.getTag();
            setViewEnabled(v, true);
        }

        Rule rule = getItem(position);
        viewHolder.title.setText((position + 1) + ". " + rule.getTitle());
        // Compute wanted width for view
        int width = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        viewHolder.imageView.setImageBitmap(rule.getBitmapOfView(width));
        viewHolder.imageView.setContentDescription(rule.getContentDescription());
        viewHolder.imageView.invalidate();

        return v;
    }

    /**
     * Set the enabled state of this view and all of its children
     *
     * @param view
     * @param enabled true to enable, false to disable
     */
    private void setViewEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                setViewEnabled(group.getChildAt(idx), enabled);
            }
        }
    }

    public class ViewHolder {
        // Handle to title TextView
        public TextView title;

        // Handle to content ImageView
        public ImageView imageView;

        private int horizontalPadding;
        private int verticalPadding;

        public ViewHolder(View base) {
            title = (TextView) base.findViewById(R.id.rule_title);
            imageView = (ImageView) base.findViewById(R.id.rule_imgContent);

            // Convert the dps to pixels, based on density scale
            int iconOffsetPx = (int) getContext().getResources().getDimension(R.dimen.rule_removedrawable_offset);

            // Fix aligment
            base.setPadding(base.getPaddingLeft(), base.getPaddingTop(), base.getPaddingRight() - iconOffsetPx, base.getPaddingBottom());
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            lp.rightMargin = iconOffsetPx;
            imageView.setLayoutParams(lp);
            ImageView separator = (ImageView) base.findViewById(R.id.rule_headerSeparator);
            lp = (LinearLayout.LayoutParams) separator.getLayoutParams();
            lp.rightMargin = iconOffsetPx;
            separator.setLayoutParams(lp);
        }
    }

    /**
     * Swaps two elements in the ArrayList
     *
     * @param indexOne index of the first element
     * @param indexTwo index of the second element
     */
    public void swapElements(int indexOne, int indexTwo) {
        setNotifyOnChange(false);
        int indexLow;
        int indexHigh;

        // We need to start with the highest index to be sure to not compromise
        // the other index
        if (indexTwo > indexOne) {
            indexLow = indexOne;
            indexHigh = indexTwo;
        } else {
            indexLow = indexTwo;
            indexHigh = indexOne;
        }
        // Pick both rules to avoid interferences
        Rule ruleLow = getItem(indexLow);
        Rule ruleHigh = getItem(indexHigh);

        remove(ruleHigh);
        insert(ruleLow, indexHigh);

        remove(ruleLow);
        insert(ruleHigh, indexLow);

        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public interface ruleAdapter_Callbacks {
        public void animateItemDeletion(View view);
    }
}
