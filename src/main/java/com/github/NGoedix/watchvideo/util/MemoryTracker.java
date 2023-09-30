package com.github.NGoedix.watchvideo.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@OnlyIn(Dist.CLIENT)
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