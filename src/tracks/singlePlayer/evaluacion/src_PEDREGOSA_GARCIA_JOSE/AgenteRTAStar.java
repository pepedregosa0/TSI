package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteRTAStar extends AgenteHeuristico {

    private int nodosExpandidos;
    private int longitudPlan;

    private HashMap<Nodo, Integer> tablaHeuristica;

    public AgenteRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super(stateObs, elapsedTimer);
        nodosExpandidos = 0;
        longitudPlan = 0;
        tablaHeuristica = new HashMap<>();
    }
    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

    }

    private Nodo RTAStar(Nodo actual) {
        if (actual.esMeta(mapa)) {
            return actual;
        }

        nodosExpandidos++;

        ArrayList<Nodo> hijos = actual.expandir(mapa);

        Nodo mejorVecino = null;
        int minF = Integer.MAX_VALUE;
        int minF2 = Integer.MAX_VALUE;

        for (Nodo hijo : hijos) {
            int h = tablaHeuristica.getOrDefault(hijo, mapa.H(hijo.x, hijo.y));

            int f = h + 1;


        return null; // no se ha encontrado un plan
    }
    
}
