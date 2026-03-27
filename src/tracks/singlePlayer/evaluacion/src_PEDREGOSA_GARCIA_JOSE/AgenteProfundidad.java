package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import tools.Vector2d;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Nodo;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteProfundidad extends AbstractPlayer {

    public Vector2d posicion;
    public Vector2d portal;
    public Nodo nodoActual;

    public AgenteProfundidad(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super();

        // El constructor puede inicializar todas las estructuras iniciales y hasta el nodo inicial pero no puede hacer 
        // nada del proceso de búsqueda
        int blockSize = stateObs.getBlockSize();

        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        portal = posiciones[0].get(0).position;
        portal.x = Math.floor(portal.x / blockSize);
        portal.y = Math.floor(portal.y / blockSize);
        posicion = stateObs.getAvatarPosition();
        posicion.x = Math.floor(posicion.x / blockSize);
        posicion.y = Math.floor(posicion.y / blockSize);

        // Nodo inicial
        nodoActual = new Nodo((short) posicion.x, (short) posicion.y, 0, false, (byte) 0);
    }

    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        // Este método debe encargarse de computar el plan y devolver la siguiente acción

        // Al encontrar el plan se deben fijar TODAS las métricas complementarias correspondientes al algoritmo
        MetricsProvider.getInstance().setNumAccionesPlan(-1);

        // ....
        // ....

        
        // Puede mostrarlas por pantalla, si lo desea, de la siguiete manera
        MetricsProvider.getInstance().printMetrics();

        return ACTIONS.ACTION_DOWN;
    }
    

    private Nodo DFS(Nodo inicial, Nodo objetivo, Mapa mapa)
    {
        return null;
    }

    private Nodo DFSRecursivo(Nodo actual, Nodo objetivo, Nodo[] visitados, Mapa mapa)
    {
        return null;
    }

    private Nodo[] generarSucesores(Nodo actual, Mapa mapa)
    {
        return null;
    }
}
