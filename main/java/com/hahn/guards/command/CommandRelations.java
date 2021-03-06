package com.hahn.guards.command;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.hahn.guards.GuardEventHandler;
import com.hahn.guards.util.GuardColor;

public class CommandRelations extends CommandBase {

	@Override
	public String getCommandName() {
		return "relations";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		if (sender instanceof EntityPlayer) {
            return "/relations  Print your relations with other players";
        } else {
        	return "Only players can use /relations";
        }
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (sender instanceof EntityPlayer) {
			Map<String, Byte> relations = GuardEventHandler.getRelationsMap(sender.getCommandSenderName());
			if (relations.size() == 0) {
				sender.addChatMessage(new ChatComponentText("You have no relations"));
			} else {
				sender.addChatMessage(new ChatComponentText("Your relations:"));
				for (Entry<String, Byte> r: relations.entrySet()) {
					String state = (r.getValue() < 0 ? "WAR" : "PEACE");
					String name = r.getKey();
					
					ChatComponentText chat = new ChatComponentText(name);
					GuardColor c = GuardEventHandler.getColor(name);
					chat.getChatStyle().setColor(EnumChatFormatting.getValueByName(c.name()));
					
					ChatComponentText chatMain = new ChatComponentText(" [" + state + "] " + r.getValue());
					chat.appendSibling(chatMain);
					
					sender.addChatMessage(chat);
				}
			}
		}
	}

}
