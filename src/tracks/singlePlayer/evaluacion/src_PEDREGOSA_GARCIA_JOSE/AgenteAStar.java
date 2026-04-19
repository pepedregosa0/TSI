package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.HashMap;

public class AgenteAStar extends AgenteHeuristico {
	// Estructuras de datos para A*
	private PriorityQueue<Nodo> abiertos;
	private HashSet<Nodo> cerrados;
	private HashMap<Nodo, Integer> mapaCostos;
	
	// METRICAS
	private int nodosExpandidos;
	private long tiempoEjecucion;

	public AgenteAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// Inicialización de agente heurístico
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
			tiempoEjecucion = (fin - inicio);
			
			if (meta != null) {
				plan = reconstruirPlan(meta);
				imprimirMetricas();
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


	/**
	 * Implementación del algoritmo A* para encontrar el camino óptimo desde el nodo inicial hasta la meta.
	 * El algoritmo utiliza una cola de prioridad para gestionar los nodos abiertos,
	 * un conjunto para los nodos cerrados y un mapa para almacenar los costos g(n) de cada nodo.
	 * @param nodoInicial El nodo desde el cual se inicia la búsqueda.
	 * @return El nodo meta encontrado, o null si no se encuentra una solución.
	 */
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

	/**
	 * Imprime las métricas de la ejecución del algoritmo A*.
	 * Métricas incluidas:
	 * - Número de acciones en el plan encontrado.
	 * - Número de nodos abiertos durante la búsqueda.
	 * - Número de nodos cerrados durante la búsqueda.
	 * - Número de nodos expandidos durante la búsqueda.
	 * - Tiempo de ejecución en milisegundos.
	 */
	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNumAccionesPlan(plan.size());
		MetricsProvider.getInstance().setNodosAbiertos(abiertos.size());
		MetricsProvider.getInstance().setNodosCerrados(cerrados.size());
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
}