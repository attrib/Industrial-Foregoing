/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.industrial.block.resourceproduction.tile;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.tile.RangeManager;
import com.buuz135.industrial.config.machine.resourceproduction.BlockBreakerConfig;
import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.utils.BlockUtils;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class BlockBreakerTile extends IndustrialAreaWorkingTile<BlockBreakerTile> {

    private int getMaxProgress;
    private int getPowerPerOperation;

    @Save
    private SidedInventoryComponent<BlockBreakerTile> output;

    public BlockBreakerTile() {
        super(ModuleResourceProduction.BLOCK_BREAKER, RangeManager.RangeType.BEHIND, false,  BlockBreakerConfig.powerPerOperation);
        this.addInventory(this.output = (SidedInventoryComponent<BlockBreakerTile>) new SidedInventoryComponent<BlockBreakerTile>("output", 54, 22, 3 * 6, 0).
                setColor(DyeColor.ORANGE).
                setRange(6, 3));
        this.getMaxProgress = BlockBreakerConfig.maxProgress;
        this.getPowerPerOperation = BlockBreakerConfig.powerPerOperation;
    }

    @Override
    public WorkAction work() {
        if (hasEnergy(getPowerPerOperation)) {
            if (isLoaded(getPointedBlockPos()) && !world.isAirBlock(getPointedBlockPos()) && BlockUtils.canBlockBeBroken(this.world, getPointedBlockPos())) {
                FakePlayer fakePlayer = IndustrialForegoing.getFakePlayer(this.world, getPointedBlockPos());
                fakePlayer.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
                if (this.world.getBlockState(this.getPointedBlockPos()).getBlockHardness(this.world, this.getPointedBlockPos()) >= 0 && this.world.getBlockState(getPointedBlockPos()).canHarvestBlock(this.world, getPointedBlockPos(), fakePlayer)) {
                    for (ItemStack blockDrop : BlockUtils.getBlockDrops(this.world, getPointedBlockPos())) {
                        ItemStack result = ItemHandlerHelper.insertItem(output, blockDrop, false);
                        if (!result.isEmpty()) {
                            BlockUtils.spawnItemStack(result, this.world, getPointedBlockPos());
                        }
                    }
                    this.world.setBlockState(getPointedBlockPos(), Blocks.AIR.getDefaultState());
                    increasePointer();
                    return new WorkAction(1, getPowerPerOperation);
                }
            } else {
                increasePointer();
            }
        }
        return new WorkAction(1, 0);
    }

    @Override
    protected EnergyStorageComponent<BlockBreakerTile> createEnergyStorage() {
        return new EnergyStorageComponent<>(BlockBreakerConfig.maxStoredPower, 10, 20);
    }

    @Override
    public int getMaxProgress() {
        return getMaxProgress;
    }

    @Nonnull
    @Override
    public BlockBreakerTile getSelf() {
        return this;
    }
}
