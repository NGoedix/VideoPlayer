package com.github.NGoedix.videoplayer.commands.arguments;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class SymbolStringArgumentSerializer implements ArgumentSerializer<SymbolStringArgumentType, SymbolStringArgumentSerializer.Template> {

   @Override
   public void writePacket(Template properties, PacketByteBuf buf) {}

   @Override
   public Template fromPacket(PacketByteBuf buf) {
      return new Template();
   }

   @Override
   public void writeJson(Template properties, JsonObject json) {}

   @Override
   public Template getArgumentTypeProperties(SymbolStringArgumentType argumentType) {
      return new Template();
   }

   public final class Template implements ArgumentSerializer.ArgumentTypeProperties<SymbolStringArgumentType> {
      @Override
      public SymbolStringArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return SymbolStringArgumentType.symbolString();
      }

      @Override
      public ArgumentSerializer<SymbolStringArgumentType, ?> getSerializer() {
         return SymbolStringArgumentSerializer.this;
      }
   }
}