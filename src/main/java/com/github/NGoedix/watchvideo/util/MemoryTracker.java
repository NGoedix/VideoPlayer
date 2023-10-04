package com.github.NGoedix.watchvideo.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.MemoryUtil;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class MemoryTracker {
   public static ByteBuffer create(int pSize) {
      return BufferUtils.createByteBuffer(pSize);
   }
}