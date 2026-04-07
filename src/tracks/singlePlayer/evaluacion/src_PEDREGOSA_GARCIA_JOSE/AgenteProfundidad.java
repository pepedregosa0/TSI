package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import tools.Vector2d;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Nodo;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Mapa;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteProfundidad extends AbstractPlayer {

    public Vector2d posicion;
    public Vector2d portal;
    public Nodo nodoActual;
    public Mapa mapa;

    private ArrayList<ACTIONS> plan = null;

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

        System.out.println("Posicion inicial: " + posicion);
        System.out.println("Posicion portal: " + portal);
        // Nodo inicial
        mapa = new Mapa(stateObs, posicion, portal);
        nodoActual = new Nodo((short) posicion.x, (short) posicion.y, 0, false, (byte) 0);

        /*Nodo nodo = new Nodo((short) posicion.x, (short) posicion.y, 1, false, (byte) 0);
        System.out.println("Nodo inicial: " + nodo);
        ArrayList<Nodo> hijos = nodo.expandir(mapa);
        for (Nodo hijo : hijos) {
            System.out.println("Hijo: " + hijo);
        } */
    }

    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (plan == null) {
            MetricsProvider.getInstance().setNumAccionesPlan(-1);

            HashSet<Nodo> visitados = new HashSet<>();
            Nodo meta = DFSRecursivo(nodoActual, visitados);
            if (meta != null) {
                plan = reconstruirPlan(meta);
                MetricsProvider.getInstance().setNumAccionesPlan(plan.size());
                MetricsProvider.getInstance().printMetrics();
            }
            else {
                System.out.println("No se ha encontrado un plan");
                plan = new ArrayList<>();
                plan.add(ACTIONS.ACTION_NIL);
            }
        }
        if (plan.size() > 0) {
            ACTIONS accion = plan.remove(0);
            System.out.println("Accion: " + accion);
            return accion;
        }
        
        return ACTIONS.ACTION_NIL;
    }

    private Nodo DFSRecursivo(Nodo actual, HashSet<Nodo> visitados)
    {
        // Criterio de parada
        if (((short) portal.x) == actual.x && ((short) portal.y) == actual.y)
            return actual;

        visitados.add(actual);
        // Expandimos el nodo actual
        ArrayList<Nodo> hijos = actual.expandir(mapa);
        for (Nodo hijo : hijos) {
            if (!visitados.contains(hijo)) {
                Nodo resultado = DFSRecursivo(hijo, visitados);
                if (resultado != null)
                    return resultado;
            }
        }
        return null;
    }

    private ArrayList<ACTIONS> reconstruirPlan(Nodo meta) {
        ArrayList<ACTIONS> plan = new ArrayList<>();
        Nodo actual = meta;
        while (actual.getPadre() != null) {
            plan.add(0, actual.getAccionPadre());
            actual = actual.getPadre();
        }
        System.out.println("Plan encontrado: " + plan);
        return plan;
    }
}
