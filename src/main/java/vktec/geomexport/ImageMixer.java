// vim: noet

package vktec.geomexport;

import java.nio.ByteBuffer;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.system.MemoryUtil;

public class ImageMixer {
	public static NativeImage composite(NativeImage destImage, NativeImage srcImage) {
		NativeImage.Format format = destImage.getFormat();
		if (format != srcImage.getFormat()) {
			throw new UnsupportedOperationException("Images to composite must have the same format");
		}
		if (!format.hasAlphaChannel()) {
			throw new UnsupportedOperationException("Images to composite must have alpha channel");
		}

		NativeImage target = ImageMixer.clone(destImage);
		ByteBuffer dest = getDataBuffer(target);
		ByteBuffer src = getDataBuffer(srcImage);

		if (dest.limit() != src.limit()) {
			throw new UnsupportedOperationException("Images to composite must be the same size");
		}

		byte[] destPixel = new byte[format.getChannelCount()];
		byte[] srcPixel = new byte[format.getChannelCount()];
		int alphaChannel = destPixel.length - 1;
		while (dest.remaining() >= destPixel.length) {
			dest.mark();
			dest.get(destPixel);
			src.get(srcPixel);

			int alpha = ub2i(srcPixel[alphaChannel]);
			if (alpha > 0) {
				for (int i = 0; i < alphaChannel; i++) {
					destPixel[i] = (byte)((255-alpha) * ub2i(destPixel[i]) + alpha * ub2i(srcPixel[i]) / 255);
				}
			}

			dest.reset();
			dest.put(destPixel);
		}

		return target;
	}

	public static NativeImage tint(NativeImage image, int rgb) {
		NativeImage.Format format = image.getFormat();
		if (format != NativeImage.Format.RGB && format != NativeImage.Format.RGBA) {
			throw new UnsupportedOperationException("Cannot tint a luminance image");
		}

		final int white = 0xFFFFFF;
		if ((rgb & white) == white) return image;

		NativeImage target = ImageMixer.clone(image);
		ByteBuffer buf = getDataBuffer(target);

		int[] color = {rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF};

		byte[] pixel = new byte[format.getChannelCount()];
		while (buf.remaining() >= pixel.length) {
			buf.mark();
			buf.get(pixel);

			for (int i = 0; i < 3; i++) {
				pixel[i] = (byte)(ub2i(pixel[i]) * color[i] / 255);
			}

			buf.reset();
			buf.put(pixel);
		}

		return target;
	}

	public static NativeImage clone(NativeImage image) {
		NativeImage target = new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), false);
		target.copyFrom(image);
		return target;
	}

	private static int ub2i(byte unsignedByte) {
		return (int)unsignedByte & 0xFF;
	}

	private static ByteBuffer getDataBuffer(NativeImage img) {
		return MemoryUtil.memByteBufferSafe(img.pointer, (int)img.sizeBytes);
	}
}
