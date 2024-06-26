package ideamc.titleplugin.GUI;

import ideamc.titleplugin.TitlePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static ideamc.titleplugin.GUI.biyao.readshopdatabase;
import static ideamc.titleplugin.GUI.biyao.createShopTitleItem;
import static ideamc.titleplugin.Title.BuyTitle.buycoin;
import static ideamc.titleplugin.Title.BuyTitle.buypoint;
import static ideamc.titleplugin.TitlePlugin.instance;

public class ShopGui implements Listener {
    private static final int itemsPerPage = 45; // 每页45个物品
    private static int totalPages;
    private static int currentPage;
    private static Inventory gui;
    private static List<biyao.TitleData> titles;

    public ShopGui(TitlePlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void showShopGui(CommandSender sender) {
        titles = readshopdatabase((Player) sender);
        totalPages = (titles.size() + itemsPerPage - 1) / itemsPerPage;
        currentPage = 0;

        gui = Bukkit.createInventory(null, 54, "称号商店");

        refillInventory((Player) sender);
    }
    private static void refillInventory(Player player) {
        // 清空当前Inventory
        if (gui != null) {
            gui.clear();
        }

        // 重新填充当前页的物品
        for (int i = currentPage * itemsPerPage; i < Math.min((currentPage + 1) * itemsPerPage, titles.size()); i++) {
            biyao.TitleData titleData = titles.get(i);
            ItemStack titleItem = createShopTitleItem(titleData);
            gui.addItem(titleItem);
        }

        // 保持导航按钮状态不变
        if (totalPages > 1) {
            //前一页item
            ItemStack previousPageItem = new ItemStack(Material.BOOK);
            ItemMeta previousPagemeta = previousPageItem.getItemMeta();
            previousPagemeta.setDisplayName("前一页");
            previousPageItem.setItemMeta(previousPagemeta);
            //后一页item
            ItemStack nextPageItem = new ItemStack(Material.BOOK);
            ItemMeta nextPagemeta = nextPageItem.getItemMeta();
            nextPagemeta.setDisplayName("后一页");
            nextPageItem.setItemMeta(nextPagemeta);

            if (currentPage == 0) {
                gui.setItem(50, nextPageItem);
            } else if (currentPage > 0) {
                gui.setItem(48, previousPageItem);
                gui.setItem(50, nextPageItem);
            } else if (currentPage == totalPages) {
                gui.setItem(48, previousPageItem);
            }
        }

        player.openInventory(gui); // 重新打开Inventory以更新视图
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.equals(gui)) {
            event.setCancelled(true); // 防止玩家直接拿取物品

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.NAME_TAG) {
                String stitle_id = event.getCurrentItem().getItemMeta().getDisplayName();
                int title_id = Integer.parseInt(stitle_id);
                String sql = "SELECT * FROM Title WHERE title_id = '" + title_id + "'";
                List<biyao.TitleData> rs = instance.getDatabase().readQuery(sql, player, "title");
                if (rs != null) {
                    for (biyao.TitleData t : rs) {
                        String type = t.getType();
                        if (type.equals("coin")) {
                            buycoin(player, title_id);
                            player.closeInventory();
                        } else if (type.equals("points")) {
                            buypoint(player, title_id);
                            player.closeInventory();
                        } else {
                            player.sendMessage("[TitlePlugin]§4此称号不能购买");
                            player.closeInventory();
                        }
                    }
                }
            }

            int slot = event.getRawSlot();

            if (slot == 48) { // 前一页按钮被点击
                if (currentPage > 0) {
                    currentPage--;
                    refillInventory(player);
                }
            } else if (slot == 50) { // 后一页按钮被点击
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    refillInventory(player);
                }
            }
        }
    }
}