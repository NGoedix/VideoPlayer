package com.github.NGoedix.watchvideo.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class SymbolStringArgumentSerializer implements ArgumentTypeInfo<SymbolStringArgumentType, SymbolStringArgumentSerializer.Template> {

   public void serializeToNetwork(SymbolStringArgumentSerializer.Template pTemplate, FriendlyByteBuf pBuffer) {
      // No specific data to serialize for this type, so this method does nothing.
   }

   public SymbolStringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      // No specific data to deserialize for this type, so just return a new template.
      return new SymbolStringArgumentSerializer.Template();
   }

   public void serializeToJson(SymbolStringArgumentSerializer.Template pTemplate, JsonObject pJson) {
      // No specific data to serialize for this type, so this method does nothing.
   }

   public SymbolStringArgumentSerializer.Template unpack(SymbolStringArgumentType pArgument) {
      // No specific data to unpack for this type, so just return a new template.
      return new SymbolStringArgumentSerializer.Template();
   }

   public final class Template implements ArgumentTypeInfo.Template<SymbolStringArgumentType> {

      public SymbolStringArgumentType instantiate(CommandBuildContext pContext) {
         return SymbolStringArgumentType.symbolString();
      }

      public ArgumentTypeInfo<SymbolStringArgumentType, ?> type() {
         return SymbolStringArgumentSerializer.this;
      }
   }
}