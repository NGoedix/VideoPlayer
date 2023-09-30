package com.github.NGoedix.watchvideo.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class SymbolStringArgumentSerializer implements IArgumentSerializer<SymbolStringArgumentType> {
   @Override
   public void serializeToNetwork(SymbolStringArgumentType pArgument, PacketBuffer pBuffer) {

   }

   public SymbolStringArgumentType deserializeFromNetwork(PacketBuffer pBuffer) {
      // No specific data to deserialize for this type, so just return a new template.
      return SymbolStringArgumentType.symbolString();
   }

   @Override
   public void serializeToJson(SymbolStringArgumentType pArgument, JsonObject pJson) {

   }
}