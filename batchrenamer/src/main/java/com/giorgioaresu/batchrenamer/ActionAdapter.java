package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ActionAdapter extends BaseAdapter {

    //public SparseBooleanArray animating = new SparseBooleanArray();

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<Action> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();

    private Context mContext;

    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context The current context.
     */
    public ActionAdapter(Context context) {
        init(context, new ArrayList<Action>());
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public ActionAdapter(Context context, List<Action> objects) {
        init(context, objects);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(Action object) {
        synchronized (mLock) {
            mObjects.add(object);
        }
        notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(Action object, int index) {
        synchronized (mLock) {
            mObjects.add(index, object);
        }
        notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(Action object) {
        synchronized (mLock) {
            mObjects.remove(object);
        }
        notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
        }
        notifyDataSetChanged();
    }

    private void init(Context context, List<Action> objects) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
    }

    @Override
    public int getViewTypeCount() {
        return Action.ActionIDS.COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    public Action getItem(int position) {
        if (position < 0 || position >= mObjects.size()) {
            return null;
        }

        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(Action item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        //first get the action from our data model
        ActionHolder holder;

        //don't have a convert view so we're going to have to create a new one
        if (convertView == null) {
            convertView = mInflater.inflate(getItem(position).getViewId(), parent, false);

            //using the ViewHolder pattern to reduce lookups
            // TODO: Differentiate holder based on item type
            holder = new ActionHolder();
            holder.title = (TextView) convertView.findViewById(R.id.action_title);

            // If user clicks on trash icon we remove that list item
            convertView.findViewById(R.id.action_remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionHolder ah = (ActionHolder) getRootTag(v, 5);
                    if (ah != null) {
                        remove(getItem(ah.position));
                    }
                }
            });

            /*convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            //v.setBackgroundColor(Color.BLACK);
                            break;
                        case MotionEvent.ACTION_MOVE:

                            break;
                        case MotionEvent.ACTION_UP:
                            //v.setBackgroundColor(Color.WHITE);

                            break;
                    }
                    return false;
                }
            });*/

            convertView.setTag(holder);
        } else {
            //we have a convertView so we're just going to use it's content

            //we want to be sure it's not invisible in case of recycling view of dragged item
            convertView.setVisibility(View.VISIBLE);

            //get the holder so we can set the image
            holder = (ActionHolder) convertView.getTag();
        }

        //actually set the contents based on our action
        holder.title.setText(mObjects.get(position).getTitle() + position);
        holder.position = position;

        return convertView;
    }

    static class ActionHolder {
        TextView title;
        int position;
    }

    /**
     * Find the first element with a tag going up the view hyerarchy
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
