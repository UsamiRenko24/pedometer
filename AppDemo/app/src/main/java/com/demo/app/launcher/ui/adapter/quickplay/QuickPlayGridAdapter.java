package com.demo.app.launcher.ui.adapter.quickplay;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.demo.app.R;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.ui.view.quickplay.QuickPlayWaveHeaderLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Quick Play 三列网格（含顶部 Play Hub 头图）。 */
public class QuickPlayGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_GAME = 1;
    private static final int ICON_RADIUS_PX = 48;

    public interface Listener {
        void onPlay(@NonNull GameItemUi item);
    }

    private final List<GameItemUi> items = new ArrayList<>();
    private final Listener listener;
    private int headerStatusBarInset;

    public QuickPlayGridAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setHeaderStatusBarInset(int px) {
        if (headerStatusBarInset == px) {
            return;
        }
        headerStatusBarInset = px;
        if (getItemCount() > 0) {
            notifyItemChanged(0);
        }
    }

    public void submit(@NonNull List<GameItemUi> games) {
        items.clear();
        items.addAll(games);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_GAME;
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 0 : items.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_quick_play_header, parent, false));
        }
        return new GameHolder(inflater.inflate(R.layout.item_quick_play_game, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind(headerStatusBarInset);
        } else if (holder instanceof GameHolder) {
            ((GameHolder) holder).bind(items.get(position - 1), listener);
        }
    }

    static final class HeaderHolder extends RecyclerView.ViewHolder {
        private final QuickPlayWaveHeaderLayout header;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.quick_play_header);
        }

        void bind(int statusBarInset) {
            View content = header.findViewById(R.id.quick_play_header_content);
            if (content == null) {
                return;
            }
            int top = statusBarInset + dp(content, 12);
            content.setPadding(
                    content.getPaddingLeft(),
                    top,
                    content.getPaddingRight(),
                    content.getPaddingBottom());
        }

        private static int dp(@NonNull View view, int dp) {
            return Math.round(dp * view.getResources().getDisplayMetrics().density);
        }
    }

    static final class GameHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final TextView rating;
        private final TextView play;

        GameHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.quick_play_game_icon);
            name = itemView.findViewById(R.id.quick_play_game_name);
            rating = itemView.findViewById(R.id.quick_play_game_rating);
            play = itemView.findViewById(R.id.quick_play_game_play);
        }

        void bind(@NonNull GameItemUi item, @NonNull Listener listener) {
            String title = !item.getDisplayTitle().isEmpty()
                    ? item.getDisplayTitle()
                    : item.getName();
            name.setText(title);
            if (item.getRating() > 0) {
                rating.setText(String.format(Locale.US, "%.1f", item.getRating()));
            } else {
                rating.setText("—");
            }
            String iconUrl = !item.getIconUrl().isEmpty() ? item.getIconUrl() : item.getImageUrl();
            if (TextUtils.isEmpty(iconUrl)) {
                icon.setImageDrawable(null);
            } else {
                Glide.with(icon)
                        .load(iconUrl)
                        .transform(new RoundedCorners(ICON_RADIUS_PX))
                        .centerCrop()
                        .into(icon);
            }
            View.OnClickListener open = v -> listener.onPlay(item);
            play.setOnClickListener(open);
            itemView.setOnClickListener(open);
        }
    }
}
