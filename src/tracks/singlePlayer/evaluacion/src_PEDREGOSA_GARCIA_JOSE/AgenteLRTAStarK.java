package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

import java.util.LinkedList;

public class AgenteLRTAStarK extends AgenteHeuristico {
	// Valor de k para el look-ahead
	private static int k = 5;
	
	// Tablas para almacenar la heurística y los soportes de los nodos en el look-ahead
	private HashMap<Nodo, Integer> tablaHeuristica;
	private HashMap<Nodo, Nodo> tablaSoportes;

	// METRICAS
	private int nodosExpandidos;
	private int numAcciones;
	private int actualizacionesHeuristica;
	private long tiempoEjecucion;


	public AgenteLRTAStarK(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// Inicializacion con agente heuristico
		super(stateObs, elapsedTimer);

		nodosExpandidos = 0;
		numAcciones = 0;
		actualizacionesHeuristica = 0;
		tablaHeuristica = new HashMap<>();
		tablaSoportes = new HashMap<>();
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (nodoActual.esMeta(mapa)) {
			imprimirMetricas();
			return ACTIONS.ACTION_NIL;
		}
		long inicio = System.nanoTime();
		lookAheadUpdateK(nodoActual);
		ACTIONS siguienteAccion = LRTAStarK(nodoActual);
		long fin = System.nanoTime();

		tiempoEjecucion += (fin - inicio);
		imprimirMetricas();
		return siguienteAccion;
	}

	/**
	 * Selecciona la mejor acción a tomar desde el nodo actual utilizando la función f(n) = g(n) + h(n),
	 * donde g(n) es el costo acumulado (en este caso, asumimos un costo uniforme de 1 por acción) y h(n) es la heurística del nodo.
	 * La heurística es distinta para cada nodo y se actualiza en la tablaHeuristica durante el proceso de look-ahead.
	 * @param nodo desde el cual se seleccionará la acción a tomar
	 * @return la acción a tomar para avanzar hacia el nodo con el menor valor de f(n) entre los vecinos del nodo actual
	 */
	private ACTIONS LRTAStarK(Nodo nodo) {
		// Espacio local de busqueda
		ArrayList<Nodo> vecinos = nodoActual.expandir(mapa);
		// No contamos otra expansion ya que se ha hecho en lookAheadUpdateK
		
		// Si ha muerto o atrapado
		if (vecinos.isEmpty()) {
			return ACTIONS.ACTION_NIL;
		}
		
		Nodo mejorVecino = null;
		int minF = Integer.MAX_VALUE;
		
		// Enconctrar los dos mejores veciones según f(n) = g(n) + h(n)
		for (Nodo vecino : vecinos) {
			// c(x, y) + h(n)
			int fVecino = 1 + heuristica(vecino); 
			
			if (fVecino < minF) {
				minF = fVecino;
				mejorVecino = vecino;
			}
		}

		numAcciones++;
		nodoActual = mejorVecino;

		return mejorVecino.getAccionPadre();
	}

	/**
	 * Realiza un look-ahead de profundidad k desde el nodo inicial, actualizando la heurística de los nodos visitados y sus soportes.
	 * Para cada nodo visitado, se calcula el mejor vecino según la función f(n) y se actualiza la tabla de soportes. 
	 * Si la heurística del nodo actual es menor que el valor mínimo encontrado entre sus vecinos,
	 * se actualiza la heurística del nodo y se propaga el cambio a los nodos de los que es soporte, hasta un máximo de k propagaciones.
	 */
	private void lookAheadUpdateK(Nodo nodoInicial) {
		LinkedList<Nodo> cola = new LinkedList<>();
		cola.add(nodoInicial);

		int contador = k - 1;
		while (!cola.isEmpty()) {
			Nodo x = cola.poll();
		
			ArrayList<Nodo> sucesoresX = x.expandir(mapa);
			nodosExpandidos++;
			Nodo mejorVecinoX = null;
			// Se utliza el primer mejor vecino
			int minF = Integer.MAX_VALUE;

			if (sucesoresX.isEmpty())
				continue;

			for (Nodo sucesorX : sucesoresX) {
				int fVecino = 1 + heuristica(sucesorX);

				if (fVecino < minF) {
					minF = fVecino;
					mejorVecinoX = sucesorX;
				}
			}

			// Actualizamos la tabla de soportes para el nodo x
			tablaSoportes.put(x, mejorVecinoX);

			boolean propagar = false;
			int hXActual = heuristica(x);

			// En caso de que la heurística del nodo x sea menor que el valor mínimo
			// actualizamos la heurística del nodo x y propagamos
			if (hXActual < minF) {
				tablaHeuristica.put(x, minF);
				actualizacionesHeuristica++;
				propagar = true;
			}
			
			if (propagar) {
				// intentamos propagar el cambio a los nodos de los que x es soporte
				for (Nodo sucesor : sucesoresX) {
					Nodo soporteSucesor = tablaSoportes.get(sucesor);

					if (contador > 0 && soporteSucesor != null && soporteSucesor.equals(x)) {
						cola.add(sucesor);
						contador--;
					}
				}
			}
		}
	}

	/**
	 * Calcula la heurística de un nodo utilizando la tabla de heurísticas si el nodo ya ha sido visitado o H(nodo) si no se ha visitado antes. 
	 * @param nodo para el cual se desea calcular la heurística
	 * @return la heurística del nodo, que es el valor almacenado en la tabla de heurísticas si el nodo ya ha sido visitado, o H(nodo) si no se ha visitado antes
	 */
	private int heuristica(Nodo nodo) {
		if (tablaHeuristica.containsKey(nodo))
			return tablaHeuristica.get(nodo);
		return mapa.H(nodo.x, nodo.y);
	}

	/**
	 * Imprime las métricas del agente.
	 * Incluye las métricas:
	 * - Número de nodos expandidos
	 * - Número de acciones tomadas
	 * - Número de actualizaciones de la tabla heurística
	 * - Tiempo de ejecución en milisegundos
	 */
	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
		MetricsProvider.getInstance().setNumActualizacionesTabla(actualizacionesHeuristica);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
		
}
