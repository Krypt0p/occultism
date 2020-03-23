/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.container;

import com.github.klikli_dev.occultism.common.tile.DimensionalMineshaftTileEntity;
import com.github.klikli_dev.occultism.exceptions.ItemHandlerMissingException;
import com.github.klikli_dev.occultism.registry.OccultismContainers;
import com.github.klikli_dev.occultism.registry.OccultismRecipes;
import com.github.klikli_dev.occultism.util.RecipeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class DimensionalMineshaftContainer extends Container {

    //region Fields
    public ItemStackHandler inputHandler;
    public ItemStackHandler outputHandler;
    public DimensionalMineshaftTileEntity otherworldMiner;
    public PlayerInventory playerInventory;
    //endregion Fields

    //region Initialization
    public DimensionalMineshaftContainer(int id, PlayerInventory playerInventory,
                                         DimensionalMineshaftTileEntity otherworldMiner) {
        super(OccultismContainers.OTHERWORLD_MINER.get(), id);
        this.playerInventory = playerInventory;
        this.otherworldMiner = otherworldMiner;
        this.inputHandler = otherworldMiner.inputHandler.orElseThrow(ItemHandlerMissingException::new);
        this.outputHandler = otherworldMiner.outputHandler.orElseThrow(ItemHandlerMissingException::new);

        this.setupPlayerInventorySlots(playerInventory.player);
        this.setupPlayerHotbar(playerInventory.player);
        this.setupMinerInventory();
    }
    //endregion Initialization

    //region Overrides
    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return player.getDistanceSq(this.otherworldMiner.getPos().getX() + 0.5D,
                this.otherworldMiner.getPos().getY() + 0.5D,
                this.otherworldMiner.getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.outputHandler.getSlots()) {
                if (!this.mergeItemStack(itemstack1, this.outputHandler.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, this.outputHandler.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
    //endregion Overrides

    //region Methods
    protected void setupPlayerInventorySlots(PlayerEntity player) {
        int playerInventoryTop = 84;
        int playerInventoryLeft = 8;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new Slot(player.inventory, j + i * 9 + 9, playerInventoryLeft + j * 18,
                        playerInventoryTop + i * 18));
    }

    protected void setupPlayerHotbar(PlayerEntity player) {
        int hotbarTop = 142;
        int hotbarLeft = 8;
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(player.inventory, i, hotbarLeft + i * 18, hotbarTop));
    }

    protected void setupMinerInventory() {
        int outputGridTop = 17;
        int outputGridLeft = 98;
        int index = 0;

        IItemHandler outputHandler = this.otherworldMiner.outputHandler.orElseThrow(ItemHandlerMissingException::new);
        IItemHandler inputHandler = this.otherworldMiner.inputHandler.orElseThrow(ItemHandlerMissingException::new);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(
                        new OutputSlot(outputHandler, index++, outputGridLeft + j * 18, outputGridTop + i * 18));
            }
        }

        this.addSlot(new InputSlot(inputHandler, 0, 26, 35));
    }
    //endregion Methods

    public class InputSlot extends SlotItemHandler {

        //region Initialization
        public InputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
        //endregion Initialization

        //region Overrides
        public boolean isItemValid(ItemStack stack) {
            RecipeManager recipeManager = DimensionalMineshaftContainer.this.otherworldMiner.getWorld().getRecipeManager();
            return RecipeUtil.isValidIngredient(recipeManager, OccultismRecipes.MINER_TYPE.get(), stack);
        }
        //endregion Overrides
    }

    public class OutputSlot extends SlotItemHandler {
        //region Initialization
        public OutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
        //endregion Initialization

        //region Overrides
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
        //endregion Overrides
    }
}
