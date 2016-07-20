package us.chiraq.practicepots.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

	public static List<Chunk> chunks = new ArrayList<Chunk>();
	
	@EventHandler
	public void onChunkLoad(ChunkUnloadEvent e) {
		if (chunks.contains(e.getChunk())) {
			e.setCancelled(true);
		}
	}
	
}
