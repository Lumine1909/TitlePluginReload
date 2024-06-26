package ideamc.titleplugin;

import ideamc.titleplugin.GUI.biyao;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static ideamc.titleplugin.TitlePlugin.instance;


public class Papi extends PlaceholderExpansion {

    private final TitlePlugin plugin;

    public Papi(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "suxiaolin"; //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "TitlePlugin";
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    @NotNull
    public String onRequest(OfflinePlayer player, String params) {
        UUID player_uuid = player.getUniqueId();
        String stplayer_uuid = player_uuid.toString();
        if (params.equalsIgnoreCase("player_prefix")) {
            String sql = "SELECT * FROM PlayerTitle WHERE prefix_enable = true AND player_uuid = '" + stplayer_uuid + "'";
            List<biyao.TitleData> rs = instance.getDatabase().readQuery(sql, null, "playertitle");
            if (rs != null) {
                for (biyao.TitleData t : rs) {
                    int title_id = t.getTitleId();
                    String sql1 = "SELECT * FROM Title WHERE title_id = " + title_id;
                    List<biyao.TitleData> rs1 = instance.getDatabase().readQuery(sql1, null, "title");
                    if (rs1 != null) {
                        for (biyao.TitleData t1 : rs1) {
                            return t1.getTitleName();
                        }
                    }
                }
            }
        } else if (params.equalsIgnoreCase("player_suffix")) {
            String sql = "SELECT * FROM PlayerTitle WHERE suffix_enable = true AND player_uuid = '" + stplayer_uuid + "'";
            List<biyao.TitleData> rs = instance.getDatabase().readQuery(sql, null, "playertitle");
            if (rs != null) {
                for (biyao.TitleData t : rs) {
                    int title_id = t.getTitleId();
                    String sql1 = "SELECT * FROM Title WHERE title_id = " + title_id;
                    List<biyao.TitleData> rs1 = instance.getDatabase().readQuery(sql1, null, "title");
                    if (rs1 != null) {
                        for (biyao.TitleData t1 : rs1) {
                            return t1.getTitleName();
                        }
                    }
                }
            }
        }

        return "";
    }
}