package me.pk2.pherob.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tree {

    private final Block baseBlock;
    private final Block[] treeBlocks;
    private final Material[] oldBlockTypes;
    private final Byte[] oldBlocksData;

    public Tree(Block baseBlock, Block... treeBlocks) {
        this.baseBlock = baseBlock;
        this.treeBlocks = treeBlocks;
        this.oldBlockTypes = Arrays.stream(treeBlocks).map(Block::getType).toArray(Material[]::new);
        this.oldBlocksData = Arrays.stream(treeBlocks).map(Block::getData).toArray(Byte[]::new);
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Block[] getTreeBlocks() {
        return this.treeBlocks;
    }

    public void breakTree() {
        for (Block newTreeBlock : this.treeBlocks) newTreeBlock.breakNaturally();
    }

    public void placeTree() {
        for (int i = 0; i < treeBlocks.length; i++) treeBlocks[i].setType(oldBlockTypes[i]);
    }

    public void regenerateTree() {
        for (int i = 0; i < treeBlocks.length; i++)
            if (treeBlocks[i].getType() == null || treeBlocks[i].getType() == Material.AIR) {
                Material oldBlock = oldBlockTypes[i];
                byte oldData = oldBlocksData[i];
                Block treeBlock = treeBlocks[i];
                treeBlock.setType(oldBlock);
                treeBlock.setData(oldData);
            }
    }

    public static Tree getTreeAt(Block baseBlock) {
        List<Block> treeBlocks = new ArrayList<>();

        List<Block> logBlocks = new ArrayList<>();

        int currentCheckedBlocks = 0;
        Block latestBlock;

        while (baseBlock.getRelative(BlockFace.UP, currentCheckedBlocks).getType() == baseBlock.getType()) {
            logBlocks.add(baseBlock.getRelative(BlockFace.UP, currentCheckedBlocks));
            currentCheckedBlocks++;
        }

        latestBlock = logBlocks.get(logBlocks.size() - 1);

        List<Block> leafBlocks = BlockUtil.getBlocks(latestBlock, 3).stream().filter(relativeBlock -> relativeBlock.getType() == Material.LEAVES || relativeBlock.getType() == Material.LEAVES_2).collect(Collectors.toList());

        if (logBlocks.size() <= 2 || leafBlocks.isEmpty()) return null;

        treeBlocks.addAll(logBlocks);
        treeBlocks.addAll(leafBlocks);
        Block[] arr = treeBlocks.toArray(new Block[0]);

        treeBlocks.clear();
        return new Tree(baseBlock, arr);
    }

    @Override
    public String toString() {
        return "Tree{" +
                "baseBlock=" + baseBlock.getLocation() +
                ", treeBlocks=" + Arrays.toString(Arrays.stream(treeBlocks).map(Block::getType).toArray(Material[]::new)) +
                '}';
    }
}