package com.jackpf.halo4servicerecord2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServiceRecordParser
{
	private final static String	GAMEMODE_WARGAMES			= "3", //wargames ID
								GAMEMODE_WARGAMES_CUSTOM	= "6"; //wargames custom ID
	
	private Element	document;
	private Map<String, String> stats;
	
	public final static int PARSE_OK					= 0x0,
							PARSE_ERROR_NETWORK			= 0x1,
							PARSE_ERROR_PARSING			= 0x2,
							PARSE_ERROR_PLAYERNOTFOUND	= 0x3,
							PARSE_ERROR_UNKOWN			= 0x4;
	public static int statusCode = PARSE_OK;
	
	//for halo 3?
	public ServiceRecordParser()
	{
		
	}
	
	public ServiceRecordParser(InputStream xmlStream)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try
		{
			if(xmlStream == null)
				throw new Exception(Integer.toString(PARSE_ERROR_NETWORK));
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(xmlStream).getDocumentElement();
			
			statusCode = PARSE_OK;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			statusCode = PARSE_ERROR_NETWORK;
		}
	}
	
	public Map<String, String> getStats()
	{
		if(statusCode > 0)
			return null;
		else if(Integer.parseInt(document.getElementsByTagName("StatusCode").item(0).getFirstChild().getNodeValue()) > 1)
		{
			statusCode = PARSE_ERROR_PLAYERNOTFOUND;
			return null;
		}
		
		stats = new HashMap<String, String>();
		
		try
		{
			getPlayerStats();
			getWeaponStats();
			getWarGamesStats(GAMEMODE_WARGAMES);
			getWarGamesStats(GAMEMODE_WARGAMES_CUSTOM);
		}
		catch(Exception e)
		{
			//System.err.println(e.getMessage());
			statusCode = PARSE_ERROR_PARSING;
			return null;
		}
		
		return stats;
	}
	
	private void getPlayerStats() throws Exception
	{
		if(document == null)
			return;
		
		NodeList statsList = document.getChildNodes();
		
		for(int i = 0; i < statsList.getLength(); i++)
		{
			Node stat = statsList.item(i);
			
			//special cases
			String k = "", v = "";
			if(stat.getNodeName().equals("FavoriteWeaponImageUrl") || stat.getNodeName().equals("EmblemImageUrl"))
			{
				k = stat.getNodeName();
				v = stat.getChildNodes().item(1).getFirstChild().getNodeValue();
				
				stats.put(k, v);
			}
			else if(stat.getNodeName().equals("TopMedals"))
			{
				NodeList medals = stat.getChildNodes();
				
				for(int j = 0; j < medals.getLength(); j++)
				{
					Element medal = (Element) medals.item(j);
					
					k = "Medal" + (j + 1);
					v = ((Element) medal.getElementsByTagName("ImageUrl").item(0)).getElementsByTagName("AssetUrl").item(0).getFirstChild().getNodeValue();
					
					stats.put(k, v);
					
					//name & description as well
					k = "Medal" + (j + 1) + "_name";
					v = ((Element) medal.getElementsByTagName("Name").item(0)).getFirstChild().getNodeValue();
					
					stats.put(k, v);
					
					k = "Medal" + (j + 1) + "_description";
					v = ((Element) medal.getElementsByTagName("Description").item(0)).getFirstChild().getNodeValue();
					
					stats.put(k, v);
				}
			}
			else if(stat.getNodeName().equals("Specializations"))
			{
				NodeList specialisations = stat.getChildNodes();
				
				int completedCount = 0;
				
				for(int j = 0; j < specialisations.getLength(); j++)
				{
					Element specialisation = (Element) specialisations.item(j);
					
					String	name = specialisation.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue(),
							current = specialisation.getElementsByTagName("IsCurrent").item(0).getFirstChild().getNodeValue(),
							level = specialisation.getElementsByTagName("Level").item(0).getFirstChild().getNodeValue(),
							completed = specialisation.getElementsByTagName("Completed").item(0).getFirstChild().getNodeValue(),
							asset = specialisation.getElementsByTagName("ImageUrl").item(0).getChildNodes().item(1).getFirstChild().getNodeValue();
					
					if(current.equals("true"))
					{
						k = "SpecialisationCurrent";
						v = name;
						
						stats.put(k, v);
						
						k = "SpecialisationLevel";
						v = level;
						
						stats.put(k, v);
					}
					
					if(completed.equals("true"))
					{
						completedCount++;
						
						k = "SpecialisationCompleted" + completedCount;
						v = asset;
						
						stats.put(k, v);
						
						//name & description as well
						k = "SpecialisationCompleted" + completedCount + "_name";
						v = ((Element) specialisation.getElementsByTagName("Name").item(0)).getFirstChild().getNodeValue();
						
						stats.put(k, v);
						
						k = "SpecialisationCompleted" + completedCount + "_description";
						v = ((Element) specialisation.getElementsByTagName("Description").item(0)).getFirstChild().getNodeValue();
						
						stats.put(k, v);
					}
				}
			}
			else if(stat.getNodeName().equals("TopSkillRank"))
			{
				k = stat.getNodeName();
				
				if(stat.hasChildNodes())
				{
					NodeList topSkillRank = stat.getChildNodes();
					
					v = topSkillRank.item(0).getFirstChild().getNodeValue();
				}
				else
					v = "0";
				
				stats.put(k, v);
			}
			else if(stat.getNodeName().equals("SkillRanks"))
			{
				NodeList playlists = stat.getChildNodes();
				
				for(int j = 0; j < playlists.getLength(); j++)
				{
					Element playlist = (Element) playlists.item(j);

					k = "PlaylistName" + (j + 1);
					v = ((Element) playlist.getElementsByTagName("PlaylistName").item(0)).getFirstChild().getNodeValue();
					
					stats.put(k, v);
					
					Element csr = (Element) playlist.getElementsByTagName("CurrentSkillRank").item(0);
					k = "PlaylistCSR" + (j + 1);
					v = (csr.hasChildNodes()) ? csr.getFirstChild().getNodeValue() : "0";
					
					stats.put(k, v);
				}
			}
			else
			{
				k = stat.getNodeName();
				v = (stat.getFirstChild() != null) ? stat.getFirstChild().getNodeValue() : stat.getNodeValue();
				
				stats.put(k, v);
			}
		}
	}
	
	private void getWeaponStats() throws Exception
	{
		//
	}
	
	private void getWarGamesStats(String mode) throws Exception
	{
		if(document == null)
			return;
		
		NodeList gameModes = document.getElementsByTagName("GameModes").item(0).getChildNodes();
		
		Node warGames = null;
		
		for(int i = 0; i < gameModes.getLength(); i++)
		{
			Node gameMode = gameModes.item(i);
			
			if(gameMode.getAttributes().getNamedItem("i:type").getNodeValue().equals("WarGamesDetailsFull") &&
			   gameMode.getFirstChild().getFirstChild().getNodeValue().equals(mode))
			{
				warGames = gameMode;
				break;
			}
		}
		
		if(warGames == null)
		{
			System.err.println("Could not find wargames stats");
			return;
		}
		
		NodeList statsList = warGames.getChildNodes();
		
		for(int i = 0; i < statsList.getLength(); i++)
		{
			Node stat = statsList.item(i);
			stats.put(((mode.equals(GAMEMODE_WARGAMES)) ? "Wargames" : "WargamesCustom") + stat.getNodeName(), stat.getFirstChild().getNodeValue());
		}
	}
	
	public Map<Integer, Map<String, String>> getRecentGamesStats()
	{
		if(statusCode > 0)
			return null;
		
		Map<Integer, Map<String, String>> recentGames = new HashMap<Integer, Map<String, String>>();
		
		NodeList games = document.getElementsByTagName("Game");
		
		for(int i = 0; i < games.getLength(); i++)
		{
			Element game = (Element) games.item(i);
			NodeList gameStats = game.getChildNodes();

			Map<String, String> entry = new HashMap<String, String>();
			
			for(int j = 0; j < gameStats.getLength(); j++)
			{
				if(gameStats.item(j).getNodeType() == Node.ELEMENT_NODE)
					entry.put(gameStats.item(j).getNodeName(), gameStats.item(j).getTextContent());
			}
			
			recentGames.put(i, entry);
		}
		
		return recentGames;
	}
	
	//parses 343's silly time format
	public static String parseTime(String time)
	{
		time = time.replaceAll("P|T", "").
					replaceAll("[0-9]+S", "").
					replaceAll("([0-9]+)", " $1").
					replaceAll("D", "d").
					replaceAll("H", "h").
					replaceAll("M", "m");
		
		return (time.length() > 0) ? time : "-";
	}
}
