package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

public class AgenteRTAStar extends AgenteHeuristico {
	// METRICAS
	private int nodosExpandidos;
	private int numAcciones;
	private long tiempoEjecucion;

	// Tabla hash para almacenar las heurísticas aprendidas durante la ejecución del algoritmo
	// y evitar caer en ciclos infinitos.
	private HashMap<Nodo, Integer> tablaHeuristica;

	public AgenteRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// Inicializacion con agente heuristico
		super(stateObs, elapsedTimer);
		
		nodosExpandidos = 0;
		numAcciones = 0;
		tablaHeuristica = new HashMap<>();
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (nodoActual.esMeta(mapa)) {
			imprimirMetricas();

			return ACTIONS.ACTION_NIL;
		}
		long inicio = System.nanoTime();
		ACTIONS siguienteAccion = RTAStar(nodoActual);
		long fin = System.nanoTime();

		tiempoEjecucion += (fin - inicio);
		imprimirMetricas();
		return siguienteAccion;
	}

	/**
	 * Realiza una iteración del algoritmo RTA* para seleccionar la siguiente acción a tomar desde el nodo actual.
	 * @param nodo El nodo actual desde el cual se desea seleccionar la siguiente acción.
	 * @return La acción seleccionada para avanzar hacia el objetivo. Si no se pueden expandir vecinos, se devuelve ACTION_NIL.
	 */
	private ACTIONS RTAStar(Nodo nodo) {
		// Espacio local de busqueda
		ArrayList<Nodo> vecinos = nodoActual.expandir(mapa);
		nodosExpandidos++;

		// Si ha muerto o atrapado
		if (vecinos.isEmpty()) {
			imprimirMetricas();
			return ACTIONS.ACTION_NIL;
		}

		Nodo mejorVecino = null;
		int minF = Integer.MAX_VALUE;
		int segundoMinF = Integer.MAX_VALUE;

		// Enconctrar los dos mejores veciones según f(n) = g(n) + h(n)
		for (Nodo vecino : vecinos) {
			// c(x, y) + h(n)
			int fVecino = 1 + heuristica(vecino); 
			
			if (fVecino < minF) {
				segundoMinF = minF;
				minF = fVecino;
				mejorVecino = vecino;
			}
			else if (fVecino < segundoMinF) {
				segundoMinF = fVecino;
			}
		}
		
		// Si no hay segundo mínimo elegimos el primer minimo como valor
		if (segundoMinF == Integer.MAX_VALUE) {
			segundoMinF = minF;
		}

		// Regla de aprendizaje (Espacio local de aprendizaje)
		int hActual = heuristica(nodoActual);
		int nuevaHeuristica = Math.max(hActual, segundoMinF);
		tablaHeuristica.put(nodoActual, nuevaHeuristica);

		ACTIONS accionSeleccionada = mejorVecino.getAccionPadre();
		nodoActual = mejorVecino; 

		numAcciones++;
		MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);

		return accionSeleccionada;
	}

	/**
	 * @param nodo El nodo para el cual se desea calcular la heurística.
	 * @return El valor de la heurística para el nodo dado. Si el nodo no ha sido evaluado previamente, se calcula utilizando el mapa y se devuelve el resultado.
	 */
	private int heuristica(Nodo nodo) {
		if (tablaHeuristica.containsKey(nodo))
			return tablaHeuristica.get(nodo);
		return mapa.H(nodo.x, nodo.y);
	}

	/**
	 * Imprime las métricas de rendimiento del agente.
	 * Métricas incluidas:
	 * - Número de acciones tomadas hasta el momento.
	 * - Número de nodos expandidos durante la búsqueda.
	 * - Tiempo de ejecución acumulado en milisegundos.
	 */
	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
	
}
