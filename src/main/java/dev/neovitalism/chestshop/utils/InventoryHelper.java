package dev.neovitalism.chestshop.utils;

import dev.neovitalism.chestshop.shop.ShopItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class InventoryHelper {
    public static boolean canInsert(Inventory inv, List<ItemStack> stacks) {
        stacks = stacks.stream().map(ItemStack::copy).toList();
        DefaultedList<ItemStack> contents = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.size(); i++) {
            if (inv instanceof PlayerInventory && i >= 36) break;
            contents.set(i, inv.getStack(i).copy());
        }
        for (ItemStack stack : stacks) {
            for (int i = 0; i < contents.size(); i++) {
                if (stack.isEmpty()) break;
                ItemStack content = contents.get(i);
                if (content.isEmpty()) {
                    contents.set(i, stack.copy());
                    stack.setCount(0);
                } else {
                    int toCombine = InventoryHelper.amountToFit(content, stack);
                    if (toCombine == 0) continue;
                    content.setCount(content.getCount() + toCombine);
                    stack.setCount(stack.getCount() - toCombine);
                }
            }
            if (stack.getCount() != 0) return false;
        }
        return true;
    }

    public static void insert(Inventory inv, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            for (int i = 0; i < inv.size(); i++) {
                if (inv instanceof PlayerInventory && i >= 36) break;
                if (stack.isEmpty()) break;
                ItemStack content = inv.getStack(i);
                if (content.isEmpty()) {
                    inv.setStack(i, stack.copy());
                    stack.setCount(0);
                } else {
                    int toCombine = InventoryHelper.amountToFit(content, stack);
                    if (toCombine == 0) continue;
                    content.setCount(content.getCount() + toCombine);
                    stack.setCount(stack.getCount() - toCombine);
                }
            }
        }
    }

    private static int amountToFit(ItemStack stack, ItemStack other) {
        if (stack.isEmpty()) return other.getCount();
        if (!ItemStack.areItemsAndComponentsEqual(stack, other)) return 0;
        return Math.min(stack.getMaxCount() - stack.getCount(), other.getCount());
    }

    public static int countInventory(Inventory inv, ShopItem shopItem) {
        int count = 0;
        for (int j = 0; j < inv.size(); ++j) {
            ItemStack item = inv.getStack(j);
            if (shopItem.matches(item)) count += item.getCount();
        }
        return count;
    }

    public static List<ItemStack> getMatchingItems(Inventory inv, ShopItem shopItem, boolean remove) {
        List<ItemStack> items = new ArrayList<>();
        int toRemove = shopItem.quantity();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack item = inv.getStack(i);
            if (shopItem.matches(item)) {
                int left = item.getCount() - toRemove;
                if (left >= 0) {
                    items.add(item.copyWithCount(toRemove));
                    if (remove) item.setCount(left);
                    break;
                } else {
                    items.add(item.copy());
                    toRemove -= item.getCount();
                    if (remove) item.setCount(0);
                }
            }
        }
        return items;
    }

    public static boolean canFitMatchingItems(Inventory inventory, Inventory other, ShopItem shopItem) {
        return InventoryHelper.canInsert(inventory, InventoryHelper.getMatchingItems(other, shopItem, false));
    }
}
