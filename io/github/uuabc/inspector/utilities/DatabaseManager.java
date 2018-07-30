package io.github.uuabc.inspector.utilities;

import com.google.gson.*;
import org.spongepowered.api.service.sql.*;
import org.spongepowered.api.*;
import javax.sql.*;
import org.spongepowered.api.block.*;
import io.github.uuabc.inspector.Inspector;

import org.spongepowered.api.entity.living.player.*;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;
import java.sql.*;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import org.spongepowered.api.world.*;
import com.google.common.collect.*;
import java.util.*;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.InvalidDataException;

public class DatabaseManager {
	private static boolean mysqle;
	private static Connection connInsert;
	private static boolean tran = false;
	private static long begintime;
	private static Map<UUID, Integer> Worldmapuuid = new HashMap<UUID, Integer>();
	private static Map<Integer, UUID> Worldmapid = new HashMap<Integer, UUID>();
	private static Map<String, Integer> Playermapuuid = new HashMap<String, Integer>();
	private static Map<Integer, String> Playermapid = new HashMap<Integer, String>();
	private static Map<Integer, String> Playermapidn = new HashMap<Integer, String>();
	private static Map<String, String> Playermapnameuuid = new HashMap<String, String>();
	private static Map<Integer, String> BLOCKID = new HashMap<Integer, String>();
	private static Map<String, Integer> BLOCKST = new HashMap<String, Integer>();

	public DatabaseManager() {
		new GsonBuilder().create();
	}

	private Connection getDatabaseConnection() throws SQLException {
		Connection connection = null;
		if (connection == null || connection.isClosed()) {
			final SqlService sql = (SqlService) Sponge.getServiceManager().provide(SqlService.class).get();

			if (((String) Utils.getConfigValue("database.database")).equals("mysql")) {
				DatabaseManager.mysqle = true;

				final String host = (String) Utils.getConfigValue("database.mysql.host");
				final String port = (String) Utils.getConfigValue("database.mysql.port");
				final String username = (String) Utils.getConfigValue("database.mysql.username");
				final String password = (String) Utils.getConfigValue("database.mysql.password");
				final String database = (String) Utils.getConfigValue("database.mysql.database");
				final DataSource datasource = sql.getDataSource("jdbc:mysql://" + host + ":" + port + "/" + database
						+ "?user=" + username + "&password=" + password);
				connection = datasource.getConnection();
			} else if (((String) Utils.getConfigValue("database.database")).equals("h2")) {
				DatabaseManager.mysqle = true;
				DataSource datasource = sql.getDataSource("jdbc:h2:./Inspector/h2inspector.db");
				connection = datasource.getConnection();
			} else if (((String) Utils.getConfigValue("database.database")).equals("sqlite")) {
				DatabaseManager.mysqle = false;
				DataSource datasource = sql.getDataSource("jdbc:sqlite:./Inspector/sqliteInspector.db");
				connection = datasource.getConnection();

			}

		}
		return connection;
	}

	public void gamestartdb() {
		startdb();
		getBLOCKdb();
		addWorld();
		getPlayerdb();
	}

	private void startdb() {
		Statement stmt;
		String sql;
		try {
			Connection c = this.getDatabaseConnection();
			stmt = c.createStatement();
			if (DatabaseManager.mysqle) {
				sql = "CREATE TABLE IF NOT EXISTS INSP_BLOCKINFO(id int primary key auto_increment,x INT(8) NOT NULL,y INT(3) NOT NULL,z INT(8) NOT NULL,Worldid int(2), PLAYERID INT NOT NULL, TIME timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, OLDBLOCK INT(8) NOT NULL, NEWBLOCK INT(8) NOT NULL)";
			} else {
				sql = "CREATE TABLE IF NOT EXISTS INSP_BLOCKINFO(id  INTEGER PRIMARY KEY autoincrement not null,x INT(8) NOT NULL,y INT(3) NOT NULL,z INT(8) NOT NULL,Worldid int(2), PLAYERID INT NOT NULL, TIME timestamp NOT NULL DEFAULT (datetime('now','localtime')), OLDBLOCK INT(8) NOT NULL, NEWBLOCK INT(8) NOT NULL)";
			}
			stmt.executeUpdate(sql);
			stmt.close();

			stmt = c.createStatement();
			if (DatabaseManager.mysqle) {
				sql = "CREATE TABLE IF NOT EXISTS INSP_Container(id int primary key auto_increment,x INT(8) NOT NULL,y INT(3) NOT NULL,z INT(8) NOT NULL,Worldid int(2), PLAYERID INT NOT NULL, TIME timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, oldItem INT, newItem INT)";
			} else {
				sql = "CREATE TABLE IF NOT EXISTS INSP_Container(id  INTEGER PRIMARY KEY autoincrement not null,x INT(8) NOT NULL,y INT(3) NOT NULL,z INT(8) NOT NULL,Worldid int(2), PLAYERID INT NOT NULL, TIME timestamp NOT NULL DEFAULT (datetime('now','localtime')), oldItem INT, newItem INT)";
			}
			stmt.executeUpdate(sql);
			stmt.close();

			stmt = c.createStatement();
			if (DatabaseManager.mysqle) {
				sql = "CREATE TABLE IF NOT EXISTS insp_blockstate(id int primary key auto_increment,blockstate TEXT NOT NULL)";
			} else {
				sql = "CREATE TABLE IF NOT EXISTS insp_blockstate(id  INTEGER PRIMARY KEY autoincrement,blockstate TEXT NOT NULL)";
			}
			stmt.executeUpdate(sql);
			stmt.close();
			stmt = c.createStatement();
			if (DatabaseManager.mysqle) {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS INSP_WORLD(id  INTEGER(2) PRIMARY KEY AUTO_INCREMENT,worldUUID varchar(40) unique,worldname varchar(40))");
			} else {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS INSP_WORLD(id  INTEGER PRIMARY KEY AUTOINCREMENT,worldUUID varchar(40) unique,worldname varchar(40))");
			}
			stmt.close();

			stmt = c.createStatement();
			if (DatabaseManager.mysqle) {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS INSP_PLAYERS(ID INTEGER PRIMARY KEY AUTO_INCREMENT, UUID varchar(100) unique NOT NULL, NAME varchar(40) NOT NULL,TIME timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)");
			} else {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS INSP_PLAYERS(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, UUID unique TEXT NOT NULL, NAME TEXT NOT NULL,TIME timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)");
			}
			stmt.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateBlockInformation(final int x, final int y, final int z, final UUID worldUUID,
			final String playerUUID, final String playerName, final BlockState oldBlockState,
			final BlockState newBlockState) {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			int oldb = getBLOCKID(oldBlockState.toString());
			int newb = getBLOCKID(newBlockState.toString());
			int pid = this.getPlayerId(playerUUID);
			if (pid == -1) {
				addPlayerToDatabase(playerUUID, playerName);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pid = this.getPlayerId(playerUUID);
			}

			Integer wUUID = Worldmapuuid.get(worldUUID);
			if (wUUID == null) {
				addWorld();
				wUUID = Worldmapuuid.get(worldUUID);
				if (wUUID == null)
					wUUID = -1;
			}

			try {
				if (!DatabaseManager.tran || DatabaseManager.connInsert == null
						|| DatabaseManager.connInsert.isClosed()) {
					DatabaseManager.connInsert = this.getDatabaseConnection();
					DatabaseManager.connInsert.setAutoCommit(false);
					DatabaseManager.begintime = System.currentTimeMillis();
					DatabaseManager.tran = true;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			String sql2 = "INSERT INTO INSP_BLOCKINFO (x,y,z,Worldid,PLAYERID,OLDBLOCK,NEWBLOCK) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement stmtInsert;
			try {
				stmtInsert = DatabaseManager.connInsert.prepareStatement(sql2);
				stmtInsert.setInt(1, x);
				stmtInsert.setInt(2, y);
				stmtInsert.setInt(3, z);
				stmtInsert.setInt(4, wUUID);
				stmtInsert.setInt(5, pid);
				stmtInsert.setInt(6, oldb);
				stmtInsert.setInt(7, newb);
				stmtInsert.execute();
				stmtInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (DatabaseManager.tran && DatabaseManager.begintime + 2000 < System.currentTimeMillis()) {
				while (true) {
					try {
						DatabaseManager.connInsert.commit();
						DatabaseManager.connInsert.close();
						DatabaseManager.tran = false;
						break;
					} catch (SQLiteException e) {
						if (e.getResultCode() == SQLiteErrorCode.SQLITE_BUSY) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							continue;
						}
						break;
					} catch (SQLException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}).submit((Object) Inspector.instance());
	}

	public void updateContainerInformation(final int x, final int y, final int z, final UUID worldUUID,
			final String playerUUID, String playerName, final ItemStackSnapshot oldItemStackSnapshot,
			final ItemStackSnapshot newItemStackSnapshot) {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			int pid = this.getPlayerId(playerUUID);
			if (pid == -1) {
				addPlayerToDatabase(playerUUID, playerName);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pid = this.getPlayerId(playerUUID);
			}
			DataContainer oldItem = oldItemStackSnapshot.createStack().toContainer();
			DataContainer newItem = newItemStackSnapshot.createStack().toContainer();

			Integer oldI = null;
			Integer newI = null;
			try {
				oldI = getBLOCKID(DataFormats.JSON.write(oldItem));
				newI = getBLOCKID(DataFormats.JSON.write(newItem));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			Integer wUUID = Worldmapuuid.get(worldUUID);
			if (wUUID == null) {
				addWorld();
				wUUID = Worldmapuuid.get(worldUUID);
			}

			try {
				if (!DatabaseManager.tran || DatabaseManager.connInsert == null
						|| DatabaseManager.connInsert.isClosed()) {
					DatabaseManager.connInsert = this.getDatabaseConnection();
					DatabaseManager.connInsert.setAutoCommit(false);
					DatabaseManager.begintime = System.currentTimeMillis();
					DatabaseManager.tran = true;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			String sql2 = "INSERT INTO INSP_Container (x,y,z,Worldid,PLAYERID,oldItem,newItem) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement stmtInsert;
			try {
				stmtInsert = DatabaseManager.connInsert.prepareStatement(sql2);
				stmtInsert.setInt(1, x);
				stmtInsert.setInt(2, y);
				stmtInsert.setInt(3, z);
				stmtInsert.setInt(4, wUUID);
				stmtInsert.setInt(5, pid);
				stmtInsert.setInt(6, oldI);
				stmtInsert.setInt(7, newI);
				stmtInsert.execute();
				stmtInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}).submit((Object) Inspector.instance());
	}

	public static void conInsertclose() {

		try {
			if (connInsert == null || connInsert.isClosed()) {
				return;
			}
			connInsert.commit();
			connInsert.close();
			DatabaseManager.tran = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addWorld() {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			getWorlddb();
			try {
				if (!DatabaseManager.tran || DatabaseManager.connInsert == null
						|| DatabaseManager.connInsert.isClosed()) {
					DatabaseManager.connInsert = this.getDatabaseConnection();
					DatabaseManager.connInsert.setAutoCommit(false);
					DatabaseManager.begintime = System.currentTimeMillis();
					DatabaseManager.tran = true;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			final Iterator<World> ite = Sponge.getServer().getWorlds().iterator();
			while (ite.hasNext()) {
				World i = ite.next();
				if (Worldmapuuid.get(i.getUniqueId()) == null) {
					String sql = "INSERT INTO INSP_WORLD (worldUUID, worldname) VALUES ('" + i.getUniqueId() + "','"
							+ i.getName() + "');";
					try {
						Statement stmt = DatabaseManager.connInsert.createStatement();
						stmt.executeUpdate(sql);
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}
			getWorlddb();
		}).submit((Object) Inspector.instance());
	}

	private void getWorlddb() {
		try {
			Worldmapuuid.clear();
			Worldmapid.clear();
			Connection c = Inspector.instance().getDatabaseManager().getDatabaseConnection();
			final PreparedStatement stmt1 = c.prepareStatement("SELECT * FROM INSP_WORLD");
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			final ResultSet rs = stmt1.executeQuery();
			while (rs.next()) {
				Worldmapuuid.put(UUID.fromString(rs.getString("worldUUID")), rs.getInt("id"));
				Worldmapid.put(rs.getInt("id"), UUID.fromString(rs.getString("worldUUID")));
			}
			rs.close();
			stmt1.close();
			c.close();
		} catch (SQLException var6) {
			var6.printStackTrace();
		}
	}

	private int getBLOCKID(final String blockstate) {
		Integer bl = BLOCKST.get(blockstate);
		if (bl == null) {

			try {
				final Connection c = this.getDatabaseConnection();
				final Statement stmt = c.createStatement();
				String sql = "INSERT INTO insp_blockstate (blockstate) VALUES ('" + blockstate + "')";
				conInsertclose();
				stmt.executeUpdate(sql);
				stmt.close();

				final PreparedStatement preparedStmt = c
						.prepareStatement("SELECT * from insp_blockstate where blockstate=?");
				preparedStmt.setString(1, blockstate);
				conInsertclose();
				final ResultSet rs = preparedStmt.executeQuery();
				while (rs.next()) {
					BLOCKST.put(rs.getString("blockstate"), rs.getInt("id"));
					BLOCKID.put(rs.getInt("id"), rs.getString("blockstate"));
				}
				rs.close();
				preparedStmt.close();
				bl = BLOCKST.get(blockstate);
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return bl;
	}

	private void getBLOCKdb() {
		try {
			Connection c = Inspector.instance().getDatabaseManager().getDatabaseConnection();
			final PreparedStatement stmt1 = c.prepareStatement("SELECT * FROM insp_blockstate");
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			final ResultSet rs = stmt1.executeQuery();
			while (rs.next()) {
				BLOCKST.put(rs.getString("blockstate"), rs.getInt("id"));
				BLOCKID.put(rs.getInt("id"), rs.getString("blockstate"));
			}
			rs.close();
			stmt1.close();
			c.close();
		} catch (SQLException var6) {
			var6.printStackTrace();
		}
	}

	private String getBLOCKSTATE(final int blockid) {
		return BLOCKID.get(blockid);
	}

	public boolean isPlayerInDatabase(final Player player) {
		boolean isInDatabase = false;
		if (Playermapuuid.isEmpty()) {
			getPlayerdb();
		}
		if (Playermapuuid.get(player.getUniqueId().toString()) != null) {
			return true;
		}
		return isInDatabase;
	}

	public void addPlayerToDatabase(final String puuid, String pname) {

		try {
			if (!DatabaseManager.tran || DatabaseManager.connInsert == null || DatabaseManager.connInsert.isClosed()) {
				DatabaseManager.connInsert = this.getDatabaseConnection();
				DatabaseManager.connInsert.setAutoCommit(false);
				DatabaseManager.begintime = System.currentTimeMillis();
				DatabaseManager.tran = true;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		String sql = "INSERT INTO INSP_PLAYERS (UUID, NAME) VALUES ('" + puuid + "','" + pname + "')";
		try {
			DatabaseManager.connInsert.createStatement().executeUpdate(sql);
			final PreparedStatement preparedStmt = DatabaseManager.connInsert.prepareStatement("SELECT * from INSP_PLAYERS where UUID = ? "); 
			preparedStmt.setString(1, puuid);
			final ResultSet rs = preparedStmt.executeQuery();
			while (rs.next()) {
				DatabaseManager.Playermapuuid.put(rs.getString("UUID"), rs.getInt("id"));
				DatabaseManager.Playermapid.put(rs.getInt("id"), rs.getString("UUID"));
				DatabaseManager.Playermapidn.put(rs.getInt("id"), rs.getString("name"));
				DatabaseManager.Playermapnameuuid.put(rs.getString("name"), rs.getString("UUID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void getPlayerdb() {
		try {
			Connection c = this.getDatabaseConnection();
			final PreparedStatement preparedStmt = c.prepareStatement("SELECT * from INSP_PLAYERS");
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			final ResultSet rs = preparedStmt.executeQuery();
			while (rs.next()) {
				DatabaseManager.Playermapuuid.put(rs.getString("UUID"), rs.getInt("id"));
				DatabaseManager.Playermapid.put(rs.getInt("id"), rs.getString("UUID"));
				DatabaseManager.Playermapidn.put(rs.getInt("id"), rs.getString("name"));
				DatabaseManager.Playermapnameuuid.put(rs.getString("name"), rs.getString("UUID"));
			}
			rs.close();
			preparedStmt.close();
			c.close();
		} catch (SQLException var7) {
			var7.printStackTrace();
		}
	}

	public String getPlayeUUIDfrName(String name) {
		String playeruuid = DatabaseManager.Playermapnameuuid.get(name);

		return playeruuid;
	}

	public int getPlayerId(final String uniqueId) {
		int id = -1;
		try {
			id = Playermapuuid.get(uniqueId);
		} catch (Exception a) {
			id = -1;
		}
		return id;
	}

	private String getPlayerUniqueId(final int id) {
		String uuid;
		uuid = Playermapid.get(id);
		return uuid;
	}

	public String getPlayerName(final int id) {
		String name = "";
		name = Playermapidn.get(id);
		return name;
	}

	private ItemStack deserializeItemStack(final String json) {
		DataContainer container = null;
		try {
			container = DataFormats.JSON.read(json);
		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		return ItemStack.builder().build((DataView) container).get();

	}

	public ArrayList<ItemStackInformation> getContainerInformationAt(final Location<World> location) {
		final ArrayList<ItemStackInformation> ItemInformation = Lists.newArrayList();
		try {
			Connection e = this.getDatabaseConnection();
			final PreparedStatement stmt = e
					.prepareStatement("SELECT * FROM INSP_Container WHERE x=? AND y=? AND z=? AND worldid=?");
			stmt.setInt(1, location.getBlockX());
			stmt.setInt(2, location.getBlockY());
			stmt.setInt(3, location.getBlockZ());
			stmt.setInt(4, Worldmapuuid.get(((World) location.getExtent()).getUniqueId()));
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			final ResultSet rs = stmt.executeQuery();

			Timestamp blocktime;

			while (rs.next()) {

				if (DatabaseManager.mysqle) {
					blocktime = rs.getTimestamp("time");
				} else {
					blocktime = Timestamp.valueOf(rs.getString("time"));
				}

				final ItemStack oldItemStack = this.deserializeItemStack(getBLOCKSTATE(rs.getInt("oldItem")));
				final ItemStack newItemStack = this.deserializeItemStack(getBLOCKSTATE(rs.getInt("newItem")));
				ItemInformation.add(new ItemStackInformation(oldItemStack, newItemStack, blocktime,
						this.getPlayerUniqueId(rs.getInt("playerid")), this.getPlayerName(rs.getInt("playerid"))));
			}
			rs.close();
			stmt.close();
			e.close();
		} catch (SQLException var9) {
			var9.printStackTrace();
		}
		return ItemInformation;
	}

	public ArrayList<BlockViewInfo> getBlockInformationAt(final Location<World> location) {
		final ArrayList<BlockViewInfo> BlockViewInformation = Lists.newArrayList();
		try {
			Connection e = this.getDatabaseConnection();
			final PreparedStatement stmt = e
					.prepareStatement("SELECT * FROM INSP_BLOCKINFO WHERE x=? AND y=? AND z=? AND worldid=?");
			stmt.setInt(1, location.getBlockX());
			stmt.setInt(2, location.getBlockY());
			stmt.setInt(3, location.getBlockZ());
			stmt.setInt(4, Worldmapuuid.get(((World) location.getExtent()).getUniqueId()));
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			final ResultSet rs = stmt.executeQuery();

			Timestamp blocktime;

			while (rs.next()) {

				if (DatabaseManager.mysqle) {
					blocktime = rs.getTimestamp("time");
				} else {
					blocktime = Timestamp.valueOf(rs.getString("time"));
				}

				final BlockSnapshot oldBlockSnapshot = this
						.deserializeBlockSnapshot(dbstrTOJ(location.getExtent().getUniqueId(), rs.getInt("x"),
								rs.getInt("y"), rs.getInt("z"), rs.getInt("oldBlock")));
				final BlockSnapshot newBlockSnapshot = this
						.deserializeBlockSnapshot(dbstrTOJ(location.getExtent().getUniqueId(), rs.getInt("x"),
								rs.getInt("y"), rs.getInt("z"), rs.getInt("newBlock")));
				String oldId;
				BlockType oldType;
				if (oldBlockSnapshot == null) {
					oldType = null;
					oldId = getBLOCKSTATE(rs.getInt("oldBlock"));
				} else {
					BlockState oldState = oldBlockSnapshot.getState();
					oldType = oldState.getType();
					oldId = oldState.getId();
				}
				BlockType newType;
				String newId;
				if (newBlockSnapshot == null) {
					newType = null;
					newId = getBLOCKSTATE(rs.getInt("newBlock"));
				} else {

					BlockState newState = newBlockSnapshot.getState();
					newType = newState.getType();
					newId = newState.getId();
				}
				BlockViewInformation.add(new BlockViewInfo(oldType, newType, oldId, newId, blocktime,
						this.getPlayerName(rs.getInt("playerid")), this.getPlayerUniqueId(rs.getInt("playerid"))));
			}
			rs.close();
			stmt.close();
			e.close();
		} catch (SQLException var9) {
			var9.printStackTrace();
		}
		return BlockViewInformation;
	}

	private DataContainer dbstrTOJ(UUID worlduuid, int x, int y, int z, final int Blockid) {
		String s = "{\"ContentVersion\":1,\"WorldUuid\":\"%s\",\"Position\":{\"X\":\"%d\",\"Y\":\"%d\",\"Z\":\"%d\"},\"BlockState\":{\"ContentVersion\":2,\"BlockState\":\"";
		s = String.format(s, worlduuid, x, y, z);
		s = s + "%s\"}}";
		s = String.format(s, getBLOCKSTATE(Blockid));
		DataContainer container = DataContainer.createNew();
		try {
			container = DataFormats.JSON.read(s);
			// Inspector.instance().getLogger().info(container.toString());

		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		;
		return container;
	}

	private int min(int xmin, int xmax) {
		if (xmin < xmax)
			return xmin;
		else
			return xmax;
	}

	private int max(int xmin, int xmax) {
		if (xmax > xmin)
			return xmax;
		else
			return xmin;
	}

	public List<BlockInformation> getBlockInformationAt(final UUID worlduuid, final String playerUniqueId, String time,
			int xmmin, int xmmax, int ymmin, int ymmax, int zmmin, int zmmax) {
		final List<BlockInformation> blockInformation = Lists.newArrayList();
		final int playerId = this.getPlayerId(playerUniqueId);

		int xmin = min(xmmin, xmmax);
		int xmax = max(xmmin, xmmax);
		int ymin = min(ymmin, ymmax);
		int ymax = max(ymmin, ymmax);
		int zmin = min(zmmin, zmmax);
		int zmax = max(zmmin, zmmax);
		/*
		 * Inspector.instance().getLogger().info(xmin + ":" + xmax +":"+ ymin +":"+ ymax
		 * + ":"+zmin +":"+ zmax + ":" + time + ":" + Worldmapuuid.get(worlduuid) + ":"
		 * + playerId+"===="+playerUniqueId);
		 */
		try {
			final Connection c = this.getDatabaseConnection();
			ResultSet rs;
			final PreparedStatement stmt;
			if (playerUniqueId == null) {
				stmt = c.prepareStatement(
						"select * from insp_blockinfo where id in (SELECT min(id) FROM INSP_BLOCKINFO WHERE x >= ? and x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ? AND Worldid=? AND  time >= ? GROUP BY x,y,z)");
				stmt.setInt(1, xmin);
				stmt.setInt(2, xmax);
				stmt.setInt(3, ymin);
				stmt.setInt(4, ymax);
				stmt.setInt(5, zmin);
				stmt.setInt(6, zmax);
				stmt.setInt(7, Worldmapuuid.get(worlduuid));
				stmt.setString(8, time);
			} else {
				stmt = c.prepareStatement(
						"select * from insp_blockinfo where id in (SELECT min(id) FROM INSP_BLOCKINFO WHERE x >= ? and x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ? AND Worldid=? AND playerId=? AND time >= ? GROUP BY x,y,z)");
				stmt.setInt(1, xmin);
				stmt.setInt(2, xmax);
				stmt.setInt(3, ymin);
				stmt.setInt(4, ymax);
				stmt.setInt(5, zmin);
				stmt.setInt(6, zmax);
				stmt.setInt(7, Worldmapuuid.get(worlduuid));
				stmt.setInt(8, playerId);
				stmt.setString(9, time);
			}
			conInsertclose(); // 查找数据前 先对插入数据进行提交 不然会出现 sqlite_busy
			rs = stmt.executeQuery();
			Timestamp blocktime;
			while (rs.next()) {
				final BlockSnapshot oldBlockSnapshot = this.deserializeBlockSnapshot(
						dbstrTOJ(worlduuid, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("oldBlock")));
				final BlockSnapshot newBlockSnapshot = this.deserializeBlockSnapshot(
						dbstrTOJ(worlduuid, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("newBlock")));

				if (oldBlockSnapshot == null || newBlockSnapshot == null) {
					continue;
				}

				if (DatabaseManager.mysqle) {
					blocktime = rs.getTimestamp("time");
				} else {
					blocktime = Timestamp.valueOf(rs.getString("time"));
				}
				blockInformation
						.add(new BlockInformation(newBlockSnapshot.getLocation().get(), oldBlockSnapshot.getState(),
								newBlockSnapshot.getState(), blocktime, playerUniqueId, this.getPlayerName(playerId)));
			}
			rs.close();
			stmt.close();
			c.close();

		} catch (SQLException var6) {
			var6.printStackTrace();
		}
		return blockInformation;

	}

	private BlockSnapshot deserializeBlockSnapshot(final DataContainer container) {
		BlockSnapshot Block = null;
		try {
			Block = BlockSnapshot.builder().build((DataView) container).get();
		} catch (Exception e) {
		}
		return Block;
	}

	public void clearExpiredData(final String timeThreshold) {
		Inspector.instance().getLogger().info("Starting purge the expired data..." + timeThreshold);

		Sponge.getScheduler().createTaskBuilder().async().execute(() -> {
			try {
				Connection c = this.getDatabaseConnection();
				String sql = "DELETE FROM INSP_BLOCKINFO WHERE TIME < \'" + timeThreshold + "\'";
				String sql1 = "DELETE FROM INSP_Container WHERE TIME < \'" + timeThreshold + "\'";
				conInsertclose();
				c.createStatement().executeUpdate(sql);
				c.createStatement().executeUpdate(sql1);
				c.close();
			} catch (SQLException var5) {
				var5.printStackTrace();
			}
		}).submit((Object) Inspector.instance());
		Inspector.instance().getLogger().info("clean up!");
	}
}
