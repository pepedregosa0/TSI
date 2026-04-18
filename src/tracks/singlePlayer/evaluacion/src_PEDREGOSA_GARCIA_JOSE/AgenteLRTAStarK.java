package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

import java.util.LinkedList;
// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteLRTAStarK extends AgenteHeuristico {

	private static int k = 5;

	private int nodosExpandidos;
	private int numAcciones;
	private int actualizacionesHeuristica;
	private long tiempoEjecucion;

	private HashMap<Nodo, Integer> tablaHeuristica;
	private HashMap<Nodo, Nodo> tablaSoportes;

	public AgenteLRTAStarK(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
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

	private void lookAheadUpdateK(Nodo nodoInicial) {
		LinkedList<Nodo> cola = new LinkedList<>();
		cola.add(nodoInicial);

		int contador = k - 1;
		while (!cola.isEmpty()) {
			Nodo x = cola.poll();
		
			ArrayList<Nodo> sucesoresX = x.expandir(mapa);
			nodosExpandidos++;
			Nodo mejorVecinoX = null;
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

			tablaSoportes.put(x, mejorVecinoX);

			boolean propagar = false;
			int hXActual = heuristica(x);

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

	private int heuristica(Nodo nodo) {
		if (tablaHeuristica.containsKey(nodo))
			return tablaHeuristica.get(nodo);
		return mapa.H(nodo.x, nodo.y);
	}

	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setNumAccionesPlan(numAcciones);
		MetricsProvider.getInstance().setNumActualizacionesTabla(actualizacionesHeuristica);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
		
}
