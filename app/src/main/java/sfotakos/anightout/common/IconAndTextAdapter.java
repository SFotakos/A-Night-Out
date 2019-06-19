package sfotakos.anightout.common;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import sfotakos.anightout.R;

// Well done tutorial https://www.codingdemos.com/android-custom-spinner-images-text/
// TODO change spinner icon as seen on https://stackoverflow.com/a/37461505/5075144
public class IconAndTextAdapter extends ArrayAdapter<String> {

    // TODO expose icon and text styling

    private Context mContext;
    private List<String> textList; //TODO turn description into resID
    private List<Integer> iconResIdList;

    private boolean displayIcon;

    public IconAndTextAdapter(@NonNull Context context, int layout, @NonNull List<String> textList, @NonNull List<Integer> iconResIdList) {
        super(context, layout);
        this.mContext = context;
        this.textList = textList;
        this.iconResIdList = iconResIdList;

        displayIcon = true;
    }

    public IconAndTextAdapter(@NonNull Context context, int layout, @NonNull List<String> textList) {
        super(context, layout);
        this.mContext = context;
        this.textList = textList;

        displayIcon = false;
    }

    @Override
    public int getCount() {
        return textList.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent, android.R.color.white);
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent, int backgroundColorId) {
        LayoutInflater inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder = new ViewHolder();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.spinner_icon_and_text, parent, false);

            holder.root = convertView.findViewById(R.id.spinner_root_linearLayout);
            holder.text = convertView.findViewById(R.id.spinner_text_textView);
            holder.icon = convertView.findViewById(R.id.spinner_icon_imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.root.setBackgroundColor(backgroundColorId);
        holder.text.setText(textList.get(position));

        if (displayIcon) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageDrawable(
                    ContextCompat.getDrawable(holder.icon.getContext(), iconResIdList.get(position)));
        } else {
            holder.icon.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent, R.color.colorPrimary);
    }

    // https://developer.android.com/training/improving-layouts/smooth-scrolling#ViewHolder
    private static class ViewHolder {
        LinearLayout root;
        ImageView icon;
        TextView text;
    }


}
