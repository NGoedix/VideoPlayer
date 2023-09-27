package com.github.NGoedix.watchvideo.commands.arguments;

import com.google.gson.JsonObject;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class SymbolStringArgumentSerializer implements ArgumentSerializer<SymbolStringArgumentType> {

   @Override
   public void serializeToNetwork(SymbolStringArgumentType pArgument, FriendlyByteBuf pBuffer) {

   }

   public SymbolStringArgumentType deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      // No specific data to deserialize for this type, so just return a new template.
      return SymbolStringArgumentType.symbolString();
   }

   @Override
   public void serializeToJson(SymbolStringArgumentType pArgument, JsonObject pJson) {

   }
}