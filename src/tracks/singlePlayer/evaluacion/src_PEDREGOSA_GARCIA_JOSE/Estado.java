package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;

import core.game.StateObservation;
import tools.Vector2d;

public class Estado {
    public Vector2d posicion;
    public short numMonedas;
    public boolean llave;
    public Mapa mapa;

    public Estado(StateObservation stateObs, Vector2d fescala, Vector2d posicion, Vector2d portal)
    {
        this.posicion = posicion;
        this.numMonedas = 0;
        this.llave = false;
        this.mapa = new Mapa(stateObs, posicion, portal);
    }

}
