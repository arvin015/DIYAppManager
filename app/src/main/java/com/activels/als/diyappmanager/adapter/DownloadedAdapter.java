package com.activels.als.diyappmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.entity.DatasetInfo;
import com.activels.als.diyappmanager.utils.Utils;
import com.activels.als.diyappmanager.view.MyProgressBar;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * Created by arvin.li on 2015/12/10.
 */
public class DownloadedAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<DatasetInfo> datasetInfoList;

    private FinalBitmap fBitmap;

    public DownloadedAdapter(Context context, List<DatasetInfo> datasetInfoList,
                             FinalBitmap fBitmap) {
        this.context = context;
        this.datasetInfoList = datasetInfoList;
        this.fBitmap = fBitmap;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datasetInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return datasetInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final DatasetInfo datasetInfo = datasetInfoList.get(i);

        final ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = inflater.inflate(R.layout.dataset_item, null);
            viewHolder.numText = (TextView) view.findViewById(R.id.numText);
            viewHolder.iconImg = (ImageView) view.findViewById(R.id.iconImg);
            viewHolder.nameText = (TextView) view.findViewById(R.id.nameText);
            viewHolder.descText = (TextView) view.findViewById(R.id.descText);
            viewHolder.typeText = (TextView) view.findViewById(R.id.typeText);
            viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
            viewHolder.sizeText = (TextView) view.findViewById(R.id.sizeText);
            viewHolder.lockBtn = (ImageView) view.findViewById(R.id.lockBtn);
            viewHolder.operateBtn = (MyProgressBar) view.findViewById(R.id.operateBtn);
            viewHolder.checkBtn = (ToggleButton) view.findViewById(R.id.checkBtn);
            viewHolder.handleBtn = (Button) view.findViewById(R.id.handleBtn);

            view.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.checkBtn.setVisibility(View.VISIBLE);
        viewHolder.operateBtn.setVisibility(View.GONE);
        viewHolder.handleBtn.setVisibility(View.GONE);
        viewHolder.checkBtn.setChecked(datasetInfo.isChecked());

        viewHolder.numText.setText("" + (i + 1));
        viewHolder.nameText.setText(datasetInfo.getName());
        viewHolder.descText.setText(datasetInfo.getInfo());
        viewHolder.sizeText.setText(datasetInfo.getSize() + "");
        viewHolder.typeText.setText(context.getResources().getStringArray(R.array.type_arr)[Integer.parseInt(datasetInfo.getType())]);
        viewHolder.dateText.setText(datasetInfo.getCovertDate());

        fBitmap.display(viewHolder.iconImg, Utils.ICON_DIR + datasetInfo.getIcon());

        return view;
    }

    static class ViewHolder {
        ImageView iconImg, lockBtn;
        TextView numText, nameText, descText, dateText, typeText, sizeText;
        MyProgressBar operateBtn;
        ToggleButton checkBtn;
        Button handleBtn;
    }

    /**
     * 设置checkbox选中状态
     *
     * @param index
     * @param checked
     */
    public void setItemChecked(int index, boolean checked) {
        datasetInfoList.get(index).setIsChecked(checked);

        notifyDataSetChanged();
    }

    /**
     * 更新显示数据
     *
     * @param datasetInfoList
     */
    public void updateData(List<DatasetInfo> datasetInfoList) {
        this.datasetInfoList = datasetInfoList;

        notifyDataSetChanged();
    }
}
