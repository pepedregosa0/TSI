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

    public byte grid[][];
    public boolean mapaMonedas[][];
    public boolean mapaLlaves[][];
    public byte mapaCatapultas[][];

    // Guardar los ids de cada moneda y cada catapulta para poder identificarlas fácilmente
    public int[][] idMonedas;
    public int[][] idCatapultas;
    public int totalMonedas = 0;
    public int totalCatapultas = 0;

    public Mapa(StateObservation stateObs, Vector2d posicion, Vector2d portal) {
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
        grid[(int)posicion.y][(int)posicion.x] = PERSONAJE;
        grid[(int)portal.y][(int)portal.x] = PUERTA;


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

        System.out.println(this);
    }

    public Mapa(int ymax, int xmax) {
        this.ymax = ymax;
        this.xmax = xmax;

        grid = new byte[ymax][xmax];
        mapaMonedas = new boolean[ymax][xmax];
        mapaLlaves = new boolean[ymax][xmax];
        mapaCatapultas = new byte[ymax][xmax];
    }

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
            }

            this.totalCatapultas = other.totalCatapultas;
            this.totalMonedas = other.totalMonedas;
            return copia;
        }
        return this;
    }

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
        return mapa;
    }
}
