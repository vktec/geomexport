// vim: noet

package vktec.geomexport;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.GuiUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.MinecraftClient;
import java.util.Arrays;
import java.io.IOException;

public class InputHandler implements IKeybindProvider, IMouseInputHandler, IHotkeyCallback {
	private static final InputHandler INSTANCE = new InputHandler();

	public static InputHandler getInstance() { return INSTANCE; }

	private InputHandler() {
		for (ConfigHotkey hk : Hotkeys.hotkeys) {
			hk.getKeybind().setCallback(this);
		}
	}

	@Override
	public void addKeysToMap(IKeybindManager manager) {
		for (ConfigHotkey hk : Hotkeys.hotkeys) {
			manager.addKeybindToMap(hk.getKeybind());
		}
	}

	@Override
	public void addHotkeys(IKeybindManager manager) {
		manager.addHotkeysForCategory("geomexport", "geomexport.hotkeys.category.all", Arrays.asList(Hotkeys.hotkeys));
	}

	@Override
	public boolean onKeyAction(KeyAction action, IKeybind key) {
		if (key == Hotkeys.TOGGLE_FOCUSED_CORNER.getKeybind()) {
			Selection.toggleFocus();
			return true;
		} else if (key == Hotkeys.MOVE_FOCUSED_CORNER.getKeybind()) {
			Selection.setFocused(MinecraftClient.getInstance().player.getBlockPos());
			return true;
		} else if (key == Hotkeys.WRITE_FILE.getKeybind()) {
			try (BlocksWriter bw = new BlocksWriter("testy_file")) {
				bw.writeRegion(MinecraftClient.getInstance().world, Selection.a, Selection.b);
			} catch (IOException e) {
				System.out.println("code broken yuou fuckface");
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onMouseClick(int mouseX, int mouseY, int eventButton, boolean eventButtonState) {
		return false;
	}

	@Override
	public boolean onMouseScroll(int mouseX, int mouseY, double _dWheel) {
		MinecraftClient mc = MinecraftClient.getInstance();

		if (GuiUtils.getCurrentScreen() != null) return false;
		if (mc.world == null) return false;
		if (mc.player == null) return false;

		int dWheel = _dWheel > 0 ? 1 : -1;
		Direction dir = Direction.getEntityFacingOrder(mc.player)[0];

		if (Hotkeys.SELECTION_GROWTH_MOD.getKeybind().isKeybindHeld()) {
			Selection.setFocused(Selection.getFocused().offset(dir, dWheel));
			return true;
		}

		return false;
	}
}