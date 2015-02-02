package info.jbcs.minecraft.waypoints;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

public class WaypointPlayerInfo {
	static HashMap<String,WaypointPlayerInfo> objects=new HashMap<String,WaypointPlayerInfo>();
	static File location;
	
	HashMap<Integer,Integer> discoveredWaypoints=new HashMap<Integer,Integer>();
	String username;
	boolean changed=false;
	
	File getFile(){
		return new File(location,username+".dat");
	}
	
	static WaypointPlayerInfo get(String nn){
		WaypointPlayerInfo info=objects.get(nn);
		if(info!=null) return info;

		if(location==null) return null;
		location.mkdirs();
		
		info=new WaypointPlayerInfo(nn);
		objects.put(nn, info);
		
		File file=info.getFile();
		if(! file.exists()) return info;
		
		try {
			info.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	void addWaypoint(int id){
		discoveredWaypoints.put(id, 1);
		changed=true;
	}
	void removeWaypoint(int id){
		discoveredWaypoints.remove(id);
		changed=true;
	}
	
	private void read(File file) throws IOException {
		RandomAccessFile input=new RandomAccessFile(file,"rw");
		input.seek(0);
		NBTTagCompound tag=(NBTTagCompound) NBTTagCompound.readNamedTag(input);
		input.close();
		
		int count=tag.getInteger("count");
		for(int i=0;i<count;i++){
			int id=tag.getInteger(""+i);
			addWaypoint(id);
		}
	}
	
	private void write(File file) throws IOException {
		int index=0;
		
		RandomAccessFile output=new RandomAccessFile(file,"rw");
		output.seek(0);
		NBTTagCompound tag=new NBTTagCompound();
		Set<Integer> keys=discoveredWaypoints.keySet();
		tag.setInteger("count", keys.size());
		for(Integer id: keys){
			tag.setInteger(""+(index++), id);
		}
		
		NBTTagCompound.writeNamedTag(tag, output);
		output.setLength(output.getFilePointer());
		output.close();

		changed=false;
	}

	WaypointPlayerInfo(String n){
		username=n.replaceAll("[^\\p{L}\\p{Nd}_]", "");
	}

	public static void writeAll() throws IOException {
		for(WaypointPlayerInfo info: objects.values()){
			if(info.changed)
				info.write(info.getFile());
		}
	}
}
