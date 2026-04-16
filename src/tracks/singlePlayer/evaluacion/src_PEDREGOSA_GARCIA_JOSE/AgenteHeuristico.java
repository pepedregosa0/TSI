package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;
import ontology.Types.ACTIONS;
import java.util.ArrayList;

public abstract class AgenteHeuristico extends AbstractPlayer {
    protected Nodo nodoActual;
    protected Mapa mapa;
    protected int contadorDeNodos;
    protected ArrayList<ACTIONS> plan;

    public AgenteHeuristico(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super();
        mapa = new Mapa(stateObs);
        nodoActual = new Nodo((short) mapa.posicionX, (short) mapa.posicionY, 0, false, (byte) 0);
        contadorDeNodos = 0;
        plan = null;
    }

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