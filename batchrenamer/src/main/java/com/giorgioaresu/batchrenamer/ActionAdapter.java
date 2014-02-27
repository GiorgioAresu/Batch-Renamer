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

public class ActionAdapter extends ArrayAdapter<Action> {
    // Handle to layout resource
    int layoutResource;
    actionAdapter_Callbacks mCallbacks;

    public ActionAdapter(Context context, int resource, List<Action> objects, actionAdapter_Callbacks callbacks) {
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
            v.findViewById(R.id.actionlistrow_button_remove).setOnClickListener(new View.OnClickListener() {
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
        }

        Action action = getItem(position);
        viewHolder.title.setText((position + 1) + ". " + action.getTitle());
        // Compute wanted width for view
        int width = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        viewHolder.imageView.setImageBitmap(action.getBitmapOfView(width));
        viewHolder.imageView.setContentDescription(action.getContentDescription());
        viewHolder.imageView.invalidate();

        return v;
    }

    public class ViewHolder {
        // Handle to title TextView
        public TextView title;

        // Handle to content ImageView
        public ImageView imageView;

        private int horizontalPadding;
        private int verticalPadding;

        public ViewHolder(View base) {
            title = (TextView) base.findViewById(R.id.action_title);
            imageView = (ImageView) base.findViewById(R.id.action_imgContent);

            // Convert the dps to pixels, based on density scale
            int iconOffsetPx = (int) getContext().getResources().getDimension(R.dimen.action_removedrawable_offset);

            // Fix aligment
            base.setPadding(base.getPaddingLeft(), base.getPaddingTop(), base.getPaddingRight() - iconOffsetPx, base.getPaddingBottom());
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            lp.rightMargin = iconOffsetPx;
            imageView.setLayoutParams(lp);
            ImageView separator = (ImageView) base.findViewById(R.id.action_headerSeparator);
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
        // Pick both actions to avoid interferences
        Action actionLow = getItem(indexLow);
        Action actionHigh = getItem(indexHigh);

        remove(actionHigh);
        insert(actionLow, indexHigh);

        remove(actionLow);
        insert(actionHigh, indexLow);

        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    public interface actionAdapter_Callbacks {
        public void animateItemDeletion(View view);
    }
}
