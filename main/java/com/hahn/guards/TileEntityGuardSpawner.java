package com.hahn.guards;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.Vec3;

public class TileEntityGuardSpawner extends TileEntityDispenser {
	private static final int SPAWN_DELAY = 60;
	private static final int AMNT_NEEDED = 3 * 16;
	
	private int spawnDelay = SPAWN_DELAY;
	private String ownerName;
	
	public void setOwnerName(String name) {
		this.ownerName = name;
		
		if (!worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	public String getOwnerName() {
		return this.ownerName;
	}
	
	private int getNeeded() {
		int numGuards = GuardEventHandler.getNumGuards(worldObj, getOwnerName());
		
		// Return amount needed
		if (numGuards > 1) {
			return (int) Math.ceil(AMNT_NEEDED * (numGuards * numGuards));
		} else {
			return AMNT_NEEDED;
		}
	}
	
	private int getAvaliable() {
		int amnt = 0;
        for (int i=0; i < this.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemFood) {
            	// reduce amnt by item heal amount
            	ItemFood food = (ItemFood) stack.getItem();
                amnt += food.func_150905_g(stack) * stack.stackSize;
            }
        }
        
        return amnt;
    }
	
	private void consume(int amnt) {
		for (int i=0; i < this.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemFood) {
            	// reduce amnt by item heal amount
            	ItemFood food = (ItemFood) stack.getItem();
            	int healAmnt = food.func_150905_g(stack);
            	
            	// Amount need to take from this stack to use all amnt
            	int amntToTake = (int) Math.ceil((double) amnt / healAmnt);
            	
            	if (amntToTake < stack.stackSize) {
            		amnt -= healAmnt * amntToTake;
            		stack.stackSize -= amntToTake;
            	} else if (amntToTake >= stack.stackSize) {
            		amnt -= healAmnt * stack.stackSize;
            		this.setInventorySlotContents(i, null);
            	}
            }
            
            if (amnt <= 0) return;
        }
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (!worldObj.isRemote && --spawnDelay <= 0) {
			spawnDelay = 60;
			
			int has = getAvaliable();
			int need = getNeeded();
			
			if (has >= need) {
				consume(need);
				
				EntityIronGolem golem = new EntityStoneGolem(worldObj, getOwnerName());
				
				golem.setPosition(this.xCoord, this.yCoord, this.zCoord);
				Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(golem, 6, 4, Vec3.createVectorHelper(this.xCoord, this.yCoord, this.zCoord));
				golem.setPosition(vec3.xCoord, vec3.yCoord + 1, vec3.zCoord);
				
				GuardEventHandler.addNumGuards(worldObj, getOwnerName(), 1);
				
				worldObj.spawnEntityInWorld(golem);
			}
		}
	}
	
	@Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        
        if (getOwnerName() != null) {
        	nbt.setString("ownerName", getOwnerName());
        }
    }
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt.hasKey("ownerName")) {
			setOwnerName(nbt.getString("ownerName"));
		}
	}
	
	@Override
	public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        
        if (getOwnerName() != null) {
        	nbt.setString("ownerName", getOwnerName());
        }
		
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

	@Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		super.onDataPacket(net, packet);
		
		NBTTagCompound nbt = packet.func_148857_g();
		if (nbt.hasKey("ownerName")) {
			setOwnerName(nbt.getString("ownerName"));
		}
    }
	
	@Override
	public boolean isItemValidForSlot(int par1, ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemFood;
    }
	
	@Override
	public boolean canUpdate() {
        return true;
    }
	
	@Override
	public String getInventoryName() {
        return "Guard Spawner";
    }
}