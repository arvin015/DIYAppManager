package com.activels.als.diyappmanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.activels.als.diyappmanager.R;

/**
 * Created by arvin.li on 2015/11/11.
 */
public class TypeAdapter extends BaseAdapter {

    private Context context;

    private String[] contents;

    private int selectedIndex = 0;

    public TypeAdapter(Context context, String[] contents) {
        this.context = context;
        this.contents = contents;
    }

    @Override
    public int getCount() {
        return contents.length;
    }

    @Override
    public Object getItem(int i) {
        return contents[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (view == null) {
            viewHolder = new ViewHolder();

            view = LayoutInflater.from(context).inflate(R.layout.type_item, null);

            viewHolder.textView = (TextView) view.findViewById(R.id.typeText);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.choicedImg);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.textView.setText(contents[i]);

        if (selectedIndex == i) {
            viewHolder.textView.setTextColor(context.getResources().getColor(R.color.selected_blue_color));
            viewHolder.imageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.textView.setTextColor(Color.BLACK);
        }

        return view;
    }

    /**
     * 设置被选中的类型索引
     *
     * @param index
     */
    public void setSelectedIndex(int index) {

        this.selectedIndex = index;

        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
