package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Nodo;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteProfundidad extends AbstractPlayer {

    public 

    public AgenteProfundidad(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super();

        // El constructor puede inicializar todas las estructuras iniciales y hasta el nodo inicial pero no puede hacer 
        // nada del proceso de búsqueda
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

        return ACTIONS.ACTION_NIL;
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
