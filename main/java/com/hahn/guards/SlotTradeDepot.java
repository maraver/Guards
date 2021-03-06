package com.hahn.guards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class SlotTradeDepot extends Slot {

	public SlotTradeDepot(IInventory inventory, int par2, int par3, int par4) {
		super(inventory, par2, par3, par4);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return super.isItemValid(stack) && stack.getItem() instanceof ItemFood;
    }
	
}
