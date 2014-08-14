package tsteelworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import nf.fr.ephys.cookiecore.helpers.RenderHelper;
import nf.fr.ephys.cookiecore.util.MultiFluidTank;
import org.lwjgl.opengl.GL11;
import tsteelworks.common.blocks.logic.DeepTankLogic;
import tsteelworks.common.container.DeepTankContainer;
import tsteelworks.common.network.PacketMoveFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class DeepTankGui extends GuiContainer {
	public static final ResourceLocation BACKGROUND = new ResourceLocation("tsteelworks", "textures/gui/deeptank.png");

	private static final int TANK_WIDTH = 104;
	private static final int TANK_HEIGHT = 104;
	private static final int TANK_YPOS = 120;
	private static final int TANK_XPOS = 54;

	public DeepTankGui(InventoryPlayer inventoryplayer, DeepTankLogic tank) {
		super(new DeepTankContainer(inventoryplayer, tank));

		xSize = 248;
		ySize = 136;
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		final String title = StatCollector.translateToLocal("tank.DeepTank");

		fontRendererObj.drawString(title, (xSize / 2) - (fontRendererObj.getStringWidth(title) / 2), 17, 0x404040);

		MultiFluidTank fluidTank = ((DeepTankContainer) inventorySlots).getLogic().getFluidTank();

		final String capacity = String.format(StatCollector.translateToLocal("tank.capacity"), fluidTank.getFluidAmount(), fluidTank.getCapacity());

		fontRendererObj.drawString(capacity, (xSize / 2) - (fontRendererObj.getStringWidth(capacity) / 2), ySize - 1, 0x404040);

		FluidStack hoveredStack = getFluidAtPos(mouseX, mouseY);

		if (hoveredStack != null)
			drawFluidStackTooltip(hoveredStack, mouseX - guiLeft, mouseY - guiTop);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BACKGROUND);

		drawTexturedModalRect(guiLeft + 46, guiTop, 0, 0, 120, ySize);

		//Liquids
		RenderHelper.loadBlockMap();

		MultiFluidTank tank = ((DeepTankContainer) inventorySlots).getLogic().getFluidTank();

		float yBottom = guiTop + TANK_HEIGHT;
		for (int i = 0; i < tank.getNbFluids(); i++) {
			FluidStack liquid = tank.getFluid(i);
			IIcon icon = liquid.getFluid().getStillIcon();

			float liquidSize = (float) liquid.amount / tank.getCapacity() * TANK_HEIGHT;

			// the render is still broken because we're clipping float to ints and it causes imprecision
			RenderHelper.drawTexturedRect(icon, guiLeft + TANK_XPOS, TANK_WIDTH, (int) yBottom, (int) Math.ceil(liquidSize), zLevel);

			yBottom -= Math.round(liquidSize);
		}

		//Liquid gauge
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.mc.getTextureManager().bindTexture(BACKGROUND);
		drawTexturedModalRect(guiLeft + 54, guiTop + 16, 120, 0, 104, 104);
	}

	protected void drawFluidStackTooltip(FluidStack liquid, int x, int z) {
		List<String> tooltips = new ArrayList<>();

		tooltips.add(liquid.getFluid().getLocalizedName(liquid));
		tooltips.add(EnumChatFormatting.LIGHT_PURPLE + "mB: " + liquid.amount);

		drawHoveringText(tooltips, x, z, fontRendererObj);
	}

	private FluidStack getFluidAtPos(int posX, int posY) {
		final int leftX = guiLeft + TANK_XPOS;

		if (posX < leftX || posX > leftX + TANK_WIDTH)
			return null;

		MultiFluidTank fluidTank = ((DeepTankContainer) inventorySlots).getLogic().getFluidTank();

		if (fluidTank.getCapacity() == 0) return null;

		final int bottomY = guiTop + TANK_YPOS + TANK_HEIGHT;

		float liquidOffset = 0;
		for (int i = 0; i < fluidTank.getNbFluids(); i++) {
			FluidStack stack = fluidTank.getFluid(i);

			float liquidSize = (float) stack.amount / fluidTank.getCapacity() * TANK_HEIGHT;

			if (posY >= bottomY - (liquidSize + liquidOffset) && posY < bottomY - liquidOffset) {
				return stack;
			}

			liquidOffset += liquidSize;
		}

		return null;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		FluidStack fluid = getFluidAtPos(mouseX, mouseY);

		if (fluid == null) return;

		DeepTankLogic logic = ((DeepTankContainer) inventorySlots).getLogic();

		PacketMoveFluidHandler.moveFluidGUI(logic, fluid);
	}
}
