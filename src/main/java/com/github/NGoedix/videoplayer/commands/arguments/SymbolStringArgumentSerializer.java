package com.github.NGoedix.videoplayer.commands.arguments;

import com.google.gson.JsonObject;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class SymbolStringArgumentSerializer implements ArgumentSerializer<SymbolStringArgumentType> {

   @Override
   public void toPacket(SymbolStringArgumentType type, PacketByteBuf buf) {

   }

   @Override
   public SymbolStringArgumentType fromPacket(PacketByteBuf buf) {
      return SymbolStringArgumentType.symbolString();
   }

   @Override
   public void toJson(SymbolStringArgumentType type, JsonObject json) {

   }
}