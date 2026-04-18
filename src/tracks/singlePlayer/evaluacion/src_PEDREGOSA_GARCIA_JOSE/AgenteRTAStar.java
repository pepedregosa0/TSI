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
	private int numAcciones;
	private long tiempoEjecucion;

	private HashMap<Nodo, Integer> tablaHeuristica;

	public AgenteRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		super(stateObs, elapsedTimer);
		nodosExpandidos = 0;
		numAcciones = 0;
		tablaHeuristica = new HashMap<>();
	}
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (nodoActual.esMeta(mapa)) {
			MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
			MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
			MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
			MetricsProvider.getInstance().printMetrics();

			return ACTIONS.ACTION_NIL;
		}
		long inicio = System.nanoTime();
		ACTIONS siguienteAccion = RTAStar(nodoActual);
		long fin = System.nanoTime();

		tiempoEjecucion += (fin - inicio);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
		MetricsProvider.getInstance().printMetrics();
		return siguienteAccion;
	}

	private ACTIONS RTAStar(Nodo nodo) {
		// Espacio local de busqueda
		ArrayList<Nodo> vecinos = nodoActual.expandir(mapa);
		nodosExpandidos++;

		// Si ha muerto o atrapado
		if (vecinos.isEmpty()) {
			MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
			MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
			MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
			MetricsProvider.getInstance().printMetrics();
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

	private int heuristica(Nodo nodo) {
		if (tablaHeuristica.containsKey(nodo))
			return tablaHeuristica.get(nodo);
		return mapa.H(nodo.x, nodo.y);
	}
	
}
