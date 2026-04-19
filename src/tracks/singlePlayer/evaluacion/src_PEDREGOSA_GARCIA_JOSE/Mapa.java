package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import static tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Constantes.*;
import core.game.StateObservation;
import core.game.Observation;
import java.util.ArrayList;
import core.vgdl.VGDLRegistry;
import tools.Vector2d;

public class Mapa {
	public int ymax;
	public int xmax;

	public Vector2d posicion;
	public Vector2d portal;
	public short posicionX;
	public short posicionY;
	public short portalX;
	public short portalY;

	// Representacion del mapa
	public byte grid[][];
	public boolean mapaMonedas[][];
	public boolean mapaLlaves[][];
	public byte mapaCatapultas[][];
	
	// Guardar los ids de cada moneda y cada catapulta para poder identificarlas fácilmente
	public int[][] idMonedas;
	public int[][] idCatapultas;
	public int totalMonedas = 0;
	public int totalCatapultas = 0;
	
	// Para algoritmos informados
	public int mapaHeuristico[][];

	/**
	 * Constructor de la clase Mapa, se encarga de generar el mapa a partir del estado de observación del juego
	 * @param stateObs El estado de observación del juego, se utiliza para generar el mapa con la información de las casillas,
	 * la posición del personaje, la posición del portal, etc
	*/
	public Mapa(StateObservation stateObs) {
		ArrayList<Observation>[][] obsGrid = stateObs.getObservationGrid();
		this.ymax = obsGrid.length;
		this.xmax = obsGrid[0].length;
		stateObs.getAvatarType();
		grid = new byte[ymax][xmax];
		mapaMonedas = new boolean[ymax][xmax];
		mapaLlaves = new boolean[ymax][xmax];
		mapaCatapultas = new byte[ymax][xmax];

		idMonedas = new int[ymax][xmax];
		idCatapultas = new int[ymax][xmax];

		mapaHeuristico = new int[ymax][xmax];

		for (int i = 0; i < ymax; i++) { 
			for (int j = 0; j < xmax; j++) {
				grid[i][j] = SUELO;
				mapaMonedas[i][j] = false;
				mapaLlaves[i][j] = false;
				mapaCatapultas[i][j] = 0;

				idMonedas[i][j] = -1;
				idCatapultas[i][j] = -1;
			}
		}

		// Para conseguir la posicion del personaje y del portal
		int blockSize = stateObs.getBlockSize();

		ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
		portal = posiciones[0].get(0).position;
		portal.x = Math.floor(portal.x / blockSize);
		portal.y = Math.floor(portal.y / blockSize);
		portalX = (short) portal.x;
		portalY = (short) portal.y;
		posicion = stateObs.getAvatarPosition();
		posicion.x = Math.floor(posicion.x / blockSize);
		posicion.y = Math.floor(posicion.y / blockSize);
		posicionX = (short) posicion.x;
		posicionY = (short) posicion.y;

		//------------------------------------------------------------------------------------
		// Obstáculos
		int idWall = VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall");
		int idWater = VGDLRegistry.GetInstance().getRegisteredSpriteValue("water");
		
		// Meta (Puertas)
		int idLocked = VGDLRegistry.GetInstance().getRegisteredSpriteValue("locked");
		int idUnlocked = VGDLRegistry.GetInstance().getRegisteredSpriteValue("unlocked");
		// Recursos
		int idCoins = VGDLRegistry.GetInstance().getRegisteredSpriteValue("coins");
		int idKey = VGDLRegistry.GetInstance().getRegisteredSpriteValue("key");
		// Catapultas
		int idCatNorth = VGDLRegistry.GetInstance().getRegisteredSpriteValue("northfacing");
		int idCatSouth = VGDLRegistry.GetInstance().getRegisteredSpriteValue("southfacing");
		int idCatEast = VGDLRegistry.GetInstance().getRegisteredSpriteValue("eastfacing");
		int idCatWest = VGDLRegistry.GetInstance().getRegisteredSpriteValue("westfacing");
		// Conseguir la info de las casillas
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions();
		/*
		// Tipo de casillas
		System.out.println("ymax: " + ymax + " xmax: " + xmax);
		System.out.println("ID Wall: " + idWall);
		System.out.println("ID Water: " + idWater);
		System.out.println("ID Locked: " + idLocked);
		System.out.println("ID Unlocked: " + idUnlocked);
		System.out.println("ID Coins: " + idCoins);
		System.out.println("ID Key: " + idKey);
		System.out.println("ID CatNorth: " + idCatNorth);
		System.out.println("ID CatSouth: " + idCatSouth);
		System.out.println("ID CatEast: " + idCatEast);
		System.out.println("ID CatWest: " + idCatWest);*/
		// Imprimir las posiciones de los objetos immovables
		for (int i = 0; i < immovablePositions.length; i++){
			for (int j = 0; j < immovablePositions[i].size(); j++) {
				int x = (int)(immovablePositions[i].get(j).position.x / stateObs.getBlockSize());
				int y = (int)(immovablePositions[i].get(j).position.y / stateObs.getBlockSize());
				int itype = immovablePositions[i].get(j).itype;
				//System.out.println("Objeto immovable en (" + x + ", " + y + "): " + itype);
				if (itype == idWall) {
					grid[y][x] = PARED;
				} else if (itype == idWater) {
					grid[y][x] = AGUA;
				} else if (itype == idCatNorth) {
					mapaCatapultas[y][x] = CATNORTH;
					idCatapultas[y][x] = totalCatapultas++;
				} else if (itype == idCatSouth) {
					mapaCatapultas[y][x] = CATSOUTH;
					idCatapultas[y][x] = totalCatapultas++;
				} else if (itype == idCatEast) {
					mapaCatapultas[y][x] = CATEAST;
					idCatapultas[y][x] = totalCatapultas++;
				} else if (itype == idCatWest) {
					mapaCatapultas[y][x] = CATWEST;
					idCatapultas[y][x] = totalCatapultas++;
				}
			} 
		}
		grid[(int)portalY][(int)portalX] = PUERTA;
		grid[(int)posicionY][(int)posicionX] = PERSONAJE;


		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
		for (int i = 0; i < resourcesPositions.length; i++){
			for (int j = 0; j < resourcesPositions[i].size(); j++) {
				int x = (int)(resourcesPositions[i].get(j).position.x / stateObs.getBlockSize());
				int y = (int)(resourcesPositions[i].get(j).position.y / stateObs.getBlockSize());
				int itype = resourcesPositions[i].get(j).itype;
				//System.out.println("Recurso en (" + x + ", " + y + "): " + itype);
				if (itype == idCoins) {
					mapaMonedas[y][x] = true;
					idMonedas[y][x] = totalMonedas++;
				} else if (itype == idKey) {
					mapaLlaves[y][x] = true;
				}
			} 
		}

		// Para el mapa heuristico
		for (int i = 0; i < ymax; i++) { 
			for (int j = 0; j < xmax; j++) {
				mapaHeuristico[i][j] = distanciaManhattan(j, i, (int)portalX, (int)portalY);
			}
		}
		//System.out.println(this);
	}

	// Constructor para mapa vacio (creo que no se usa, pero por si acaso)
	public Mapa(int ymax, int xmax) {
		this.ymax = ymax;
		this.xmax = xmax;

		grid = new byte[ymax][xmax];
		mapaMonedas = new boolean[ymax][xmax];
		mapaLlaves = new boolean[ymax][xmax];
		mapaCatapultas = new byte[ymax][xmax];
	}

	// Método para copiar el mapa de forma eficiente (creo que se usa, pero por si acaso)
	public Mapa copy(Mapa other)
	{
		if (this != other)
		{
			Mapa copia = new Mapa(this.ymax, this.xmax);
			
			// Copia eficiente de los mapas
			for (int i = 0; i < this.ymax; i++) 
			{
				System.arraycopy(this.grid[i], 0, copia.grid[i], 0, this.xmax);
				System.arraycopy(this.mapaMonedas[i], 0, copia.mapaMonedas[i], 0, this.xmax);
				System.arraycopy(this.mapaLlaves[i], 0, copia.mapaLlaves[i], 0, this.xmax);
				System.arraycopy(this.mapaCatapultas[i], 0, copia.mapaCatapultas[i], 0, this.xmax);

				System.arraycopy(this.idMonedas[i], 0, copia.idMonedas[i], 0, this.xmax);
				System.arraycopy(this.idCatapultas[i], 0, copia.idCatapultas[i], 0, this.xmax);
				System.arraycopy(this.mapaHeuristico[i], 0, copia.mapaHeuristico[i], 0, this.xmax);
			}

			this.totalCatapultas = other.totalCatapultas;
			this.totalMonedas = other.totalMonedas;
			return copia;
		}
		return this;
	}

	// DEPURACION
	@Override
	public String toString() {
		String mapa = "";
		mapa += "Mapa generado:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (grid[i][j] + " ");
			}
			mapa += "\n";
		}
		mapa += "Mapa de monedas:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (mapaMonedas[i][j] ? "1 " : "0 ");
			}
			mapa += "\n";
		}
		mapa += "Mapa de llaves:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (mapaLlaves[i][j] ? "1 " : "0 ");
			} 
			mapa += "\n";
		}
		mapa += "Mapa de catapultas:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (mapaCatapultas[i][j] + " ");
			}
			mapa += "\n";
		}
		mapa += "ID de monedas:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (idMonedas[i][j] + " ");
			}
			mapa += "\n";
		}
		mapa += "ID de catapultas:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (idCatapultas[i][j] + " ");
			}
			mapa += "\n";
		}
		mapa += "Mapa heurístico:\n";
		for (int i = 0; i < ymax; i++) {
			for (int j = 0; j < xmax; j++) {
				mapa += (mapaHeuristico[i][j] + " ");
			}
			mapa += "\n";
		}
		mapa += "Posición del personaje: (" + posicionX + ", " + posicionY + ")\n";
		mapa += "Posición del portal: (" + portalX + ", " + portalY + ")\n";
		return mapa;
	}

	/**
	 * Calcula la distancia Manhattan entre una casilla y otra
	 * @param x1 Coordenada X de la primera casilla
	 * @param y1 Coordenada Y de la primera casilla
	 * @param x2 Coordenada X de la segunda casilla
	 * @param y2 Coordenada Y de la segunda casilla
	 * @return La distancia Manhattan entre las dos casillas
	 */
	public int distanciaManhattan(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}

	/**
	 * Devuelve la heurística de una casilla dada,
	 * se calcula con la distancia Manhattan entre la casilla y el portal, ignorando obstáculos
	 * @param x Coordenada X de la casilla
	 * @param y Coordenada Y de la casilla
	 * @return La heurística de la casilla
	 */
	public int H(int x, int y) {
		return mapaHeuristico[y][x];
	}
}
