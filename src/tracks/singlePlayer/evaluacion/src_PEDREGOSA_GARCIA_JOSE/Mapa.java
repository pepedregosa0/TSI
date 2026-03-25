package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import core.game.StateObservation;
import core.game.Observation;
import java.util.ArrayList;
import core.vgdl.VGDLRegistry;

public class Mapa {
    private static final byte SUELO = 0;
    private static final byte AGUA = 1;
    private static final byte PARED = 2;
    private static final byte PUERTA = 3;
    private static final byte CATNORTH = 1;
    private static final byte CATSOUTH = 2;
    private static final byte CATEAST = 3;
    private static final byte CATWEST = 4;

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
                System.out.println("Objeto immovable en (" + x + ", " + y + "): " + immovablePositions[i].get(j).itype);
            } 
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
