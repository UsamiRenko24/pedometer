package com.demo.app.launcher.ui.adapter.discover;

import androidx.annotation.Nullable;

import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;

/** Discover 信息流里的一行。 */
public final class DiscoverFeedRow {

    public final int type;
    @Nullable
    public final GameItemUi game;
    @Nullable
    public final GameSectionUi section;

    private DiscoverFeedRow(int type, @Nullable GameItemUi game, @Nullable GameSectionUi section) {
        this.type = type;
        this.game = game;
        this.section = section;
    }

    public static DiscoverFeedRow marker(int type) {
        return new DiscoverFeedRow(type, null, null);
    }

    public static DiscoverFeedRow game(int type, GameItemUi game) {
        return new DiscoverFeedRow(type, game, null);
    }

    public static DiscoverFeedRow section(int type, GameSectionUi section) {
        return new DiscoverFeedRow(type, null, section);
    }
}
