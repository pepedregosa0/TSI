package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import core.game.StateObservation;
import core.game.Observation;
import java.util.ArrayList;
import core.vgdl.VGDLRegistry;

public class Mapa {
    public int ancho;
    public int alto;

    public short grid[][];
    public boolean mapaMonedas[][];
    public boolean mapaLlaves[][];
    public boolean mapaCatapultas[][];

    public Mapa(StateObservation stateObs) {
        ArrayList<Observation>[][] obsGrid = stateObs.getObservationGrid();
        this.ancho = obsGrid.length;
        this.alto = obsGrid[0].length;
        stateObs.getAvatarType();
        grid = new short[ancho][alto];
        mapaMonedas = new boolean[ancho][alto];
        mapaLlaves = new boolean[ancho][alto];
        mapaCatapultas = new boolean[ancho][alto];
        
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
    }

    public Mapa(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;

        grid = new short[ancho][alto];
        mapaMonedas = new boolean[ancho][alto];
        mapaLlaves = new boolean[ancho][alto];
        mapaCatapultas = new boolean[ancho][alto];
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
