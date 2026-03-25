package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import static tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Constantes.*;
import core.game.StateObservation;
import core.game.Observation;
import java.util.ArrayList;
import core.vgdl.VGDLRegistry;

public class Mapa {
    public int ancho;
    public int alto;

    public byte grid[][];
    public boolean mapaMonedas[][];
    public boolean mapaLlaves[][];
    public byte mapaCatapultas[][];

    public Mapa(StateObservation stateObs) {
        ArrayList<Observation>[][] obsGrid = stateObs.getObservationGrid();
        this.ancho = obsGrid.length;
        this.alto = obsGrid[0].length;
        stateObs.getAvatarType();
        grid = new byte[ancho][alto];
        mapaMonedas = new boolean[ancho][alto];
        mapaLlaves = new boolean[ancho][alto];
        mapaCatapultas = new byte[ancho][alto];

        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < alto; j++) {
                grid[i][j] = SUELO;
                mapaMonedas[i][j] = false;
                mapaLlaves[i][j] = false;
                mapaCatapultas[i][j] = 0;
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
        // Tipo de casillas
        System.out.println("Ancho: " + ancho + " Alto: " + alto);
        System.out.println("ID Wall: " + idWall);
        System.out.println("ID Water: " + idWater);
        System.out.println("ID Locked: " + idLocked);
        System.out.println("ID Unlocked: " + idUnlocked);
        System.out.println("ID Coins: " + idCoins);
        System.out.println("ID Key: " + idKey);
        System.out.println("ID CatNorth: " + idCatNorth);
        System.out.println("ID CatSouth: " + idCatSouth);
        System.out.println("ID CatEast: " + idCatEast);
        System.out.println("ID CatWest: " + idCatWest);
        // Imprimir las posiciones de los objetos immovables
        for (int i = 0; i < immovablePositions.length; i++){
            for (int j = 0; j < immovablePositions[i].size(); j++) {
                int x = (int)(immovablePositions[i].get(j).position.x / stateObs.getBlockSize());
                int y = (int)(immovablePositions[i].get(j).position.y / stateObs.getBlockSize());
                int itype = immovablePositions[i].get(j).itype;
                System.out.println("Objeto immovable en (" + x + ", " + y + "): " + itype);
                if (itype == idWall) {
                    grid[x][y] = PARED;
                } else if (itype == idWater) {
                    grid[x][y] = AGUA;
                } else if (itype == idLocked) {
                    grid[x][y] = PUERTA;
                } else if (itype == idUnlocked) {
                    grid[x][y] = PUERTA_ABIERTA;
                } else if (itype == idCatNorth) {
                    mapaCatapultas[x][y] = CATNORTH;
                } else if (itype == idCatSouth) {
                    mapaCatapultas[x][y] = CATSOUTH;
                } else if (itype == idCatEast) {
                    mapaCatapultas[x][y] = CATEAST;
                } else if (itype == idCatWest) {
                    mapaCatapultas[x][y] = CATWEST;
                }
            } 
        }

        System.out.println("Mapa generado:");
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < alto; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Mapa de monedas:");
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < alto; j++) {
                System.out.print(mapaMonedas[i][j] ? "1 " : "0 ");
            }
            System.out.println();
        }
        System.out.println("Mapa de llaves:");
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < alto; j++) {
                System.out.print(mapaLlaves[i][j] ? "1 " : "0 ");
            } 
            System.out.println();
        }
        System.out.println("Mapa de catapultas:");
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < alto; j++) {
                System.out.print(mapaCatapultas[i][j] + " ");
            }
            System.out.println();
        }
    }

    public Mapa(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;

        grid = new byte[ancho][alto];
        mapaMonedas = new boolean[ancho][alto];
        mapaLlaves = new boolean[ancho][alto];
        mapaCatapultas = new byte[ancho][alto];
    }

    public Mapa copy(Mapa other)
    {
        if (this != other)
        {
            Mapa copia = new Mapa(this.ancho, this.alto);
            
            // Copia eficiente de los mapas
            for (int i = 0; i < this.ancho; i++) 
            {
                System.arraycopy(this.grid[i], 0, copia.grid[i], 0, this.alto);
                System.arraycopy(this.mapaMonedas[i], 0, copia.mapaMonedas[i], 0, this.alto);
                System.arraycopy(this.mapaLlaves[i], 0, copia.mapaLlaves[i], 0, this.alto);
                System.arraycopy(this.mapaCatapultas[i], 0, copia.mapaCatapultas[i], 0, this.alto);
            }
            return copia;
        }
        return this;
    }
}
