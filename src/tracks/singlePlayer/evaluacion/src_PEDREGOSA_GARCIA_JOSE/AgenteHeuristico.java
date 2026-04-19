package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;
import ontology.Types.ACTIONS;
import java.util.ArrayList;

public abstract class AgenteHeuristico extends AbstractPlayer {
    protected Nodo nodoActual;
    protected Mapa mapa;
    protected ArrayList<ACTIONS> plan;

    // Se usa para la antigüedad de los nodos como id
    protected int contadorDeNodos;

    public AgenteHeuristico(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super();
        // Inicializar el mapa y el nodo actual
        mapa = new Mapa(stateObs);
        nodoActual = new Nodo((short) mapa.posicionX, (short) mapa.posicionY, 0, false, (byte) 0);
        contadorDeNodos = 0;
        plan = null;
    }

    /**
     * Reconstruye el plan desde el nodo objetivo hasta el nodo inicial siguiendo los padres de cada nodo.
     * @param objetivo El nodo objetivo desde el cual se reconstruirá el plan.
     * @return Una lista de acciones que representa el plan desde el nodo inicial hasta el nodo objetivo.
     */
    protected ArrayList<ACTIONS> reconstruirPlan(Nodo objetivo) {
        ArrayList<ACTIONS> plan = new ArrayList<>();
        Nodo actual = objetivo;

        while (actual.getPadre() != null) {
            plan.add(0, actual.getAccionPadre());
            actual = actual.getPadre();
        }
        //System.out.println("Plan encontrado: " + plan);
        return plan;
    }
}