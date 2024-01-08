package com.github.NGoedix.videoplayer.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
   private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

   public static ByteBuffer create(int pSize) {
      long i = ALLOCATOR.malloc(pSize);
      if (i == 0L) {
         throw new OutOfMemoryError("Failed to allocate " + pSize + " bytes");
      } else {
         return MemoryUtil.memByteBuffer(i, pSize);
      }
   }
}