package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ActionAdapter extends ArrayAdapter<Action> {
    // Handle to layout resource
    int layoutResource;

    List<Action> mObjects;

    public ActionAdapter(Context context, int resource, List<Action> objects) {
        super(context, resource, objects);
        layoutResource = resource;
        mObjects = objects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getItem(int position) {
        if (position < 0 || position >= mObjects.size()) {
            return null;
        }

        return mObjects.get(position);
    }

    /**
     * {@inheritDoc}
     */
    public View getView(int position, View convertView, ViewGroup parent) {
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
                    ViewHolder viewHolder = (ViewHolder) getRootTag(v, 5);
                    if (viewHolder != null) {
                        remove(getItem(viewHolder.position));
                        notifyDataSetChanged();
                    }
                }
            });

            /*
             * v.setOnTouchListener(new View.OnTouchListener() {
             *   @Override
             *   public boolean onTouch(View view, MotionEvent motionEvent) {
             *       switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
             *           case MotionEvent.ACTION_DOWN:
             *               view.setBackgroundColor(Color.BLACK);
             *               break;
             *           case MotionEvent.ACTION_MOVE:
             *
             *               break;
             *           case MotionEvent.ACTION_UP:
             *               view.setBackgroundColor(Color.WHITE);
             *
             *               break;
             *       }
             *       return false;
             *   }
             * });
             */

            v.setTag(viewHolder);
        } else {
            //we want to be sure it's not invisible in case of recycling view of dragged item
            v.setVisibility(View.VISIBLE);
            viewHolder = (ViewHolder) v.getTag();
        }

        Action action = mObjects.get(position);

        viewHolder.position = position;
        viewHolder.title.setText(action.getTitle());
        viewHolder.imageView.setImageBitmap(action.getBitmapOfView(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight()));
        viewHolder.imageView.setContentDescription(action.getContentDescription());
        viewHolder.imageView.invalidate();

        return v;
    }

    private class ViewHolder {
        // Position in the list
        public int position;

        // Handle to title TextView
        public TextView title;

        // Handle to content ImageView
        public ImageView imageView;

        public ViewHolder(View base) {
            title = (TextView) base.findViewById(R.id.action_title);
            imageView = (ImageView) base.findViewById(R.id.action_imgContent);
        }
    }

    /**
     * Find the first element with a tag going up the view hierarchy
     *
     * @param v     element from which to start
     * @param limit limit of interactions to avoid going up too much
     * @return the closest ancestor of {@code v} that has a tag, or {@code null} if not found in {@code limit} interactions
     */
    private static Object getRootTag(View v, int limit) {
        for (int l = limit; l > 0; l--) {
            if (v.getTag() == null) {
                v = (View) v.getParent();
            } else {
                return v.getTag();
            }
        }
        // Root view not found
        return null;
    }

    /**
     * Swaps two elements in the ArrayList
     *
     * @param indexOne index of the first element
     * @param indexTwo index of the second element
     */
    public void swapElements(int indexOne, int indexTwo) {
        Action temp = mObjects.get(indexOne);

        mObjects.set(indexOne, mObjects.get(indexTwo));
        mObjects.set(indexTwo, temp);

        notifyDataSetChanged();
    }
}
