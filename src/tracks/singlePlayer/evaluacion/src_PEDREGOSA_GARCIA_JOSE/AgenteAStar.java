package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.HashMap;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteAStar extends AgenteHeuristico {
    PriorityQueue<Nodo> abiertos;
    HashSet<Nodo> cerrados;
    public HashMap<Nodo, Integer> mapaCostos;

    public int nodosExpandidos;

    public AgenteAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        super(stateObs, elapsedTimer);
        abiertos = new PriorityQueue<>();
        cerrados = new HashSet<>();
        mapaCostos = new HashMap<>();
    }

    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (plan == null) {
            long inicio = System.nanoTime();
            Nodo meta = AStar(nodoActual);
            long fin = System.nanoTime();
            long tiempoEjecucion = (fin - inicio) / 1000000; // Convert to milliseconds
            MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion);
            
            if (meta != null) {
                plan = reconstruirPlan(meta);
                // en nodos expandidos se contabiliza uno de más pero no sé por qué, así que lo resto aquí
                MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos - 1);
                MetricsProvider.getInstance().setNodosCerrados(cerrados.size());
                MetricsProvider.getInstance().setNodosAbiertos(abiertos.size());
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
            //System.out.println("Accion: " + accion);
            return accion;
        }
        
        return ACTIONS.ACTION_NIL;
    }


    private Nodo AStar(Nodo nodoInicial) {
        nodoInicial.g = 0;
        nodoInicial.h = mapa.H(nodoInicial.x, nodoInicial.y);
        nodoInicial.f = nodoInicial.g + nodoInicial.h;

        nodoInicial.idInsercion = contadorDeNodos++;
        abiertos.add(nodoInicial);

        while (!abiertos.isEmpty()) {
            // actual = mejorCandidato
            Nodo actual = abiertos.poll();

            if (actual.esMeta(mapa))
                return actual;

            cerrados.add(actual);

            nodosExpandidos++;

            for (Nodo hijo : actual.expandir(mapa)) {
                hijo.g = actual.g + 1;
                hijo.h = mapa.H(hijo.x, hijo.y);
                hijo.f = hijo.g + hijo.h;
                
                int costoAnterior = mapaCostos.getOrDefault(hijo, Integer.MAX_VALUE);
                
                if (hijo.g < costoAnterior) {
                    mapaCostos.put(hijo, hijo.g);
                    hijo.idInsercion = contadorDeNodos++;
                    if (cerrados.contains(hijo)) { // menor g(n)
                        cerrados.remove(hijo);
                        abiertos.add(hijo);
                    }
                    else if (abiertos.contains(hijo)) { // actualizar g(n) porque mejor camino encontrado
                        abiertos.remove(hijo);
                        abiertos.add(hijo);
                    }
                    else // nodo no visitado, añadir
                        abiertos.add(hijo);
                }
            }
        }
        return null; // no hay solucion
    }
}